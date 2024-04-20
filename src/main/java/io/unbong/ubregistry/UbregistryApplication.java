package io.unbong.ubregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(UBRegistryConfigProperties.class)
public class UbregistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(UbregistryApplication.class, args);
    }

}
