package arc.backend.teavm.emu;

import arc.Files.*;
import arc.backend.teavm.plugin.Annotations.*;
import arc.files.*;
import arc.util.*;

import java.io.*;

@Replace(Fi.class)
public class FiEmu{
    protected File file;
    protected FileType type;

    protected FiEmu(){
    }

    public FiEmu(String fileName){
    }

    public FiEmu(File file){
    }

    protected FiEmu(String fileName, FileType type){
    }

    protected FiEmu(File file, FileType type){
    }

    public String path(){
        throw new ArcRuntimeException("Stub");
    }

    public String name(){
        throw new ArcRuntimeException("Stub");
    }

    public String extension(){
        throw new ArcRuntimeException("Stub");
    }

    public String nameWithoutExtension(){
        throw new ArcRuntimeException("Stub");
    }

    /** @return the path and filename without the extension, e.g. dir/dir2/file.png -> dir/dir2/file */
    public String pathWithoutExtension(){
        throw new ArcRuntimeException("Stub");
    }

    public FileType type(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a stream for reading this file as bytes.
     * @throw ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public InputStream read(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a buffered stream for reading this file as bytes.
     * @throws ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public BufferedInputStream read(int bufferSize){
        return new BufferedInputStream(read(), bufferSize);
    }

    /**
     * Returns a reader for reading this file as characters.
     * @throws ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public Reader reader(){
        return new InputStreamReader(read());
    }

    /**
     * Returns a reader for reading this file as characters.
     * @throw ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public Reader reader(String charset){
        try{
            return new InputStreamReader(read(), charset);
        }catch(UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a buffered reader for reading this file as characters.
     * @throws ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public BufferedReader reader(int bufferSize){
        return new BufferedReader(new InputStreamReader(read()), bufferSize);
    }

    /**
     * Returns a buffered reader for reading this file as characters.
     * @throw ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public BufferedReader reader(int bufferSize, String charset){
        return new BufferedReader(reader());
    }

    /**
     * Reads the entire file into a string using the platform's default charset.
     * @throw ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public String readString(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Reads the entire file into a string using the specified charset.
     * @throw ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public String readString(String charset){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Reads the entire file into a byte array.
     * @throw ArcRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    public byte[] readBytes(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Reads the entire file into the byte array. The byte array must be big enough to hold the file's data.
     * @param bytes the array to load the file into
     * @param offset the offset to start writing bytes
     * @param size the number of bytes to read, see {@link #length()}
     * @return the number of read bytes
     */
    public int readBytes(byte[] bytes, int offset, int size){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a stream for writing to this file. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public OutputStream write(boolean append){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a buffered stream for writing to this file. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @param bufferSize The size of the buffer.
     * @throws ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public OutputStream write(boolean append, int bufferSize){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Reads the remaining bytes from the specified stream and writes them to this file. The stream is closed. Parent directories
     * will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public void write(InputStream input, boolean append){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a writer for writing to this file using the default charset. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public Writer writer(boolean append){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a writer for writing to this file. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @param charset May be null to use the default charset.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public Writer writer(boolean append, String charset){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Writes the specified string to the file using the default charset. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public void writeString(String string, boolean append){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Writes the specified string to the file as UTF-8. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @param charset May be null to use the default charset.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public void writeString(String string, boolean append, String charset){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Writes the specified bytes to the file. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public void writeBytes(byte[] bytes, boolean append){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Writes the specified bytes to the file. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @throw ArcRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file, or if it could not be written.
     */
    public void writeBytes(byte[] bytes, int offset, int length, boolean append){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns the paths to the children of this directory that satisfy the specified filter. Returns an empty list if this file
     * handle represents a file and not a directory. On the desktop, an {@link FileType#Internal} handle to a directory on the
     * classpath will return a zero length array.
     * @throw ArcRuntimeException if this file is an {@link FileType#Classpath} file.
     */
    public Fi[] list(FileFilter filter){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns the paths to the children of this directory that satisfy the specified filter. Returns an empty list if this file
     * handle represents a file and not a directory. On the desktop, an {@link FileType#Internal} handle to a directory on the
     * classpath will return a zero length array.
     * @throw ArcRuntimeException if this file is an {@link FileType#Classpath} file.
     */
    public Fi[] list(FilenameFilter filter){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns the paths to the children of this directory. Returns an empty list if this file handle represents a file and not a
     * directory. On the desktop, an {@link FileType#Internal} handle to a directory on the classpath will return a zero length
     * array.
     * @throw ArcRuntimeException if this file is an {@link FileType#Classpath} file.
     */
    public Fi[] list(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns the paths to the children of this directory with the specified suffix. Returns an empty list if this file handle
     * represents a file and not a directory. On the desktop, an {@link FileType#Internal} handle to a directory on the classpath
     * will return a zero length array.
     * @throw ArcRuntimeException if this file is an {@link FileType#Classpath} file.
     */
    public Fi[] list(String suffix){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns true if this file is a directory. Always returns false for classpath files. On Android, an {@link FileType#Internal}
     * handle to an empty directory will return false. On the desktop, an {@link FileType#Internal} handle to a directory on the
     * classpath will return false.
     */
    public boolean isDirectory(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a handle to the child with the specified name.
     * @throw ArcRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} and the child
     * doesn't exist.
     */
    public Fi child(String name){
        throw new ArcRuntimeException("Stub");
    }

    public Fi parent(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns a handle to the sibling with the specified name.
     * @throw ArcRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} and the sibling
     * doesn't exist, or this file is the root.
     */
    public Fi sibling(String name){
        throw new ArcRuntimeException("Stub");
    }

    /** @throw ArcRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
    public void mkdirs(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns true if the file exists. On Android, a {@link FileType#Classpath} or {@link FileType#Internal} handle to a directory
     * will always return false.
     */
    public boolean exists(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Deletes this file or empty directory and returns success. Will not delete a directory that has children.
     * @throw ArcRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file.
     */
    public boolean delete(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Deletes this file or directory and all children, recursively.
     * @throw ArcRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file.
     */
    public boolean deleteDirectory(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Copies this file or directory to the specified file or directory. If this handle is a file, then 1) if the destination is a
     * file, it is overwritten, or 2) if the destination is a directory, this file is copied into it, or 3) if the destination
     * doesn't exist, {@link #mkdirs()} is called on the destination's parent and this file is copied into it with a new name. If
     * this handle is a directory, then 1) if the destination is a file, ArcRuntimeException is thrown, or 2) if the destination is
     * a directory, this directory is copied into it recursively, overwriting existing files, or 3) if the destination doesn't
     * exist, {@link #mkdirs()} is called on the destination and this directory is copied into it recursively.
     * @throw ArcRuntimeException if the destination file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file,
     * or copying failed.
     */
    public void copyTo(Fi dest){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Moves this file to the specified file, overwriting the file if it already exists.
     * @throw ArcRuntimeException if the source or destination file handle is a {@link FileType#Classpath} or
     * {@link FileType#Internal} file.
     */
    public void moveTo(Fi dest){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns the length in bytes of this file, or 0 if this file is a directory, does not exist, or the size cannot otherwise be
     * determined.
     */
    public long length(){
        throw new ArcRuntimeException("Stub");
    }

    /**
     * Returns the last modified time in milliseconds for this file. Zero is returned if the file doesn't exist. Zero is returned
     * for {@link FileType#Classpath} files. On Android, zero is returned for {@link FileType#Internal} files. On the desktop, zero
     * is returned for {@link FileType#Internal} files on the classpath.
     */
    public long lastModified(){
        throw new ArcRuntimeException("Stub");
    }

    public String toString(){
        throw new ArcRuntimeException("Stub");
    }
}
