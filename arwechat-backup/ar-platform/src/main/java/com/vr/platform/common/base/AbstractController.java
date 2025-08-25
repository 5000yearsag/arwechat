package com.vr.platform.common.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.platform.authentication.detail.CustomUserDetailsUser;
import com.vr.platform.common.utils.MPPageConvert;
import com.vr.platform.common.utils.UserUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;


public abstract class AbstractController {

	@Autowired
	protected MPPageConvert mpPageConvert;

	@Autowired
	public ObjectMapper objectMapper;

	protected CustomUserDetailsUser getUser() {
		return UserUtil.getUser();
	}

	@SneakyThrows
	protected String getUserId() {
		return UserUtil.getUserId();
	}
}
