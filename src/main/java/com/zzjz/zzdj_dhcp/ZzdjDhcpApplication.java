package com.zzjz.zzdj_dhcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZzdjDhcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzdjDhcpApplication.class, args);
    }

}

