package arc.packer;

import arc.packer.TexturePacker.*;
import arc.struct.*;

/** @author Nathan Sweet */
public class GridPacker implements Packer{
    private final Settings settings;

    public GridPacker(Settings settings){
        this.settings = settings;
    }

    @Override
    public Array<Page> pack(Array<Rect> inputRects){
        return pack(null, inputRects);
    }

    @Override
    public Array<Page> pack(ProgressListener progress, Array<Rect> inputRects){
        if(!settings.silent) System.out.print("Packing");

        // Rects are packed with right and top padding, so the max size is increased to match. After packing the padding is
        // subtracted from the page size.
        int paddingX = settings.paddingX, paddingY = settings.paddingY;
        int adjustX = paddingX, adjustY = paddingY;
        if(settings.edgePadding){
            if(settings.duplicatePadding){
                adjustX -= paddingX;
                adjustY -= paddingY;
            }else{
                adjustX -= paddingX * 2;
                adjustY -= paddingY * 2;
            }
        }
        int maxWidth = settings.maxWidth + adjustX, maxHeight = settings.maxHeight + adjustY;

        int n = inputRects.size;
        int cellWidth = 0, cellHeight = 0;
        for(int i = 0; i < n; i++){
            Rect rect = inputRects.get(i);
            cellWidth = Math.max(cellWidth, rect.width);
            cellHeight = Math.max(cellHeight, rect.height);
        }
        cellWidth += paddingX;
        cellHeight += paddingY;

        inputRects.reverse();

        Array<Page> pages = new Array<>();
        while(inputRects.size > 0){
            progress.count = n - inputRects.size + 1;
            if(progress.update(progress.count, n)) break;

            Page page = packPage(inputRects, cellWidth, cellHeight, maxWidth, maxHeight);
            page.width -= paddingX;
            page.height -= paddingY;
            pages.add(page);
        }
        return pages;
    }

    private Page packPage(Array<Rect> inputRects, int cellWidth, int cellHeight, int maxWidth, int maxHeight){
        Page page = new Page();
        page.outputRects = new Array<>();

        int n = inputRects.size;
        int x = 0, y = 0;
        for(int i = n - 1; i >= 0; i--){
            if(x + cellWidth > maxWidth){
                y += cellHeight;
                if(y > maxHeight - cellHeight) break;
                x = 0;
            }
            Rect rect = inputRects.remove(i);
            rect.x = x;
            rect.y = y;
            rect.width += settings.paddingX;
            rect.height += settings.paddingY;
            page.outputRects.add(rect);
            x += cellWidth;
            page.width = Math.max(page.width, x);
            page.height = Math.max(page.height, y + cellHeight);
        }

        // Flip so rows start at top.
        for(int i = page.outputRects.size - 1; i >= 0; i--){
            Rect rect = page.outputRects.get(i);
            rect.y = page.height - rect.y - rect.height;
        }
        return page;
    }
}
