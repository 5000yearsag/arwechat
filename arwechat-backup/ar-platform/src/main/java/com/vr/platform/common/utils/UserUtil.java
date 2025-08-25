package com.vr.platform.common.utils;

import com.vr.platform.authentication.detail.CustomUserDetailsUser;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class UserUtil {

    public CustomUserDetailsUser getUser() {
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(object != null){
            return (CustomUserDetailsUser) object;
        }
        return null;
    }

    @SneakyThrows
    public String getUserId() {
        return getUser() == null ? null :getUser().getUserId();
    }

}
