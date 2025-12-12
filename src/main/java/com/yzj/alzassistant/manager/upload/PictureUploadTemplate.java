package com.yzj.alzassistant.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.date.DateUtil;
import com.yzj.alzassistant.config.CosClientConfig;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.manager.CosManager;
import com.yzj.alzassistant.model.dto.file.UploadAvatarResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

/**
 * 图片上传模板类
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    public final UploadAvatarResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        validPicture(inputSource);

        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);

        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            file = File.createTempFile("avatar_", null);
            processFile(inputSource, file);

            cosManager.putObject(uploadPath, file);

            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法读取图片信息");
            }

            return buildResult(originFilename, file, uploadPath, image);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    protected abstract void validPicture(Object inputSource);

    protected abstract String getOriginFilename(Object inputSource);

    protected abstract void processFile(Object inputSource, File file) throws Exception;

    private UploadAvatarResult buildResult(String originFilename, File file, String uploadPath, BufferedImage image) {
        UploadAvatarResult result = new UploadAvatarResult();
        int picWidth = image.getWidth();
        int picHeight = image.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        result.setUrl(cosClientConfig.getHost() + uploadPath);
        result.setPicName(FileUtil.mainName(originFilename));
        result.setPicSize(FileUtil.size(file));
        result.setPicWidth(picWidth);
        result.setPicHeight(picHeight);
        result.setPicScale(picScale);
        result.setPicFormat(FileUtil.getSuffix(originFilename));
        return result;
    }

    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("临时文件删除失败: {}", file.getAbsolutePath());
        }
    }
}

