package com.zzjz.zzdj_dhcp.task;

import com.zzjz.zzdj_dhcp.bean.Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author 房桂堂
 * @description MyTask
 * @date 2019/1/16 8:34
 */
public class MyTask {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        long t1 = System.currentTimeMillis();
        String cmd_scope = "netsh dhcp server show scope";
        System.out.println("###准备执行 " + cmd_scope + " 命令###");
        List<String> scopes = new ArrayList<>();
        List<Client> clients = new ArrayList<>();
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(cmd_scope);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("GBK")));
        long t2 = System.currentTimeMillis();
        System.out.println("第一个命令执行用时:" + (t2 - t1));
        String line;
        while((line=br.readLine())!=null){
            System.out.println(line);
            //填入所有scope  ( 192.168.1.0    - 255.255.255.0  -活动          -test                 -  )
            if (line.split("\\.").length >= 4) {
                scopes.add(line.substring(0, line.trim().indexOf(" ") + 1));
            }
        }
        br.close();
        System.out.println(scopes);

        //模拟真实环境 有多个scope 大概36个 假设每个平均用时3秒
        for (int i = 0; i < 36; i++) {
            scopes.add("192.168.5.0");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(6);
        CompletionService<String> pool = new ExecutorCompletionService<String>(executorService);
        List<Future<String>> resultList = new ArrayList<>();


        for (String scope : scopes) {

            resultList.add(pool.submit(() -> {
                long ta = System.currentTimeMillis();

                if ("192.168.5.0".equals(scope)) {
                    Thread.sleep(3000);
                    return "假任务";
                }
                String cmd_clients = "netsh dhcp server scope " + scope + " show clients";
                System.out.println("###准备执行 " + cmd_clients + " 命令###");
                Process p1 = rt.exec(cmd_clients);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(p1.getInputStream(), Charset.forName("GBK")));
                String clientLine;

                //headers存储clients头 防止不同主机结果顺序不一致 当前顺序是（IP 地址  - 子网掩码  - 唯一的 ID   -  租用过期    - 种类）
                List<String> headers = new ArrayList<>();
                while ((clientLine = br2.readLine()) != null) {

                    System.out.println(clientLine);
                    //填入所有client (192.168.1.242   - 255.255.255.0  - 30-9c-23-b5-0a-8b   -2019/1/22 8:47:31      -D)
                    if (clientLine.split("\\.").length >= 7) {
                        //scopes.add(clientLine.substring(0, clientLine.trim().indexOf(" ") + 1));
                        List<String> valueList = new ArrayList<>();
                        for (String s : clientLine.split("  ")) {
                            if (s.length() > 0) {
                                if (s.split("-").length < 3) {
                                    valueList.add(s.replace("-", "").trim());
                                } else {
                                    //mac地址
                                    valueList.add(s.substring(s.indexOf("-") + 1, s.length()).trim());
                                }
                            }
                        }

                        Client client = new Client();
                        client.setScope(scope);
                        for (int i = 0; i < headers.size(); i++) {
                            if ("IP 地址".equals(headers.get(i))) {
                                client.setIp(valueList.get(i));
                            } else if ("子网掩码".equals(headers.get(i))) {
                                client.setSubnetMask(valueList.get(i));
                            } else if ("唯一的 ID".equals(headers.get(i))) {
                                client.setMac(valueList.get(i));
                            } else if ("租用过期".equals(headers.get(i))) {
                                client.setExpire(valueList.get(i));
                            } else if ("种类".equals(headers.get(i))) {
                                client.setType(valueList.get(i));
                            }
                        }
                        clients.add(client);

                    } else if (clientLine.contains("IP 地址")) {
                        for (String s : clientLine.split("-")) {

                            headers.add(s.trim());
                        }
                    }
                }
                br2.close();

                long tb = System.currentTimeMillis();
                return "task " + scope + " completed.耗时：" + (tb - ta);
            }));

        }

        //关闭线程池
        executorService.shutdown();
        for(int i = 0; i < resultList.size(); i++){
            String result = pool.take().get();
            System.out.println(result);
        }
        long t4 = System.currentTimeMillis();
        System.out.println("总用时:" + (t4 - t1));
        clients.forEach(System.out::println);
        System.out.println("over1");


    }
}
