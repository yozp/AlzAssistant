package com.yzj.alzassistant.core.chat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 管理进行中的流式对话会话（按 userId + appId 维度）。
 */
@Component
@Slf4j
public class ActiveChatSessionRegistry {

    // 存储进行中的流式对话会话（按 userId + appId 维度）
    private final ConcurrentMap<String, ActiveChatSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 注册新的流式对话会话。
     * 如果 userId + appId 已有会话，则请求停止旧会话并返回新会话。
     */
    public ActiveChatSession register(Long userId, Long appId) {
        String key = buildKey(userId, appId);
        ActiveChatSession newSession = new ActiveChatSession(userId, appId, key);
        ActiveChatSession oldSession = sessionMap.put(key, newSession);
        if (oldSession != null) {
            oldSession.requestStop("新请求覆盖旧请求");
        }
        return newSession;
    }

    /**
     * 停止指定应用下当前用户的流式对话。
     */
    public boolean stop(Long userId, Long appId, String reason) {
        String key = buildKey(userId, appId);
        ActiveChatSession session = sessionMap.get(key);
        if (session == null) {
            return false;
        }
        session.requestStop(reason);
        return true;
    }

    /**
     * 移除指定会话（无论成功/失败/取消）。
     */
    public void removeIfSame(ActiveChatSession session) {
        if (session == null) {
            return;
        }
        sessionMap.remove(session.getSessionKey(), session);
    }

    /**
     * 构建会话键。
     */
    private String buildKey(Long userId, Long appId) {
        return userId + ":" + appId;
    }

    /**
     * 进行中的流式对话会话。
     */
    @Getter
    public static class ActiveChatSession {
        private final Long userId;
        private final Long appId;
        private final String sessionKey;
        private final AtomicBoolean stopRequested = new AtomicBoolean(false);
        private final AtomicBoolean persisted = new AtomicBoolean(false);
        private final StringBuilder partialResponseBuilder = new StringBuilder();
        private final Sinks.Empty<Void> stopSink = Sinks.empty();

        ActiveChatSession(Long userId, Long appId, String sessionKey) {
            this.userId = userId;
            this.appId = appId;
            this.sessionKey = sessionKey;
        }

        public Mono<Void> stopSignal() {
            return stopSink.asMono();
        }

        public boolean isStopRequested() {
            return stopRequested.get();
        }

        public void appendPartial(String text) {
            if (text == null || text.isEmpty()) {
                return;
            }
            synchronized (partialResponseBuilder) {
                partialResponseBuilder.append(text);
            }
        }

        public String getPartialResponse() {
            synchronized (partialResponseBuilder) {
                return partialResponseBuilder.toString();
            }
        }

        public void requestStop(String reason) {
            if (!stopRequested.compareAndSet(false, true)) {
                return;
            }
            stopSink.tryEmitEmpty();
            log.info("请求停止流式对话，sessionKey={}, reason={}", sessionKey, reason);
        }

        public boolean markPersisted() {
            return persisted.compareAndSet(false, true);
        }
    }
}
