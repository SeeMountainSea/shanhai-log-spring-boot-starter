package com.shanhai.log.service;


import com.shanhai.log.utils.RequestLogInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public interface RequestLogService {
  /**
   * 存储日志
   * @param requestLogInfo
   */
  default void saveLog(RequestLogInfo requestLogInfo){};
  /**
   * 获取文件下载日志
   * @param requestLogInfo 日志信息
   * @param fileDownloadLogRule 获取文件下载日志处理规则
   * @return
   */
  default String getFileDownLoadLog(RequestLogInfo requestLogInfo,String fileDownloadLogRule){ return "-";};
  /**
   * 获取当前登录用户
   * @return
   */
  default String getCurrentUser(HttpServletRequest request){return "-";};
  /**
   * 获取当前登录用户（通过请求报文或响应报文）
   * @return
   */
  default String getCurrentUser(RequestLogInfo requestLogInfo){ return "-";};

  /**
  /**
   * 补充自定义扩展日志信息
   * @return
   */
  default Map<String,Object> getExtLogInfo(HttpServletRequest request){
    return new HashMap<>();
  };
  /**
   * 自定义源IP获取方法
   * @return
   */
  default String getReqSourceIp(HttpServletRequest request){ return "-";};
  /**
   * 存储日志(日志需要脱敏)
   */
  default void saveLog(RequestLogInfo requestLogInfo,String dataMaskingRule){};
  /**
   * 服务运行节点标识（适应Docker环境获取标识）
   * @param request
   * @return
   */
  default String getCurrentServerNodeFlag(HttpServletRequest request){ return "-";};

  /**
   * 获取重写响应
   * @param httpDiffResponse
   * @return
   */
  default Object getOverrideHttpDiffResponse(Object httpDiffResponse){ return httpDiffResponse;};
}
