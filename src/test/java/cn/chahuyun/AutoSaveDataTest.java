package cn.chahuyun;


import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
import org.junit.Before;
import org.junit.Test;


public class AutoSaveDataTest {

    public static class MockPlugin extends JavaPlugin {
        public static final MockPlugin INSTANCE = new MockPlugin();
        public MockPlugin() {
            super(new JvmPluginDescriptionBuilder("org.test.test", "1.0.0").build());
        }
    }

    @Before
    public void beforeTest() {
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());
//        PluginManager.INSTANCE.loadPlugin(MockPlugin.INSTANCE);
        PluginManager.INSTANCE.enablePlugin(MockPlugin.INSTANCE);
    }


    @Test
    public void testData() {

//


//        assertEquals("乌拉",SessionData.INSTANCE.getSessionMap().get("乌拉"));
    }
}
