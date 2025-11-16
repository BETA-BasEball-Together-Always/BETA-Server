package com.beta.infra.community.repository;

import com.beta.infra.community.entity.HashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HashtagJpaRepository extends JpaRepository<HashtagEntity, Long> {

    List<HashtagEntity> findByTagNameIn(List<String> hashtags);

    @Modifying
    @Query("UPDATE HashtagEntity h SET h.usageCount = h.usageCount - 1 WHERE h.id IN :hashtagIds")
    void updateCountDecrement(@Param("hashtagIds") List<Long> hashtagIds);

    @Modifying
    @Query(value =  """
      INSERT INTO hashtag (tag_name, usage_count, created_at, updated_at) 
      VALUES (:tagName, 1, NOW(), NOW()) 
      ON DUPLICATE KEY UPDATE 
          usage_count = usage_count + 1,
          updated_at = NOW()
      """, nativeQuery = true)
    void upsertHashTags(@Param("tagName") String tagName);
}
