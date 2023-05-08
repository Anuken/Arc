package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import java.util.*;
import java.util.concurrent.*;

/** Fast sorting implementation written by zxtej. Don't ask me how it works. */
public class SortedSpriteBatch extends SpriteBatch{
    static ForkJoinHolder commonPool;

    boolean multithreaded = (Core.app.getVersion() >= 21 && !Core.app.isIOS()) || Core.app.isDesktop();
    int[] contiguous = new int[2048], contiguousCopy = new int[2048];
    DrawRequest[] copy = new DrawRequest[0];
    int[] locs = new int[contiguous.length];
    
    protected DrawRequest[] requests = new DrawRequest[10000];
    protected boolean sort;
    protected boolean flushing;
    protected float[] requestZ = new float[10000];
    protected int numRequests = 0;

    {
        for(int i = 0; i < requests.length; i++){
            requests[i] = new DrawRequest();
        }

        if(multithreaded){
            try{
                commonPool = new ForkJoinHolder();
            }catch(Throwable t){
                multithreaded = false;
            }
        }
    }

    @Override
    protected void setSort(boolean sort){
        if(this.sort != sort){
            flush();
        }
        this.sort = sort;
    }

    @Override
    protected void setShader(Shader shader, boolean apply){
        if(!flushing && sort){
            throw new IllegalArgumentException("Shaders cannot be set while sorting is enabled. Set shaders inside Draw.run(...).");
        }
        super.setShader(shader, apply);
    }

    @Override
    protected void setBlending(Blending blending){
        this.blending = blending;
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        if(sort && !flushing){
            if(numRequests + count - offset >= this.requests.length) expandRequests();
            float[] requestZ = this.requestZ;
            DrawRequest[] requests = this.requests;

            for(int i = offset; i < count; i += SPRITE_SIZE){
                final DrawRequest req = requests[numRequests];
                requestZ[numRequests] = req.z = z;
                System.arraycopy(spriteVertices, i, req.vertices, 0, req.vertices.length);
                req.texture = texture;
                req.blending = blending;
                req.run = null;
                numRequests ++;
            }
        }else{
            super.draw(texture, spriteVertices, offset, count);
        }
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        if(sort && !flushing){
            if(numRequests >= requests.length) expandRequests();
            final DrawRequest req = requests[numRequests];
            req.x = x;
            req.y = y;
            requestZ[numRequests] = req.z = z;
            req.originX = originX;
            req.originY = originY;
            req.width = width;
            req.height = height;
            req.color = colorPacked;
            req.rotation = rotation;
            req.region.set(region);
            req.mixColor = mixColorPacked;
            req.blending = blending;
            req.texture = null;
            req.run = null;
            numRequests ++;
        }else{
            super.draw(region, x, y, originX, originY, width, height, rotation);
        }
    }

    @Override
    protected void draw(Runnable request){
        if(sort && !flushing){
            if(numRequests >= requests.length) expandRequests();
            final DrawRequest req = requests[numRequests];
            req.run = request;
            req.blending = blending;
            req.mixColor = mixColorPacked;
            req.color = colorPacked;
            requestZ[numRequests] = req.z = z;
            req.texture = null;
            numRequests ++;
        }else{
            super.draw(request);
        }
    }

    protected void expandRequests(){
        final DrawRequest[] requests = this.requests, newRequests = new DrawRequest[requests.length * 7 / 4];
        System.arraycopy(requests, 0, newRequests, 0, Math.min(newRequests.length, requests.length));
        for(int i = requests.length; i < newRequests.length; i++){
            newRequests[i] = new DrawRequest();
        }
        this.requests = newRequests;
        this.requestZ = Arrays.copyOf(requestZ, newRequests.length);
    }

    @Override
    protected void flush(){
        flushRequests();
        super.flush();
    }

    protected void flushRequests(){
        if(!flushing && numRequests > 0){
            flushing = true;
            sortRequests();
            float preColor = colorPacked, preMixColor = mixColorPacked;
            Blending preBlending = blending;

            for(int j = 0; j < numRequests; j++){
                final DrawRequest req = requests[j];

                colorPacked = req.color;
                mixColorPacked = req.mixColor;

                super.setBlending(req.blending);

                if(req.run != null){
                    req.run.run();
                }else if(req.texture != null){
                    super.draw(req.texture, req.vertices, 0, req.vertices.length);
                }else{
                    super.draw(req.region, req.x, req.y, req.originX, req.originY, req.width, req.height, req.rotation);
                }
            }

            colorPacked = preColor;
            mixColorPacked = preMixColor;
            color.abgr8888(colorPacked);
            mixColor.abgr8888(mixColorPacked);
            blending = preBlending;

            numRequests = 0;

            flushing = false;
        }
    }

