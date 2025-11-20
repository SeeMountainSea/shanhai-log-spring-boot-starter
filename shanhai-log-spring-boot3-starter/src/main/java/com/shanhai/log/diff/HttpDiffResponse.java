/*
 * MIT License
 *
 * Copyright (c) 2021 SeeMountainSea
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.shanhai.log.diff;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 统一响应类
 * @author Shanhai
 */
@Getter
@Setter
public class HttpDiffResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 响应码
     * @mock 200
     */
    private String code;
    /**
     * 响应提示
     * @mock success
     */
    private String message;
    /**
     * 变更前数据
     */
    @JsonIgnore
    private Object sourceData;
    /**
     * 变更后数据
     */
    @JsonIgnore
    private Object targetData;
    /**
     * 原始Data
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;

    public HttpDiffResponse() {
        this.code="200";
        this.message="success";
    }
    public static  HttpDiffResponse resp(String code, String message, Object sourceData, Object targetData){
        return resp(code,message,sourceData,targetData,null);
    }
    public static  HttpDiffResponse resp(String code, String message, Object sourceData, Object targetData,Object respData) {
        HttpDiffResponse result = new HttpDiffResponse();
        result.setCode(code);
        result.setMessage(message);
        result.setSourceData(sourceData);
        result.setTargetData(targetData);
        result.setData(respData);
        return result;
    }
}
