package io.unbong.ubregistry.cluster;

import io.unbong.ubregistry.UBRegistryConfigProperties;
import io.unbong.ubregistry.service.UBRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry cluster
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-20 14:09
 */
@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;

    String host;

    Server myself;
    UBRegistryConfigProperties properties;


    private List<Server> servers = new ArrayList<>();
    public Cluster(UBRegistryConfigProperties properties) {
        this.properties = properties;
    }


    /**
     *
     */
    public void init(){

        host =  new InetUtils(new InetUtilsProperties())
                .findFirstNonLoopbackHostInfo().getIpAddress();

        log.debug("---> host:{}" , host);

        //
        myself = new Server("http://" + host + ":" + port,
                true, false, -1);

        log.debug("---> myself: {}", myself);

        List<Server> serverList = new ArrayList<>();
        for (String url : properties.getServerList()) {
            Server server = new Server();
            if(url.contains("localhost"))
            {
               url= url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
               url = url.replace("127.0.0.1", host);
            }
            if(url.equals(myself.getUrl()))
            {
                serverList.add(myself);
            }
            else {
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1);
                serverList.add(server);
            }

        }
        // todo ...
        servers = serverList;

        ServerHealth serverHealth = new ServerHealth(this);
        serverHealth.checkServerHealth();

    }



    public List<Server> getServers(){
        return servers;
    }

    public Server self(){
        // 找到自己并返回

        myself.setVersion(UBRegistryService.VERSION.get());
        return myself;
    }


    public Server leader(){
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

}
