package arc.packer;

import arc.packer.TexturePacker.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;

import java.io.*;
import java.util.regex.*;

/** @author Nathan Sweet */
public class TexturePackerFileProcessor extends FileProcessor{
    private final Settings defaultSettings;
    private ObjectMap<File, Settings> dirToSettings = new ObjectMap<>();
    private Json json = new Json();
    private String packFileName;
    private File root;
    Seq<File> ignoreDirs = new Seq<>();
    boolean countOnly;
    int packCount;

    public TexturePackerFileProcessor(){
        this(new Settings(), "pack.aatls");
    }

    public TexturePackerFileProcessor(Settings defaultSettings, String packFileName){
        this.defaultSettings = defaultSettings;

        if(packFileName.toLowerCase().endsWith(defaultSettings.atlasExtension.toLowerCase()))
            packFileName = packFileName.substring(0, packFileName.length() - defaultSettings.atlasExtension.length());
        this.packFileName = packFileName;

        setFlattenOutput(true);
        addInputSuffix(".png", ".jpg", ".jpeg");

        // Sort input files by name to avoid platform-dependent atlas output changes.
        setComparator(Structs.comparing(File::getName));
    }

    @Override
    public Seq<Entry> process(File inputFile, File outputRoot) throws Exception{
        root = inputFile;

        // Collect pack.json setting files.
        final Seq<File> settingsFiles = new Seq<>();
        FileProcessor settingsProcessor = new FileProcessor(){
            @Override
            protected void processFile(Entry inputFile){
                settingsFiles.add(inputFile.inputFile);
            }
        };
        settingsProcessor.addInputRegex("pack\\.json");
        settingsProcessor.process(inputFile, null);
        // Sort parent first.
        settingsFiles.sort(Structs.comparingInt(file -> file.toString().length()));
        for(File settingsFile : settingsFiles){
            // Find first parent with settings, or use defaults.
            Settings settings = null;
            File parent = settingsFile.getParentFile();
            while(true){
                if(parent.equals(root)) break;
                parent = parent.getParentFile();
                settings = dirToSettings.get(parent);
                if(settings != null){
                    settings = settings.copy();
                    break;
                }
            }
            if(settings == null) settings = defaultSettings.copy();
            // Merge settings from current directory.
            merge(settings, settingsFile);
            dirToSettings.put(settingsFile.getParentFile(), settings);
        }

        // Count the number of texture packer invocations.
        countOnly = true;
        super.process(inputFile, outputRoot);
        countOnly = false;

        // Do actual processing.
        return super.process(inputFile, outputRoot);
    }

    void merge(Settings settings, File settingsFile){
        try{
            json.readFields(settings, new JsonReader().parse(new FileReader(settingsFile)));
        }catch(Exception ex){
            throw new ArcRuntimeException("Error reading settings file: " + settingsFile, ex);
        }
    }

    @Override
    public Seq<Entry> process(File[] files, File outputRoot) throws Exception{
        // Delete pack file and images.
        if(countOnly && outputRoot.exists()) deleteOutput(outputRoot);
        return super.process(files, outputRoot);
    }

    protected void deleteOutput(File outputRoot) throws Exception{
        // Load root settings to get scale.
        File settingsFile = new File(root, "pack.json");
        Settings rootSettings = defaultSettings;
        if(settingsFile.exists()){
            rootSettings = rootSettings.copy();
            merge(rootSettings, settingsFile);
        }

        String atlasExtension = rootSettings.atlasExtension == null ? "" : rootSettings.atlasExtension;
        atlasExtension = Pattern.quote(atlasExtension);

        for(int i = 0, n = rootSettings.scale.length; i < n; i++){
            FileProcessor deleteProcessor = new FileProcessor(){
                @Override
                protected void processFile(Entry inputFile) throws Exception{
                    inputFile.inputFile.delete();
                }
            };
            deleteProcessor.setRecursive(false);

            File packFile = new File(rootSettings.getScaledPackFileName(packFileName, i));

            String prefix = packFile.getName();
            int dotIndex = prefix.lastIndexOf('.');
            if(dotIndex != -1) prefix = prefix.substring(0, dotIndex);
            deleteProcessor.addInputRegex("(?i)" + prefix + "\\d*\\.(png|jpg|jpeg)");
            deleteProcessor.addInputRegex("(?i)" + prefix + atlasExtension);

            String dir = packFile.getParent();
            if(dir == null)
                deleteProcessor.process(outputRoot, null);
            else if(new File(outputRoot + "/" + dir).exists()) //
                deleteProcessor.process(outputRoot + "/" + dir, null);
        }
    }

