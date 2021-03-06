package com.paymybuddy.paymybuddyweb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
	}
)
public class PayMyBuddyWebApplication {
	private static final Logger logger = LogManager.getLogger("PayMyBuddyWebApplication");

	public static void main(String[] args) {
		logger.info("Application start");
		SpringApplication.run(PayMyBuddyWebApplication.class, args);
		logger.info("Application running");
	}
}