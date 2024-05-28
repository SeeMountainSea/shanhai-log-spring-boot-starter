package com.wangshanhai.examples.api;

import com.shanhai.log.annotation.RequestLog;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 文件测试
 */
@Controller
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    @ResponseBody
    @RequestLog(message = "简单文件上传",fileUpload = true)
    public String upload(HttpServletRequest request, @RequestParam(value = "file", required = true) MultipartFile [] file, String f1) {
        return f1;
    }

    @RequestMapping("/download")
    @RequestLog(message = "简单文件下载",fileDownload = true)
    public ResponseEntity download(String f1) throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=\"" +f1  + ".tnt\"")
                .body(new InputStreamResource(Files.newInputStream(new File("d:/xxx.zip").toPath())));
    }
}
