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
        // 候选节点
        Server candidate = null;
        System.out.println(" ======>>>> ELECT from servers = " + servers);
        if (servers.isEmpty()) {
            candidate = myself;
        } else if (servers.size() == 1) {
            candidate = servers.get(0); // myself
        } else {
            for (Server server : servers) {
                // 如果候选server集中,状态是有效的
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
        servers.forEach(server -> server.setLeader(false));
        // 把当前候选节点,,选举成leader节点
        candidate.setLeader(true);
        System.out.println(" ======>>>> servers after ELECT = " + servers);
    }
}
