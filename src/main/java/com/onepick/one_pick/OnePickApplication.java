package com.onepick.one_pick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OnePickApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnePickApplication.class, args);
	}

}
