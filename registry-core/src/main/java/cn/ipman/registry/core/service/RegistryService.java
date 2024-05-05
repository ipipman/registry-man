package cn.ipman.registry.core.service;

import cn.ipman.registry.core.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * 注册中心服务接口。
 * 提供服务实例的注册、注销、查询等操作，以及实例状态的刷新和版本控制。
 *
 * @Author IpMan
 * @Date 2024/4/13 19:27
 */
public interface RegistryService {

    /**
     * 注册一个服务实例。
     *
     * @param service 服务名称。
     * @param instance 待注册的服务实例元数据。
     * @return 注册成功返回服务实例的元数据，包含注册信息和分配的ID。
     */
    InstanceMeta register(String service, InstanceMeta instance);

    /**
     * 注销一个服务实例。
     *
     * @param service 服务名称。
     * @param instance 待注销的服务实例元数据。
     * @return 注销成功返回服务实例的元数据。
     */
    InstanceMeta unregister(String service, InstanceMeta instance);

    /**
     * 获取指定服务的所有实例元数据列表。
     *
     * @param service 服务名称。
     * @return 返回指定服务的所有实例列表。
     */
    List<InstanceMeta> getAllInstances(String service);

    /**
     * 刷新一个服务实例的状态。
     *
     * @param instance 需要刷新状态的服务实例元数据。
     * @param service 服务名称。
     * @return 返回刷新操作的发生时间戳（毫秒）。
     */
    long reNew(InstanceMeta instance, String... service);

    /**
     * 获取指定服务实例的版本号。
     *
     * @param service 服务名称。
     * @return 返回指定服务的版本号，若不存在则返回null。
     */
    Long version(String service);

    /**
     * 获取多个服务实例的版本号。
     *
     * @param services 服务名称数组。
     * @return 返回一个映射，包含指定服务的名称和版本号。
     */
    Map<String, Long> versions(String... services);

    // 快照功能相关方法，用于获取当前服务实例的快照并恢复至特定状态。
    // Snapshot snapshot();
    // void restore(Snapshot snapshot);
}
