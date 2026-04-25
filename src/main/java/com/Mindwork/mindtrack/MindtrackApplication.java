package com.Mindwork.mindtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MindtrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(MindtrackApplication.class, args);
	}

}
