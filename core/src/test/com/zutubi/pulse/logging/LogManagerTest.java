package com.zutubi.pulse.logging;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;

/**
 * <class-comment/>
 */
public class LogManagerTest extends PulseTestCase
{
    public static int instanceCount = 0;

    private LogManager logManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        Map<String, HandlerFactory> factories = new HashMap<String, HandlerFactory>();
        factories.put("ConsoleHandler", new ConsoleHandlerFactory());
                
        logManager = new LogManager();
        logManager.setFactories(factories);
        instanceCount = 0;
    }

    protected void tearDown() throws Exception
    {
        logManager = null;

        java.util.logging.LogManager.getLogManager().reset();

        super.tearDown();
    }

    public void testSimpleLogLevel()
    {
        Properties props = new Properties();
        props.put("a.b.c.level", "INFO");

        logManager.configure(props);

        Logger l = Logger.getLogger("a.b.c");
        assertEquals(Level.INFO, l.getLevel());
    }

    public void testMultipleLogLevels()
    {
        Properties props = new Properties();
        props.put("a.b.level", "FINEST");
        props.put("a.b.c.level", "SEVERE");
        props.put("x.y.level", "FINER");

        logManager.configure(props);

        assertEquals(Level.FINEST, Logger.getLogger("a.b").getLevel());
        assertEquals(Level.SEVERE, Logger.getLogger("a.b.c").getLevel());
        assertEquals(Level.FINER, Logger.getLogger("x.y").getLevel());
    }

    public void testSettingLevelForRootLogger()
    {
        Properties props = new Properties();
        props.put(".level", "FINEST");

        logManager.configure(props);

        assertEquals(Level.FINEST, Logger.getLogger("").getLevel());
    }

    public void testOverridingSimpleLogLevel()
    {
        Properties props = new Properties();
        props.put("a.b.c.level", "INFO");
        logManager.configure(props);

        props.put("a.b.c.level", "FINE");
        logManager.configure(props);

        Logger l = Logger.getLogger("a.b.c");
        assertEquals(Level.FINE, l.getLevel());
    }

    public void testLogResetValuesToDefaults()
    {
        Properties props = new Properties();
        props.put("a.b.c.level", "FINE");
        logManager.configure(props);

        // need to re-add the test handler.
        logManager.reset();

        // the default level should be INFO.
        Logger l = Logger.getLogger("a.b.c");
        assertEquals(null, l.getLevel());
    }

    public void testLogConfig()
    {
        Properties props = new Properties();
        props.put("config", "com.zutubi.pulse.logging.TestConfigObject");
        assertEquals(0, instanceCount);
        logManager.configure(props);
        assertEquals(1, instanceCount);
    }

    public void testDefaultConsoleHandler()
    {
        Properties props = new Properties();
        props.put("handlers", "myConsoleHandler");
        props.put("myConsoleHandler.type", "ConsoleHandler");
        props.put("a.b.c.handler", "myConsoleHandler");
        logManager.configure(props);

        Logger l = Logger.getLogger("a.b.c");
        Handler[] handlers = l.getHandlers();
        assertNotNull(handlers);
        assertEquals(1, handlers.length);
        assertTrue(handlers[0] instanceof ConsoleHandler);
    }

    public void testConfiguringHandler()
    {
        Properties props = new Properties();
        props.put("handlers", "myConsoleHandler");
        props.put("myConsoleHandler.type", "ConsoleHandler");
        props.put("myConsoleHandler.level", "FINE");
        props.put("myConsoleHandler.formatter", "java.util.logging.XMLFormatter");
        props.put("a.b.c.handler", "myConsoleHandler");
        logManager.configure(props);

        Logger l = Logger.getLogger("a.b.c");
        Handler[] handlers = l.getHandlers();
        assertNotNull(handlers);
        assertEquals(1, handlers.length);
        assertTrue(handlers[0] instanceof ConsoleHandler);
        assertTrue(handlers[0].getFormatter() instanceof XMLFormatter);
        assertEquals(Level.FINE, handlers[0].getLevel());
    }

    public void testMultipleHandlers()
    {
        Properties props = new Properties();
        props.put("handlers", "myConsoleHandler, myOtherConsoleHandler");

        props.put("myConsoleHandler.type", "ConsoleHandler");
        props.put("myConsoleHandler.level", "SEVERE");
        props.put("a.b.c.handler", "myConsoleHandler");

        props.put("myOtherConsoleHandler.type", "ConsoleHandler");
        props.put("myOtherConsoleHandler.level", "FINE");
        props.put("x.y.z.handler", "myOtherConsoleHandler");

        logManager.configure(props);

        Logger l = Logger.getLogger("a.b.c");
        Handler[] handlers = l.getHandlers();
        assertEquals(1, handlers.length);
        assertTrue(handlers[0] instanceof ConsoleHandler);
        assertEquals(Level.SEVERE, handlers[0].getLevel());

        Logger l2 = Logger.getLogger("x.y.z");
        handlers = l2.getHandlers();
        assertEquals(1, handlers.length);
        assertTrue(handlers[0] instanceof ConsoleHandler);
        assertEquals(Level.FINE, handlers[0].getLevel());
    }

    public void testListOfHandlersForSingleNamespace()
    {
        Properties props = new Properties();
        props.put("handlers", "myConsoleHandler, myOtherConsoleHandler");

        props.put("myConsoleHandler.type", "ConsoleHandler");
        props.put("myOtherConsoleHandler.type", "ConsoleHandler");
        props.put("a.b.c.handler", "myConsoleHandler, myOtherConsoleHandler");

        Logger l = Logger.getLogger("a.b.c");
        assertEquals(0, l.getHandlers().length);

        logManager.configure(props);

        assertEquals(2, l.getHandlers().length);
    }

    public void testLogReset()
    {
        Properties props = new Properties();
        props.put("handlers", "myConsoleHandler");
        props.put("myConsoleHandler.type", "ConsoleHandler");
        props.put("a.b.c.handler", "myConsoleHandler");
        props.put("a.b.c.level", "FINE");

        logManager.configure(props);
        assertEquals(Level.FINE, Logger.getLogger("a.b.c").getLevel());

        logManager.reset();
        assertEquals(null, Logger.getLogger("a.b.c").getLevel());
        assertEquals(0, Logger.getLogger("a.b.c").getHandlers().length);
    }

    public void testResetLevels()
    {
        Properties props = new Properties();
        props.put("handlers", "myConsoleHandler");

        props.put("myConsoleHandler.type", "ConsoleHandler");
        props.put("a.b.c.handler", "myConsoleHandler");
        props.put("a.b.c.level", "FINE");

        logManager.configure(props);
        assertEquals(Level.FINE, Logger.getLogger("a.b.c").getLevel());

        logManager.resetLevels();
        assertEquals(null, Logger.getLogger("a.b.c").getLevel());
        assertEquals(1, Logger.getLogger("a.b.c").getHandlers().length);
    }
}
