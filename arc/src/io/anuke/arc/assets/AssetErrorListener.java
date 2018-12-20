package io.anuke.arc.assets;

public interface AssetErrorListener{
    void error(AssetDescriptor asset, Throwable throwable);
}
