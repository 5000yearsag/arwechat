package com.vr.platform.modules.sys.controller;

import com.vr.platform.common.annotation.SysLog;
import com.vr.platform.common.base.AbstractController;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.common.utils.Constant;
import com.vr.platform.modules.sys.entity.ResetPasswordReq;
import com.vr.platform.modules.sys.entity.SysUser;
import com.vr.platform.modules.sys.service.SysUserService;
import com.vr.platform.modules.sys.vo.UserInfoVO;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 系统用户
 *
 * @author czx
 * @email object_czx@163.com
 */
@RestController
@RequestMapping("/api/sys/user")
@AllArgsConstructor
@Api(value = "SysUserController", tags = "系统用户")
public class SysUserController extends AbstractController {

    @Resource
    private SysUserService sysUserService;

    /**
     * 所有用户列表
     */
    @GetMapping(value = "/list")
    public ResponseFormat<List<UserInfoVO>> list() {
        List<UserInfoVO> userList = sysUserService.getUserList();
        return ResponseFormat.success(userList);
    }

    @GetMapping(value = "/getMe")
    public ResponseFormat<UserInfoVO> getMe() {
        UserInfoVO res = new UserInfoVO();
        SysUser user = sysUserService.getById(getUser().getUserId());
        BeanUtils.copyProperties(user,res);
        return ResponseFormat.success(res);
    }

    /**
     * 修改登录用户密码
     */
    @SysLog("修改密码")
    @PostMapping(value = "/resetPassword")
    public ResponseFormat resetPassword(@RequestBody ResetPasswordReq request) {

        //更新密码
        sysUserService.updatePassword(getUser(),request);
        return ResponseFormat.success();
    }


    /**
     * 保存用户
     */
    @SysLog("保存用户")
    @PostMapping(value = "/save")
    public ResponseFormat save(@RequestBody @Validated SysUser user) {
        return ResponseFormat.success();
    }

    /**
     * 修改用户
     */
    @SysLog("修改用户")
    @PostMapping(value = "/update")
    public ResponseFormat update(@RequestBody @Validated SysUser user) {
        return ResponseFormat.success();
    }

    /**
     * 删除用户
     */
    @SysLog("删除用户")
    @PostMapping(value = "/delete")
    public ResponseFormat delete(@RequestBody SysUser user) {
        if (user == null || user.getId() == null) {
            return ResponseFormat.fail("参数错误");
        }

        if (user.getId().equals(Constant.SUPER_ADMIN)) {
            return ResponseFormat.fail("系统管理员不能删除");
        }

        if (user.getId().equals(getUserId())) {
            return ResponseFormat.fail("当前用户不能删除");
        }
        sysUserService.deleteUser(user.getId());
        return ResponseFormat.success();
    }
}
