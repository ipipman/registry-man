package cn.ipman.registry.core.cluster;

import cn.ipman.registry.core.conf.RegistryConfigProperties;
import cn.ipman.registry.core.service.RegistryManService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/14 21:00
 */
@Slf4j
public class Cluster {

    static String ip;

    /*
     * 静态代码块，用于初始化当前主机的非回环（非本地）IP地址。
     * 使用Spring Boot提供的InetUtils工具类来获取IP地址。
     * 如果无法获取到非回环IP地址，则默认使用"127.0.0.1"作为回退。
     */
    static {
        try (InetUtils inetUtils = new InetUtils(new InetUtilsProperties())) {
            // 尝试获取当前主机的非回环IP地址
            ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
            log.debug(" ===>>> findFirstNonLoopBackHostInfo().getIpAddress() = " + ip);
        } catch (Exception e) {
            ip = "127.0.0.1";
        }
    }

    // 从配置中自动注入服务器端口
    @Value("${server.port}")
    String port;

    // 代表当前服务器实例的信息
    @Getter
    Server MYSELF;
    // Server LEADER;

    // 存储注册中心所有服务器的信息
    @Getter
    List<Server> servers;

    // 注册中心配置属性
    RegistryConfigProperties registryConfigProperties;

    // 用于检查服务器的健康状态
    ServerHealth serverHealth;

    /**
     * 构造函数：创建Cluster实例。
     *
     * @param registryConfigProperties 注册中心配置属性，用于配置集群与注册中心的交互。
     */
    public Cluster(RegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    /**
     * 初始化函数，用于设置和初始化当前服务器以及所有服务器的信息。
     * 此函数不接受参数，也不返回任何值。
     * 过程中会将当前服务器 myself 以及从 registryConfigProperties 配置中获取的其他服务器信息记录下来，并检查服务器的健康状态。
     */
    public void init() {
        // 初始化当前服务器信息
        myself();

        // 初始化所有服务器的信息
        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerlist()) {
            // 判断当前服务器是否为自身节点
            if (MYSELF.getUrl().equalsIgnoreCase(url)
                    || MYSELF.getUrl().equals(convertLocalhost(url))) {
                // 当前服务器为自身节点时的处理
                log.info("add myself to servers: {}", MYSELF);
                servers.add(MYSELF);
            } else {
                // 其他服务器的处理
                log.info("add server to servers: " + url);
                Server server = new Server();
                server.setUrl(convertLocalhost(url));
                server.setStatus(false);       // 默认状态设置为false
                server.setLeader(false);       // 默认不是leader
                server.setVersion(-1L);        // 默认版本号设置为-1
                servers.add(server);
            }
        }
        this.servers = servers;
        // 输出初始化后的服务器列表和当前服务器信息
        log.info(" =======>>>>>> initialized, servers:{}", servers);
        log.info(" =======>>>>>> initialized, myself:{}", myself());

        // 初始化服务器健康检查
        serverHealth = new ServerHealth(this);
        serverHealth.checkServerHealth();
    }


    /**
     * 获取当前服务器实例的信息。
     * 该方法首先检查MYSELF静态变量是否已经被初始化，如果没有，则创建一个新的Server实例并记录日志。
     * 然后，更新MYSELF实例的版本号为注册中心服务的最新版本号。
     *
     * @return 返回当前服务器的实例信息。
     */
    public Server myself() { // 192.168.31.232
        if (MYSELF == null) {
            // 如果MYSELF尚未初始化，则创建一个新的Server实例
            @SuppressWarnings("all")
            Server myself = new Server("http://" + ip + ":" + port, false, true, -1);
            log.info(" ========>>>>>>  myself:{}", myself);
            MYSELF = myself; // 将新创建的Server实例赋值给静态变量MYSELF
        }
        // 更新MYSELF实例的版本号为最新版本
        MYSELF.setVersion(RegistryManService.VERSION.get());
        return MYSELF;
    }

    /**
     * 将URL中的 "localhost" 或 "127.0.0.1" 替换成指定的IP地址。
     *
     * @param url 需要转换的原始URL。
     * @return 如果URL中包含 "localhost" 或 "127.0.0.1"，则将其替换为ip变量中指定的IP地址后返回，
     *         如果不包含，则直接返回原始URL。
     */
    private String convertLocalhost(String url) {
        // 检查URL是否包含"localhost"，如果是，则替换为ip变量中的IP地址
        if (url.contains("localhost")) {
            return url.replace("localhost", ip);
        }
        // 检查URL是否包含"127.0.0.1"，如果是，则替换为ip变量中的IP地址
        if (url.contains("127.0.0.1")) {
            return url.replace("127.0.0.1", ip);
        }
        // 直接返回原始URL
        return url;
    }

    /**
     * 获取当前服务器列表中的leader节点。
     * 此方法通过遍历服务器列表，筛选出状态为活动（isStatus）且角色为leader（isLeader）的服务器。
     * 如果找到，则返回该服务器；如果没有找到，则返回null。
     *
     * @return Server 如果找到leader节点，则返回该节点；否则返回null。
     */
    public Server getLeader() {
        // 获取leader节点
        return this.servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

    /**
     * 判断当前对象是否为领导者。
     *
     * @return boolean 如果当前对象是领导者，则返回true；否则返回false。
     */
    public boolean isLeader() {
        return myself().isLeader();
    }
}
