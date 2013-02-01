package com.zutubi.tove.actions;

import com.google.common.base.Function;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigurationActionsTest extends ZutubiTestCase
{
    private static final String RESULT_MESSAGE = "did it";

    private static Object obj = null;
    private DefaultObjectFactory objectFactory = new DefaultObjectFactory();

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

    public void testResultExplicit() throws Exception
    {
        resultHelper("result", RESULT_MESSAGE);
    }

    public void testResultExplicitNoMessage() throws Exception
    {
        resultHelper("resultNoMessage", "default fallback");
    }

    public void testResultDefaultNoLabel() throws Exception
    {
        resultHelper("defaultNoLabel", "action 'defaultNoLabel' triggered");
    }

    public void testResultDefaultLabel() throws Exception
    {
        resultHelper("defaultLabel", "action 'labelled' triggered");
    }

    public void testResultDefaultFeedback() throws Exception
    {
        resultHelper("defaultFeedback", "fedbacked");
    }

    public void testResultDefaultLabelAndFeedback() throws Exception
    {
        resultHelper("defaultLabelAndFeedback", "fedbacked");
    }

    public void testResultDefaultFeedbackReferencesName() throws Exception
    {
        resultHelper("defaultFeedbackReferencesName", "name is defaultFeedbackReferencesName");
    }

    private void resultHelper(String actionName, String expectedMessage) throws Exception
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, ResultActions.class, objectFactory);
        assertTrue(ca.hasAction(actionName));

        ActionResult result = ca.execute(actionName, new T(), null);
        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals(expectedMessage, result.getMessage());
        assertEquals(0, result.getInvalidatedPaths().size());
    }

    public void testEnabledMethod() throws Exception
    {
        assertEnabledMethod(true, EnabledMethod.class);
    }

    public void testEnabledMethodWrongReturn() throws Exception
    {
        assertEnabledMethod(false, EnabledMethodWrongReturn.class);
    }

    public void testEnabledMethodWrongArgument() throws Exception
    {
        assertEnabledMethod(false, EnabledMethodWrongArgument.class);
    }

    public void testEnabledMethodNoValid() throws Exception
    {
        assertEnabledMethod(false, EnabledMethodNoValid.class);
    }

    public void testActionsEnabled() throws Exception
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, EnabledMethod.class, objectFactory);
        assertTrue(ca.actionsEnabled(new T(), true));
        assertFalse(ca.actionsEnabled(new T(), false));
    }

    private void assertEnabledMethod(boolean hasEnabledMethod, Class clazz)
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, clazz, objectFactory);
        assertEquals(hasEnabledMethod, ca.hasEnabledMethod());
    }

    public void testCustomiseNoArgNoReturn() throws Exception
    {
        assertCustomiseMethod("noArgNoReturn");
    }

    public void testCustomiseNoArg() throws Exception
    {
        assertCustomiseMethod("noArg");
    }

    public void testCustomiseNoReturn() throws Exception
    {
        assertCustomiseMethod("noReturn");
    }

    public void testCustomiseArgAndReturn() throws Exception
    {
        assertCustomiseMethod("argAndReturn");
    }

    public void testCustomiseArgWrongType() throws Exception
    {
        assertNoCustomiseMethod("argWrongType");
    }

    public void testCustomiseReturnWrongType() throws Exception
    {
        assertNoCustomiseMethod("returnWrongType");
    }

    private void assertCustomiseMethod(String actionName)
    {
        assertNotNull(getCustomiseMethod(actionName));
    }

    private void assertNoCustomiseMethod(String actionName)
    {
        assertNull(getCustomiseMethod(actionName));
    }

    public void testPrepareNoArgNoReturn() throws Exception
    {
        assertPrepareMethod("noArgNoReturn");
    }

    public void testPrepareNoArg() throws Exception
    {
        assertPrepareMethod("noArg");
    }

    public void testPrepareNoReturn() throws Exception
    {
        assertPrepareMethod("noReturn");
    }

    public void testPrepareArgAndReturn() throws Exception
    {
        assertPrepareMethod("argAndReturn");
    }

    public void testPrepareReturnSubType() throws Exception
    {
        assertPrepareMethod("returnSubtype");
    }

    public void testPrepareReturnSuperType() throws Exception
    {
        assertNoPrepareMethod("returnSupertype");
    }

    public void testPrepareArgWrongType() throws Exception
    {
        assertNoPrepareMethod("argWrongType");
    }

    public void testPrepareReturnActionNoArg() throws Exception
    {
        assertNoPrepareMethod("returnActionNoArg");
    }

    public void testPrepareReturnMismatchType() throws Exception
    {
        assertNoPrepareMethod("returnMismatchType");
    }

    private void assertPrepareMethod(String actionName)
    {
        assertNotNull(getPrepareMethod(actionName));
    }

    private void assertNoPrepareMethod(String actionName)
    {
        assertNull(getPrepareMethod(actionName));
    }

    private Method getCustomiseMethod(String actionName)
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, CustomiseActions.class, objectFactory);
        return ca.getAction(actionName).getCustomiseMethod();
    }

    private Method getPrepareMethod(String actionName)
    {
        ConfigurationActions ca = new ConfigurationActions(T.class, PrepareActions.class, objectFactory);
        return ca.getAction(actionName).getPrepareMethod();
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
        List<String> gotNames = CollectionUtils.map(got, new Function<ConfigurationAction, String>()
        {
            public String apply(ConfigurationAction configurationAction)
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

    public static class ConfigSubType extends ConfigType
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

    public static class CustomiseActions
    {
        public void customiseNoArgNoReturn()
        {
        }

        public void doNoArgNoReturn(T t)
        {
        }

        public String customiseNoArg()
        {
            return "customiseNoArg";
        }

        public void doNoArg(T t, ConfigType c)
        {
        }

        public void customiseNoReturn(T t)
        {
        }

        public void doNoReturn(T t, ConfigType c)
        {
        }

        public String customiseArgAndReturn(T t)
        {
            return "customiseArgAndReturn";
        }

        public void doArgAndReturn(T t, ConfigType c)
        {
        }

        public void customiseArgWrongType(ConfigType t)
        {
        }

        public void doArgWrongType(T t, ConfigType c)
        {
        }

        public Long customiseReturnWrongType(T t)
        {
            return 0L;
        }

        public void doReturnWrongType(T t)
        {
        }
    }

    public static class PrepareActions
    {
        public void prepareNoArgNoReturn()
        {
        }

        public void doNoArgNoReturn(T t)
        {
        }

        public ConfigType prepareNoArg()
        {
            return new ConfigType();
        }

        public void doNoArg(T t, ConfigType c)
        {
        }

        public void prepareNoReturn(T t)
        {
        }

        public void doNoReturn(T t, ConfigType c)
        {
        }

        public ConfigType prepareArgAndReturn(T t)
        {
            return new ConfigType();
        }

        public void doArgAndReturn(T t, ConfigType c)
        {
        }

        public ConfigSubType prepareReturnSubtype()
        {
            return new ConfigSubType();
        }

        public void doReturnSubtype(T t, ConfigType c)
        {
        }

        public ConfigType prepareReturnSupertype()
        {
            return new ConfigType();
        }

        public void doReturnSupertype(T t, ConfigSubType c)
        {
        }

        public void prepareArgWrongType(ConfigType t)
        {
        }

        public void doArgWrongType(T t, ConfigType c)
        {
        }

        public ConfigType prepareReturnActionNoArg(T t)
        {
            return new ConfigType();
        }

        public void doReturnActionNoArg(T t)
        {
        }

        public T prepareReturnMismatchType(T t)
        {
            return new T();
        }

        public void doReturnMismatchType(T t, ConfigType c)
        {
        }
    }

    public static class EnabledMethod
    {
        public boolean actionsEnabled(T t, boolean deeplyValid)
        {
            return deeplyValid;
        }
    }

    public static class EnabledMethodWrongReturn
    {
        public Object actionsEnabled(T t, boolean deeplyValid)
        {
            return true;
        }
    }

    public static class EnabledMethodWrongArgument
    {
        public boolean actionsEnabled(ConfigType c, boolean deeplyValid)
        {
            return true;
        }
    }

    public static class EnabledMethodNoValid
    {
        public boolean actionsEnabled(T t)
        {
            return true;
        }
    }

    public static class ResultActions
    {
        public ActionResult doResult(T t)
        {
            return new ActionResult(ActionResult.Status.SUCCESS, RESULT_MESSAGE);
        }

        public ActionResult doResultNoMessage(T t)
        {
            return new ActionResult(ActionResult.Status.SUCCESS, null);
        }

        public void doDefaultNoLabel(T t)
        {
        }

        public void doDefaultLabel(T t)
        {
        }

        public void doDefaultFeedback(T t)
        {
        }

        public void doDefaultLabelAndFeedback(T t)
        {
        }

        public void doDefaultFeedbackReferencesName(T t)
        {
        }
    }
}
