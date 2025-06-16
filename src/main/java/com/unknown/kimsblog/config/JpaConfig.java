// JpaConfig.java
package com.unknown.kimsblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.unknown.kimsblog.repository")
public class JpaConfig {
    // JPA Auditing 기능을 활성화하여 @CreatedDate 등이 자동으로 동작하도록 함
}