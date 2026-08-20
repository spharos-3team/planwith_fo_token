package com.planwith.planwith_fo_token;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_token.config.AuthProperties;
import com.planwith.planwith_fo_token.config.DeployProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthProperties.class, DeployProperties.class})
public class PlanwithFoTokenApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoTokenApplication.class, args);
	}

}
