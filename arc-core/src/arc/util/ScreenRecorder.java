package arc.util;

import arc.*;

import java.lang.reflect.*;

public class ScreenRecorder{
    private static Runnable record;

    static{
        try{
            Class<?> recorderClass = Class.forName("arc.gif.GifRecorder");
            Object recorder = recorderClass.getConstructor().newInstance();
            Reflect.set(recorder, "exportDirectory", Core.files.local("../../gifs"));
            Method r = recorderClass.getMethod("update");
            Object[] args = {};
            record = () -> {
                try{
                    r.invoke(recorder, args);
                }catch(Throwable ignored){}
            };
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    public static void record(){
        if(record == null) return;
        record.run();
    }
}
