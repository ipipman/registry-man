package cn.ipman.registry.service;

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

    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
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
    public InstanceMeta unregister(String service, InstanceMeta instance) {
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

    public Long reNew(InstanceMeta instance, String... services) {
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
}
