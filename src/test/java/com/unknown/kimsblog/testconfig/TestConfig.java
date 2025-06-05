package com.unknown.kimsblog.testconfig;

import com.unknown.kimsblog.service.PostService;
import com.unknown.kimsblog.service.TemporaryPostService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
    @Bean
    public PostService postService() {
        return Mockito.mock(PostService.class);
    }

    @Bean
    public TemporaryPostService temporaryPostService() {
        return Mockito.mock(TemporaryPostService.class);
    }
}