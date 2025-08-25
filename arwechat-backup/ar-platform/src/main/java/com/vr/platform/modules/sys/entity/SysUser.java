package com.vr.platform.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.vr.platform.common.bean.entity.BaseInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 系统用户
 *
 * @author czx
 * @email object_czx@163.com
 */
@Data
@TableName("t_user")
@ApiModel(value = "系统用户")
public class SysUser extends BaseInfo {

    @ApiModelProperty(value = "用户名")
    @NotBlank(message = "用户名不能为空")
    public String username;

    @ApiModelProperty(value = "姓名")
    public String name;

    @ApiModelProperty(value = "密码")
    public String password;

    @ApiModelProperty(value = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    public String email;

    @ApiModelProperty(value = "手机号")
    public String phone;

    @ApiModelProperty(value = "头像")
    public String avatar;

}
