package com.br.org.icol.icolbackend;

import org.springframework.boot.SpringApplication;

public class TestIcolsystemApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.default", "dev");
        SpringApplication.from(IcolsystemApplication::main)
                .with(PostgresTestConfiguration.class)
                .run(args);
    }
}
