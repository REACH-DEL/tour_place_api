package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.Place;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Mapper
@Repository
public interface PlaceMapper {
    
    @Insert("""
            INSERT INTO place (place_id, place_name, description, main_image, lat, longitude, created_at, updated_at)
            VALUES (#{placeId}::UUID, #{placeName}, #{description}, #{mainImage}, #{lat}, #{longitude}, NOW(), NOW())
            """)
    void insert(Place place);

    @Results(id = "placeMapper", value = {
            @Result(property = "placeId", column = "place_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "placeName", column = "place_name"),
            @Result(property = "description", column = "description"),
            @Result(property = "mainImage", column = "main_image"),
            @Result(property = "lat", column = "lat"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("""
            SELECT * FROM place WHERE place_id = #{placeId}::UUID
            """)
    Optional<Place> findById(UUID placeId);

    @ResultMap("placeMapper")
    @Select("""
            SELECT * FROM place ORDER BY created_at DESC
            """)
    List<Place> findAll();

    @ResultMap("placeMapper")
    @Select("""
            SELECT * FROM place WHERE place_name ILIKE #{searchTerm} ORDER BY created_at DESC
            """)
    List<Place> searchByName(String searchTerm);

    @ResultMap("placeMapper")
    @Select("""
            UPDATE place SET place_name = #{place.placeName}, description = #{place.description},
                            main_image = #{place.mainImage}, lat = #{place.lat}, longitude = #{place.longitude},
                            updated_at = NOW()
            WHERE place_id = #{place.placeId}::UUID
            RETURNING *
            """)
    Optional<Place> update(@Param("place") Place place);

    @Delete("""
            DELETE FROM place WHERE place_id = #{placeId}::UUID
            """)
    void delete(UUID placeId);

    @Select("""
            SELECT COUNT(*) FROM place
            """)
    int count();
}
