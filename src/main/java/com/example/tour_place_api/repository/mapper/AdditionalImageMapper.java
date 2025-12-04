package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.AdditionalImage;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Mapper
@Repository
public interface AdditionalImageMapper {
    
    @Insert("""
            INSERT INTO additional_image (image_id, image_url)
            VALUES (#{imageId}::UUID, #{imageUrl})
            """)
    void insert(AdditionalImage image);

    @Results(id = "additionalImageMapper", value = {
            @Result(property = "imageId", column = "image_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "imageUrl", column = "image_url")
    })
    @Select("""
            SELECT * FROM additional_image WHERE image_id = #{imageId}::UUID
            """)
    Optional<AdditionalImage> findById(UUID imageId);

    @ResultMap("additionalImageMapper")
    @Select("""
            SELECT * FROM additional_image
            """)
    List<AdditionalImage> findAll();

    @Delete("""
            DELETE FROM additional_image WHERE image_id = #{imageId}::UUID
            """)
    void delete(UUID imageId);
}
