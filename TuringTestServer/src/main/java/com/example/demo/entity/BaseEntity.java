package com.example.demo.entity;

import java.io.Serializable;
import java.util.Date;

public class BaseEntity implements Serializable{
    private String createdUser;
    private Date createdTime;
    private String modifiedUser;
    private Date modifiedTime;

    public String getCreatedUser() {
        return createdUser;
    }
    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }
    public Date getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    public String getModifiedUser() {
        return modifiedUser;
    }
    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }
    public Date getModifiedTime() {
        return modifiedTime;
    }
    public void setModifiedTime(Date emodifiedTime) {
        this.modifiedTime = emodifiedTime;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((createdUser == null) ? 0 : createdUser.hashCode());
        result = prime * result + ((createdTime == null) ? 0 : createdTime.hashCode());
        result = prime * result + ((modifiedUser == null) ? 0 : modifiedUser.hashCode());
        result = prime * result + ((modifiedTime == null) ? 0 : modifiedTime.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseEntity other = (BaseEntity) obj;
        if (createdUser == null) {
            if (other.createdUser != null)
                return false;
        } else if (!createdUser.equals(other.createdUser))
            return false;
        if (createdTime == null) {
            if (other.createdTime != null)
                return false;
        } else if (!createdTime.equals(other.createdTime))
            return false;
        if (modifiedUser == null) {
            if (other.modifiedUser != null)
                return false;
        } else if (!modifiedUser.equals(other.modifiedUser))
            return false;
        if (modifiedTime == null) {
            if (other.modifiedTime != null)
                return false;
        } else if (!modifiedTime.equals(other.modifiedTime))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "BaseEntity [createdUser=" + createdUser + ", createdTime=" + createdTime + ", modifiedUser="
                + modifiedUser + ", modifiedTime=" + modifiedTime + "]";
    }

    
}
