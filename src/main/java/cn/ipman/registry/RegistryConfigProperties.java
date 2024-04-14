package cn.ipman.registry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/14 20:35
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "registry")
public class RegistryConfigProperties {
    List<String> serverlist;
}
