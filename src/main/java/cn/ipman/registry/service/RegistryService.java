package cn.ipman.registry.service;

import java.util.List;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/13 19:27
 */
public class RegistryService {

    InstanceMata register(String servieName, InstanceMata instanceMata);

    InstanceMata unregister(String servieName, InstanceMata instanceMata);

    List<InstanceMata>  getAllInstances(String serviceName);


    // todo


}
