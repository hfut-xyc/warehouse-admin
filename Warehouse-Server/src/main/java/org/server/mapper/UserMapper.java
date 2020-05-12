package org.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.server.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {

	List<User> getUserList(@Param("keyword") String keyword);

	User getUserByUsername(String username);

	int addUser(User user);

	int updateUserEnabled(@Param("enabled") boolean enabled, @Param("id") int id);

	int deleteUserById(int id);
}
