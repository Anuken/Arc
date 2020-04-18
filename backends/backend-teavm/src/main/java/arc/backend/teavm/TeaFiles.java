package arc.backend.teavm;

import arc.*;
import arc.files.*;
import arc.util.*;
import org.teavm.jso.browser.*;

public class TeaFiles implements Files{
    public static final Storage localStorage = Storage.getLocalStorage();

    @Override
    public Fi get(String path, FileType type){
        //classpath files are treated as internal
        if(type == FileType.classpath) type = FileType.internal;
        if(type != FileType.internal){
            throw new ArcRuntimeException("FileType '" + type + "' not supported in TeaVM backend");
        }
        return new TeaFi(path, type);
    }

    @Override
    public String getExternalStoragePath(){
        return null;
    }

    @Override
    public boolean isExternalStorageAvailable(){
        return false;
    }

    @Override
    public String getLocalStoragePath(){
        return null;
    }

    @Override
    public boolean isLocalStorageAvailable(){
        return false;
    }
}
