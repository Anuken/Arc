package arc.net.dns;

public final class SRVRecord implements Comparable<SRVRecord>{
    public final int ttl;
    public final short priority;
    public final short weight;
    public final int port;
    public final String target;

    public SRVRecord(int ttl, short priority, short weight, int port, String target){
        this.ttl = ttl;
        this.priority = priority;
        this.weight = weight;
        this.port = port;
        this.target = target;
    }

    @Override
    public int compareTo(SRVRecord o){
        if(this.priority != o.priority){
            return Short.compare(this.priority, o.priority);
        }else{
            return Short.compare(this.weight, o.weight);
        }
    }
}
