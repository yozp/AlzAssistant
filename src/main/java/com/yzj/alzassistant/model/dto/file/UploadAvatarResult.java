package com.yzj.alzassistant.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传头像结果类
 */
@Data
public class UploadAvatarResult implements Serializable {

    private String url;

    private String picName;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private static final long serialVersionUID = 1L;
}

