package cn.ipman.registry.core.api;

import cn.ipman.registry.core.cluster.Cluster;
import cn.ipman.registry.core.cluster.Server;
import cn.ipman.registry.core.cluster.Snapshot;
import cn.ipman.registry.core.model.InstanceMeta;
import cn.ipman.registry.core.service.RegistryManService;
import cn.ipman.registry.core.service.RegistryService;
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
public class IMRegistryController {

    @Autowired
    private RegistryService registryService; // 注册中心服务

    @Autowired
    private Cluster cluster;   // 注册中心集群管理

    /**
     * 服务注册。
     *
     * @param service 待注册服务名称。
     * @param instanceMeta 服务实例元数据。
     * @return 注册后的服务实例元数据。
     */
    @RequestMapping("/reg")
    public InstanceMeta registry(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info("register {} @ {}", service, instanceMeta);
        checkLeader();
        return registryService.register(service, instanceMeta);
    }

    /**
     * 服务注销。
     *
     * @param service 待注销服务名称。
     * @param instanceMeta 待注销服务实例元数据。
     * @return 注销后的服务实例元数据。
     */
    @RequestMapping("/unreg")
    public InstanceMeta unRegistry(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info("unregister {} @ {}", service, instanceMeta);
        checkLeader();
        return registryService.unregister(service, instanceMeta);
    }

    /**
     * 查询所有服务实例。
     *
     * @param service 待查询服务名称。
     * @return 该服务的所有实例列表。
     */
    @RequestMapping("/findall")
    public List<InstanceMeta> findAll(@RequestParam String service) {
        log.info(" ====> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }

    /**
     * 实例更新。
     *
     * @param service 服务名称。
     * @param instance 实例元数据。
     * @return 续期结果，通常为续期时间。
     */
    @RequestMapping("/renew")
    public long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {}", service, instance);
        checkLeader();
        return registryService.reNew(instance, service);
    }

    /**
     * 批量实例更新。
     *
     * @param services 服务名称集合。
     * @param instanceMeta 实例元数据。
     * @return 续期结果，通常为续期时间。
     */
    @RequestMapping("/renews")
    public long renews(@RequestParam String services, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ====> renews {}", services);
        checkLeader();
        return registryService.reNew(instanceMeta, services.split(","));
    }

    /**
     * 查询服务版本。
     *
     * @param service 服务名称。
     * @return 服务版本号。
     */
    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        log.info(" ====> version {}", service);
        return registryService.version(service);
    }

    /**
     * 查询多个服务的版本。
     *
     * @param services 服务名称集合。
     * @return 各服务的版本号映射。
     */
    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String services) {
        log.info(" ====> versions {}", services);
        return registryService.versions(services.split(","));
    }

    /**
     * 获取集群快照。
     *
     * @return 集群快照信息。
     */
    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        log.info(" ===> snapshot");
        return RegistryManService.snapshot();
    }

    /**
     * 获取集群状态。
     *
     * @return 集群中服务器列表。
     */
    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> cluster");
        return cluster.getServers();
    }
    /**
     * 获取当前服务器信息。用于多个注册中心server之间的信息同步
     *
     * @return 当前服务器信息。
     */
    @RequestMapping("/info")
    public Server info() {
        return myself();
    }

    /**
     * 获取当前服务器在集群中的信息。
     *
     * @return 当前服务器在集群中的信息。
     */
    @RequestMapping("/myself")
    public Server myself() {
        return cluster.myself();
    }

    /**
     * 设置当前服务器为主/从节点。
     *
     * @return 更新后的当前服务器信息。
     */
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
     * 检查是否为集群领导者。仅领导者可以进行注册和注销操作。
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
