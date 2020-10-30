package arc.mock;

import arc.audio.*;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockAudio extends Audio{

    @Override
    protected void initialize(){
        //doesn't even try to initialize
    }
}