    protected void sortRequests(){
        if(multithreaded){
            sortRequestsThreaded();
        }else{
            sortRequestsStandard();
        }
    }

    protected void sortRequestsThreaded(){
        final int numRequests = this.numRequests;
        if(copy.length < numRequests) copy = new DrawRequest[numRequests + (numRequests >> 3)];
        final DrawRequest[] items = requests, itemCopy = copy;
        final float[] itemZ = requestZ;
        final Future<?> initTask = commonPool.pool.submit(() -> System.arraycopy(items, 0, itemCopy, 0, numRequests));

        int[] contiguous = this.contiguous;
        int ci = 0, cl = contiguous.length;
        float z = itemZ[0];
        int startI = 0;
        // Point3: <z, index, length>
        for(int i = 1; i < numRequests; i++){
            if(itemZ[i] != z){ // if contiguous section should end
                contiguous[ci] = Float.floatToRawIntBits(z + 16f);
                contiguous[ci + 1] = startI;
                contiguous[ci + 2] = i - startI;
                ci += 3;
                if(ci + 3 > cl){
                    contiguous = Arrays.copyOf(contiguous, cl <<= 1);
                }
                z = itemZ[startI = i];
            }
        }
        contiguous[ci] = Float.floatToRawIntBits(z + 16f);
        contiguous[ci + 1] = startI;
        contiguous[ci + 2] = numRequests - startI;
        this.contiguous = contiguous;

        final int L = (ci / 3) + 1;

        if(contiguousCopy.length < contiguous.length) this.contiguousCopy = new int[contiguous.length];

        final int[] sorted = CountingSort.countingSortMapMT(contiguous, contiguousCopy, L);

        if(locs.length < L + 1) locs = new int[L + L / 10];
        final int[] locs = this.locs;
        for(int i = 0; i < L; i++){
            locs[i + 1] = locs[i] + sorted[i * 3 + 2];
        }
        try{
            initTask.get();
        }catch(Exception ignored){
            System.arraycopy(items, 0, itemCopy, 0, numRequests);
        }
        PopulateTask.tasks = sorted;
        PopulateTask.src = itemCopy;
        PopulateTask.dest = items;
        PopulateTask.locs = locs;
        commonPool.pool.invoke(new PopulateTask(0, L));
    }

    protected void sortRequestsStandard(){ // Non-threaded implementation for weak devices
        final int numRequests = this.numRequests;
        if(copy.length < numRequests) copy = new DrawRequest[numRequests + (numRequests >> 3)];
        final DrawRequest[] items = copy;
        final float[] itemZ = requestZ;
        System.arraycopy(requests, 0, items, 0, numRequests);
        int[] contiguous = this.contiguous;
        int ci = 0, cl = contiguous.length;
        float z = itemZ[0];
        int startI = 0;
        // Point3: <z, index, length>
        for(int i = 1; i < numRequests; i++){
            if(itemZ[i] != z){ // if contiguous section should end
                contiguous[ci] = Float.floatToRawIntBits(z + 16f);
                contiguous[ci + 1] = startI;
                contiguous[ci + 2] = i - startI;
                ci += 3;
                if(ci + 3 > cl){
                    contiguous = Arrays.copyOf(contiguous, cl <<= 1);
                }
                z = itemZ[startI = i];
            }
        }
        contiguous[ci] = Float.floatToRawIntBits(z + 16f);
        contiguous[ci + 1] = startI;
        contiguous[ci + 2] = numRequests - startI;
        this.contiguous = contiguous;

        final int L = (ci / 3) + 1;

        if(contiguousCopy.length < contiguous.length) contiguousCopy = new int[contiguous.length];

        final int[] sorted = CountingSort.countingSortMap(contiguous, contiguousCopy, L);

        int ptr = 0;
        final DrawRequest[] dest = requests;
        for(int i = 0; i < L * 3; i += 3){
            final int pos = sorted[i + 1], length = sorted[i + 2];
            if(length < 10){
                final int end = pos + length;
                for(int sj = pos, dj = ptr; sj < end; sj++, dj++){
                    dest[dj] = items[sj];
                }
            }else System.arraycopy(items, pos, dest, ptr, Math.min(length, dest.length - ptr));
            ptr += length;
        }
    }

