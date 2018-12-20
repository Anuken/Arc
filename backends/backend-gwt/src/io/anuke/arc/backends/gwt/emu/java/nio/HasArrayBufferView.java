package java.nio;

import com.google.gwt.typedarrays.shared.ArrayBufferView;

public interface HasArrayBufferView{

    ArrayBufferView getTypedArray();

    int getElementSize();
}
