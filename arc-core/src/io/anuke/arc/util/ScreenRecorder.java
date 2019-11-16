package io.anuke.arc.util;

import io.anuke.arc.*;
import io.anuke.arc.files.*;

import java.lang.reflect.*;

public class ScreenRecorder{
    private static Runnable record;

    static{
        try{
            Class<?> recorderClass = Class.forName("io.anuke.arc.recorder.GifRecorder");
            Object recorder = recorderClass.getConstructor().newInstance();
            Method method = recorderClass.getMethod("setExportDirectory", FileHandle.class);
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
