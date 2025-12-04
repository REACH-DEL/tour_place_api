package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.Favorite;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Mapper
@Repository
public interface FavoriteMapper {
    
    @Insert("""
            INSERT INTO favorite (fav_id, user_id, place_id, created_at, updated_at)
            VALUES (#{favId}::UUID, #{userId}::UUID, #{placeId}::UUID, NOW(), NOW())
            """)
    void insert(Favorite favorite);

    @Results(id = "favoriteMapper", value = {
            @Result(property = "favId", column = "fav_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "placeId", column = "place_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("""
            SELECT * FROM favorite WHERE fav_id = #{favId}::UUID
            """)
    Optional<Favorite> findById(UUID favId);

    @ResultMap("favoriteMapper")
    @Select("""
            SELECT * FROM favorite WHERE user_id = #{userId}::UUID ORDER BY created_at DESC
            """)
    List<Favorite> findByUserId(UUID userId);

    @ResultMap("favoriteMapper")
    @Select("""
            SELECT * FROM favorite WHERE user_id = #{userId}::UUID AND place_id = #{placeId}::UUID
            """)
    Optional<Favorite> findByUserAndPlace(UUID userId, UUID placeId);

    @Delete("""
            DELETE FROM favorite WHERE fav_id = #{favId}::UUID
            """)
    void delete(UUID favId);

    @Delete("""
            DELETE FROM favorite WHERE user_id = #{userId}::UUID AND place_id = #{placeId}::UUID
            """)
    void deleteByUserAndPlace(UUID userId, UUID placeId);
}
