package com.yzj.alzassistant.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateMyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;
}

