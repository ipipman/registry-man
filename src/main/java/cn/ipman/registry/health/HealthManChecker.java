package cn.ipman.registry.health;

import cn.ipman.registry.model.InstanceMeta;
import cn.ipman.registry.service.RegistryManService;
import cn.ipman.registry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实例健康状态检查器。该类负责定期检查注册的服务实例是否活跃，如果某个实例在指定超时时间内没有更新其状态，
 * 则将其从注册表中移除。
 *
 * @Author IpMan
 * @Date 2024/4/13 20:42
 */
@Slf4j
public class HealthManChecker implements HealthChecker {

    // 注册服务接口，用于注册和注销服务实例
    RegistryService registryService;

    /**
     * 健康检查器构造函数。
     *
     * @param registryService 注册服务实例，不可为null。
     */
    public HealthManChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    // 定时任务执行器，用于执行定期检查任务
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    // 超时时间，默认为20秒，如果服务实例在该时间内没有更新状态，则认为其不活跃
    long timeout = 20_000;

    /**
     * 启动健康检查器。该方法会创建一个定时任务，该任务会定期检查注册的服务实例是否活跃。
     */
    @Override
    public void start() {
        // 使用固定延迟执行策略启动定时任务，任务间隔为30秒，初始延迟为10秒
        executor.scheduleWithFixedDelay(
                () -> {
                    log.info("Health checker running...");
                    long now = System.currentTimeMillis();
                    // 遍历所有已注册的服务实例，检查其是否超时
                    RegistryManService.TIMESTAMPS.keySet().forEach(serviceAndInstance -> {
                        // 检查服务实例的最后更新时间是否超过超时时间，如果是，则将其从注册表中移除
                        long timestamp = RegistryManService.TIMESTAMPS.get(serviceAndInstance);
                        if (now - timestamp > timeout) {
                            log.info(" === > Health checker: {} is down", serviceAndInstance);
                            // 解析服务名称和实例URL
                            int index = serviceAndInstance.indexOf("@");
                            String service = serviceAndInstance.substring(0, index);
                            String url = serviceAndInstance.substring(index + 1);

                            // 注销不活跃的服务实例
                            InstanceMeta instanceMeta = InstanceMeta.from(url);
                            registryService.unregister(service, instanceMeta);
                            RegistryManService.TIMESTAMPS.remove(service);
                        }
                    });
                },
                10, 30, TimeUnit.SECONDS);
    }


    /**
     * 停止健康检查器。该方法会取消所有计划的检查任务。
     */
    @Override
    public void stop() {
        executor.shutdown();
    }

}
