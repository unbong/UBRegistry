package io.unbong.ubregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-21 10:05
 */
@Slf4j
public class Election {

    Cluster cluster;

    public Election(Cluster cluster) {
        this.cluster = cluster;
    }


    public void elect() {
        // 1.各种节点自己选，算法保证大家选的是同一个
        // 2.外部有一个分布式锁， 谁抢到，谁是leader
        // 3.分布式一致性算法  paxos raft

        Server candidate = null;
        for (Server server : this.cluster.getServers()) {
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
            log.debug("---> election failed. {}", this.cluster.getServers());
        }
    }
}
