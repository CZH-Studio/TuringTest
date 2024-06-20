package com.example.demo.converter;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;

public class UserConverter {

    public static UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setUsername(user.getUsername());
        return userDTO;
    }

    public static User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setUid(userDTO.getUid());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword()); 
        return user;
    }
}
