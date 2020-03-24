package arc.backend.android;

import android.content.res.AssetFileDescriptor;
import arc.Core;
import arc.Files.FileType;
import arc.backend.android.ZipResourceFile.ZipEntryRO;
import arc.files.Fi;
import arc.util.ArcRuntimeException;

import java.io.*;

/** @author sarkanyi */
public class AndroidZipFi extends AndroidFi{
    private AssetFileDescriptor assetFd;
    private ZipResourceFile expansionFile;
    private String path;

    public AndroidZipFi(String fileName){
        super(null, fileName, FileType.internal);
        initialize();
    }

    public AndroidZipFi(File file, FileType type){
        super(null, file, type);
        initialize();
    }

    private void initialize(){
        path = file.getPath().replace('\\', '/');
        expansionFile = ((AndroidFiles)Core.files).getExpansionFile();
        assetFd = expansionFile.getAssetFileDescriptor(getPath());

        // needed for listing entries and exists() of directories
        if(isDirectory())
            path += "/";
    }

    @Override
    public AssetFileDescriptor getAssetFileDescriptor(){
        return assetFd;
    }

    private String getPath(){
        return path;
    }

    @Override
    public InputStream read(){
        InputStream input = null;

        try{
            input = expansionFile.getInputStream(getPath());
        }catch(IOException ex){
            throw new ArcRuntimeException("Error reading file: " + file + " (ZipResourceFile)", ex);
        }
        return input;
    }

    @Override
    public Fi child(String name){
        if(file.getPath().length() == 0)
            return new AndroidZipFi(new File(name), type);
        return new AndroidZipFi(new File(file, name), type);
    }

    @Override
    public Fi sibling(String name){
        if(file.getPath().length() == 0)
            throw new ArcRuntimeException("Cannot get the sibling of the root.");
        return Core.files.get(new File(file.getParent(), name).getPath(), type); //this way we can find the sibling even if it's not inside the obb
    }

    @Override
    public Fi parent(){
        File parent = file.getParentFile();
        if(parent == null)
            parent = new File("");
        return new AndroidZipFi(parent.getPath());
    }

    @Override
    public Fi[] list(){
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        Fi[] handles = new Fi[zipEntries.length];
        for(int i = 0, n = handles.length; i < n; i++)
            handles[i] = new AndroidZipFi(zipEntries[i].mFileName);
        return handles;
    }

    @Override
    public Fi[] list(FileFilter filter){
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        Fi[] handles = new Fi[zipEntries.length];
        int count = 0;
        for(int i = 0, n = handles.length; i < n; i++){
            Fi child = new AndroidZipFi(zipEntries[i].mFileName);
            if(!filter.accept(child.file()))
                continue;
            handles[count] = child;
            count++;
        }
        if(count < zipEntries.length){
            Fi[] newHandles = new Fi[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            handles = newHandles;
        }
        return handles;
    }

    @Override
    public Fi[] list(FilenameFilter filter){
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        Fi[] handles = new Fi[zipEntries.length];
        int count = 0;
        for(int i = 0, n = handles.length; i < n; i++){
            String path = zipEntries[i].mFileName;
            if(!filter.accept(file, path))
                continue;
            handles[count] = new AndroidZipFi(path);
            count++;
        }
        if(count < zipEntries.length){
            Fi[] newHandles = new Fi[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            handles = newHandles;
        }
        return handles;
    }

    @Override
    public Fi[] list(String suffix){
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        Fi[] handles = new Fi[zipEntries.length];
        int count = 0;
        for(int i = 0, n = handles.length; i < n; i++){
            String path = zipEntries[i].mFileName;
            if(!path.endsWith(suffix))
                continue;
            handles[count] = new AndroidZipFi(path);
            count++;
        }
        if(count < zipEntries.length){
            Fi[] newHandles = new Fi[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            handles = newHandles;
        }
        return handles;
    }

    @Override
    public boolean isDirectory(){
        return assetFd == null;
    }

    @Override
    public long length(){
        return assetFd != null ? assetFd.getLength() : 0;
    }

    @Override
    public boolean exists(){
        return assetFd != null || expansionFile.getEntriesAt(getPath()).length != 0;
    }
}
