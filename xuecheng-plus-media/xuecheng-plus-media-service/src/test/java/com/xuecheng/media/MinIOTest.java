package com.xuecheng.media;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/2 21:10
 * @Description minio上传测试
 */
public class MinIOTest {
    // 创建MinioClient对象
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.72.132:9000")
                    .credentials("lVlBNX0E14ytCsU1U6y1", "ZUVnkbAygsVP43lmCEhGs1D5ZF8R8OW8bTUrVSqC")
                    .build();

    /**
     * 上传测试方法
     */
    @Test
    public void uploadTest() {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            .object("2024/pic01.jpg")    // 同一个桶内对象名不能重复
                            .filename("C:\\Users\\刘\\Pictures\\Saved Pictures\\pic01.jpg")
                            .build()
            );
            System.out.println("上传成功");
        } catch (Exception e) {
            System.out.println("上传失败");
        }
    }

    @Test
    public void deleteTest() {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket("testbucket")
                    .object("/2024/pic01.png")
                    .build());
            System.out.println("删除成功");
        } catch (Exception e) {
            System.out.println("删除失败");
        }
    }

    @Test
    public void getFileTest() {
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket("testbucket")
                    .object("/2024/pic01.jpg")
                    .build());
            FileOutputStream fileOutputStream = new FileOutputStream("D:\\Java_Project\\tmp.jpg");
            // byte[] buffer = new byte[1024];
            // int len;
            // while ((len = inputStream.read(buffer)) != -1) {
            //     fileOutputStream.write(buffer,0,len);
            // }
            // inputStream.close();
            // fileOutputStream.close();
            IOUtils.copy(inputStream, fileOutputStream);
            System.out.println("下载成功");
        } catch (Exception e) {
            System.out.println("下载失败");
        }
    }

    @Test
    void test() {
        String s = "abc";
        String substring = s.substring(s.lastIndexOf("."));
        System.out.println(substring);
    }
}
