package com.zzjz.zzdj_dhcp.task;

import com.google.gson.Gson;
import com.zzjz.zzdj_dhcp.bean.Client;
import com.zzjz.zzdj_dhcp.util.Constant;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 房桂堂
 * @description MyTask
 * @date 2019/1/16 8:34
 */
@Component
public class MyTask {
    private final static Logger LOGGER = LoggerFactory.getLogger(MyTask.class);

    @Scheduled(cron = "0 */10 * * * *")
    private void queryAllScopeDhcp() throws IOException, InterruptedException, ExecutionException  {
        long t1 = System.currentTimeMillis();
        String cmdScope = "netsh dhcp server show scope";
        System.out.println("###准备执行 " + cmdScope + " 命令###");
        List<String> scopes = new ArrayList<>();
        List<Client> clients = new ArrayList<>();
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(cmdScope);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("GBK")));
        long t2 = System.currentTimeMillis();
        System.out.println("第一个命令执行用时:" + (t2 - t1));
        String line;
        while((line=br.readLine())!=null){
            System.out.println(line);
            //填入所有scope  ( 192.168.1.0    - 255.255.255.0  -活动          -test                 -  )
            if (line.split("\\.").length >= 4) {
                scopes.add(line.substring(0, line.trim().indexOf(" ") + 1).trim());
            }
        }
        br.close();
        System.out.println(scopes);

        //模拟真实环境 有多个scope 大概36个 假设每个平均用时3秒
        for (int i = 0; i < 36; i++) {
            scopes.add("192.168.5.0");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CompletionService<String> pool = new ExecutorCompletionService<String>(executorService);
        List<Future<String>> resultList = new ArrayList<>();

        for (String scope : scopes) {
            resultList.add(pool.submit(() -> {
                long ta = System.currentTimeMillis();
                if ("192.168.5.0".equals(scope)) {
                    Thread.sleep(3000);
                    return "假任务";
                }
                String cmdClients = "netsh dhcp server scope " + scope + " show clients";
                System.out.println("###准备执行 " + cmdClients + " 命令###");
                Process p1 = rt.exec(cmdClients);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(p1.getInputStream(), Charset.forName("GBK")));
                String clientLine;

                //headers存储clients头 防止不同主机结果顺序不一致 当前顺序是（IP 地址  - 子网掩码  - 唯一的 ID   -  租用过期    - 种类）
                List<String> headers = new ArrayList<>();
                while ((clientLine = br2.readLine()) != null) {

                    System.out.println(clientLine);
                    //填入所有client (192.168.1.242   - 255.255.255.0  - 30-9c-23-b5-0a-8b   -2019/1/22 8:47:31      -D)
                    if (clientLine.split("\\.").length >= 7) {
                        List<String> valueList = new ArrayList<>();
                        for (String s : clientLine.split("  ")) {
                            if (s.length() > 0) {
                                if (s.split("-").length < 3) {
                                    valueList.add(s.replace("-", "").trim());
                                } else {
                                    //mac地址
                                    valueList.add(s.substring(s.indexOf("-") + 1).trim());
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
        Gson gson = new Gson();
        System.out.println("clients总数为: " + clients.size());
        System.out.println("clients数据: " + gson.toJson(clients));
        insertToEs(clients);
        System.out.println("over");
    }

    /**
     * 将client数据插入es
     * @param clients clients
     */
    private void insertToEs(List<Client> clients) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(Constant.ES_HOST, Constant.ES_PORT, Constant.ES_METHOD)));
        BulkRequest request = new BulkRequest();
        String dayStr = new DateTime().toString("yyyy.MM.dd");
        for (Client cl : clients) {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("ip", cl.getIp());
            jsonMap.put("mac", cl.getMac());
            jsonMap.put("subnetmask", cl.getSubnetMask());
            jsonMap.put("expire", cl.getExpire());
            jsonMap.put("type", cl.getType());
            jsonMap.put("scope", cl.getScope());
            jsonMap.put("@timestamp", new Date());
            request.add(new IndexRequest("dhcp_" + dayStr, "doc").source(jsonMap));
        }
        BulkResponse bulkResponse = client.bulk(request);
        LOGGER.info("Eventlog插入执行结果:" +  (bulkResponse.hasFailures() ? "有错误" : "成功"));
        LOGGER.info("Eventlog插入执行用时:" + bulkResponse.getTook().getMillis() + "毫秒");
        client.close();

    }
}
