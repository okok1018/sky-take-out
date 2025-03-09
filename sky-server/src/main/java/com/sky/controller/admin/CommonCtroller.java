package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.MinioOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonCtroller {
    @Autowired
    private MinioOssUtil minioOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("文件上传");
        try {
            byte[] bytes = file.getBytes();
            String filepath = minioOssUtil.upload(bytes, file.getOriginalFilename());
            return Result.success(filepath);
        } catch (Exception e) {
            log.error("文件上传失败"+e.getMessage());
            return Result.error("文件上传失败: " + e.getMessage());
        }

    }


}
