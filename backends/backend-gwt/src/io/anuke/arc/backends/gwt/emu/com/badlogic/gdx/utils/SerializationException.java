package io.anuke.arc.util;

/**
 * Indicates an error during serialization due to misconfiguration or during deserialization due to invalid input data.
 * @author Nathan Sweet
 */
public class SerializationException extends RuntimeException{
    private StringBuilder trace;

    public SerializationException(){
        super();
    }

    public SerializationException(String message, Throwable cause){
        super(message, cause);
    }

    public SerializationException(String message){
        super(message);
    }

    public SerializationException(Throwable cause){
        super("", cause);
    }

    /** Returns true if any of the exceptions that caused this exception are of the specified type. */
    public boolean causedBy(Class type){
        return causedBy(this, type);
    }

    private boolean causedBy(Throwable ex, Class type){
        Throwable cause = ex.getCause();
        if(cause == null || cause == ex) return false;
        return false;
    }

    public String getMessage(){
        if(trace == null) return super.getMessage();
        StringBuilder sb = new StringBuilder(512);
        sb.append(super.getMessage());
        if(sb.length() > 0) sb.append('\n');
        sb.append("Serialization trace:");
        sb.append(trace);
        return sb.toString();
    }

    /**
     * Adds information to the exception message about where in the the object graph serialization failure occurred. Serializers
     * can catch {@link io.anuke.arc.util.serialization.SerializationException}, add trace information, and rethrow the exception.
     */
    public void addTrace(String info){
        if(info == null) throw new IllegalArgumentException("info cannot be null.");
        if(trace == null) trace = new StringBuilder(512);
        trace.append('\n');
        trace.append(info);
    }
}
