package cn.ipman.registry.cluster;

import cn.ipman.registry.model.InstanceMeta;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * 注册中心, 快照类，用于保存系统或服务的当前状态。
 *
 * @Author IpMan
 * @Date 2024/4/14 19:30
 */
@Data
public class Snapshot {

    final LinkedMultiValueMap<String, InstanceMeta> REGISTRY; // 服务注册表，保存实例的元数据
    final Map<String, Long> VERSIONS;   // 版本映射，记录每个实例的版本号
    final Map<String, Long> TIMESTAMPS; // 服务时间戳映射，记录每个服务@实例的最后更新时间戳
    final long version;                 // 注册中心全局最新的版本号


    /**
     * 构造函数，初始化快照对象。
     *
     * @param registry 注册表，保存实例元数据的映射。
     * @param versions 版本映射，保存每个实例的当前版本号。
     * @param timestamps 时间戳映射，保存每个实例的最后更新时间戳。
     * @param version 快照的全局版本号。
     */
    public Snapshot(LinkedMultiValueMap<String, InstanceMeta> registry,
                    Map<String, Long> versions,
                    Map<String, Long> timestamps,
                    long version) {
        this.REGISTRY = registry;
        this.VERSIONS = versions;
        this.TIMESTAMPS = timestamps;
        this.version = version;
    }

}
