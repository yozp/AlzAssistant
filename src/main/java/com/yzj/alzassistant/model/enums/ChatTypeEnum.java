package com.yzj.alzassistant.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 对话类型枚举类
 */
@Getter
public enum ChatTypeEnum {

    CHAT_TYPE_ENUM("普通对话模式", "chat");

    private final String text;
    private final String value;

    ChatTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static ChatTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ChatTypeEnum anEnum : ChatTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
