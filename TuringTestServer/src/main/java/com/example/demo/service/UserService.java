package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.Rank;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;

public interface UserService {

    User getUserByUid(Long uid);

    void updateScoreByUid(Long uid, Long score);

    // UserDTO updateUserByUid(Long id, String name, String email);
    // void deleteUserByUid(Long id);
    Long addNewUser(UserDTO UserDTO);

    Long userLogin(UserDTO UserDTO);
    
    List<Rank> getUserRanking(Long uid);

}
