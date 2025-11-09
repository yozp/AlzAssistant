package com.yzj.alzassistant.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yzj.alzassistant.model.enums.ChatTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 对话结果保存类
 */
public class ChatFileSaver {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/chat_output";

    /**
     * 保存聊天结果
     */
    public static File saveChatResult(String chatType, String chatResult) {
        String baseDirPath = buildUniqueDir(ChatTypeEnum.CHAT_TYPE_ENUM.getValue());
        writeToFile(baseDirPath, "chat_result.txt", chatResult);
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径：tmp/chat_output/bizType_雪花ID
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    private static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
