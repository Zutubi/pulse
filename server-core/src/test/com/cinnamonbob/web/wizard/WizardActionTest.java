package com.cinnamonbob.web.wizard;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.core.ObjectFactory;
import com.opensymphony.xwork.ActionContext;
import junit.framework.TestCase;

import java.util.HashMap;

/**
 * <class-comment/>
 */
public class WizardActionTest extends TestCase
{

    public WizardActionTest(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ActionContext.getContext().setSession(new HashMap());
    }

    protected void tearDown() throws Exception
    {
        ActionContext.getContext().setSession(null);
        super.tearDown();
    }

    public void testOneStepWizard()
    {
        WizardAction action = createWizard(OneStepWizard.class.getName());
        action.validate();
        assertFalse(action.hasErrors());

        assertEquals("a", action.execute());

        OneStepWizard wizard = (OneStepWizard) ActionContext.getContext().getSession().get(OneStepWizard.class.getName());
        assertTrue(wizard.state.isInitialised());

        action = createWizard(OneStepWizard.class.getName());
        action.setNext("next");
        action.validate();
        assertFalse(action.hasErrors());
        assertEquals(ActionSupport.SUCCESS, action.execute());

        assertTrue(wizard.state.isValidated());
        assertTrue(wizard.state.isExecuted());
        assertTrue(wizard.isComplete());
    }

    public void testTwoStepWizard()
    {
        WizardAction action = createWizard(TwoStepWizard.class.getName());
        action.validate();
        assertFalse(action.hasErrors());

        assertEquals("one", action.execute());

        TwoStepWizard wizard = (TwoStepWizard) ActionContext.getContext().getSession().get(TwoStepWizard.class.getName());
        assertTrue(wizard.stateOne.isInitialised());
        assertEquals(wizard.stateOne, wizard.getCurrentState());

        action = createWizard(TwoStepWizard.class.getName());
        action.setNext("next");
        action.validate();
        assertFalse(action.hasErrors());
        assertEquals("two", action.execute());
        assertEquals(wizard.stateTwo, wizard.getCurrentState());

        assertTrue(wizard.stateOne.isValidated());
        assertTrue(wizard.stateOne.isExecuted());
        assertFalse(wizard.isComplete());

        assertTrue(wizard.stateTwo.isInitialised());

        action = createWizard(TwoStepWizard.class.getName());
        action.setNext("next");
        action.validate();
        assertFalse(action.hasErrors());
        assertEquals(ActionSupport.SUCCESS, action.execute());

        assertTrue(wizard.stateTwo.isValidated());
        assertTrue(wizard.stateTwo.isExecuted());
        assertTrue(wizard.isComplete());
    }

    private WizardAction createWizard(String name)
    {
        WizardAction action = new WizardAction();
        action.setWizardClass(name);
        // do not require any autowiring from spring in this test.
        action.setObjectFactory(new ObjectFactory());
        return action;
    }
}
