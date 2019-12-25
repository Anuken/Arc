package arc.assets;

public class RefCountedContainer{
    Object object;
    int refCount = 1;

    public RefCountedContainer(Object object){
        if(object == null) throw new IllegalArgumentException("Object must not be null");
        this.object = object;
    }

    public void incRefCount(){
        refCount++;
    }

    public void decRefCount(){
        refCount--;
    }

    public int getRefCount(){
        return refCount;
    }

    public void setRefCount(int refCount){
        this.refCount = refCount;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> type){
        return (T)object;
    }

    public void setObject(Object asset){
        this.object = asset;
    }
}
