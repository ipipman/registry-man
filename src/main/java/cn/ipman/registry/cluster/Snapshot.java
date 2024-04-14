package cn.ipman.registry.cluster;

import cn.ipman.registry.model.InstanceMeta;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * 注册中心, 镜像
 *
 * @Author IpMan
 * @Date 2024/4/14 19:30
 */
@Data
public class Snapshot {

    final LinkedMultiValueMap<String, InstanceMeta> REGISTRY;
    final Map<String, Long> VERSIONS;
    final Map<String, Long> TIMESTAMPS;
    final long version;

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
