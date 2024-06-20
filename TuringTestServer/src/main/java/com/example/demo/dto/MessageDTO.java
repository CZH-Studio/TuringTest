package com.example.demo.dto;
 
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
 
/**
 * 消息 DTO
 *
 * @author wsl
 * @date 2024/2/20
 */
// @AllArgsConstructor
public class MessageDTO implements Serializable {
 
    private static final long serialVersionUID = 1L;
 
    @ApiModelProperty(value = "角色", notes = "说明: user-用户, assistant-助手", example = "user")
    private String role;
 
    @ApiModelProperty(value = "消息内容", notes = "说明: 消息内容", example = "你好")
    private String content;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
 
}