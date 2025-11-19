package com.shanhai.log.utils;

/**
 * BMB专用日志级别
 * @author Shanhai
 */
public class BmBLevel {
    /**
     * 系统普通用户
     * @apiNote
     * 职能:系统业务使用(运营等)
     * 审计日志：无权
     */
    public  static final String SYS_USER="SYS_USER";
    /**
     * 系统管理员
     * @apiNote
     * 职能:系统核心数据维护(如用户|角色|权限等)
     * 审计日志：无权
     */
    public  static final String SYS_ADMIN="SYS_ADMIN";
    /**
     * 安全保密管理员
     * @apiNote
     * 职能:系统用户权限管理|用户操作行为安全设计
     * 审计日志：SYS_USER+SYS_ADMIN
     */
    public  static final String SEC_ADMIN="SEC_ADMIN";
    /**
     * 安全审计员
     * @apiNote
     * 职能: 系统管理员和安全保密员的操作行为进行审计跟踪
     * 审计日志：SYS_ADMIN+SEC_ADMIN
     */
    public  static final String AUD_ADMIN="AUD_ADMIN";
}
