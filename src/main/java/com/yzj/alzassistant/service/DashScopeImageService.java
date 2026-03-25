package com.yzj.alzassistant.service;

/**
 * DashScope 多模态图片理解（qwen-vl），供图片识别工具调用。
 */
public interface DashScopeImageService {

    /**
     * 理解图片内容（OCR、物体与场景描述等），返回中文描述。
     *
     * @param imageData 图片字节
     * @return 描述文本
     */
    String understandImage(byte[] imageData);
}
