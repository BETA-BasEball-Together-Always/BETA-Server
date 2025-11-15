package com.beta.integration.community;

import com.beta.common.docker.TestContainer;
import com.beta.common.fixture.PostFixture;
import com.beta.common.fixture.TeamFixture;
import com.beta.common.fixture.UserFixture;
import com.beta.common.security.JwtTokenProvider;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.infra.common.repository.BaseballTeamRepository;
import com.beta.infra.community.entity.HashtagEntity;
import com.beta.infra.community.repository.HashtagJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import com.beta.presentation.community.request.PostCreateRequest;
import com.beta.presentation.community.response.PostUploadResponse;
import com.beta.infra.community.gcs.GcsStorageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("멱등성 통합 테스트")
class IdempotencyIntegrationTest extends TestContainer {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BaseballTeamRepository baseballTeamRepository;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private HashtagJpaRepository hashtagJpaRepository;

    @MockitoBean
    private GcsStorageClient gcsStorageClient;

    private BaseballTeamEntity testTeam;
    private UserEntity testUser;
    private String accessToken;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        testTeam = TeamFixture.createDoosan();
        baseballTeamRepository.save(testTeam);

        testUser = UserFixture.createActiveUser("test_social_id", "테스트유저", testTeam);
        userJpaRepository.save(testUser);

        accessToken = jwtTokenProvider.generateAccessToken(testUser.getId(), testTeam.getCode(), "USER");

        headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("동일한 요청을 5초 이내에 재시도하면 409 CONFLICT 반환")
    void should_returnConflict_when_duplicateRequestWithinTTL() {
        // given - 해시태그 준비
        HashtagEntity hashtag = PostFixture.createHashtag("야구");
        hashtagJpaRepository.save(hashtag);

        PostCreateRequest request = new PostCreateRequest(
                "테스트 게시글 내용",
                true,
                null,
                List.of("야구")
        );

        HttpEntity<PostCreateRequest> entity = new HttpEntity<>(request, headers);

        // when - 첫 번째 요청 (성공)
        ResponseEntity<PostUploadResponse> response1 = restTemplate.postForEntity(
                "/api/v1/posts", entity, PostUploadResponse.class
        );

        // then - 첫 번째 요청 성공 확인
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).isNotNull();
        assertThat(response1.getBody().isSuccess()).isTrue();

        // when - 두 번째 요청 (동일한 요청을 즉시 재시도)
        ResponseEntity<PostUploadResponse> response2 = restTemplate.postForEntity(
                "/api/v1/posts", entity, PostUploadResponse.class
        );

        // then - 멱등성 체크로 인해 CONFLICT 반환
        assertThat(response2.getStatusCode()).isIn(
                HttpStatus.CONFLICT,
                HttpStatus.TOO_MANY_REQUESTS,
                HttpStatus.BAD_REQUEST
        );

        // then - 게시글이 중복 생성되지 않았는지 확인
        assertThat(postJpaRepository.count()).isEqualTo(1);
    }
}
