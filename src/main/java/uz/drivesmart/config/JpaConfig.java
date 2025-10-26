package uz.drivesmart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "uz.drivesmart.repository")
public class JpaConfig {
}