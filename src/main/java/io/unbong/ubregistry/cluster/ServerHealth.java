package io.unbong.ubregistry.cluster;

import io.unbong.ubregistry.http.HttpInvoker;
import io.unbong.ubregistry.service.UBRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-20 21:41
 */
@Slf4j
public class ServerHealth {

    Cluster cluster;
    Server myself;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
        myself = cluster.self();
    }

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    final long interval = 5 * 1000;
    public void checkServerHealth(){
        executor.scheduleAtFixedRate(()->{
            try {
                updateServers();    // 更新服务器状态
                doElection();      // 选主

                if( ! cluster.self().isLeader()
                        && cluster.self().getVersion() < cluster.leader().getVersion()){
                    syncSnapShotFromLeader();
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }

        },0, interval, TimeUnit.MILLISECONDS);
    }

    private void doElection() {

        Election electoin = new Election(this.cluster);
        // does need leader?
        List<Server> masters =  this.cluster.getServers()
                .stream().filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());

        // elect leader
        if(masters.isEmpty())
        {
            log.debug("no leader. {} ",this.cluster.getServers());
            electoin.elect();
        }
        else if(masters.size() > 1){
            log.debug("---> there is more than one leader. {} ", this.cluster.getServers());

            electoin.elect();
        }else{
            log.debug("---> no need elect leader" + masters.get(0));
        }
    }

    private void updateServers() {
        // 并行的访问节点
        cluster.getServers().stream().parallel().forEach(server -> {
            // skip self
            if(server.equals(myself))
                return;
            // health check server
            try{
                Server serverInfo = HttpInvoker.httpGet(server.getUrl()+"/info", Server.class);
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

    /**
     *
     */
    private void syncSnapShotFromLeader() {

        Server leader =  cluster.leader();

        log.debug("leader version:{}, self version: {}", leader.getVersion(), cluster.self().getVersion());
        log.debug("---> sync leader {}", leader);
        SnapShot snapShot = HttpInvoker.httpGet(leader.getUrl()+ "/snapshot", SnapShot.class);
        log.debug("---> leader snapShot: {}", snapShot);
        UBRegistryService.restore(snapShot);

    }


}
