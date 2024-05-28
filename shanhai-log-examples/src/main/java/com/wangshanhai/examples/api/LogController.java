package com.wangshanhai.examples.api;

import com.shanhai.log.annotation.RequestLog;
import com.wangshanhai.examples.service.ManyParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api")
public class LogController {
    @Autowired
    private ManyParamService manyParamService;
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

    @RequestLog(module = "Order",currentUser ="#{#currentUser}", message = "分页查询订单-当前用户：#{#currentUser},当前页：#{#current}，每页条数：#{#size}")
    @PostMapping(value = "/logshow/req")
    @ResponseBody
    public String logshow(HttpServletRequest request, HttpServletResponse response, @RequestParam("currentUser") String currentUser,
                          @RequestParam("size") Long size, @RequestParam("current")Long current,@RequestBody String json){
        return "success";
    }
    @RequestLog(module = "Order", message = "分页查询订单-Body")
    @RequestMapping(value = "/body")
    @ResponseBody
    public String logshow(@RequestBody String body){
        return "success";
    }

    @RequestLog(module = "Order", message = "多入参测试")
    @RequestMapping(value = "/many")
    @ResponseBody
    public String many(@RequestBody String body){
        manyParamService.test("参数首位",body);
        return "success";
    }
}
