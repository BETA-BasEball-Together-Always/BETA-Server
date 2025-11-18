package com.beta.infra.community.repository.querydsl;

import com.beta.infra.community.entity.QPostEntity;
import com.beta.infra.community.entity.QPostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.repository.PostRepositoryCustom;
import com.beta.infra.community.repository.dao.PostWithImages;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<PostWithImages> findPostWithImages(Long postId) {
        QPostEntity post = QPostEntity.postEntity;
        QPostImageEntity image = QPostImageEntity.postImageEntity;
        Map<Long, PostWithImages> result =
                queryFactory.select(
                    post.id,
                    post.userId,
                    post.content,
                    post.channel,
                    post.commentCount,
                    post.likeCount,
                    post.sadCount,
                    post.funCount,
                    post.hypeCount,
                    post.createdAt,
                    image.id,
                    image.imgUrl,
                    image.sort
                ).from(post)
                        .leftJoin(image)
                        .on(post.id.eq(image.postId).in(image.status.eq(Status.ACTIVE), image.status.eq(Status.MARKED_FOR_DELETION)))
                .where(post.id.eq(postId).and(post.status.eq(Status.ACTIVE)))
                .orderBy(post.createdAt.desc())
                .transform(
                        GroupBy.groupBy(post.id).as(
                                Projections.fields(
                                        PostWithImages.class,
                                        post.id.as("postId"),
                                        post.userId,
                                        post.content,
                                        post.channel,
                                        post.commentCount,
                                        post.likeCount,
                                        post.sadCount,
                                        post.funCount,
                                        post.hypeCount,
                                        post.createdAt,
                                        GroupBy.list(
                                                Projections.fields(
                                                        PostWithImages.Images.class,
                                                        image.id.as("imageId"),
                                                        image.imgUrl,
                                                        image.sort
                                                )
                                        ).as("images")
                                )
                        )
                );
        return Optional.ofNullable(result.get(postId));
    }
}
