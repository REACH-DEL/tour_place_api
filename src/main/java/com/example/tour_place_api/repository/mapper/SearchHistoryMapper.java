package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.SearchHistory;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
@Repository
public interface SearchHistoryMapper {
    
    @Insert("""
            INSERT INTO search_history (search_id, user_id, place_id, created_at, updated_at)
            VALUES (#{searchId}::UUID, #{userId}::UUID, #{placeId}::UUID, NOW(), NOW())
            """)
    void insert(SearchHistory searchHistory);

    @Results(id = "searchHistoryMapper", value = {
            @Result(property = "searchId", column = "search_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "placeId", column = "place_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("""
            SELECT * FROM search_history WHERE search_id = #{searchId}::UUID
            """)
    Optional<SearchHistory> findById(UUID searchId);

    @ResultMap("searchHistoryMapper")
    @Select("""
            SELECT * FROM search_history 
            WHERE user_id = #{userId}::UUID AND place_id = #{placeId}::UUID
            """)
    Optional<SearchHistory> findByUserAndPlace(UUID userId, UUID placeId);

    @ResultMap("searchHistoryMapper")
    @Select("""
            SELECT * FROM search_history 
            WHERE user_id = #{userId}::UUID 
            ORDER BY updated_at DESC 
            LIMIT 10
            """)
    List<SearchHistory> findLatestByUserId(UUID userId);

    @Update("""
            UPDATE search_history 
            SET updated_at = NOW() 
            WHERE search_id = #{searchId}::UUID
            """)
    void updateTimestamp(UUID searchId);

    @Update("""
            UPDATE search_history 
            SET updated_at = NOW() 
            WHERE user_id = #{userId}::UUID AND place_id = #{placeId}::UUID
            """)
    void updateTimestampByUserAndPlace(UUID userId, UUID placeId);

    @Delete("""
            DELETE FROM search_history WHERE search_id = #{searchId}::UUID
            """)
    void delete(UUID searchId);
}

