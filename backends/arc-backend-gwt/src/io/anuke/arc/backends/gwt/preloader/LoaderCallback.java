package io.anuke.arc.backends.gwt.preloader;

public interface LoaderCallback<T>{
    void success(T result);

    void error();
}
