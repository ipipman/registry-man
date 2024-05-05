package cn.ipman.registry.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


import java.util.List;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/14 20:35
 */
@Data
@ConfigurationProperties(prefix = "registry")
public class RegistryConfigProperties {
    /**
     * 服务器列表，存储了注册中心服务器的地址信息。
     * 这个属性会绑定配置文件中registry.serverList的值，允许配置多个服务器地址。
     */
    List<String> serverlist;
}
