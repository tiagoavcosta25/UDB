package org.socialnetwork.user;


import org.socialnetwork.annotations.Mapper;

@Mapper
public class UserMapper {
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(user.getUsername(), user.getPassword());
    }

    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return new User(userDTO.username(), userDTO.password());
    }
}
