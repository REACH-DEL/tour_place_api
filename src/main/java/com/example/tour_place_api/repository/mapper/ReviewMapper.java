package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.Review;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Mapper
@Repository
public interface ReviewMapper {
    
    @Insert("""
            INSERT INTO review (review_id, user_id, place_id, rating, comment, created_at, updated_at)
            VALUES (#{reviewId}::UUID, #{userId}::UUID, #{placeId}::UUID, #{rating}, #{comment}, NOW(), NOW())
            """)
    void insert(Review review);

    @Results(id = "reviewMapper", value = {
            @Result(property = "reviewId", column = "review_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "placeId", column = "place_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "rating", column = "rating"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("""
            SELECT * FROM review WHERE review_id = #{reviewId}::UUID
            """)
    Optional<Review> findById(UUID reviewId);

    @ResultMap("reviewMapper")
    @Select("""
            SELECT r.* FROM review r
            WHERE r.place_id = #{placeId}::UUID
            ORDER BY r.created_at DESC
            """)
    List<Review> findByPlaceId(UUID placeId);

    @ResultMap("reviewMapper")
    @Select("""
            SELECT r.* FROM review r
            WHERE r.user_id = #{userId}::UUID
            ORDER BY r.created_at DESC
            """)
    List<Review> findByUserId(UUID userId);

    @ResultMap("reviewMapper")
    @Select("""
            SELECT * FROM review WHERE user_id = #{userId}::UUID AND place_id = #{placeId}::UUID
            """)
    Optional<Review> findByUserAndPlace(UUID userId, UUID placeId);

    @ResultMap("reviewMapper")
    @Select("""
            UPDATE review SET rating = #{rating}, comment = #{comment}, updated_at = NOW()
            WHERE review_id = #{reviewId}::UUID
            RETURNING *
            """)
    Optional<Review> update(@Param("reviewId") UUID reviewId, @Param("rating") Integer rating, @Param("comment") String comment);

    @Delete("""
            DELETE FROM review WHERE review_id = #{reviewId}::UUID
            """)
    void delete(UUID reviewId);

    @Select("""
            SELECT COALESCE(AVG(rating), 0) FROM review WHERE place_id = #{placeId}::UUID
            """)
    Double getAverageRatingByPlaceId(UUID placeId);

    @Select("""
            SELECT COUNT(*) FROM review WHERE place_id = #{placeId}::UUID
            """)
    Long getTotalReviewsByPlaceId(UUID placeId);

    @Select("""
            SELECT COUNT(*) FROM review WHERE place_id = #{placeId}::UUID AND rating = #{rating}
            """)
    Long getRatingCountByPlaceIdAndRating(@Param("placeId") UUID placeId, @Param("rating") Integer rating);
}

