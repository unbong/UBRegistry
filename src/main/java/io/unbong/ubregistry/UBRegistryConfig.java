package io.unbong.ubregistry;

import io.unbong.ubregistry.cluster.Cluster;
import io.unbong.ubregistry.helth.HealthChecker;
import io.unbong.ubregistry.helth.UBHealthChecker;
import io.unbong.ubregistry.service.RegistryService;
import io.unbong.ubregistry.service.UBRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UB Registry Config
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-13 20:50
 */
@Configuration
public class UBRegistryConfig {

    @Bean
    public RegistryService registryService(){
        return new UBRegistryService();
    }

    //
    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService){
        return  new UBHealthChecker(registryService);
    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired UBRegistryConfigProperties properties)
    {
        return new Cluster(properties);
    }
}
