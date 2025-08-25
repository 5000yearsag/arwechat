package com.vr.platform.modules.sys.entity;

import lombok.Data;

@Data
public class ResetPasswordReq {
    private String oldPassword;
    private String newPassword;
    private String captcha;
    private String randomStr;
}
