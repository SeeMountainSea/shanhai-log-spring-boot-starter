package com.wangshanhai.examples.api;

import com.shanhai.log.annotation.RequestLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class LogController {
    /**
     * 文件上传测试样例
     * @return
     */
    @RequestLog(module = "Order",currentUser ="#{#currentUser}", message = "分页查询订单-当前用户：#{#currentUser},当前页：#{#current}，每页条数：#{#size}")
    @RequestMapping(value = "/logshow")
    @ResponseBody
    public String logshow(@RequestParam("currentUser") String currentUser, @RequestParam("size") Long size, @RequestParam("current")Long current){
       return "success";
    }
    @RequestLog(module = "Order", message = "分页查询订单-Body")
    @RequestMapping(value = "/body")
    @ResponseBody
    public String logshow(@RequestBody String body){
        return "success";
    }
}
