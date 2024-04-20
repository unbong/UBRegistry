package io.unbong.ubregistry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Description
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-20 14:13
 */
@Data
@ConfigurationProperties("ubregistry")
public class UBRegistryConfigProperties {

    private List<String> serverList;
}
