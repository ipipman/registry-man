package cn.ipman.registry.cluster;

import java.util.List;

/**
 * Registry中心Server节点选举
 *
 * @Author IpMan
 * @Date 2024/4/14 20:41
 */
public class Election {

    public void elect(Server myself, List<Server> servers) {
        // 最终的候选节点, 也是leader节点
        Server candidate = null;
        System.out.println(" ======>>>> ELECT from servers = " + servers);

        if (servers.isEmpty()) {
            candidate = myself;         // myself
        } else if (servers.size() == 1) {
            candidate = servers.get(0); // myself
        } else {
            for (Server server : servers) {
                // 从候选server集中, 选举leader server
                if (server.isStatus()) {
                    // 并且没有候选节点时, 则被选中为候选节点
                    if (candidate == null) {
                        candidate = server;
                        continue;
                    }
                    if (server.hashCode() < candidate.hashCode()) { // TODO 可以改成比数据版本
                        candidate = server;
                    }
                }
            }
        }
        if (candidate == null) candidate = myself;
        System.out.println(" ======>>>> ELECT candidate = " + candidate);
        // 将所有节点标记为从节点
        servers.forEach(server -> server.setLeader(false));

        // 最终将版本最大的候选节点, 选举成leader节点 (这里通过对象引用,进行修改)
        candidate.setLeader(true);
        System.out.println(" ======>>>> servers after ELECT = " + servers);
    }
}
