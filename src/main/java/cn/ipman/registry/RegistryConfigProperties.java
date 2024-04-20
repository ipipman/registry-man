package cn.ipman.registry;

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
    List<String> serverlist;
}
