package com.sky.utils;

import com.sky.properties.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Data
@Slf4j
public class MinioOssUtil {

    private  MinioProperties minioProperties;

    @Autowired
    public MinioOssUtil(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    /**
     * 将文件上传到minio服务器，并根据文件名返回一个访问地址，返回给前端
     * @param bytes
     * @param objectName
     * @return
     * @throws Exception
     */
    public String upload(byte[] bytes, String objectName) throws Exception {
        // 格式化文件名
        String formattedObjectName = formatObjectName(objectName);
        validateInput(bytes, objectName);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            // 创建MinioClient实例
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKeyId(), minioProperties.getAccessKeySecret())
                    .build();

            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(byteArrayInputStream, bytes.length, -1)
                    .contentType("image/png")
                    .build();

            minioClient.putObject(objectArgs);

            return buildUrl(objectName);

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("认证或加密相关错误", e);
        } catch (InsufficientDataException | ErrorResponseException | IOException |
                 InternalException | InvalidResponseException | XmlParserException e) {
            throw new RuntimeException("文件上传过程中发生错误", e);
        } catch (ServerException e) {
            throw new RuntimeException("MinIO服务器返回了错误", e);
        }
    }

    /**
     * 对参数进行检测，并不符合格式要求的进行相应的报错信息显示
     * @param imageData
     * @param objectName
     */
    private void validateInput(byte[] imageData, String objectName) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("图像数据不能为空");
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        if (minioProperties.getEndpoint() == null || minioProperties.getEndpoint().trim().isEmpty() ||
                minioProperties.getAccessKeyId() == null || minioProperties.getAccessKeyId().trim().isEmpty() ||
                minioProperties.getAccessKeySecret() == null || minioProperties.getAccessKeySecret().trim().isEmpty() ||
                minioProperties.getBucketName() == null || minioProperties.getBucketName().trim().isEmpty()) {
            throw new IllegalStateException("MinIO配置不完整");
        }
    }

    /**
     * 传入原始文件名，自动配合UUID生成前缀，并且对后缀进行拼接，格式化文件名
     * @param originalFileName
     * @return
     */
    private String formatObjectName(String originalFileName) {
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i); // 包括点号
        }

        // 使用UUID生成唯一前缀
        String uuidPrefix = UUID.randomUUID().toString();

        // 返回拼接后的文件名
        return uuidPrefix + extension;
    }

    /**
     * 生成一个符合minio标准的访问地址
     * @param objectName
     * @return
     */
    private String buildUrl(String objectName) {
        return String.format("%s/%s/%s", minioProperties.getEndpoint(), minioProperties.getBucketName(), objectName);
    }


}
