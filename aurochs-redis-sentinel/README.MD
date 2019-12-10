## aurochs-redis-sentinel：redis哨兵分组分布式服务端
1、从server获取集群配置，按一致性算法分组，每一组都是一个sentinel集群，一主二从配置；<br />
2、与redis连接，操作数据。<br />

## 设计图
![redis-sentinel](https://github.com/sekift/aurochs/blob/master/aurochs-redis-sentinel/doc/redis-sentinel.png)

## redis配置
1）master1组的配置<br />
服务类型 |是否是主服务器|IP地址|端口
---------|------ | -------|--------
Redis	|	是	|	127.0.0.1	|6379
Redis	|	否	|	127.0.0.1	|6380
Redis	|	否	|	127.0.0.1	|6381
Sentinel|	-	|	127.0.0.1	|26379
Sentinel|   -	|	127.0.0.1	|26380
Sentinel|	-	|	127.0.0.1	|26381

1）master2组的配置<br />
服务类型|是否是主服务器|IP地址|端口
---------|------ | -------|--------
Redis	|	是	|	127.0.0.1	|7379
Redis	|	否	|	127.0.0.1	|7380
Redis	|	否	|	127.0.0.1	|7381
Sentinel|	-	|	127.0.0.1	|27379
Sentinel|	-	|	127.0.0.1	|27380
Sentinel|	-	|	127.0.0.1	|27381

以上配置好之后可以使用StartRedisServer类统一启动（在windows下，linux上的启动可以自行编写sh实现）。<br />

## 验证
执行单元测试，启动redis-cli分别连接master1和master2来get key进行验证。<br />