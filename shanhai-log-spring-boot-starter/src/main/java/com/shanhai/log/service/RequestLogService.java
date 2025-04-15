package com.shanhai.log.service;


import com.shanhai.log.utils.RequestLogInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface RequestLogService {
  /**
   * 存储日志
   * @param requestLogInfo
   */
  public void saveLog(RequestLogInfo requestLogInfo);

  /**
   * 获取当前登录用户
   * @return
   */
  public String getCurrentUser(HttpServletRequest request);
  /**
   * 补充自定义扩展日志信息
   * @return
   */
  public Map<String,Object> getExtLogInfo(HttpServletRequest request);
  /**
   * 自定义源IP获取方法
   * @return
   */
  public String getReqSourceIp(HttpServletRequest request);
  /**
   * 存储日志(日志需要脱敏)
   */
  default void saveLog(RequestLogInfo requestLogInfo,String dataMaskingRule){};
}
