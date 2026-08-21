package com.planwith.planwith_fo_token;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.planwith.planwith_fo_token.config.AuthProperties;
import com.planwith.planwith_fo_token.config.DeployProperties;
import com.planwith.planwith_fo_token.config.PortOneProperties;
import com.planwith.planwith_fo_token.config.TokenChargeReconcileProperties;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;
import com.planwith.planwith_fo_token.config.TokenMockMemberProperties;
import com.planwith.planwith_fo_token.config.TokenOutboxProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		PortOneProperties.class,
		TokenKafkaProperties.class,
		TokenOutboxProperties.class,
		TokenChargeReconcileProperties.class,
		TokenMockMemberProperties.class
})
public class PlanwithFoTokenApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoTokenApplication.class, args);
	}
}
