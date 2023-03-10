package com.youlai.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.youlai.common.base.IBaseEnum;
import com.youlai.common.constant.SystemConstants;
import com.youlai.common.enums.GenderEnum;
import com.youlai.security.util.SecurityUtils;
import com.youlai.system.converter.UserConverter;
import com.youlai.system.dto.UserAuthInfo;
import com.youlai.system.listener.UserImportListener;
import com.youlai.system.mapper.SysUserMapper;
import com.youlai.system.pojo.dto.UserImportDTO;
import com.youlai.system.pojo.entity.SysUser;
import com.youlai.system.pojo.entity.SysUserRole;
import com.youlai.system.pojo.form.UserForm;
import com.youlai.system.pojo.po.UserAuthPO;
import com.youlai.system.pojo.po.UserFormPO;
import com.youlai.system.pojo.po.UserPO;
import com.youlai.system.pojo.query.UserPageQuery;
import com.youlai.system.pojo.vo.user.UserLoginVO;
import com.youlai.system.pojo.vo.user.UserExportVO;
import com.youlai.system.pojo.vo.user.UserVO;
import com.youlai.system.service.SysUserRoleService;
import com.youlai.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ?????????????????????
 *
 * @author haoxr
 * @date 2022/1/14
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleService userRoleService;
    private final UserImportListener userImportListener;
    private final UserConverter userConverter;

    /**
     * ????????????????????????
     *
     * @param queryParams
     * @return
     */
    @Override
    public IPage<UserVO> listUserPages(UserPageQuery queryParams) {

        // ????????????
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<UserPO> page = new Page<>(pageNum, pageSize);

        // ????????????
        Page<UserPO> userPoPage = this.baseMapper.listUserPages(page, queryParams);

        // ????????????
        Page<UserVO> userVoPage = userConverter.po2Vo(userPoPage);

        return userVoPage;
    }

    /**
     * ??????????????????
     *
     * @param userId
     * @return
     */
    @Override
    public UserForm getUserFormData(Long userId) {
        UserFormPO userFormPO = this.baseMapper.getUserDetail(userId);
        // ????????????po->form
        UserForm userForm = userConverter.po2Form(userFormPO);
        return userForm;
    }

    /**
     * ????????????
     *
     * @param userForm ??????????????????
     * @return
     */
    @Override
    public boolean saveUser(UserForm userForm) {

        String username = userForm.getUsername();

        long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        Assert.isTrue(count == 0, "??????????????????");

        // ???????????? form->entity
        SysUser entity = userConverter.form2Entity(userForm);

        // ????????????????????????
        String defaultEncryptPwd = passwordEncoder.encode(SystemConstants.DEFAULT_USER_PASSWORD);
        entity.setPassword(defaultEncryptPwd);

        // ????????????
        boolean result = this.save(entity);

        if (result) {
            // ??????????????????
            userRoleService.saveUserRoles(entity.getId(), userForm.getRoleIds());
        }
        return result;
    }

    /**
     * ????????????
     *
     * @param userId   ??????ID
     * @param userForm ??????????????????
     * @return
     */
    @Override
    @Transactional
    public boolean updateUser(Long userId, UserForm userForm) {

        String username = userForm.getUsername();

        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .ne(SysUser::getId, userId)
        );
        Assert.isTrue(count == 0, "??????????????????");

        // form -> entity
        SysUser entity = userConverter.form2Entity(userForm);

        // ????????????
        boolean result = this.updateById(entity);

        if (result) {
            // ??????????????????
            userRoleService.saveUserRoles(entity.getId(), userForm.getRoleIds());
        }
        return result;
    }

    /**
     * ????????????
     *
     * @param idsStr ??????ID????????????????????????(,)??????
     * @return
     */
    @Override
    public boolean deleteUsers(String idsStr) {
        Assert.isTrue(StrUtil.isNotBlank(idsStr), "???????????????????????????");
        // ????????????
        List<Long> ids = Arrays.asList(idsStr.split(",")).stream()
                .map(idStr -> Long.parseLong(idStr)).collect(Collectors.toList());
        boolean result = this.removeByIds(ids);
        return result;

    }

    /**
     * ??????????????????
     *
     * @param userId   ??????ID
     * @param password ????????????
     * @return
     */
    @Override
    public boolean updatePassword(Long userId, String password) {
        String encryptedPassword = passwordEncoder.encode(password);
        boolean result = this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, encryptedPassword)
        );

        return result;
    }

    /**
     * ?????????????????????????????????
     *
     * @param username
     * @return
     */
    @Override
    public UserAuthInfo getUserAuthInfo(String username) {
        UserAuthPO userAuthPO = this.baseMapper.getUserAuthInfo(username);
        UserAuthInfo userAuthInfo = userConverter.po2AuthDto(userAuthPO);
        return userAuthInfo;
    }

    /**
     * ????????????
     *
     * @param userImportDTO
     * @return
     */
    @Transactional
    @Override
    public String importUsers(UserImportDTO userImportDTO) throws IOException {

        Long deptId = userImportDTO.getDeptId();
        List<Long> roleIds = Arrays.stream(userImportDTO.getRoleIds().split(","))
                .map(roleId -> Convert.toLong(roleId))
                .collect(Collectors.toList());
        InputStream inputStream = userImportDTO.getFile().getInputStream();

        ExcelReaderBuilder excelReaderBuilder = EasyExcel.read(inputStream, UserImportDTO.UserItem.class, userImportListener);
        ExcelReaderSheetBuilder sheet = excelReaderBuilder.sheet();
        List<UserImportDTO.UserItem> list = sheet.doReadSync();

        Assert.isTrue(CollectionUtil.isNotEmpty(list), "????????????????????????");

        // ??????????????????
        List<UserImportDTO.UserItem> validDataList = list.stream()
                .filter(item -> StrUtil.isNotBlank(item.getUsername()))
                .collect(Collectors.toList());

        Assert.isTrue(CollectionUtil.isNotEmpty(validDataList), "????????????????????????");

        long distinctCount = validDataList.stream()
                .map(UserImportDTO.UserItem::getUsername)
                .distinct()
                .count();
        Assert.isTrue(validDataList.size() == distinctCount, "???????????????????????????????????????????????????");

        List<SysUser> saveUserList = Lists.newArrayList();

        StringBuilder errMsg = new StringBuilder();
        for (int i = 0; i < validDataList.size(); i++) {
            UserImportDTO.UserItem userItem = validDataList.get(i);

            String username = userItem.getUsername();
            if (StrUtil.isBlank(username)) {
                errMsg.append(StrUtil.format("???{}????????????????????????????????????????????????", i + 1));
                continue;
            }

            String nickname = userItem.getNickname();
            if (StrUtil.isBlank(nickname)) {
                errMsg.append(StrUtil.format("???{}???????????????????????????????????????????????????", i + 1));
                continue;
            }

            SysUser user = new SysUser();
            user.setUsername(username);
            user.setNickname(nickname);
            user.setMobile(userItem.getMobile());
            user.setEmail(userItem.getEmail());
            user.setDeptId(deptId);
            // ????????????
            user.setPassword(passwordEncoder.encode(SystemConstants.DEFAULT_USER_PASSWORD));
            // ????????????
            Integer gender = (Integer) IBaseEnum.getValueByLabel(userItem.getGender(), GenderEnum.class);
            user.setGender(gender);

            saveUserList.add(user);
        }

        if (CollectionUtil.isNotEmpty(saveUserList)) {
            boolean result = this.saveBatch(saveUserList);
            Assert.isTrue(result, "????????????????????????????????????????????????");

            List<SysUserRole> userRoleList = new ArrayList<>();

            if (CollectionUtil.isNotEmpty(roleIds)) {

                roleIds.forEach(roleId -> {
                    userRoleList.addAll(
                            saveUserList.stream()
                                    .map(user -> new SysUserRole(user.getId(), roleId)).
                                    collect(Collectors.toList()));
                });
            }

            userRoleService.saveBatch(userRoleList);
        }

        errMsg.append(StrUtil.format("??????{}????????????????????????{}??????????????????????????????{}???", list.size(), saveUserList.size(), list.size() - saveUserList.size()));
        return errMsg.toString();

    }

    /**
     * ????????????????????????
     *
     * @param queryParams
     * @return
     */
    @Override
    public List<UserExportVO> listExportUsers(UserPageQuery queryParams) {
        List<UserExportVO> list = this.baseMapper.listExportUsers(queryParams);
        return list;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @Override
    public UserLoginVO getLoginUserInfo() {
        // ????????????entity
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, SecurityUtils.getUsername())
                .select(
                        SysUser::getId,
                        SysUser::getNickname,
                        SysUser::getAvatar
                )
        );
        // entity->VO
        UserLoginVO userLoginVO = userConverter.entity2LoginUser(user);

        // ??????????????????
        Set<String> roles = SecurityUtils.getRoles();
        userLoginVO.setRoles(roles);
        return userLoginVO;
    }


}
