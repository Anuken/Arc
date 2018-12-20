package io.anuke.arc.backends.android.surfaceview;

import io.anuke.arc.input.InputProcessor;

public interface InputProcessorLW extends InputProcessor{

    void touchDrop(int x, int y);

}
