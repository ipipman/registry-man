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

    // Map<k -> 服务, v -> 实例>
    public final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    // Map<k -> 服务, v -> 变更后的版本-全局递增>
    public final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    // Map<k -> 服务@实例, v -> 变更的时间戳>
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    // 注册中心变更后的版本, 全局递增
    public final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        // 如果这个服务和实例都存在
        if (metas != null && !metas.isEmpty()) {
            if (metas.contains(instance)) {
                log.info(" ====> instance {} already registered", instance.toHttpUrl());
                instance.setStatus(true);
                return instance;
            }
        }
        // 如果这个服务不存在,需要注册服务和节点
        log.info(" ====> register instance {}", instance.toHttpUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);

        // 记录实例注册时间
        reNew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());

        return instance;
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas == null || metas.isEmpty()) {
            return null;
        }
        log.info(" ====> unregister instance {}", instance.toHttpUrl());
        metas.removeIf(m -> m.equals(instance));
        instance.setStatus(false);

        reNew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());

        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }

    public synchronized long reNew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toHttpUrl(), now);
        }
        return now;
    }

    public Long version(String service) {
        return VERSIONS.get(service);
    }

    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services)
                .collect(Collectors.toMap(x -> x, VERSIONS::get, (a, b) -> b));
    }

    public static synchronized Snapshot snapshot() {
        // copy this registry data... to snapshot
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new ConcurrentHashMap<>(VERSIONS);
        Map<String, Long> timestamps = new ConcurrentHashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    public static synchronized long restore(Snapshot snapshot) {
        // restore registry data... by snapshot
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
