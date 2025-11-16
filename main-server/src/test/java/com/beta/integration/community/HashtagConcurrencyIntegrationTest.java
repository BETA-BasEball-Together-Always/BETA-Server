package com.beta.integration.community;

import com.beta.application.community.service.PostWriteService;
import com.beta.common.docker.TestContainer;
import com.beta.common.fixture.TeamFixture;
import com.beta.common.fixture.UserFixture;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.infra.common.repository.BaseballTeamRepository;
import com.beta.infra.community.entity.HashtagEntity;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.repository.HashtagJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("해시태그 UPSERT 동시성 테스트")
class HashtagConcurrencyIntegrationTest extends TestContainer {

    @Autowired private PostWriteService postWriteService;
    @Autowired private HashtagJpaRepository hashtagJpaRepository;
    @Autowired private PostJpaRepository postJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BaseballTeamRepository baseballTeamRepository;
    @MockitoBean private GcsStorageClient gcsStorageClient;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        BaseballTeamEntity team = baseballTeamRepository.save(TeamFixture.createDoosan());
        testUser = userJpaRepository.save(UserFixture.createActiveUser("test_id", "테스터", team));
    }

    @AfterEach
    void tearDown() {
        postJpaRepository.deleteAll();
        hashtagJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        baseballTeamRepository.deleteAll();
    }

    @Test
    @DisplayName("100 스레드 - 해시태그 UPSERT 데드락 없이 성공")
    void concurrentHashtagUpsert_shouldNotDeadlock() throws InterruptedException {

        int threadCount = 100;
        List<String> hashtags = List.of("야구", "응원", "두산", "KIA", "LG");

        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {

            executor.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드 대기

                    List<String> shuffled = new ArrayList<>(hashtags);
                    Collections.shuffle(shuffled);

                    postWriteService.savePost(
                            testUser.getId(),
                            true,
                            "게시글",
                            "DOOSAN",
                            shuffled,
                            null
                    );

                    success.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).as("시간 안에 완료되어야 데드락 없음").isTrue();
        assertThat(success.get()).as("모든 요청 성공").isEqualTo(threadCount);

        List<HashtagEntity> result = hashtagJpaRepository.findByTagNameIn(hashtags);
        assertThat(result).hasSize(5);
        result.forEach(h -> assertThat(h.getUsageCount()).isEqualTo((long) threadCount));
    }

    @Test
    @DisplayName("100 스레드 - 동일 게시글 해시태그 추가 데드락 테스트")
    void concurrentPostUpdate_shouldNotDeadlock() throws InterruptedException {

        postWriteService.savePost(
                testUser.getId(), true, "초기", "DOOSAN", null, null
        );
        Long postId = postJpaRepository.findAll().getFirst().getId();

        int threadCount = 100;
        List<String> hashtags = List.of("야구", "응원", "두산");

        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {

            executor.submit(() -> {
                try {
                    startLatch.await();

                    List<String> shuffled = new ArrayList<>(hashtags);
                    Collections.shuffle(shuffled);

                    postWriteService.updatePost(
                            testUser.getId(),
                            postId,
                            "수정",
                            shuffled,
                            null,
                            null
                    );

                    success.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).as("데드락 없이 완료").isTrue();

        List<HashtagEntity> result = hashtagJpaRepository.findByTagNameIn(hashtags);
        assertThat(result).hasSize(3);
    }

}
