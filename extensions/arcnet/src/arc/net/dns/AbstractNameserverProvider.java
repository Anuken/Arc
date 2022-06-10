/*
 * This file is a modified class of dnsjava, an implementation of the dns protocol in java.
 * Licensed under the BSD-3-Clause.
 */
package arc.net.dns;

import arc.struct.*;
import arc.util.*;

import java.net.*;

/**
 * Base class for resolver config providers that provides a default implementation for the lists and
 * utility methods to prevent duplicates.
 */
public abstract class AbstractNameserverProvider implements NameServerProvider{

    private final Seq<InetSocketAddress> nameservers = new Seq<>(3);

    @Override
    public Seq<InetSocketAddress> getNameservers(){
        return nameservers.copy();
    }

    protected final void reset(){
        nameservers.clear();
    }

    protected void addNameServer(InetSocketAddress server){
        if(!nameservers.contains(server)){
            nameservers.add(server);
            Log.debug("[DNS] Added @ to nameservers", server);
        }
    }
}
