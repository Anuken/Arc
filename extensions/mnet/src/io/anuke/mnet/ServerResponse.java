package io.anuke.mnet;

/**
 * Class that represents Response from a server. Might contain
 * <li>Accept of connection with response data</li>
 * <li>Error e.g. if you're already connected</li>
 * <li>Connection timeout</li>
 * <li>Connection refuse e.g. Server didn't accept your password or whatever. Also contains response</li>
 */
public class ServerResponse{

    /**
     * Type of response. If type is {@link ResponseType#ACCEPTED} then socket is considered to be connected
     * and you can expect to receive data. Nothing happens in any other cases
     */

    private ResponseType type;
    private Object response;

    public ServerResponse(ResponseType type, Object response){
        this.type = type;
        this.response = response;
    }

    public ServerResponse(ResponseType type){
        this.type = type;
    }

    /**
     * Type of response. If type is {@link ResponseType#ACCEPTED} then socket is considered to be connected
     * and you can expect to receive data. Nothing happens in any other cases
     */
    public ResponseType getType(){
        return type;
    }

    /**
     * Response data that server responded with. most likely not null if type is ACCEPTED or NOT_ACCEPTED
     * Can be null
     */
    public Object getResponse(){
        return response;
    }

    @Override
    public String toString(){
        return "ServerResponse{" +
        "type=" + type +
        ", response=" + (response == null ? "null" : response.toString()) +
        '}';
    }
}
