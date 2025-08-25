package com.vr.platform.common.bean.entity;

import lombok.Data;

@Data
public class WxAPIToken {
    private String access_token;
    private int expires_in;
}
