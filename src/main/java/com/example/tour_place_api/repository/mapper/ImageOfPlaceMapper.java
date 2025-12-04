package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.ImageOfPlace;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Mapper
@Repository
public interface ImageOfPlaceMapper {
    
    @Insert("""
            INSERT INTO image_of_place (ip_id, place_id, image_id)
            VALUES (#{ipId}::UUID, #{placeId}::UUID, #{imageId}::UUID)
            """)
    void insert(ImageOfPlace imageOfPlace);

    @Results(id = "imageOfPlaceMapper", value = {
            @Result(property = "ipId", column = "ip_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "placeId", column = "place_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "imageId", column = "image_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR)
    })
    @Select("""
            SELECT * FROM image_of_place WHERE place_id = #{placeId}::UUID
            """)
    List<ImageOfPlace> findByPlaceId(UUID placeId);

    @Delete("""
            DELETE FROM image_of_place WHERE ip_id = #{ipId}::UUID
            """)
    void delete(UUID ipId);

    @Delete("""
            DELETE FROM image_of_place WHERE place_id = #{placeId}::UUID
            """)
    void deleteByPlaceId(UUID placeId);
}
