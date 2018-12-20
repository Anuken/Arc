package io.anuke.arc;

import io.anuke.arc.files.FileHandle;
import io.anuke.arc.utils.ArcRuntimeException;

/**
 * Provides standard access to the filesystem, classpath, Android SD card, and Android assets directory.
 * @author mzechner
 * @author Nathan Sweet
 */
public interface Files{
    /**
     * Returns a handle representing a file or directory.
     * @param type Determines how the path is resolved.
     * @throws ArcRuntimeException if the type is classpath or internal and the file does not exist.
     * @see FileType
     */
    FileHandle getFileHandle(String path, FileType type);

    /** Convenience method that returns a {@link FileType#Classpath} file handle. */
    FileHandle classpath(String path);

    /** Convenience method that returns a {@link FileType#Internal} file handle. */
    FileHandle internal(String path);

    /** Convenience method that returns a {@link FileType#External} file handle. */
    FileHandle external(String path);

    /** Convenience method that returns a {@link FileType#Absolute} file handle. */
    FileHandle absolute(String path);

    /** Convenience method that returns a {@link FileType#Local} file handle. */
    FileHandle local(String path);

    /**
     * Returns the external storage path directory. This is the SD card on Android and the home directory of the current user on
     * the desktop.
     */
    String getExternalStoragePath();

    /**
     * Returns true if the external storage is ready for file IO. Eg, on Android, the SD card is not available when mounted for use
     * with a PC.
     */
    boolean isExternalStorageAvailable();

    /**
     * Returns the local storage path directory. This is the private files directory on Android and the directory of the jar on the
     * desktop.
     */
    String getLocalStoragePath();

    /** Returns true if the local storage is ready for file IO. */
    boolean isLocalStorageAvailable();

    /**
     * Indicates how to resolve a path to a file.
     * @author mzechner
     * @author Nathan Sweet
     */
    enum FileType{
        /**
         * Path relative to the root of the classpath. Classpath files are always readonly. Note that classpath files are not
         * compatible with some functionality on Android, such as {@link Audio#newSound(FileHandle)} and
         * {@link Audio#newMusic(FileHandle)}.
         */
        Classpath,

        /**
         * Path relative to the asset directory on Android and to the application's root directory on the desktop. On the desktop,
         * if the file is not found, then the classpath is checked. This enables files to be found when using JWS or applets.
         * Internal files are always readonly.
         */
        Internal,

        /** Path relative to the root of the SD card on Android and to the home directory of the current user on the desktop. */
        External,

        /**
         * Path that is a fully qualified, absolute filesystem path. To ensure portability across platforms use absolute files only
         * when absolutely (heh) necessary.
         */
        Absolute,

        /** Path relative to the private files directory on Android and to the application's root directory on the desktop. */
        Local
    }
}
