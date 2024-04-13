package cn.ipman.registry.service;

import cn.ipman.registry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/13 19:27
 */
public interface RegistryService {

    InstanceMeta register(String service, InstanceMeta instanceMata);

    InstanceMeta unregister(String service, InstanceMeta instanceMata);

    List<InstanceMeta> getAllInstances(String service);

    // 刷新一个实例的状态
    Long reNew(InstanceMeta instanceMeta, String... service);

    // 获取当前实例的版本
    Long version(String service);

    // 获取多个实例的版本
    public Map<String, Long> versions(String... services);


}
