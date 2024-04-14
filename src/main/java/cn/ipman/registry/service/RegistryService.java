package cn.ipman.registry.service;

import cn.ipman.registry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * Interface for registry service.
 *
 * @Author IpMan
 * @Date 2024/4/13 19:27
 */
public interface RegistryService {

    InstanceMeta register(String service, InstanceMeta instance);

    InstanceMeta unregister(String service, InstanceMeta instance);

    List<InstanceMeta> getAllInstances(String service);

    // 刷新一个实例的状态
    long reNew(InstanceMeta instance, String... service);

    // 获取当前实例的版本
    Long version(String service);

    // 获取多个实例的版本
    Map<String, Long> versions(String... services);

    // Snapshot snapshot();
    // void restore(Snapshot snapshot);
}
