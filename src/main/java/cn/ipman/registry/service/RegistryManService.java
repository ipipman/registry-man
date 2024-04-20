package cn.ipman.registry.service;

import cn.ipman.registry.cluster.Snapshot;
import cn.ipman.registry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 注册中心实现类
 *
 * @Author IpMan
 * @Date 2024/4/13 19:34
 */
@Slf4j
public class RegistryManService implements RegistryService {

    // 保存服务与实例元数据的映射
    public final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    // 保存服务及其变更后的版本（全局递增）
    public final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    // 保存服务@实例与变更时间戳的映射
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    // 注册中心整体的变更版本，全局递增
    public final static AtomicLong VERSION = new AtomicLong(0);

    /**
     * 注册服务实例
     *
     * @param service 服务名称
     * @param instance 服务实例元数据
     * @return 注册后的服务实例元数据
     */
    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        // 检查该服务是否已存在该实例
        if (metas != null && !metas.isEmpty()) {
            if (metas.contains(instance)) {
                log.info(" ====> instance {} already registered", instance.toHttpUrl());
                instance.setStatus(true);
                return instance;
            }
        }
        // 为新服务或实例进行注册
        log.info(" ====> register instance {}", instance.toHttpUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);

        // 更新实例注册时间
        reNew(instance, service);
        // 更新服务版本
        VERSIONS.put(service, VERSION.incrementAndGet());

        return instance;
    }

    /**
     * 注销服务实例
     *
     * @param service 服务名称
     * @param instance 服务实例元数据
     * @return 注销后的服务实例元数据，如果不存在则返回null
     */
    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas == null || metas.isEmpty()) {
            return null;
        }
        log.info(" ====> unregister instance {}", instance.toHttpUrl());
        metas.removeIf(m -> m.equals(instance));
        instance.setStatus(false);

        // 更新实例注销时间
        reNew(instance, service);
        // 更新服务版本
        VERSIONS.put(service, VERSION.incrementAndGet());

        return instance;
    }

    /**
     * 获取指定服务的所有实例元数据
     *
     * @param service 服务名称
     * @return 该服务的所有实例元数据列表
     */
    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }

    /**
     * 更新指定服务实例的时间戳
     *
     * @param instance 服务实例元数据
     * @param services 受影响的服务名称集合
     * @return 当前系统时间戳
     */
    public synchronized long reNew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toHttpUrl(), now);
        }
        return now;
    }

    /**
     * 获取指定服务的当前版本号
     *
     * @param service 服务名称
     * @return 服务的版本号，如果不存在则返回null
     */
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    /**
     * 获取多个服务的当前版本号
     *
     * @param services 服务名称集合
     * @return 服务名称与版本号的映射关系
     */
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services)
                .collect(Collectors.toMap(x -> x, VERSIONS::get, (a, b) -> b));
    }

    /**
     * 获取当前注册中心的快照
     *
     * @return 注册中心的快照实例
     */
    public static synchronized Snapshot snapshot() {
        // 复制当前注册中心的数据到快照
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new ConcurrentHashMap<>(VERSIONS);
        Map<String, Long> timestamps = new ConcurrentHashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    /**
     * 根据快照恢复注册中心数据
     *
     * @param snapshot 注册中心的快照实例
     * @return 恢复后的版本号
     */
    public static synchronized long restore(Snapshot snapshot) {
        // 使用Leader快照数据恢复注册中心数据
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getREGISTRY());

        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVERSIONS());

        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTIMESTAMPS());

        VERSION.set(snapshot.getVersion());
        return snapshot.getVersion();
    }
}
