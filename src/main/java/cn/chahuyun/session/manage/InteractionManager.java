package cn.chahuyun.session.manage;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.exception.InteractionTimeoutException;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Routes interactive replies through the plugin's single message listener. */
public final class InteractionManager {

    private static final Map<InteractionKey, CompletableFuture<MessageEvent>> PENDING = new ConcurrentHashMap<>();

    private InteractionManager() {
    }

    public static MessageEvent awaitNext(User user) {
        InteractionKey key = InteractionKey.from(user);
        CompletableFuture<MessageEvent> future = new CompletableFuture<>();
        if (PENDING.size() >= Math.max(1L, HuYanSession.CONFIG.getRuntimeCacheMaximumSize())) {
            throw new IllegalStateException("当前进行中的交互过多，请稍后重试");
        }
        if (PENDING.putIfAbsent(key, future) != null) {
            throw new IllegalStateException("该用户已有一个进行中的交互");
        }

        try {
            return future.get(Math.max(1L, HuYanSession.CONFIG.getInteractionTimeoutSeconds()), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            user.sendMessage("操作已超时，请重新发起指令。");
            throw new InteractionTimeoutException("等待用户下一条消息超时", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InteractionTimeoutException("交互被中断", e);
        } catch (ExecutionException e) {
            throw new InteractionTimeoutException("交互已取消", e.getCause());
        } finally {
            PENDING.remove(key, future);
        }
    }

    public static boolean accept(MessageEvent event) {
        CompletableFuture<MessageEvent> future = PENDING.get(InteractionKey.from(event));
        return future != null && future.complete(event);
    }

    public static void shutdown() {
        InteractionTimeoutException cause = new InteractionTimeoutException("插件正在卸载");
        PENDING.values().forEach(future -> future.completeExceptionally(cause));
        PENDING.clear();
    }

    static int pendingCount() {
        return PENDING.size();
    }

    private static final class InteractionKey {
        private final long botId;
        private final long subjectId;
        private final long userId;

        private InteractionKey(long botId, long subjectId, long userId) {
            this.botId = botId;
            this.subjectId = subjectId;
            this.userId = userId;
        }

        static InteractionKey from(User user) {
            long subjectId = user instanceof Member ? ((Member) user).getGroup().getId() : user.getId();
            return new InteractionKey(user.getBot().getId(), subjectId, user.getId());
        }

        static InteractionKey from(MessageEvent event) {
            return new InteractionKey(event.getBot().getId(), event.getSubject().getId(), event.getSender().getId());
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof InteractionKey)) return false;
            InteractionKey that = (InteractionKey) object;
            return botId == that.botId && subjectId == that.subjectId && userId == that.userId;
        }

        @Override
        public int hashCode() {
            int result = Long.hashCode(botId);
            result = 31 * result + Long.hashCode(subjectId);
            result = 31 * result + Long.hashCode(userId);
            return result;
        }
    }
}
