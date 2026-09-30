package com.EmployeeService.EmployeeService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients(basePackages = {"com.elms.employeeservice"})
@EnableCaching
@EnableJpaRepositories(basePackages = {"com.elms.employeeservice.repository", "com.elms.common.idempotency", "com.elms.common.events", "com.elms.common.outbox"})
@EntityScan(basePackages = {"com.elms.employeeservice.entity", "com.elms.common.idempotency", "com.elms.common.events", "com.elms.common.outbox"})
@SpringBootApplication(scanBasePackages = {"com.elms.employeeservice", "com.elms.common"})
public class EmployeeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeServiceApplication.class, args);
	}

}
