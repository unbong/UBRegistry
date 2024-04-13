package io.unbong.ubregistry;

import io.unbong.ubregistry.model.InstanceMeta;
import io.unbong.ubregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Registryc Controller
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-13 20:49
 */
@RestController
@Slf4j
public class UBRegistryController {


    @Autowired
    RegistryService registryService;

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance){
        log.debug(" ---->register {} @ {}", service, instance);
        return registryService.register(service, instance);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance){
        log.debug(" ---->unregister {} @ {}", service, instance);
        return registryService.unregister(service, instance);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta>  findAllInstance(@RequestParam String service)
    {
        log.debug(" ---->findAllInstance {}", service);
        return registryService.getAllInstance(service);
    }


    @RequestMapping("/renews")
    public long  renews(@RequestParam String service, @RequestBody InstanceMeta instance)
    {
        log.debug(" ---->renew {}, instnace", service, instance);
        return registryService.renew(instance,service.split(","));
    }

    @RequestMapping("/versions")
    public Map<String, Long> version(@RequestParam String service, @RequestBody InstanceMeta instance)
    {
        log.debug(" ---->version {}, instnace", service, instance);
        return registryService.versions(service.split(","));
    }


}
