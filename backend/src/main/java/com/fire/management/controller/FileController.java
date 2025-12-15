package com.fire.management.controller;

import com.fire.management.annotation.RequirePermission;
import com.fire.management.common.Result;
import com.fire.management.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传单个照片
     */
    @PostMapping("/upload")
    @RequirePermission("facility:collect")
    public Result<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileService.uploadPhoto(file);
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 批量上传照片
     */
    @PostMapping("/upload-batch")
    @RequirePermission("facility:collect")
    public Result<List<String>> uploadPhotos(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String url = fileService.uploadPhoto(file);
                urls.add(url);
            } catch (IOException e) {
                return Result.error("文件上传失败：" + e.getMessage());
            }
        }
        
        return Result.success(urls);
    }
}
