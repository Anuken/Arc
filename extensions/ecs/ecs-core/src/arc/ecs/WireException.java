package arc.ecs;


public class WireException extends RuntimeException{

    public WireException(Class<? extends BaseSystem> klazz){
        super("Not added to base: " + klazz.getSimpleName());
    }

    public WireException(String message, Throwable cause){
        super(message, cause);
    }

    public WireException(String message){
        super(message);
    }
}