    static class CountingSort{
        private static final int processors = Runtime.getRuntime().availableProcessors() * 8;

        static int[] locs = new int[100];
        static final int[][] locses = new int[processors][100];

        static final IntIntMap[] countses = new IntIntMap[processors];

        private static Point2[] entries = new Point2[100];

        private static int[] entries3 = new int[300], entries3a = new int[300];
        private static Integer[] entriesBacking = new Integer[100];

        private static final CountingSortTask[] tasks = new CountingSortTask[processors];
        private static final CountingSortTask2[] task2s = new CountingSortTask2[processors];
        private static final Future<?>[] futures = new Future<?>[processors];

        static{
            for(int i = 0; i < countses.length; i++) countses[i] = new IntIntMap();
            for(int i = 0; i < entries.length; i++) entries[i] = new Point2();

            for(int i = 0; i < processors; i++){
                tasks[i] = new CountingSortTask();
                task2s[i] = new CountingSortTask2();
            }
        }

        static class CountingSortTask implements Runnable{
            static int[] arr;
            int start, end, id;

            public void set(int start, int end, int id){
                this.start = start;
                this.end = end;
                this.id = id;
            }

            @Override
            public void run(){
                final int id = this.id, start = this.start, end = this.end;
                int[] locs = locses[id];
                final int[] arr = CountingSortTask.arr;
                final IntIntMap counts = countses[id];
                counts.clear();
                int unique = 0;
                for(int i = start; i < end; i++){
                    int loc = counts.getOrPut(arr[i * 3], unique);
                    arr[i * 3] = loc;
                    if(loc == unique){
                        if(unique >= locs.length){
                            locs = Arrays.copyOf(locs, unique * 3 / 2);
                        }
                        locs[unique++] = 1;
                    }else{
                        locs[loc]++;
                    }
                }
                locses[id] = locs;
            }
        }

        static class CountingSortTask2 implements Runnable{
            static int[] src, dest;
            int start, end, id;

            public void set(int start, int end, int id){
                this.start = start;
                this.end = end;
                this.id = id;
            }

            @Override
            public void run(){
                final int start = this.start, end = this.end;
                final int[] locs = locses[id];
                final int[] src = CountingSortTask2.src, dest = CountingSortTask2.dest;
                for(int i = end - 1, i3 = i * 3; i >= start; i--, i3 -= 3){
                    final int destPos = --locs[src[i3]] * 3;
                    dest[destPos] = src[i3];
                    dest[destPos + 1] = src[i3 + 1];
                    dest[destPos + 2] = src[i3 + 2];
                }
            }
        }

        static int[] countingSortMapMT(final int[] arr, final int[] swap, final int end){
            final IntIntMap[] countses = CountingSort.countses;
            final int[][] locs = CountingSort.locses;
            final int threads = Math.min(processors, (end + 4095) / 4096); // 4096 Point3s to process per thread
            final int thread_size = end / threads + 1;
            final CountingSortTask[] tasks = CountingSort.tasks;
            final CountingSortTask2[] task2s = CountingSort.task2s;
            final Future<?>[] futures = CountingSort.futures;
            CountingSortTask.arr = CountingSortTask2.src = arr;
            CountingSortTask2.dest = swap;

            for(int s = 0, thread = 0; thread < threads; thread++, s += thread_size){
                CountingSortTask task = tasks[thread];
                final int stop = Math.min(s + thread_size, end);
                task.set(s, stop, thread);
                task2s[thread].set(s, stop, thread);
                futures[thread] = commonPool.pool.submit(task);
            }

            int unique = 0;
            for(int i = 0; i < threads; i++){
                try{
                    futures[i].get();
                }catch(ExecutionException | InterruptedException e){
                    commonPool.pool.execute(tasks[i]);
                }
                unique += countses[i].size;
            }

            final int L = unique;
            if(entriesBacking.length < L){
                entriesBacking = new Integer[L * 3 / 2];
                entries3 = new int[L * 3 * 3 / 2];
                entries3a = new int[L * 3 * 3 / 2];
            }
            final int[] entries = CountingSort.entries3, entries3a = CountingSort.entries3a;
            final Integer[] entriesBacking = CountingSort.entriesBacking;
            int j = 0;
            for(int i = 0; i < threads; i++){
                if(countses[i].size == 0) continue;
                final IntIntMap.Entries countEntries = countses[i].entries();
                final IntIntMap.Entry entry = countEntries.next();
                entries[j] = entry.key;
                entries[j + 1] = entry.value;
                entries[j + 2] = i;
                j += 3;
                while(countEntries.hasNext){
                    countEntries.next();
                    entries[j] = entry.key;
                    entries[j + 1] = entry.value;
                    entries[j + 2] = i;
                    j += 3;
                }
            }

            for(int i = 0; i < L; i++){
                entriesBacking[i] = i;
            }
            Arrays.sort(entriesBacking, 0, L, Structs.comparingInt(i -> entries[i * 3]));
            for(int i = 0; i < L; i++){
                int from = entriesBacking[i] * 3, to = i * 3;
                entries3a[to] = entries[from];
                entries3a[to + 1] = entries[from + 1];
                entries3a[to + 2] = entries[from + 2];
            }

            for(int i = 0, pos = 0; i < L * 3; i += 3){
                pos = (locs[entries3a[i + 2]][entries3a[i + 1]] += pos);
            }

            for(int thread = 0; thread < threads; thread++){
                futures[thread] = commonPool.pool.submit(task2s[thread]);
            }
            for(int i = 0; i < threads; i++){
                try{
                    futures[i].get();
                }catch(ExecutionException | InterruptedException e){
                    commonPool.pool.execute(task2s[i]);
                }
            }
            return swap;
        }

