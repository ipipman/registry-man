package cn.ipman.registry.health;


/**
 * 探活
 *
 * @Author IpMan
 * @Date 2024/4/13 19:50
 */
public interface HealthChecker {

    void start();

    void stop();
}
