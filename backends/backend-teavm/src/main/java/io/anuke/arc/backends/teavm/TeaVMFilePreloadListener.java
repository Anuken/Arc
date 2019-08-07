package io.anuke.arc.backends.teavm;

/**
 *
 * @author Alexey Andreev
 */
public interface TeaVMFilePreloadListener {
    void error();

    void complete();
}
