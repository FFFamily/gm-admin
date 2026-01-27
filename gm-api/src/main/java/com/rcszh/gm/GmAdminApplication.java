package com.rcszh.gm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.rcszh.gm.user.mapper", "com.rcszh.gm.ow.mapper", "com.rcszh.gm.file.mapper"})
public class GmAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmAdminApplication.class, args);
    }
}
