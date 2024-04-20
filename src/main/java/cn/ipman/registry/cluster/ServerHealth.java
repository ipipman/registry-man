package cn.ipman.registry.cluster;

import cn.ipman.registry.health.HttpInvoker;
import cn.ipman.registry.service.RegistryManService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * 检查注册中心所有Server的健康状态
 *
 * @Author IpMan
 * @Date 2024/4/14 21:13
 */
@Slf4j
public class ServerHealth {

    // 注册中心集群对象
    Cluster cluster;

    /**
     * @param cluster 注册中心集群对象
     */
    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * 定期检查注册中心服务器健康状态，并进行Leader选举和同步操作
     */
    public void checkServerHealth() {
        // 定期执行健康检查和领导选举任务
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 创建一个给定初始延迟的间隔性的任务，之后的每次任务执行时间为 初始延迟 + N * delay(间隔)
        executor.scheduleAtFixedRate(
                () -> {
                    try {
                        // 更新服务器信息
                        updateServer();

                        // 进行leader节点选举
                        doElect();

                        log.debug(" ===*****%%%$$$>>> isLeader=" + cluster.isLeader()
                                + ",myself-version=" + cluster.getMYSELF().getVersion()
                                + ",leader-version=" + cluster.getLeader().getVersion());

                        // 如果当前服务器不是Leader且版本较低，则从Leader同步最新注册信息
                        if (!cluster.isLeader()
                                && cluster.getMYSELF().getVersion() < cluster.getLeader().getVersion()) {
                            // 改成首次刷 TODO 优先级低
                            // 改成 判断版本号 DONE
                            // 改成 判断LEADER是否改变 DONE
                            // 把这个类拆分为多个类 DONE
                            // 控制读写分离 TODO 客户端
                            // 优化实时性同步 TODO 优先级低

                            log.debug(" ===*****%%%$$$>>> syncFromLeader: " + cluster.getLeader());
                            // 从Leader同步快照
                            long v = syncSnapshotFromLeader();
                            log.debug(" ===*****%%%$$$>>> sync success new version: " + v);
                        }

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                , 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 更新所有服务器的信息
     */
    private void updateServer() {
        long start = System.currentTimeMillis();
        cluster.getServers().stream().parallel()
                .filter(s -> !s.equals(cluster.MYSELF)) // 过滤掉当前server
                .forEach(this::checkServerInfo); // 检查其它server的状态
        log.debug(" =====>>>>>> updateServer info: " + (System.currentTimeMillis() - start) + " ms");
    }


    /**
     * 进行Leader选举
     */
    private void doElect() {
        // leader 选举类
        Election election = new Election();

        // 获取所有有效的Leader节点
        List<Server> servers = cluster.getServers();
        List<Server> masters = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());

        // 没有Leader或存在多个Leader时进行重新选举
        if (masters.isEmpty()) {
            log.warn(" =========>>>>> ELECT: no masters: {}", servers);
            election.elect(cluster.myself(), servers);
        } else if (masters.size() > 1) {
            log.warn(" =========>>>>> ELECT: more than one master: {}", masters);
            election.elect(cluster.myself(), servers);
        } else {
            log.warn(" =========>>>>> ELECT: on need elect master: {}", masters);
        }
    }

    /**
     * 检查指定服务器的信息
     *
     * @param server 待检查的服务器
     */
    public void checkServerInfo(Server server) {
        try {
            // 通过HTTP GET请求获取其它服务器信息
            Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
            log.info(" =========>>>>> health check success for {}.", server);
            if (!server.isStatus()) {
                server.setStatus(true);
            }
            // 更新服务器的状态和领导者信息
            server.setVersion(serverInfo.getVersion());
            server.setLeader(serverInfo.isLeader());
        } catch (RuntimeException ex) {
            log.warn(" =========>>>>> health check failed for {}", server);//, ex);
            // 如果服务器状态异常，则标记为不可用
            if (server.isStatus()) {
                server.setStatus(false);
                server.setLeader(false);
            }
        }
    }

    /**
     * 从Leader同步快照数据
     *
     * @return 同步后的新版本号，若失败则返回-1
     */
    private long syncSnapshotFromLeader() {
        try {
            log.info(" =========>>>>> syncSnapshotFromLeader {}", cluster.getLeader().getUrl() + "/snapshot");
            Snapshot snapshot = HttpInvoker.httpGet(cluster.getLeader().getUrl() + "/snapshot", Snapshot.class);
            return RegistryManService.restore(snapshot);
        } catch (Exception ex) {
            log.error(" =========>>>>> syncSnapshotFromLeader failed.", ex);
        }
        return -1;
    }

}
