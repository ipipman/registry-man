package cn.ipman.registry.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 注册中心Server类
 *
 * @Author IpMan
 * @Date 2024/4/14 20:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {
    private String url;
    private boolean leader; // 是否被选举为leader
    private boolean status; // server的状态
    private long version; // server的版本
}
