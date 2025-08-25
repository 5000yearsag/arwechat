package com.vr.platform.modules.sys.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserInfoVO {

    public String id;

    @ApiModelProperty(value = "用户名")
    public String username;

    @ApiModelProperty(value = "姓名")
    public String name;

    @ApiModelProperty(value = "邮箱")
    public String email;

    @ApiModelProperty(value = "手机号")
    public String phone;

    @ApiModelProperty(value = "头像")
    public String avatar;

    public Integer status;

}
