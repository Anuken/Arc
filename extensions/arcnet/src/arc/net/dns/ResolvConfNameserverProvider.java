/*
 * This file is a modified class of dnsjava, an implementation of the dns protocol in java.
 * Licensed under the BSD-3-Clause.
 */
package arc.net.dns;

import arc.files.*;
import arc.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static arc.net.dns.ArcDns.dnsResolverPort;

public final class ResolvConfNameserverProvider extends AbstractNameserverProvider{

    @Override
    public void initialize(){
        reset();
        // first try the default unix config path
        if(!tryParseResolveConf("/etc/resolv.conf")){
            // then fallback to netware
            tryParseResolveConf("sys:/etc/resolv.cfg");
        }
    }

    private boolean tryParseResolveConf(String path){
        Fi conf = new Fi(path);

        if(conf.exists()){
            try(BufferedReader reader = conf.reader(1024)){
                String line;
                while((line = reader.readLine()) != null){
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    if(!tokenizer.hasMoreTokens()) continue;

                    if(tokenizer.nextToken().equals("nameserver")){
                        addNameServer(new InetSocketAddress(tokenizer.nextToken(), dnsResolverPort));
                    }
                }
                return true;
            }catch(IOException ignored){
            }
        }

        return false;
    }

    @Override
    public boolean isEnabled(){
        return !OS.isWindows && !OS.isAndroid;
    }
}
