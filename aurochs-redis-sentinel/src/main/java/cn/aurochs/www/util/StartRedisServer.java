package cn.aurochs.www.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dufy on 2017/3/28.
 *
 * cmd /c dir 是执行完dir命令后关闭命令窗口。<br/>
 * cmd /k dir 是执行完dir命令后不关闭命令窗口.<br/>
 * cmd /c start dir 会打开一个新窗口后执行dir指令，原窗口会关闭。<br/>
 * cmd /k start dir 会打开一个新窗口后执行dir指令，原窗口不会关闭。<br/>
 * redis-cli.exe -h 127.0.0.1 -p 端口<br/>
 * info replication -- 查看主从复制<br/>
 * info sentinel-- 查看哨兵情况<br/>
 *
 * 批量启动脚本： https://www.cnblogs.com/aflyun/p/6688219.html
 * window本地搭redis的哨兵模式：http://blog.csdn.net/liuchuanhong1/article/details/53206028<br/><br/>
 *
 * 启动服务工具类
 */
public class StartRedisServer {
    private final static String redisRootPath = "D:\\redis-3.2.100";

    public static void main(String[] args) {
        List<String> cmds = new ArrayList<String>();
        String cmdRedis6379 = "cmd /k start redis-server.exe redis6379.conf ";//redis-server.exe redis.conf
        String cmdRedis6380 = "cmd /k start redis-server.exe redis6380.conf ";//redis-server.exe redis.conf
        String cmdRedis6381 = "cmd /k start redis-server.exe redis6381.conf ";//redis-server.exe redis.conf
        String cmdRedis7379 = "cmd /k start redis-server.exe redis7379.conf ";//redis-server.exe redis.conf
        String cmdRedis7380 = "cmd /k start redis-server.exe redis7380.conf ";//redis-server.exe redis.conf
        String cmdRedis7381 = "cmd /k start redis-server.exe redis7381.conf ";//redis-server.exe redis.conf

        cmds.add(cmdRedis6379);
        cmds.add(cmdRedis6380);
        cmds.add(cmdRedis6381);
        cmds.add(cmdRedis7379);
        cmds.add(cmdRedis7380);
        cmds.add(cmdRedis7381);

        String cmdRedis26379 = "cmd /k start redis-server.exe sentinel26379.conf --sentinel";//redis-server.exe sentinel26379.conf --sentinel
        String cmdRedis26380 = "cmd /k start redis-server.exe sentinel26380.conf --sentinel";//redis-server.exe sentinel26380.conf --sentinel
        String cmdRedis26381 = "cmd /k start redis-server.exe sentinel26381.conf --sentinel";//redis-server.exe sentinel26381.conf --sentinel
        String cmdRedis27379 = "cmd /k start redis-server.exe sentinel27379.conf --sentinel";//redis-server.exe sentinel27379.conf --sentinel
        String cmdRedis27380 = "cmd /k start redis-server.exe sentinel27380.conf --sentinel";//redis-server.exe sentinel27380.conf --sentinel
        String cmdRedis27381 = "cmd /k start redis-server.exe sentinel27381.conf --sentinel";//redis-server.exe sentinel27381.conf --sentinel

        cmds.add(cmdRedis26379);
        cmds.add(cmdRedis26380);
        cmds.add(cmdRedis26381);
        cmds.add(cmdRedis27379);
        cmds.add(cmdRedis27380);
        cmds.add(cmdRedis27381);

        initRedisServer(cmds);
    }

    public static void initRedisServer(List<String> cmdStr){
        if(cmdStr != null && cmdStr.size() > 0){
            for (String cmd:cmdStr
                 ) {
                try {
                    Process exec = Runtime.getRuntime().exec(cmd, null, new File(redisRootPath));
                    System.out.println(exec.toString());
                    Thread.sleep(1*1000);
                }catch (InterruptedException e) {
                    System.out.println("线程中断异常" + e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("cmd command error" + e.getMessage());
                    e.printStackTrace();
                }

            }
        }

    }
}