    @Override
    protected void processDir(final Entry inputDir, Seq<Entry> files) throws Exception{
        if(ignoreDirs.contains(inputDir.inputFile)) return;

        // Find first parent with settings, or use defaults.
        Settings settings = null;
        File parent = inputDir.inputFile;
        while(true){
            settings = dirToSettings.get(parent);
            if(settings != null) break;
            if(parent == null || parent.equals(root)) break;
            parent = parent.getParentFile();
        }
        if(settings == null) settings = defaultSettings;

        if(settings.ignore) return;

        if(settings.combineSubdirectories){
            // Collect all files under subdirectories and ignore subdirectories without pack.json files.
            files = new FileProcessor(this){
                @Override
                protected void processDir(Entry entryDir, Seq<Entry> files){
                    if(!entryDir.inputFile.equals(inputDir.inputFile) && new File(entryDir.inputFile, "pack.json").exists()){
                        files.clear();
                        return;
                    }
                    if(!countOnly) ignoreDirs.add(entryDir.inputFile);
                }

                @Override
                protected void processFile(Entry entry){
                    addProcessedFile(entry);
                }
            }.process(inputDir.inputFile, null);
        }

        if(files.isEmpty()) return;

        if(countOnly){
            packCount++;
            return;
        }

        final Pattern digitSuffix = Pattern.compile("(.*?)(\\d+)$");

        // Sort by name using numeric suffix, then alpha.
        files.sort((entry1, entry2) -> {
            String full1 = entry1.inputFile.getName();
            int dotIndex = full1.lastIndexOf('.');
            if(dotIndex != -1) full1 = full1.substring(0, dotIndex);

            String full2 = entry2.inputFile.getName();
            dotIndex = full2.lastIndexOf('.');
            if(dotIndex != -1) full2 = full2.substring(0, dotIndex);

            String name1 = full1, name2 = full2;
            int num1 = 0, num2 = 0;

            Matcher matcher = digitSuffix.matcher(full1);
            if(matcher.matches()){
                try{
                    num1 = Integer.parseInt(matcher.group(2));
                    name1 = matcher.group(1);
                }catch(Exception ignored){
                }
            }
            matcher = digitSuffix.matcher(full2);
            if(matcher.matches()){
                try{
                    num2 = Integer.parseInt(matcher.group(2));
                    name2 = matcher.group(1);
                }catch(Exception ignored){
                }
            }
            int compare = name1.compareTo(name2);
            if(compare != 0 || num1 == num2) return compare;
            return num1 - num2;
        });

        // Pack.
        if(!settings.silent){
            try{
                System.out.println(inputDir.inputFile.getCanonicalPath());
            }catch(IOException ignored){
                System.out.println(inputDir.inputFile.getAbsolutePath());
            }
        }

        TexturePacker packer = newTexturePacker(root, settings);
        for(Entry file : files)
            packer.addImage(file.inputFile);
        pack(packer, inputDir);
    }

    protected void pack(TexturePacker packer, Entry inputDir){
        packer.pack(inputDir.outputDir, packFileName);
    }

    protected TexturePacker newTexturePacker(File root, Settings settings){
        return new TexturePacker(root, settings);
    }
}
