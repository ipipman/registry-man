package cn.ipman.registry;

import cn.ipman.registry.cluster.Cluster;
import cn.ipman.registry.cluster.Server;
import cn.ipman.registry.cluster.Snapshot;
import cn.ipman.registry.model.InstanceMeta;
import cn.ipman.registry.service.RegistryManService;
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

    @Autowired
    private Cluster cluster;

    @RequestMapping("/reg")
    public InstanceMeta registry(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info("register {} @ {}", service, instanceMeta);
        checkLeader();
        return registryService.register(service, instanceMeta);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unRegistry(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info("unregister {} @ {}", service, instanceMeta);
        checkLeader();
        return registryService.unregister(service, instanceMeta);
    }

    @RequestMapping("/findall")
    public List<InstanceMeta> findAll(@RequestParam String service) {
        log.info(" ====> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }


    @RequestMapping("/renew")
    public long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {}", service, instance);
        checkLeader();
        return registryService.reNew(instance, service);
    }

    @RequestMapping("/renews")
    public long renews(@RequestParam String services, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ====> renews {}", services);
        checkLeader();
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

    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        log.info(" ===> snapshot");
        return RegistryManService.snapshot();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> cluster");
        return cluster.getServers();
    }

    @RequestMapping("/info")
    public Server info() {
        return myself();
    }

    @RequestMapping("/myself")
    public Server myself() {
        return cluster.myself();
    }

    @RequestMapping("/sm")
    public Server setMaster() {
        cluster.myself().setLeader(!cluster.isLeader());
        return cluster.myself();
    }

    @RequestMapping("/")
    public List<Server> root() {
        return cluster();
    }

    /**
     * 只有Leader节点才能注册和反注册
     */
    void checkLeader() {
        if (!cluster.isLeader()) {
            log.error("this server {} is readonly slave, leader {} is writable.",
                    myself().getUrl(), cluster.getLeader().getUrl());
            throw new RuntimeException("this server[" + myself().getUrl()
                    + "] is a slave, can't be written.");
        }
    }
}
