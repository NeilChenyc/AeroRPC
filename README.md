# 🚧 持续开发中

# AeroRPC - High Performance RPC Framework

![Java CI](https://img.shields.io/badge/Java-17%2B-blue)
![Netty](https://img.shields.io/badge/Netty-4.1.86.Final-green)

## 📦 当前版本状态

✅ **已实现核心功能**  
✔️ 基于Netty的NIO通信框架  
✔️ 多种序列化支持（JDK/JSON/Protobuf）  
✔️ 服务注册发现基础实现  
✔️ 请求响应模型与心跳机制  
✔️ 基础服务监控统计

🔧 **待完善功能**  
▢ Zookeeper注册中心集成  
▢ 负载均衡策略实现  
▢ 熔断降级机制  
▢ SPI扩展支持  
▢ 性能优化与压力测试

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+

### 服务端示例
```java
@RpcService(interfaceClass = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
```

### 客户端调用
```java
RpcReference reference = new RpcReference();
HelloService service = reference.create(HelloService.class, "1.0");
String result = service.sayHello("World");
```

## 📌 功能清单

| 模块            | 完成状态 | 说明                     |
|-----------------|----------|--------------------------|
| 网络通信        | ✅        | 基于Netty4实现           |
| 协议编解码      | ✅        | 自定义二进制协议         |
| 动态代理        | ✅        | JDK/CGLib双模式支持      |
| 服务注册发现    | ⚠️       | 基础实现（需完善）       |
| 负载均衡        | ▢         | 计划支持多种策略         |
| 监控统计        | ⚠️       | 基础调用统计（需增强）   |

## 🛠️ 构建指南
```bash
mvn clean package -DskipTests
```

## 🤝 参与贡献
1. Fork本仓库
2. 创建特性分支（git checkout -b feature/xxx）
3. 提交修改（git commit -am '添加新特性'）
4. 推送分支（git push origin feature/xxx）
5. 创建Pull Request

## 📄 开源协议
[MIT License](LICENSE)