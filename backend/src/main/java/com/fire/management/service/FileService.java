package com.fire.management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件服务
 */
@Service
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-url}")
    private String accessUrl;

    /**
     * 上传照片（自动压缩到1080P）
     */
    public String uploadPhoto(MultipartFile file) throws IOException {
        // 验证文件
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片文件");
        }

        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new RuntimeException("无法读取图片文件");
        }

        // 压缩图片到1080P
        BufferedImage compressedImage = compressImage(originalImage, 1920, 1080);

        // 保存压缩后的图片
        File destFile = new File(uploadPath + filename);
        String formatName = extension.substring(1).toLowerCase();
        if (formatName.equals("jpg")) {
            formatName = "jpeg";
        }
        ImageIO.write(compressedImage, formatName, destFile);

        // 返回访问URL
        return accessUrl + filename;
    }

    /**
     * 压缩图片到指定尺寸
     */
    private BufferedImage compressImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // 如果图片尺寸小于目标尺寸，不压缩
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return original;
        }

        // 计算缩放比例
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        // 计算新尺寸
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // 创建压缩后的图片
        BufferedImage compressed = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = compressed.createGraphics();
        
        // 设置高质量渲染
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return compressed;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(accessUrl)) {
            return;
        }

        String filename = fileUrl.substring(accessUrl.length());
        Path filePath = Paths.get(uploadPath + filename);
        
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 忽略删除失败
        }
    }
}
