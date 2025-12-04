package com.example.tour_place_api.repository.mapper;

import com.example.tour_place_api.model.entity.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Mapper
@Repository
public interface UserMapper {
    
    @Insert("""
            INSERT INTO users (user_id, full_name, email, password, status, role, created_at, updated_at)
            VALUES (#{userId}::UUID, #{fullName}, #{email}, #{password}, #{status}, #{role}::user_role, NOW(), NOW())
            """)
    void insert(User user);

    @Results(id = "userMapper", value = {
            @Result(property = "userId", column = "user_id", javaType = UUID.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "fullName", column = "full_name"),
            @Result(property = "email", column = "email"),
            @Result(property = "password", column = "password"),
            @Result(property = "status", column = "status"),
            @Result(property = "role", column = "role"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("""
            SELECT * FROM users WHERE user_id = #{userId}::UUID
            """)
    Optional<User> findById(UUID userId);

    @ResultMap("userMapper")
    @Select("""
            SELECT * FROM users WHERE email = #{email}
            """)
    Optional<User> findByEmail(String email);

    @ResultMap("userMapper")
    @Select("""
            SELECT * FROM users
            """)
    List<User> findAll();

    @ResultMap("userMapper")
    @Select("""
            UPDATE users SET full_name = #{user.fullName}, email = #{user.email}, password = #{user.password},
                            status = #{user.status}, role = #{user.role}::user_role, updated_at = NOW()
            WHERE user_id = #{user.userId}::UUID
            RETURNING *
            """)
    Optional<User> update(@Param("user") User user);

    @Delete("""
            DELETE FROM users WHERE user_id = #{userId}::UUID
            """)
    void delete(UUID userId);

    @Select("""
            SELECT EXISTS(SELECT 1 FROM users WHERE email = #{email})
            """)
    boolean existsByEmail(String email);
}
