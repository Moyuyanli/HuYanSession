package cn.chahuyun.session.manage;

import cn.chahuyun.session.data.ApplyClusterInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JoinRequestManagerTest {

    @AfterEach
    void tearDown() {
        PluginRuntime.stop();
    }

    @Test
    void removingPendingRequestCancelsExpirationAndClearsAllIndexes() throws Exception {
        PluginRuntime.start();
        ApplyClusterInfo info = new ApplyClusterInfo();
        String memberKey = "10001.20002";
        int doorNumber = 7;

        Class<?> pendingType = Class.forName(JoinRequestManager.class.getName() + "$PendingRequest");
        Constructor<?> constructor = pendingType.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object pending = constructor.newInstance(doorNumber, memberKey, null, info);

        ScheduledFuture<?> expiration = PluginRuntime.schedule(() -> { }, 30, TimeUnit.MINUTES);
        Field expirationField = pendingType.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(pending, expiration);

        Map<Integer, Object> requests = mapField("REQUESTS");
        Map<String, Integer> requestByMember = mapField("REQUEST_BY_MEMBER");
        Map<String, ApplyClusterInfo> infoByMember = mapField("INFO_BY_MEMBER");
        requests.put(doorNumber, pending);
        requestByMember.put(memberKey, doorNumber);
        infoByMember.put(memberKey, info);

        Method removePending = JoinRequestManager.class.getDeclaredMethod(
                "removePending", int.class, pendingType, boolean.class
        );
        removePending.setAccessible(true);
        removePending.invoke(null, doorNumber, pending, true);

        assertTrue(expiration.isCancelled());
        assertTrue(requests.isEmpty());
        assertTrue(requestByMember.isEmpty());
        assertTrue(infoByMember.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> mapField(String name) throws Exception {
        Field field = JoinRequestManager.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<K, V>) field.get(null);
    }
}
