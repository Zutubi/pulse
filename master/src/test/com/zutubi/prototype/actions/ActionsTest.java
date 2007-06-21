package com.zutubi.prototype.actions;

import com.zutubi.util.bean.DefaultObjectFactory;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public class ActionsTest extends TestCase
{
    private Actions a = null;

    private static Object obj = null;

    protected void setUp() throws Exception
    {
        a = new Actions();
        a.setObjectFactory(new DefaultObjectFactory());
    }

    protected void tearDown() throws Exception
    {
        a = null;
        obj = null;
    }

    public void testDefaultActions()
    {
        List<String> actions = a.getActions(DefaultActions.class, new T());
        assertNotNull(actions);
        assertEquals(3, actions.size());
        assertTrue(actions.contains("a"));
        assertTrue(actions.contains("act"));
        assertTrue(actions.contains("action"));
    }

    public void testDefinedActions()
    {
        List<String> actions = a.getActions(DefinedActions.class, new T());
        assertNotNull(actions);
        assertEquals(1, actions.size());
        assertTrue(actions.contains("action"));
    }

    public void testExecuteAction()
    {
        assertNull(obj);
        a.execute(ExecuteActions.class, "action", new T());
        assertNotNull(obj);
    }

    public class T
    {

    }

    public static class DefaultActions
    {
        public void doA(T t)
        {

        }

        public void doAct(T t)
        {

        }

        public void doAction(T t)
        {

        }
    }

    public static class DefinedActions
    {
        public void doAct(T t)
        {

        }

        public void doAction(T t)
        {

        }

        public List<String> getActions(T t)
        {
            return Arrays.asList("action");            
        }
    }

    public static class ExecuteActions
    {
        public void doAction(T t)
        {
            obj = t;
        }
    }

    public static class InvalidActions
    {
        public void doAct()
        {

        }

        public List<String> getActions(T t)
        {
            return Arrays.asList("action");
        }
    }
}
