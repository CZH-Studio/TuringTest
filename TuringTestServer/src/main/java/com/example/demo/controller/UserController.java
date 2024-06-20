package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.Response;
import com.example.demo.dto.Rank;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;



@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Response<Long> addNewUser(@RequestBody UserDTO userDTO) {
        Response<Long> response = new Response<>();
        response.setUid(userService.addNewUser(userDTO));
        response.setData((long)0);
        return response;
    }
    
    @PostMapping("/login")
    public Response<Long> updateStudentById(@RequestBody UserDTO userDTO){
        Response<Long> response = new Response<>();
        Long uid = userService.userLogin(userDTO);
        response.setUid(uid);
        response.setData(userService.getUserByUid(uid).getScore());
        return response;
    }
    
    @PostMapping("/ranking")
    public Response<List<Rank>> getRanking(@RequestBody UserDTO userDTO){
        Response<List<Rank>> response = new Response<>();
        response.setData(userService.getUserRanking(userDTO.getUid()));
        return response;
    }

    @PostMapping("/info")
    public Response<Long> getInfo(@RequestBody UserDTO userDTO){
        Response<Long> response = new Response<>();
        Long uid = userDTO.getUid();
        response.setUid(uid);
        response.setData(userService.getUserByUid(uid).getScore());
        return response;
    }

}   
