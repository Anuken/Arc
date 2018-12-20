/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.backends.android.surfaceview;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import io.anuke.arc.Core;
import io.anuke.arc.Files;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.utils.ArcRuntimeException;

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
    public FileHandle getFileHandle(String path, FileType type){
        FileHandle handle = new AndroidFileHandle(type == FileType.Internal ? assets : null, path, type);
        if(expansionFile != null && type == FileType.Internal) handle = getZipFileHandleIfExists(handle, path);
        return handle;
    }

    private FileHandle getZipFileHandleIfExists(FileHandle handle, String path){
        try{
            assets.open(path).close(); // Check if file exists.
            return handle;
        }catch(Exception ex){
            // try APK expansion instead
            FileHandle zipHandle = new AndroidZipFileHandle(path);
            if(!zipHandle.isDirectory())
                return zipHandle;
            else if(zipHandle.exists()) return zipHandle;
        }
        return handle;
    }

    @Override
    public FileHandle classpath(String path){
        return new AndroidFileHandle(null, path, FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path){
        FileHandle handle = new AndroidFileHandle(assets, path, FileType.Internal);
        if(expansionFile != null) handle = getZipFileHandleIfExists(handle, path);
        return handle;
    }

    @Override
    public FileHandle external(String path){
        return new AndroidFileHandle(null, path, FileType.External);
    }

    @Override
    public FileHandle absolute(String path){
        return new AndroidFileHandle(null, path, FileType.Absolute);
    }

    @Override
    public FileHandle local(String path){
        return new AndroidFileHandle(null, path, FileType.Local);
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
