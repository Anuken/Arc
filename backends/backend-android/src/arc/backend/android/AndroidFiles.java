package arc.backend.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import arc.Core;
import arc.Files;
import arc.files.Fi;
import arc.util.ArcRuntimeException;

import java.io.IOException;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
public class AndroidFiles implements Files{
    protected final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    protected final String localpath;

    protected final AssetManager assets;
    private ZipResourceFile expansionFile = null;

    public AndroidFiles(AssetManager assets){
        this.assets = assets;
        localpath = sdcard;
    }

    public AndroidFiles(AssetManager assets, String localpath){
        this.assets = assets;
        this.localpath = localpath.endsWith("/") ? localpath : localpath + "/";
    }

    @Override
    public Fi get(String path, FileType type){
        Fi handle = new AndroidFi(type == FileType.internal ? assets : null, path, type);
        if(expansionFile != null && type == FileType.internal) handle = getZipFileHandleIfExists(handle, path);
        return handle;
    }

    private Fi getZipFileHandleIfExists(Fi handle, String path){
        try{
            assets.open(path).close(); // Check if file exists.
            return handle;
        }catch(Exception ex){
            // try APK expansion instead
            Fi zipHandle = new AndroidZipFi(path);
            if(!zipHandle.isDirectory())
                return zipHandle;
            else if(zipHandle.exists()) return zipHandle;
        }
        return handle;
    }

    @Override
    public String getExternalStoragePath(){
        return sdcard;
    }

    @Override
    public boolean isExternalStorageAvailable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public String getLocalStoragePath(){
        return localpath;
    }

    @Override
    public boolean isLocalStorageAvailable(){
        return true;
    }

    /**
     * This method can be called to set the version code of the APK expansion
     * file(s) used by the application
     * @param mainVersion - version code of the main expansion file
     * @param patchVersion - version code of the patch expansion file
     * @return true if the APK expansion file could be opened, false otherwise
     */
    public boolean setAPKExpansion(int mainVersion, int patchVersion){
        try{
            Context context;
            if(Core.app instanceof Activity){
                context = ((Activity)Core.app).getBaseContext();
            }else if(Core.app instanceof Fragment){
                context = ((Fragment)Core.app).getActivity().getBaseContext();
            }else{
                throw new ArcRuntimeException("APK expansion not supported for application type");
            }
            expansionFile = APKExpansionSupport.getAPKExpansionZipFile(
            context,
            mainVersion, patchVersion);
        }catch(IOException ex){
            throw new ArcRuntimeException("APK expansion main version " + mainVersion + " or patch version " + patchVersion + " couldn't be opened!");
        }
        return expansionFile != null;
    }

    /** @return The application's APK extension file */
    public ZipResourceFile getExpansionFile(){
        return expansionFile;
    }
}
