package io.unbong.ubregistry.cluster;

import io.unbong.ubregistry.UBRegistryConfigProperties;
import io.unbong.ubregistry.http.HttpInvoker;
import io.unbong.ubregistry.model.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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



    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    final long timeout = 5 * 1000;


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

        executor.scheduleAtFixedRate(()->{
            try {
                updateServers();
                electLeader();
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }

        },0, timeout, TimeUnit.MILLISECONDS);
    }

    private void electLeader() {

        // does need leader?
        List<Server> masters =  this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());

        // elect leader
        if(masters.isEmpty())
        {
            log.debug("no leader. {} ",servers);
            elect();
        }
        else if(masters.size() > 1){
            log.debug("---> there is more than one leader. {} ", servers);

            elect();
        }else{
            log.debug("---> no need elect leader" + masters.get(0));
        }


        
    }

    private void elect() {
        // 1.各种节点自己选，算法保证大家选的是同一个
        // 2.外部有一个分布式锁， 谁抢到，谁是leader
        // 3.分布式一致性算法  paxos raft

        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if(server.isStatus()){
                if(candidate == null){
                    candidate = server;
                }
                else {
                    if(candidate.hashCode() > server.hashCode())
                    {
                        candidate = server;
                    }
                }
            }
        }

        if(candidate != null){
            candidate.setLeader(true);
            log.debug("----> elected leader: {}", candidate);
        }
        else{
            log.debug("---> election failed. {}", servers);
        }
    }

    private void updateServers() {

        servers.forEach(server -> {
            // health check server
            try{
                Server serverInfo = HttpInvoker.httpGet(server.getUrl(), Server.class);
                log.debug("-----> heath check succesed for {}", serverInfo);
                if(serverInfo != null){
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            }
            catch (Exception e){
                log.debug("----> health check failed for {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public List<Server> getServers(){
        return servers;
    }

    public Server self(){
        // 找到自己并返回


        return myself;
    }


    public Server leader(){
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

}
