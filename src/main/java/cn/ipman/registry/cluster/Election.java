package cn.ipman.registry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Registry中心Server节点选举
 *
 * @Author IpMan
 * @Date 2024/4/14 20:41
 */
@Slf4j
public class Election {

    /**
     * 选举算法，用于从服务器列表中选出一个服务器作为leader。
     * 如果列表为空或只有一个服务器，那么该服务器即被选为leader。
     * 如果列表中有多个服务器，则选择状态为激活（isStatus返回true）且哈希值最小的服务器作为leader。
     * 如果没有满足条件的服务器，则将当前服务器 myself 选为leader。
     *
     * @param myself 当前服务器，用于在没有其他候选服务器时成为leader。
     * @param servers 服务器列表，用于从中选举leader。
     */
    public void elect(Server myself, List<Server> servers) {
        // 初始化候选节点为null
        Server candidate = null;
        log.debug(" ======>>>> ELECT from servers = " + servers);

        // 根据服务器列表的条件选择leader
        if (servers.isEmpty()) {
            candidate = myself;         // 若列表为空，当前服务器成为leader
        } else if (servers.size() == 1) {
            candidate = servers.get(0); // 若列表只有一个服务器，该服务器成为leader
        } else {
            // 遍历服务器列表以选举leader
            for (Server server : servers) {
                // 选举条件：服务器状态为激活且为第一个满足条件的服务器，或哈希值较小的服务器
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
        // 若最终没有选出候选节点，则将当前服务器作为候选节点
        if (candidate == null) candidate = myself;
        log.debug(" ======>>>> ELECT candidate = " + candidate);

        // 将所有服务器标记为非leader状态
        servers.forEach(server -> server.setLeader(false));

        // 将选出的候选节点标记为leader
        candidate.setLeader(true);
        log.debug(" ======>>>> servers after ELECT = " + servers);
    }
}
