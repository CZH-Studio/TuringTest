package com.example.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.example.demo.converter.UserConverter;
import com.example.demo.dto.Rank;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRepository;
import com.example.demo.exception.PasswordNotMatchException;
import com.example.demo.exception.UsernameDuplicatedException;
import com.example.demo.exception.UsernameNotExistException;

@Component
@Service
public class UserServiceImpl implements UserService{
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public User getUserByUid(Long uid) {
        List<User> userList =  userRepository.findByUid(uid);
        if (CollectionUtils.isEmpty(userList)) {
            //+ notfounderror
        }
        User user = userList.get(0);
        return user;
    }

    @Override
    public void updateScoreByUid(Long uid, Long score) {
        long currentScore = userRepository.getScoreByUid(uid);
        currentScore += score;
        userRepository.updateScoreByUid(uid, currentScore);
    }
    
    @Override
    public Long addNewUser(UserDTO userDTO) {
        List<User> userList = userRepository.findByUsername(userDTO.getUsername());
        if (!CollectionUtils.isEmpty(userList)) {
            throw new UsernameDuplicatedException("Username already exists");
        }
        User user = UserConverter.convertToEntity(userDTO);
        user.setScore((long)0);
        user.setIsDelete(0);
        Date date = new Date();
        user.setCreatedTime(date);
        user.setCreatedUser(userDTO.getUsername());
        user.setModifiedTime(date);
        user.setModifiedUser(userDTO.getUsername());
        user = userRepository.save(user);
        return user.getUid();
    }

    
    @Override
    public Long userLogin(UserDTO userDTO) {
        List<User> userlist = userRepository.findByUsername(userDTO.getUsername());
        if (CollectionUtils.isEmpty(userlist)) {
            throw new UsernameNotExistException("Username does not exist");
        }
        User user = userlist.get(0);
        if (!userDTO.getPassword().equals(user.getPassword())) {
            throw new PasswordNotMatchException("Password does not match");
        }
        return user.getUid();
    }

    @Override
    public List<Rank> getUserRanking(Long uid) {
        userRepository.createRankedUsers();
        Long rank = userRepository.getRank(uid);
        List<Map<String,String>> nearRank = userRepository.getRankingNearUser(rank);
        String irsStr = JSON.toJSONString(nearRank);
        List<Rank> nearrank = JSON.parseArray(irsStr,Rank.class);
        // System.out.println(nearrank.toString());
        userRepository.dropRankedUsers();

        List<Map<String,String>> topRank = userRepository.getTopRankingOrderByScoreDesc();
        irsStr = JSON.toJSONString(topRank);
        List<Rank> toprank = JSON.parseArray(irsStr,Rank.class);
        toprank.addAll(nearrank);
        return toprank;
    }
}
