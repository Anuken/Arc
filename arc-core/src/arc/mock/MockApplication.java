package arc.mock;

import arc.*;
import arc.struct.*;

public class MockApplication implements Application{
    @Override
    public Array<ApplicationListener> getListeners(){
        return new Array<>();
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.HeadlessDesktop;
    }

    @Override
    public String getClipboardText(){
        return null;
    }

    @Override
    public void setClipboardText(String text){

    }

    @Override
    public void post(Runnable runnable){

    }

    @Override
    public void exit(){

    }
}