        static int[] countingSortMap(final int[] arr, final int[] swap, final int end){
            int[] locs = CountingSort.locs;
            final IntIntMap counts = CountingSort.countses[0];
            counts.clear();

            int unique = 0;
            final int end3 = end * 3;
            for(int i = 0; i < end3; i += 3){
                int loc = counts.getOrPut(arr[i], unique);
                arr[i] = loc;
                if(loc == unique){
                    if(unique >= locs.length){
                        locs = Arrays.copyOf(locs, unique * 3 / 2);
                    }
                    locs[unique++] = 1;
                }else{
                    locs[loc]++;
                }
            }
            CountingSort.locs = locs;

            if(entries.length < unique){
                final int prevLength = entries.length;
                entries = Arrays.copyOf(entries, unique * 3 / 2);
                final Point2[] entries = CountingSort.entries;
                for(int i = prevLength; i < entries.length; i++) entries[i] = new Point2();
            }
            final Point2[] entries = CountingSort.entries;

            final IntIntMap.Entries countEntries = counts.entries();
            final IntIntMap.Entry entry = countEntries.next();
            entries[0].set(entry.key, entry.value);
            int j = 1;
            while(countEntries.hasNext){
                countEntries.next(); // it returns the same entry over and over again.
                entries[j++].set(entry.key, entry.value);
            }
            Arrays.sort(entries, 0, unique, Structs.comparingInt(p -> p.x));

            int prev = entries[0].y, next;
            for(int i = 1; i < unique; i++){
                locs[next = entries[i].y] += locs[prev];
                prev = next;
            }
            for(int i = end - 1, i3 = i * 3; i >= 0; i--, i3 -= 3){
                final int destPos = --locs[arr[i3]] * 3;
                swap[destPos] = arr[i3];
                swap[destPos + 1] = arr[i3 + 1];
                swap[destPos + 2] = arr[i3 + 2];
            }
            return swap;
        }
    }

    static class PopulateTask extends RecursiveAction{
        int from, to;
        static int[] tasks;
        static DrawRequest[] src;
        static DrawRequest[] dest;
        static int[] locs;

        //private static final int threshold = 256;
        PopulateTask(int from, int to){
            this.from = from;
            this.to = to;
        }

        @Override
        protected void compute(){
            final int[] locs = PopulateTask.locs;
            if(to - from > 1 && locs[to] - locs[from] > 2048){
                final int half = (locs[to] + locs[from]) >> 1;
                int mid = Arrays.binarySearch(locs, from, to, half);
                if(mid < 0) mid = -mid - 1;
                if(mid != from && mid != to){
                    invokeAll(new PopulateTask(from, mid), new PopulateTask(mid, to));
                    return;
                }
            }
            final DrawRequest[] src = PopulateTask.src, dest = PopulateTask.dest;
            final int[] tasks = PopulateTask.tasks;
            for(int i = from; i < to; i++){
                final int point = i * 3, pos = tasks[point + 1], length = tasks[point + 2];
                if(length < 10){
                    final int end = pos + length;
                    for(int sj = pos, dj = locs[i]; sj < end; sj++, dj++){
                        dest[dj] = src[sj];
                    }
                }else{
                    System.arraycopy(src, pos, dest, locs[i], Math.min(length, dest.length - locs[i]));
                }
            }
        }
    }
}
