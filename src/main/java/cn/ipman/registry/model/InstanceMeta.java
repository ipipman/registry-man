package cn.ipman.registry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务提供者实例模型类，用于描述服务实例的基本元数据。
 *
 * @Author IpMan
 * @Date 2024/3/23 14:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
@SuppressWarnings("unused")
public class InstanceMeta {

    private String scheme;  // 协议类型，如http、https
    private String host;    // 服务实例的主机地址
    private Integer port;   // 服务实例的端口号
    private String context; // 服务实例的上下文路径

    private boolean status;  // 服务状态，true代表在线，false代表离线
    private Map<String, String> parameters = new HashMap<>();   // 服务实例的额外参数信息，如机房、灰度标记等

    /**
     * 构造一个服务实例元数据对象。
     *
     * @param scheme 协议类型。
     * @param host 服务主机地址。
     * @param port 服务端口号。
     * @param context 服务上下文路径。
     */
    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    /**
     * 根据服务的基础信息生成资源路径。
     *
     * @return 返回生成的资源路径，格式为"host_port"。
     */
    public String toRcPath() {
        return String.format("%s_%d", host, port);
    }

    /**
     * 创建一个HTTP协议的服务实例元数据对象。
     *
     * @param host 服务主机地址。
     * @param port 服务端口号。
     * @return 返回一个预配置的InstanceMeta实例，协议为HTTP，上下文路径为"rpcman"。
     */
    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "rpcman");
    }

    /**
     * 根据URL创建一个服务实例元数据对象。
     *
     * @param url 服务的URL地址。
     * @return 返回解析后的InstanceMeta实例。
     */
    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        // 从URL中解析出协议、主机、端口和上下文路径
        return new InstanceMeta(
                uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath().substring(1) // - /rpcman to rpcman
        );
    }

    /**
     * 构建服务的HTTP完整URL地址。
     *
     * @return 返回构建好的HTTP URL字符串。
     */
    public String toHttpUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    /**
     * 向当前服务实例添加参数信息。
     *
     * @param params 要添加的参数信息映射。
     * @return 返回当前InstanceMeta实例，以支持链式调用。
     */
    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }

    /**
     * 将当前实例的参数信息序列化为JSON字符串。
     *
     * @return 返回参数信息的JSON字符串。
     */
    public String toMetas() {
        // 实体的元数据,机房、灰度、单元
        return JSON.toJSONString(this.getParameters());
    }
}
