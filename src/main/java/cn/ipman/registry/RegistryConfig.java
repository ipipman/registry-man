package cn.ipman.registry;

import cn.ipman.registry.health.HealthChecker;
import cn.ipman.registry.health.HealthManChecker;
import cn.ipman.registry.service.RegistryManService;
import cn.ipman.registry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registry Configuration for all beans.
 *
 * @Author IpMan
 * @Date 2024/4/13 19:50
 */
@Configuration
public class RegistryConfig {

    @Bean
    public RegistryService registryService() {
        return new RegistryManService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
        return new HealthManChecker(registryService);
    }





}
