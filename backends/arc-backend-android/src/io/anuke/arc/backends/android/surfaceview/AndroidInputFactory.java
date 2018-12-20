package io.anuke.arc.backends.android.surfaceview;

import android.content.Context;
import io.anuke.arc.Application;

import java.lang.reflect.Constructor;

/**
 * Class that instantiates AndroidInput or AndroidInputThreePlus depending on the SDK level, via reflection.
 * @author mzechner
 */
public class AndroidInputFactory{
    public static AndroidInput newAndroidInput(Application activity, Context context, Object view,
                                               AndroidApplicationConfiguration config){
        try{
            Class<?> clazz = null;
            AndroidInput input = null;

            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            if(sdkVersion >= 12){
                clazz = Class.forName("io.anuke.arc.backends.android.surfaceview.AndroidInputThreePlus");
            }else{
                clazz = Class.forName("io.anuke.arc.backends.android.surfaceview.AndroidInput");
            }
            Constructor<?> constructor = clazz.getConstructor(Application.class, Context.class, Object.class,
            AndroidApplicationConfiguration.class);
            input = (AndroidInput)constructor.newInstance(activity, context, view, config);
            return input;
        }catch(Exception e){
            throw new RuntimeException("Couldn't construct AndroidInput, this should never happen", e);
        }
    }
}
