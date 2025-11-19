package com.yzj.alzassistant.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间信息工具类
 * 提供获取当前时间、日期等时间相关信息的功能
 */
@Component
@Slf4j
public class TimeInfoTool {

    /**
     * 获取当前时间信息
     *
     * @return 格式化的当前时间信息，包含日期、时间、星期等
     */
    @Tool("获取当前准确的日期和时间信息，包括年月日、时分秒、星期等")
    public String getCurrentTime() {
        log.info("调用获取当前时间工具");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 格式化日期时间
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            // 获取星期几
            String dayOfWeek = getDayOfWeekChinese(now.getDayOfWeek().getValue());
            
            // 构建返回信息
            StringBuilder result = new StringBuilder();
            result.append("📅 当前时间信息\n\n");
            result.append("📆 日期：").append(now.format(dateFormatter)).append("\n");
            result.append("⏰ 时间：").append(now.format(timeFormatter)).append("\n");
            result.append("🗓️ 星期：").append(dayOfWeek).append("\n");
            result.append("📝 完整格式：").append(now.format(fullFormatter)).append("\n");
            result.append("🎯 年份：").append(now.getYear()).append("年\n");
            result.append("🎯 月份：").append(now.getMonthValue()).append("月\n");
            result.append("🎯 日期：").append(now.getDayOfMonth()).append("日");
            
            return result.toString();
        } catch (Exception e) {
            log.error("获取当前时间失败", e);
            return "获取时间信息失败：" + e.getMessage();
        }
    }
    
    /**
     * 获取简洁的当前日期时间
     *
     * @return 格式化的当前日期时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    @Tool("获取简洁格式的当前日期时间，格式为：yyyy-MM-dd HH:mm:ss")
    public String getCurrentDateTime() {
        log.info("调用获取简洁格式当前时间工具");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return now.format(formatter);
        } catch (Exception e) {
            log.error("获取当前时间失败", e);
            return "获取时间失败：" + e.getMessage();
        }
    }
    
    /**
     * 获取今天是星期几
     *
     * @return 星期几的中文描述
     */
    @Tool("获取今天是星期几")
    public String getTodayDayOfWeek() {
        log.info("调用获取星期几工具");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            String dayOfWeek = getDayOfWeekChinese(now.getDayOfWeek().getValue());
            return "今天是" + dayOfWeek;
        } catch (Exception e) {
            log.error("获取星期几失败", e);
            return "获取星期失败：" + e.getMessage();
        }
    }
    
    /**
     * 将数字转换为中文星期
     *
     * @param dayOfWeek 星期数字（1-7）
     * @return 中文星期描述
     */
    private String getDayOfWeekChinese(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "星期一";
            case 2 -> "星期二";
            case 3 -> "星期三";
            case 4 -> "星期四";
            case 5 -> "星期五";
            case 6 -> "星期六";
            case 7 -> "星期日";
            default -> "未知";
        };
    }
}