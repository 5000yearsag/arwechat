package com.vr.platform.modules.sys.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.vr.platform.authentication.detail.CustomUserDetailsUser;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.common.exception.BizException;
import com.vr.platform.common.exception.CustomAuthenticationException;
import com.vr.platform.common.utils.Constant;
import com.vr.platform.common.utils.MemCacheUtils;
import com.vr.platform.modules.sys.entity.ResetPasswordReq;
import com.vr.platform.modules.sys.entity.SysUser;
import com.vr.platform.modules.sys.mapper.SysUserMapper;
import com.vr.platform.modules.sys.vo.UserInfoVO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;

    public int updatePassword(CustomUserDetailsUser user, ResetPasswordReq request) {

        String code_key = MemCacheUtils.get(Constant.NUMBER_CODE_KEY + request.getRandomStr());
        if(StrUtil.isEmpty(code_key)){
            throw new BizException(BizReturnCode.BIZ_CAPTCHA_EXPIRE);
        }

        if(!request.getCaptcha().equalsIgnoreCase(code_key)){
            throw new BizException(BizReturnCode.BIZ_CAPTCHA_ERROR);
        }

        if (StrUtil.isEmpty(request.getOldPassword())) {
            throw new BizException(BizReturnCode.BIZ_OLD_PASSWORD_BLANK);
        }
        if (StrUtil.isEmpty(request.getNewPassword())) {
            throw new BizException(BizReturnCode.BIZ_NEW_PASSWORD_BLANK);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getOldPassword(),user.getPassword())) {
            throw new BizException(BizReturnCode.BIZ_OLD_PASSWORD_ERROR);
        }

        SysUser sysUser = new SysUser();
        sysUser.setId(user.getUserId());
        sysUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return sysUserMapper.updateById(sysUser);
    }

    public SysUser getById(String userId) {
        return sysUserMapper.selectById(userId);
    }

    public SysUser getUserByUsername(String username) {
        return sysUserMapper.getUserByUsername(username);
    }

    public List<UserInfoVO> getUserList() {
        return sysUserMapper.getUserList();
    }

    public void deleteUser(String userId) {
        sysUserMapper.deleteById(userId);
    }
}
