package com.beta.infra.community.entity;

import com.beta.infra.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "post_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImageEntity extends BaseEntity {

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "img_url", nullable = false)
    private String imgUrl;

    @Column(name = "origin_name", length = 100)
    private String originName;

    @Column(name = "new_name", length = 100)
    private String newName;

    @Column(name = "order", nullable = false)
    private Integer order = 0;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Builder
    public PostImageEntity(Long postId, String imgUrl, String originName, String newName,
                           Integer order, Long fileSize, String mimeType, Status status) {
        this.postId = postId;
        this.imgUrl = imgUrl;
        this.originName = originName;
        this.newName = newName;
        this.order = order;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.status = status;
    }
}
