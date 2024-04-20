package cn.ipman.registry.health;


/**
 * 健康检查接口。用于实现对服务或系统的健康状况进行检查的功能。
 *
 * @Author IpMan
 * @Date 2024/4/13 19:50
 */
public interface HealthChecker {

    /**
     * 启动健康检查。该方法应初始化健康检查所需资源，并开始执行周期性的健康检查任务。
     */
    void start();

    /**
     * 停止健康检查。该方法应停止执行中的健康检查任务，并清理相关资源。
     */
    void stop();
}
