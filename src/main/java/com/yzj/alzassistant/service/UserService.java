package com.yzj.alzassistant.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yzj.alzassistant.model.dto.user.UserQueryRequest;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.LoginUserVO;
import com.yzj.alzassistant.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码添加盐值
     */
    String getEncryptPassword(String userPassword);

    /**
     * 转化为脱敏的已登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    //-----------------------------------------------------------------------------------------------

    /**
     * 转化为脱敏的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 转化为脱敏的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 跟据查询请求构造查询语句
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

}
