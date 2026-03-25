package com.yzj.alzassistant.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * 从 http(s) URL 下载附件字节，带大小上限（图片、文档解析共用）。
 */
@Slf4j
public final class AttachmentUrlDownloader {

    private AttachmentUrlDownloader() {
    }

    /**
     * @param url      完整 URL
     * @param maxBytes 最大字节数（含），超过则抛 IllegalArgumentException
     */
    public static byte[] download(String url, int maxBytes) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL 为空");
        }
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            throw new IllegalArgumentException("仅支持 http/https URL");
        }
        try {
            URI uri = URI.create(trimmed);
            try (InputStream in = uri.toURL().openStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int n;
                long total = 0;
                while ((n = in.read(buffer)) != -1) {
                    total += n;
                    if (total > maxBytes) {
                        throw new IllegalArgumentException("下载内容超过大小上限（" + maxBytes + " 字节）");
                    }
                    out.write(buffer, 0, n);
                }
                return out.toByteArray();
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("从 URL 下载失败: {}", trimmed, e);
            throw new RuntimeException("下载失败: " + e.getMessage(), e);
        }
    }
}
