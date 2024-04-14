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

    Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    public void checkServerHealth() {
        // 定期检查
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 创建一个给定初始延迟的间隔性的任务，之后的每次任务执行时间为 初始延迟 + N * delay(间隔)
        executor.scheduleAtFixedRate(
                () -> {
                    try {
                        // 探活并更新, 其它server节点的信息
                        updateServer();
                        // 检查或重新选举,leader节点
                        doElect();

                        System.out.println(" ===*****%%%$$$>>> isLeader=" + cluster.isLeader()
                                + ",myself-version=" + cluster.getMYSELF().getVersion()
                                + ",leader-version=" + cluster.getLeader().getVersion());

                        // 如果当前server不是leader节点, 并且当前版本比leader节点版本低, 需要同步最新的registry
                        if (!cluster.isLeader() && cluster.getMYSELF().getVersion() < cluster.getLeader().getVersion()) {
                            // 改成首次刷 TODO 优先级低
                            // 改成 判断版本号 DONE
                            // 改成 判断LEADER是否改变 DONE
                            // 把这个类拆分为多个类 DONE
                            // 控制读写分离 TODO 客户端
                            // 优化实时性同步 TODO 优先级低
                            System.out.println(" ===*****%%%$$$>>> syncFromLeader: " + cluster.getLeader());
                            long v = syncSnapshotFromLeader();
                            System.out.println(" ===*****%%%$$$>>> sync success new version: " + v);
                        }

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                , 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void updateServer() {
        long start = System.currentTimeMillis();
        cluster.getServers().stream()
                .filter(s -> !s.equals(cluster.MYSELF)) // 过滤掉当前server
                .forEach(this::checkServerInfo); // 检查其它server的状态
        System.out.println(" =====>>>>>> updateServer info: " + (System.currentTimeMillis() - start) + " ms");
    }

    public void checkServerInfo(Server server) {
        try {
            // self server 访问其它 server, 看其它Server否有效
            Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
            log.info(" =========>>>>> health check success for {}.", server);
            if (!server.isStatus()) {
                server.setStatus(true);
            }
            // 获取到其它 server 的信息
            server.setVersion(serverInfo.getVersion());
            server.setLeader(serverInfo.isLeader());
        } catch (RuntimeException ex) {
            log.error(" =========>>>>> health check failed for {}", server);//, ex);
            // server状态标记为异常
            if (server.isStatus()) {
                server.setStatus(false);
                server.setLeader(false);
            }
        }
    }

    private void doElect() {
        // leader 选举类
        Election election = new Election();

        // 获取所有有效的 leader 节点
        List<Server> servers = cluster.getServers();
        List<Server> masters = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());

        // 没有leader任何节点时, 重新选举
        if (masters.isEmpty()) {
            log.error(" =========>>>>> ELECT: no masters: {}", servers);
            election.elect(cluster.myself(), servers);
        } else if (masters.size() > 1) {
            // 如果有多个leader节存在, 需要重新选举一个版本最高的节点出来
            log.error(" =========>>>>> ELECT: more than one master: {}", masters);
            election.elect(cluster.myself(), servers);
        }
    }


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
