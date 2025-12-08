package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.ActivityLog;
import com.example.tour_place_api.model.response.PlacesOverviewResponse;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Mapper
@Repository
public interface DashboardMapper {

    // Statistics queries
    // Count only regular users (exclude admin role)
    @Select("SELECT COUNT(*) FROM users WHERE role = 'user'")
    Long countTotalUsers();

    @Select("""
            SELECT COUNT(*) FROM users 
            WHERE role = 'user' AND created_at < NOW() - INTERVAL '1 month'
            """)
    Long countUsersPreviousMonth();

    @Select("SELECT COUNT(*) FROM place")
    Long countTotalPlaces();

    @Select("""
            SELECT COUNT(*) FROM place 
            WHERE created_at < NOW() - INTERVAL '1 month'
            """)
    Long countPlacesPreviousMonth();

    @Select("""
            SELECT COUNT(*) FROM (
                SELECT main_image FROM place WHERE main_image IS NOT NULL AND main_image != ''
                UNION ALL
                SELECT ai.image_url FROM additional_image ai
                JOIN image_of_place iop ON ai.image_id = iop.image_id
            ) AS all_images
            """)
    Long countTotalImages();

    @Select("""
            SELECT COUNT(*) FROM (
                SELECT main_image FROM place 
                WHERE main_image IS NOT NULL AND main_image != '' 
                AND created_at < NOW() - INTERVAL '1 month'
                UNION ALL
                SELECT ai.image_url FROM additional_image ai
                JOIN image_of_place iop ON ai.image_id = iop.image_id
                JOIN place p ON iop.place_id = p.place_id
                WHERE p.created_at < NOW() - INTERVAL '1 month'
            ) AS all_images
            """)
    Long countImagesPreviousMonth();

    // Places overview chart data
    @Results(id = "placesOverviewMapper", value = {
            @Result(property = "month", column = "month"),
            @Result(property = "monthNumber", column = "month_number"),
            @Result(property = "year", column = "year"),
            @Result(property = "count", column = "count", javaType = Long.class, jdbcType = JdbcType.BIGINT)
    })
    @Select("""
            SELECT 
                TO_CHAR(created_at, 'Mon') AS month,
                EXTRACT(MONTH FROM created_at)::INTEGER AS month_number,
                EXTRACT(YEAR FROM created_at)::INTEGER AS year,
                COUNT(*)::BIGINT AS count
            FROM place
            WHERE created_at >= NOW() - INTERVAL '${months} months'
            GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at), TO_CHAR(created_at, 'Mon')
            ORDER BY year, month_number
            """)
    List<PlacesOverviewResponse> getPlacesOverviewByMonths(@Param("months") int months);

    // Activity log queries
    @Results(id = "activityLogMapper", value = {
            @Result(property = "activityId", column = "activity_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "action", column = "action"),
            @Result(property = "entityType", column = "entity_type"),
            @Result(property = "entityId", column = "entity_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "entityName", column = "entity_name"),
            @Result(property = "userId", column = "user_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "userEmail", column = "user_email")
    })
    @Select("""
            SELECT 
                al.activity_id,
                al.action,
                al.entity_type,
                al.entity_id,
                al.entity_name,
                al.user_id,
                al.created_at,
                u.email AS user_email
            FROM activity_log al
            JOIN users u ON al.user_id = u.user_id
            ORDER BY al.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<ActivityLog> findRecentActivities(@Param("limit") int limit, @Param("offset") int offset);

    @Insert("""
            INSERT INTO activity_log (activity_id, action, entity_type, entity_id, entity_name, user_id, created_at)
            VALUES (#{activityId}::UUID, #{action}::activity_action, #{entityType}::entity_type, 
                    #{entityId}::UUID, #{entityName}, #{userId}::UUID, NOW())
            """)
    void insertActivity(ActivityLog activityLog);
}

