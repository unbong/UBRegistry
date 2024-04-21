package io.unbong.ubregistry.service;

import io.unbong.ubregistry.cluster.SnapShot;
import io.unbong.ubregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Default implementation of registryservice
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-13 20:33
 */
@Slf4j
public class UBRegistryService implements RegistryService{

    // service 集合
    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    final  static Map<String , Long> VERSIONS = new ConcurrentHashMap<>();  //
    public final  static Map<String , Long> TIMESTAMPS = new ConcurrentHashMap<>();  //

    public final static AtomicLong VERSION = new AtomicLong(0);
    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas =REGISTRY.get(service);
        if( !(metas==null) && !metas.isEmpty()){
            // if service
            if(metas.contains(instance)){
                log.debug("---->instance {} already registered", instance.toURL());
                instance.setStatus(true);
                return instance;
            }
        }

        log.debug("---->register instance {}", instance.toURL());
        REGISTRY.add(service, instance);
        /**
         * 如果每次注册，版本增加
         * 每次注册实例，记录时间戳
         */

        renew(instance,service);
        instance.setStatus(true);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public synchronized InstanceMeta  unregister(String service, InstanceMeta instance) {

        List<InstanceMeta> metas = REGISTRY.get(service);
        if(metas==null || metas.isEmpty()) {
            return null;
        }

        if(metas.contains(instance))
        {
            log.debug(" ----> unregister instance {}", instance.toURL());
//            boolean isRemoved= REGISTRY.remove(service, instance);
            metas.removeIf(m-> m.equals(instance));
            instance.setStatus(false);

            VERSIONS.put(service, VERSION.incrementAndGet());
            renew(instance,service);
            return instance;
        }

        return null;
    }

    @Override
    public List<InstanceMeta> getAllInstance(String service) {
        return REGISTRY.get(service);
    }

    public long renew( InstanceMeta instance, String... services){
        long value = System.currentTimeMillis();
        // 一个实例可能在多个服务，可以刷新多个服务，证明自己是活着的
        for (String service : services) {
            TIMESTAMPS.put(service+"@"+instance.toURL(), value);
        }

        return value;
    }


    public long version(String service){
        return VERSIONS.get(service);
    }

    public Map<String, Long> versions(String ... services){
        //toMap
       return  Arrays.stream(services).collect(Collectors.toMap(x->x, x-> VERSIONS.get(x), (a,b)->b));
    }

    public static synchronized  SnapShot snapShot(){
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String , Long> versions  = new HashMap<>(VERSIONS);
        Map<String , Long> timestamps = new HashMap<>(TIMESTAMPS) ;

        return new SnapShot(registry, versions, timestamps, VERSION.get());
    }

    public static synchronized long restore(SnapShot snapShot){
        REGISTRY.clear();
        REGISTRY.addAll(snapShot.getREGISTRY());
        VERSIONS.clear();
        VERSIONS.putAll(snapShot.getVERSIONS());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapShot.getTIMESTAMPS());
        VERSION.set(snapShot.getVERSION());
        return snapShot().getVERSION();
    }

}
