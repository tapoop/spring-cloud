package com.youlai.system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.youlai.common.base.BaseEntity;
import com.youlai.common.enums.MenuTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 菜单实体类
 *
 * @author haoxr
 * @date 2021/11/06
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SysMenu extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String name;

    private String icon;

    /**
     * 路由相对路径
     */
    private String path;

    /**
     * 组件绝对路径
     */
    private String component;

    private Integer sort;

    private Integer visible;

    private String redirectUrl;

    @TableField(exist = false)
    private List<String> roles;

    /**
     * 菜单类型(1-菜单;2-目录;3-外链;4-按钮)
     */
    private MenuTypeEnum type;

}
