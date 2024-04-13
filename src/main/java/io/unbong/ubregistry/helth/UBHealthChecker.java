package io.unbong.ubregistry.helth;

import io.unbong.ubregistry.model.InstanceMeta;
import io.unbong.ubregistry.service.RegistryService;
import io.unbong.ubregistry.service.UBRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-13 21:42
 */
@Slf4j
public class UBHealthChecker implements HealthChecker{

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    final long timeout = 20 * 1000;

    public UBHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    RegistryService registryService;

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(()->{
            log.debug("----> health checker running...");
            /**
             * 找时间戳大于30秒
             */

            long now = System.currentTimeMillis();

            UBRegistryService.TIMESTAMPS.keySet().stream().forEach(serviceAndInstance->{
                long timeStamp = UBRegistryService.TIMESTAMPS.get(serviceAndInstance);
                if(now - timeStamp> timeout){
                    log.debug(" ----> health checker {} is down", serviceAndInstance);
                    // unregist service
                    int index = serviceAndInstance.indexOf("@");
                    String service = serviceAndInstance.substring(0, index);
                    String url = serviceAndInstance.substring(index+1);
                    InstanceMeta instance = InstanceMeta.from(url);
                    this.registryService.unregister(service,instance);

                    //
                    UBRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                }
            });
        }, 10, 30, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {

    }
}
