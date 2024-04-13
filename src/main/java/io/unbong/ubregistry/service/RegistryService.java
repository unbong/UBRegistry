package io.unbong.ubregistry.service;

import io.unbong.ubregistry.model.InstanceMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface for registry service
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-13 20:26
 */
public interface RegistryService {

    InstanceMeta register(String service, InstanceMeta instance);

    InstanceMeta unregister(String service, InstanceMeta instance);

    List<InstanceMeta> getAllInstance(String service);

    // todo
    public long renew(InstanceMeta instance, String... services);

    public long version(String service);

    public Map<String, Long> versions(String ... services);
}
