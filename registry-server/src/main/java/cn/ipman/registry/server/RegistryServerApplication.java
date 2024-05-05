package cn.ipman.registry.server;

import cn.ipman.registry.core.config.IMRegistryConfig;
import cn.ipman.registry.core.config.RegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties({RegistryConfigProperties.class})
@Import({IMRegistryConfig.class}) // 开启注册中心
public class RegistryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistryServerApplication.class, args);
    }

}
