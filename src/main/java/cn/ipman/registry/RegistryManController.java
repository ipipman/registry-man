package cn.ipman.registry;

import cn.ipman.registry.model.InstanceMeta;
import cn.ipman.registry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Registry Controller.
 *
 * @Author IpMan
 * @Date 2024/4/13 19:49
 */
@RestController
@Slf4j
public class RegistryManController {

    @Autowired
    private RegistryService registryService;

    @RequestMapping("/reg")
    public InstanceMeta registry(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info("register {} @ {}", service, instanceMeta);
        return registryService.register(service, instanceMeta);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unRegistry(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info("unregister {} @ {}", service, instanceMeta);
        return registryService.unregister(service, instanceMeta);
    }

    @RequestMapping("/findall")
    public List<InstanceMeta> findAll(@RequestParam String service) {
        log.info(" ====> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }


    @RequestMapping("/renews")
    public Long renew(@RequestParam String services, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ====> renews {}", services);
        return registryService.reNew(instanceMeta, services.split(","));
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        log.info(" ====> version {}", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String services) {
        log.info(" ====> versions {}", services);
        return registryService.versions(services.split(","));
    }


}
