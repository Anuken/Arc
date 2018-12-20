package java.lang;

import com.google.gwt.core.client.GWT;

public class Thread{
    public static void sleep(long millis){
        // noop emu
    }

    public static void setDefaultUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler javaHandler){
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler(){
            @Override
            public void onUncaughtException(Throwable e){
                final Thread th = new Thread(){
                    @Override
                    public String toString(){
                        return "The only thread";
                    }
                };
                javaHandler.uncaughtException(th, e);
            }
        });
    }

    public interface UncaughtExceptionHandler{
        void uncaughtException(Thread t, Throwable e);
    }
}
