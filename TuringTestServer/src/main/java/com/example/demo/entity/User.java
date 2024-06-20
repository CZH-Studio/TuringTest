package com.example.demo.entity;

import static jakarta.persistence.GenerationType.IDENTITY;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "user")
public class User extends BaseEntity{

    @Id
    @Column(name = "uid")
    @GeneratedValue(strategy = IDENTITY)
    private Long uid;

    @Column(name = "username")
    private String username;

    @Column(name = "score")
    private Long score;

    @Column(name = "password")
    private String password;

    @Column(name = "is_delete")
    private Integer isDelete;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }


    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((score == null) ? 0 : score.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((isDelete == null) ? 0 : isDelete.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (score == null) {
            if (other.score != null)
                return false;
        } else if (!score.equals(other.score))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (isDelete == null) {
            if (other.isDelete != null)
                return false;
        } else if (!isDelete.equals(other.isDelete))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "User [uid=" + uid + ", username=" + username + ", score=" + score + ", password=" + password
                + ", isDelete=" + isDelete + "]";
    }

    
} 
