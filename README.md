# spring-cloud
### 本项目从 有来开源组织 youlai-cloud项目master分支过来
源项目地址 https://gitee.com/youlaiorg

#### 介绍
面向 SpringBoot3，基于 Java17、Spring Cloud&Alibaba 2021、Spring Boot 2.7 、Spring Authorization Server 0.3.1 全新升级OAuth2授权+微服务+UPMS管理系统解决方案。


#### Spring Authorization Server 授权码模式测试流程
1. 创建名为 oauth2 的数据库，执行docs/sql/oauth2.sql的脚本创建表
2. 启动nacos，创建 cloud-namespace-id 的namespace，导入docs/nacos/DEFAULT_GROUP.zip配置
3. 依次启动 youlai-auth、youlai-system、youlai-gateway
4. 浏览器输入网关： http://127.0.0.1:9999/youlai-system/messages 
5. 因为第一次未授权会跳转到认证中心进行认证，输入用户名/密码(admin/123456)，认证成功网关会路由到资源服务器获取数据。

