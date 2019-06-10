package io.anuke.arc.backends.android.surfaceview;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import io.anuke.arc.Core;
import io.anuke.arc.Files.FileType;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.ArcRuntimeException;
import io.anuke.arc.util.io.Streams;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
public class AndroidFileHandle extends FileHandle{
    // The asset manager, or null if this is not an internal file.
    final private AssetManager assets;

    AndroidFileHandle(AssetManager assets, String fileName, FileType type){
        super(fileName.replace('\\', '/'), type);
        this.assets = assets;
    }

    AndroidFileHandle(AssetManager assets, File file, FileType type){
        super(file, type);
        this.assets = assets;
    }

    public FileHandle child(String name){
        name = name.replace('\\', '/');
        if(file.getPath().length() == 0) return new AndroidFileHandle(assets, new File(name), type);
        return new AndroidFileHandle(assets, new File(file, name), type);
    }

    public FileHandle sibling(String name){
        name = name.replace('\\', '/');
        if(file.getPath().length() == 0) throw new ArcRuntimeException("Cannot get the sibling of the root.");
        return Core.files.getFileHandle(new File(file.getParent(), name).getPath(), type); //this way we can find the sibling even if it's inside the obb
    }

    public FileHandle parent(){
        File parent = file.getParentFile();
        if(parent == null){
            if(type == FileType.Absolute)
                parent = new File("/");
            else
                parent = new File("");
        }
        return new AndroidFileHandle(assets, parent, type);
    }

    public InputStream read(){
        if(type == FileType.Internal){
            try{
                return assets.open(file.getPath());
            }catch(IOException ex){
                throw new ArcRuntimeException("Error reading file: " + file + " (" + type + ")", ex);
            }
        }
        return super.read();
    }

    public ByteBuffer map(FileChannel.MapMode mode){
        if(type == FileType.Internal){
            FileInputStream input = null;
            try{
                AssetFileDescriptor fd = getAssetFileDescriptor();
                long startOffset = fd.getStartOffset();
                long declaredLength = fd.getDeclaredLength();
                input = new FileInputStream(fd.getFileDescriptor());
                ByteBuffer map = input.getChannel().map(mode, startOffset, declaredLength);
                map.order(ByteOrder.nativeOrder());
                return map;
            }catch(Exception ex){
                throw new ArcRuntimeException("Error memory mapping file: " + this + " (" + type + ")", ex);
            }finally{
                Streams.closeQuietly(input);
            }
        }
        return super.map(mode);
    }

    public FileHandle[] list(){
        if(type == FileType.Internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                for(int i = 0, n = handles.length; i < n; i++)
                    handles[i] = new AndroidFileHandle(assets, new File(file, relativePaths[i]), type);
                return handles;
            }catch(Exception ex){
                throw new ArcRuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list();
    }

    public FileHandle[] list(FileFilter filter){
        if(type == FileType.Internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                int count = 0;
                for(int i = 0, n = handles.length; i < n; i++){
                    String path = relativePaths[i];
                    FileHandle child = new AndroidFileHandle(assets, new File(file, path), type);
                    if(!filter.accept(child.file())) continue;
                    handles[count] = child;
                    count++;
                }
                if(count < relativePaths.length){
                    FileHandle[] newHandles = new FileHandle[count];
                    System.arraycopy(handles, 0, newHandles, 0, count);
                    handles = newHandles;
                }
                return handles;
            }catch(Exception ex){
                throw new ArcRuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list(filter);
    }

    public FileHandle[] list(FilenameFilter filter){
        if(type == FileType.Internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                int count = 0;
                for(int i = 0, n = handles.length; i < n; i++){
                    String path = relativePaths[i];
                    if(!filter.accept(file, path)) continue;
                    handles[count] = new AndroidFileHandle(assets, new File(file, path), type);
                    count++;
                }
                if(count < relativePaths.length){
                    FileHandle[] newHandles = new FileHandle[count];
                    System.arraycopy(handles, 0, newHandles, 0, count);
                    handles = newHandles;
                }
                return handles;
            }catch(Exception ex){
                throw new ArcRuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list(filter);
    }

    public FileHandle[] list(String suffix){
        if(type == FileType.Internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                int count = 0;
                for(int i = 0, n = handles.length; i < n; i++){
                    String path = relativePaths[i];
                    if(!path.endsWith(suffix)) continue;
                    handles[count] = new AndroidFileHandle(assets, new File(file, path), type);
                    count++;
                }
                if(count < relativePaths.length){
                    FileHandle[] newHandles = new FileHandle[count];
                    System.arraycopy(handles, 0, newHandles, 0, count);
                    handles = newHandles;
                }
                return handles;
            }catch(Exception ex){
                throw new ArcRuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list(suffix);
    }

    public boolean isDirectory(){
        if(type == FileType.Internal){
            try{
                return assets.list(file.getPath()).length > 0;
            }catch(IOException ex){
                return false;
            }
        }
        return super.isDirectory();
    }

    public boolean exists(){
        if(type == FileType.Internal){
            String fileName = file.getPath();
            try{
                assets.open(fileName).close(); // Check if file exists.
                return true;
            }catch(Exception ex){
                // This is SUPER slow! but we need it for directories.
                try{
                    return assets.list(fileName).length > 0;
                }catch(Exception ignored){
                }
                return false;
            }
        }
        return super.exists();
    }

    public long length(){
        if(type == FileType.Internal){
            try(AssetFileDescriptor fileDescriptor = assets.openFd(file.getPath())){
                return fileDescriptor.getLength();
            }catch(IOException ignored){
            }
        }
        return super.length();
    }

    public long lastModified(){
        return super.lastModified();
    }

    public File file(){
        if(type == FileType.Local) return new File(Core.files.getLocalStoragePath(), file.getPath());
        return super.file();
    }

    /**
     * @return an AssetFileDescriptor for this file or null if the file is not of type Internal
     * @throws IOException - thrown by AssetManager.openFd()
     */
    public AssetFileDescriptor getAssetFileDescriptor() throws IOException{
        return assets != null ? assets.openFd(path()) : null;
    }
}
