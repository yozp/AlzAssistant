package com.yzj.alzassistant.ai.tools;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 联网搜索工具类
 */
@Component
@Slf4j
public class WebSearchTool {

    // Google serpApi 的搜索接口地址
    private static final String SEARCH_API_URL = "https://serpapi.com/search.json";

    @Value("${serp-api.api-key}")
    private String serpApiKey;

    /**
     * 执行 Google 网络搜索
     *
     * @param searchQuery 搜索内容
     * @return 搜索结果摘要列表
     */
    @Tool("使用 SerpApi 提供的 Google 搜索功能进行网络搜索")
    public String googleSearch(@P("搜索内容") String searchQuery) {
        log.info("调用 SerpApi Google 搜索关键词：{}", searchQuery);

        try {
            // 1. 构建请求 URL（使用 GET 查询参数）
            String url = SEARCH_API_URL + "?q=" + java.net.URLEncoder.encode(searchQuery, "UTF-8")
                    + "&engine=google"
                    + "&api_key=" + serpApiKey;

            // 2. 发送 GET 请求
            HttpResponse response = HttpRequest.get(url).execute();

            // 3. 获取响应状态码和内容
            int status = response.getStatus();
            String body = response.body();

            if (status == 200 && ObjectUtil.isNotEmpty(body)) {
                JSONObject jsonResponse = JSONUtil.parseObj(body);

                // 获取 organic_results（谷歌自然搜索结果）
                JSONArray resultsArray = jsonResponse.getJSONArray("organic_results");

                if (resultsArray != null && !resultsArray.isEmpty()) {
                    StringBuilder resultBuilder = new StringBuilder();

                    List<JSONObject> results = resultsArray.toList(JSONObject.class);
                    int index = 1;

                    for (JSONObject result : results) {
                        String title = result.getStr("title");
                        String link = result.getStr("link");
                        String snippet = result.getStr("snippet"); // 可能为空

                        resultBuilder.append("【结果 ").append(index++).append("】\n");
                        resultBuilder.append("标题: ").append(title).append("\n");
                        resultBuilder.append("链接: ").append(link).append("\n");
                        resultBuilder.append("摘要: ").append(ObjectUtil.defaultIfNull(snippet, "无摘要信息")).append("\n\n");
                    }

                    return resultBuilder.toString();
                } else {
                    return "未找到相关结果";
                }
            } else {
                log.error("请求失败，状态码：{}，响应内容：{}", status, body);
                return "请求失败或无返回内容";
            }
        } catch (Exception e) {
            log.error("调用 SerpApi Google 搜索服务时发生错误", e);
            throw new RuntimeException("调用 SerpApi Google 搜索请求出现错误", e);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------

//    // SearchAPI 的搜索接口地址
//    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";
//
//    @Value("${search-api.api-key}")
//    private String searchApiKey;
//
//    @Tool("联网搜索")
//    public String searchWeb(
//            @P("联网搜索的查询关键词") String query) {
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("q", query);
//        paramMap.put("api_key", searchApiKey);
//        paramMap.put("engine", "baidu");
//        try {
//            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
//            // 取出返回结果的前 5 条
//            JSONObject jsonObject = JSONUtil.parseObj(response);
//            // 提取 organic_results 部分
//            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
//            List<Object> objects = organicResults.subList(0, 5);
//            // 拼接搜索结果为字符串
//            String result = objects.stream().map(obj -> {
//                JSONObject tmpJSONObject = (JSONObject) obj;
//                return tmpJSONObject.toString();
//            }).collect(Collectors.joining(","));
//            return result;
//        } catch (Exception e) {
//            return "联网搜索出错：" + e.getMessage();
//        }
//    }
}