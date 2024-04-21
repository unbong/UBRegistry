package io.unbong.ubregistry.cluster;

import io.unbong.ubregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * for sync
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-20 20:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnapShot {

    // service 集合
    LinkedMultiValueMap<String, InstanceMeta> REGISTRY ;
    Map<String , Long> VERSIONS  ;
    Map<String , Long> TIMESTAMPS ;

    long VERSION ;
}
