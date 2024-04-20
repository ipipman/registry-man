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
    private String url;     // 服务器的URL地址
    private boolean leader; // 标记该服务器是否被选举为leader
    private boolean status; // 服务器的状态，通常用于表示服务器是否在线或离线
    private long version;   // 服务器的版本
}
