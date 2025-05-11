package com.example.mdtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MdToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(MdToolApplication.class, args);
	}

}
