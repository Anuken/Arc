package arc.util;

import arc.*;
import arc.files.*;

import java.lang.reflect.*;

public class ScreenRecorder{
    private static Runnable record;

    static{
        try{
            Class<?> recorderClass = Class.forName("arc.gif.GifRecorder");
            Object recorder = recorderClass.getConstructor().newInstance();
            Method method = recorderClass.getMethod("setExportDirectory", Fi.class);
            method.invoke(recorder, Core.files.local("../../desktop/gifexport"));
            Method r = recorderClass.getMethod("update");
            Object[] args = {};
            record = () -> {
                try{
                    r.invoke(recorder, args);
                }catch(Exception ignored){
                }
            };
        }catch(Throwable ignored){}
    }

    public static void record(){
        if(record == null) return;
        record.run();
    }
}
