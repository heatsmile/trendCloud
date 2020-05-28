package com.zq;

import brave.sampler.Sampler;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@EnableEurekaClient
@EnableCaching
public class IndexDataApplication {

    public static void main(String[] args) {
        int port = 0;
        int defaultPort = 8021;
        int eurekaServerPort = 8761;
        int redisPort = 6379;

        if (NetUtil.isUsableLocalPort(eurekaServerPort)) {
            System.err.printf("", eurekaServerPort);
            System.exit(1);
        }

        if (NetUtil.isUsableLocalPort(redisPort)) {
            System.err.printf("", redisPort);
            System.exit(1);
        }

        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.startsWith("port")) {
                    String strPort = StrUtil.subAfter(arg, "port", true);
                    if (NumberUtil.isNumber(strPort)) {
                        port = Convert.toInt(strPort);
                    }
                }
            }
        }

        if (0 == port) {
            Future<Integer> future = ThreadUtil.execAsync(() -> {
                int p = 0;

                System.out.printf("请于5秒钟内输入端口号，推荐%d，超过5秒将默认使用%d%n", defaultPort, defaultPort);
                Scanner sc = new Scanner(System.in);
                while (true) {
                    String strPort = sc.nextLine();
                    if (!NumberUtil.isInteger(strPort)) {
                        System.out.println("只能是数字");
                        continue;
                    } else {
                        p = Convert.toInt(strPort);
                        sc.close();
                        break;
                    }
                }
                return p;
            });
            try {
                port = future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                port = defaultPort;
            }
        }

        if (!NetUtil.isUsableLocalPort(port)) {
            System.out.printf("端口%d被占用了，无法启动%n", port);
            System.exit(1);
        }
        new SpringApplicationBuilder(IndexDataApplication.class).properties("server.port=" + port).run(args);
    }

    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
}
