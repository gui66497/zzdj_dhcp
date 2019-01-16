package com.zzjz.zzdj_dhcp.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author 房桂堂
 * @description MyTask
 * @date 2019/1/16 8:34
 */
public class MyTask {
    public static void main(String[] args) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec("ping baidu.com");
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while((line=br.readLine())!=null){
            System.out.println(new String(line.getBytes(), "GBK"));
        }
        br.close();
        System.out.println("over");
    }
}
