package com.yzj.alzassistant.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 高德坐标转换：将 WGS84（浏览器定位）转为 GCJ-02（高德坐标系），供周边搜索等接口使用。
 */
@Service
@Slf4j
public class AmapCoordinateService {

    private static final String CONVERT_URL = "https://restapi.amap.com/v3/assistant/coordinate/convert";

    @Value("${amap.api-key:}")
    private String amapApiKey;

    /**
     * 将 WGS84 坐标（经度,纬度）转为高德 GCJ-02（经度,纬度）。
     * 若 key 未配置或转换失败，返回原坐标。
     */
    public String wgs84ToGcj02(String lngLatWgs84) {
        if (StrUtil.isBlank(lngLatWgs84) || StrUtil.isBlank(amapApiKey) || "你的高德地图API_Key".equals(amapApiKey)) {
            return lngLatWgs84;
        }
        String trimmed = lngLatWgs84.trim();
        if (!trimmed.matches("^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?$")) {
            log.warn("用户位置格式无效，无法转换: {}", lngLatWgs84);
            return lngLatWgs84;
        }
        try {
            String url = CONVERT_URL
                    + "?key=" + amapApiKey
                    + "&locations=" + java.net.URLEncoder.encode(trimmed, "UTF-8")
                    + "&coordsys=gps";
            HttpResponse response = HttpRequest.get(url).timeout(5000).execute();
            var json = JSONUtil.parseObj(response.body());
            if ("1".equals(json.getStr("status"))) {
                String locations = json.getStr("locations");
                if (StrUtil.isNotBlank(locations)) {
                    return locations.trim();
                }
            }
            log.warn("高德坐标转换失败，使用原坐标: status={}, info={}", json.getStr("status"), json.getStr("info"));
        } catch (Exception e) {
            log.warn("高德坐标转换异常，使用原坐标: {}", e.getMessage());
        }
        return lngLatWgs84;
    }
}
