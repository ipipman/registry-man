package cn.ipman.registry.core.conf;


import cn.ipman.registry.core.cluster.Cluster;
import cn.ipman.registry.core.health.HealthChecker;
import cn.ipman.registry.core.health.HealthManChecker;
import cn.ipman.registry.core.service.RegistryManService;
import cn.ipman.registry.core.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册中心配置类，用于配置所有bean。
 *
 * @Author IpMan
 * @Date 2024/4/13 19:50
 */
@Configuration
public class IMRegistryConfig {

    /**
     * 配置注册服务bean。
     *
     * @return 返回注册服务实例。
     */
    @Bean
    public RegistryService registryService() {
        return new RegistryManService();
    }

    /**
     * 配置健康检查服务bean，使用RegistryService作为依赖。
     *
     * @param registryService 注册服务实例，通过自动装配获取。
     * @return 返回健康检查服务实例。
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
        return new HealthManChecker(registryService);
    }

    /**
     * 配置集群管理bean，使用RegistryConfigProperties作为配置。
     *
     * @param registryConfigProperties 注册中心配置属性，通过自动装配获取。
     * @return 返回集群管理实例。
     */
    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired RegistryConfigProperties registryConfigProperties){
        return new Cluster(registryConfigProperties);
    }


}
