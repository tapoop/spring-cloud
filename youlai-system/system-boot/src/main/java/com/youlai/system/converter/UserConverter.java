package com.youlai.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.system.dto.UserAuthInfo;
import com.youlai.system.pojo.entity.SysUser;
import com.youlai.system.pojo.form.UserForm;
import com.youlai.system.pojo.po.UserAuthPO;
import com.youlai.system.pojo.po.UserFormPO;
import com.youlai.system.pojo.po.UserPO;
import com.youlai.system.pojo.vo.user.UserLoginVO;
import com.youlai.system.pojo.vo.user.UserVO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 用户对象转换器
 *
 * @author haoxr
 * @date 2022/6/8
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mappings({
            @Mapping(target = "genderLabel", expression = "java(com.youlai.common.base.IBaseEnum.getLabelByValue(po.getGender(), com.youlai.common.enums.GenderEnum.class))")
    })
    UserVO po2Vo(UserPO po);

    Page<UserVO> po2Vo(Page<UserPO> po);

    UserForm po2Form(UserFormPO po);

    UserForm entity2Form(SysUser entity);

    @InheritInverseConfiguration(name = "entity2Form")
    SysUser form2Entity(UserForm entity);

    @Mappings({
            @Mapping(target = "userId", source = "id")
    })
    UserLoginVO entity2LoginUser(SysUser entity);


    UserAuthInfo po2AuthDto(UserAuthPO userAuthPO);

}
