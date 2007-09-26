package com.zutubi.prototype.actions;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.DefaultObjectFactory;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 */
public class ConfigurationActionsTest extends TestCase
{
    private ActionManager a = null;

    private static Object obj = null;
    private DefaultObjectFactory objectFactory;

    protected void setUp() throws Exception
    {
        a = new ActionManager();
        objectFactory = new DefaultObjectFactory();
        a.setObjectFactory(objectFactory);
    }

    protected void tearDown() throws Exception
    {
        a = null;
        obj = null;
    }

    public void testDefaultActions() throws Exception
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, DefaultActions.class, objectFactory);
        assertActions(ca.getActions(new T()), "a", "act", "action", "noParamAction");
    }

    public void testParameterIsExtensionOfActionParam() throws Exception
    {
        ConfigurationActions ca = new ConfigurationActions(U.class, DefaultActions.class, objectFactory);
        assertActions(ca.getActions(new U()), "a", "act", "action", "noParamAction");
    }

    public void testDefaultActionsWithParams() throws Exception
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, Params.class, objectFactory);
        assertActions(ca.getActions(new T()), "configParam");
    }

    public void testDefinedActions() throws Exception
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, DefinedActions.class, objectFactory);
        assertActions(ca.getActions(new T()), "action");
    }

    public void testExecuteAction() throws Exception
    {
        assertNull(obj);
        ConfigurationActions ca = new ConfigurationActions(T.class, ExecuteActions.class, objectFactory);
        ca.execute("action", new T(), null);
        assertNotNull(obj);
    }

    public void testExecuteActionWithArg() throws Exception
    {
        assertNull(obj);
        ConfigurationActions ca = new ConfigurationActions(T.class, ExecuteActions.class, objectFactory);
        ConfigType arg = new ConfigType();
        ca.execute("actionWithArg", new T(), arg);
        assertSame(arg, obj);
    }

    private void assertActions(List<ConfigurationAction> got, String... expected)
    {
        assertEquals(expected.length, got.size());
        List<String> gotNames = CollectionUtils.map(got, new Mapping<ConfigurationAction, String>()
        {
            public String map(ConfigurationAction configurationAction)
            {
                return configurationAction.getName();
            }
        });

        Collections.sort(gotNames, new Sort.StringComparator());
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], gotNames.get(i));
        }
    }

    public static class T extends AbstractConfiguration
    {
    }

    public static class U extends T
    {
    }

    public static class ConfigType extends AbstractConfiguration
    {
    }

    public static class NonConfigType
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

        public void doNoParamAction()
        {
        }

        public void doNonMatchingType(ConfigType c)
        {
        }

        public void doNonConfigType(NonConfigType nc)
        {
        }
    }

    public static class Params
    {
        public void doConfigParam(T t, ConfigType c)
        {
        }

        public void doNonConfigParam(T t, NonConfigType c)
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

        public void doActionWithArg(T t, ConfigType arg)
        {
            obj = arg;
        }
    }
}
