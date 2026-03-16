package com.yzj.alzassistant.ai.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yzj.alzassistant.context.UserLocationContext;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 高德地图搜索工具，用于搜索附近医院和地理编码。
 */
@Component
@Slf4j
public class MapSearchTool {

    // 高德地图 API URL
    private static final String AROUND_SEARCH_URL = "https://restapi.amap.com/v3/place/around";
    // 高德地图 地理编码 API URL
    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";
    // 高德地图 IP 定位 API URL
    private static final String IP_LOCATION_URL = "https://restapi.amap.com/v3/ip";
    // 高德地图POI类型
    private static final String POI_TYPE_HOSPITAL = "090100";

    @Value("${amap.api-key:}")
    private String amapApiKey;

    @Tool("根据城市或地址搜索附近的医院。返回医院名称、地址、电话、距离等信息。如果用户没有提供位置，可先调用 geocode 获取坐标，或传入城市名由系统自动定位。")
    public String searchNearbyHospitals(
            @P("搜索中心点坐标，格式为 '经度,纬度'，例如 '116.397428,39.90923'。如果没有坐标可传入空字符串，系统将尝试根据城市定位") String location,
            @P("城市名称，例如 '北京' 或 '广州'，用于辅助定位和搜索范围限制") String city,
            @P("搜索半径，单位米，默认5000，最大50000") String radius) {
        log.info("调用地图搜索工具：location={}, city={}, radius={}", location, city, radius);

        if (StrUtil.isBlank(amapApiKey) || "你的高德地图API_Key".equals(amapApiKey)) {
            return "【地图搜索工具暂不可用】高德地图 API Key 未配置。请在 application-local.yml 中配置 amap.api-key。\n" +
                    "你可以建议用户自行搜索附近医院，推荐就近前往三甲医院的神经内科就诊。";
        }

        try {
            if (StrUtil.isBlank(location) && StrUtil.isNotBlank(city)) {
                location = geocodeCity(city);
            }
            if (StrUtil.isBlank(location)) {
                location = getLocationByIp();
            }
            if (StrUtil.isBlank(location)) {
                return "无法获取位置信息，请提供具体的城市名称或地址";
            }

            String searchRadius = StrUtil.isBlank(radius) ? "5000" : radius;

            String url = AROUND_SEARCH_URL
                    + "?key=" + amapApiKey
                    + "&location=" + location
                    + "&types=" + POI_TYPE_HOSPITAL
                    + "&radius=" + searchRadius
                    + "&offset=10"
                    + "&page=1"
                    + "&extensions=all";
            if (StrUtil.isNotBlank(city)) {
                url += "&city=" + java.net.URLEncoder.encode(city, "UTF-8");
            }

            HttpResponse response = HttpRequest.get(url).timeout(10000).execute();
            String body = response.body();
            JSONObject json = JSONUtil.parseObj(body);

            if (!"1".equals(json.getStr("status"))) {
                log.error("高德地图 API 调用失败: {}", json.getStr("info"));
                return "地图搜索失败: " + json.getStr("info");
            }

            JSONArray pois = json.getJSONArray("pois");
            if (pois == null || pois.isEmpty()) {
                return "在指定范围内未找到医院，建议扩大搜索范围或换个城市搜索";
            }

            StringBuilder result = new StringBuilder();
            result.append("附近医院搜索结果（共 ").append(pois.size()).append(" 家）：\n\n");

            for (int i = 0; i < pois.size(); i++) {
                JSONObject poi = pois.getJSONObject(i);
                result.append("【").append(i + 1).append("】").append(poi.getStr("name")).append("\n");
                result.append("  地址: ").append(poi.getStr("address", "暂无")).append("\n");
                result.append("  电话: ").append(poi.getStr("tel", "暂无")).append("\n");
                result.append("  距离: ").append(poi.getStr("distance", "未知")).append("米\n");
                String type = poi.getStr("type", "");
                if (type.contains("三级") || type.contains("三甲")) {
                    result.append("  等级: 三甲医院\n");
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            log.error("地图搜索工具执行异常", e);
            return "地图搜索出错: " + e.getMessage();
        }
    }

    @Tool("获取当前用户已授权的实时位置，返回经度,纬度（高德坐标系）。若用户未授权或未提供则返回说明文字，此时可改用 geocode 或城市名进行搜索。")
    public String getUserLocation() {
        log.info("调用获取用户位置工具");
        String lngLat = UserLocationContext.get();
        if (StrUtil.isNotBlank(lngLat)) {
            return lngLat;
        }
        return "用户未提供实时位置，请使用城市名或地址进行搜索，或让用户授权位置后再试。";
    }

    @Tool("将地址或地名转换为经纬度坐标（地理编码），返回格式为 '经度,纬度'")
    public String geocode(
            @P("需要转换的地址或地名，例如 '北京市朝阳区' 或 '广州天河区'") String address) {
        log.info("调用地理编码工具：address={}", address);

        if (StrUtil.isBlank(amapApiKey) || "你的高德地图API_Key".equals(amapApiKey)) {
            return "地理编码工具暂不可用：高德地图 API Key 未配置";
        }

        try {
            String url = GEOCODE_URL
                    + "?key=" + amapApiKey
                    + "&address=" + java.net.URLEncoder.encode(address, "UTF-8");

            HttpResponse response = HttpRequest.get(url).timeout(10000).execute();
            JSONObject json = JSONUtil.parseObj(response.body());

            if (!"1".equals(json.getStr("status"))) {
                return "地理编码失败: " + json.getStr("info");
            }

            JSONArray geocodes = json.getJSONArray("geocodes");
            if (geocodes == null || geocodes.isEmpty()) {
                return "无法解析地址: " + address;
            }

            JSONObject first = geocodes.getJSONObject(0);
            return first.getStr("location");
        } catch (Exception e) {
            log.error("地理编码工具执行异常", e);
            return "地理编码出错: " + e.getMessage();
        }
    }

    private String geocodeCity(String city) {
        try {
            String url = GEOCODE_URL
                    + "?key=" + amapApiKey
                    + "&address=" + java.net.URLEncoder.encode(city, "UTF-8");
            HttpResponse response = HttpRequest.get(url).timeout(10000).execute();
            JSONObject json = JSONUtil.parseObj(response.body());
            if ("1".equals(json.getStr("status"))) {
                JSONArray geocodes = json.getJSONArray("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    return geocodes.getJSONObject(0).getStr("location");
                }
            }
        } catch (Exception e) {
            log.warn("城市地理编码失败: {}", city, e);
        }
        return null;
    }

    private String getLocationByIp() {
        try {
            String url = IP_LOCATION_URL + "?key=" + amapApiKey;
            HttpResponse response = HttpRequest.get(url).timeout(5000).execute();
            JSONObject json = JSONUtil.parseObj(response.body());
            if ("1".equals(json.getStr("status"))) {
                String rectangle = json.getStr("rectangle");
                if (StrUtil.isNotBlank(rectangle)) {
                    String[] parts = rectangle.split(";");
                    if (parts.length == 2) {
                        String[] p1 = parts[0].split(",");
                        String[] p2 = parts[1].split(",");
                        double lng = (Double.parseDouble(p1[0]) + Double.parseDouble(p2[0])) / 2;
                        double lat = (Double.parseDouble(p1[1]) + Double.parseDouble(p2[1])) / 2;
                        return lng + "," + lat;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("IP定位失败", e);
        }
        return null;
    }
}
