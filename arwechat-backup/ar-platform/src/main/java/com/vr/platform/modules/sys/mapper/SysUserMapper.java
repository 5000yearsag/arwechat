package com.vr.platform.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vr.platform.modules.sys.entity.SysUser;
import com.vr.platform.modules.sys.vo.UserInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM t_user WHERE username = #{username}")
    SysUser getUserByUsername(@Param("username") String username);

    @Select("SELECT * FROM t_user ")
    List<UserInfoVO> getUserList();

}
