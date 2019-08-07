package io.anuke.arc.backends.teavm.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexey Andreev
 */

public class FileDescriptor {
    private List<FileDescriptor> childFiles = new ArrayList<>();
    private String name;
    private boolean directory;

    public List<FileDescriptor> getChildFiles() {
        return childFiles;
    }

    public void setChildFiles(List<FileDescriptor> childFiles) {
        this.childFiles = childFiles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
}
