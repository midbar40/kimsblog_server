package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.model.Post;
import com.unknown.kimsblog.service.PostService;
import com.unknown.kimsblog.service.TemporaryPostService;
import com.unknown.kimsblog.testconfig.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.Import;
import java.util.Collections;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class) // ✅ TestConfig를 통해 Mock 객체 사용
class PostControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PostService postService;

    @Autowired
    private TemporaryPostService temporaryPostService;

    @BeforeEach
    public void mockMvcSetup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    public void cleanUp() {
        // 테스트 후 데이터 정리 필요 없음 (Mock 객체 사용)
    }

    @DisplayName("getAllPosts: 모든 글을 조회한다")
    @Test
    public void getAllPosts() throws Exception {
        // given
        final String url = "/api/posts";
        Post savedPost = new Post("타이틀테스트", "컨텍스트테스트");
        savedPost.setId(1L); // ✅ ID 설정 (Mock 환경)

        when(postService.getAllPosts()).thenReturn(Collections.singletonList(savedPost)); // ✅ 목 객체 반환값 설정

        // when
        final ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON))
                .andDo(print()); // ✅ 응답 확인

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedPost.getId()))
                .andExpect(jsonPath("$[0].title").value(savedPost.getTitle()));
    }
}