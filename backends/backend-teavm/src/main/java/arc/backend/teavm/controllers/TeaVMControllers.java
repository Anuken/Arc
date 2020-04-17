package arc.backend.teavm.controllers;

import org.teavm.jso.browser.*;

public class TeaVMControllers{

    public void init(){
        Window.current().listenGamepadConnected(pad -> {

        });

        Window.current().listenGamepadDisconnected(pad -> {

        });
    }
}
