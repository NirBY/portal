package com.example.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class PortalApplication {

	private static final Logger logger = LoggerFactory.getLogger(PortalApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Site Directory Application");
		SpringApplication.run(PortalApplication.class, args);
		logger.info("Site Directory Application started successfully");
	}

}
