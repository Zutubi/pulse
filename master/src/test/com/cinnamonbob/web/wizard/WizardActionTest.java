package com.cinnamonbob.web.wizard;

import junit.framework.TestCase;
import com.opensymphony.xwork.ActionContext;
import com.cinnamonbob.web.ActionSupport;

import java.util.HashMap;

/**
 * <class-comment/>
 */
public class WizardActionTest extends TestCase
{
    private WizardAction action;

    public WizardActionTest(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ActionContext.getContext().setSession(new HashMap());
        action = new WizardAction();
    }

    protected void tearDown() throws Exception
    {
        action = null;
        ActionContext.getContext().setSession(null);
        super.tearDown();
    }

    public void testOneStepWizard()
    {
        action.setWizard(OneStepWizard.class.getName());
        action.validate();
        assertFalse(action.hasErrors());

        assertEquals("a", action.execute());

        OneStepWizard wizard = (OneStepWizard) ActionContext.getContext().getSession().get(OneStepWizard.class.getName());
        assertTrue(wizard.state.isInitialised());

        action = new WizardAction();
        action.setWizard(OneStepWizard.class.getName());
        action.validate();
        assertFalse(action.hasErrors());
        assertEquals(ActionSupport.SUCCESS, action.execute());

        assertTrue(wizard.state.isValidated());
        assertTrue(wizard.state.isExecuted());
        assertTrue(wizard.isComplete());
    }

    public void testTwoStepWizard()
    {
        action = new WizardAction();
        action.setWizard(TwoStepWizard.class.getName());
        action.validate();
        assertFalse(action.hasErrors());

        assertEquals("one", action.execute());

        TwoStepWizard wizard = (TwoStepWizard) ActionContext.getContext().getSession().get(TwoStepWizard.class.getName());
        assertTrue(wizard.stateOne.isInitialised());
        assertEquals(wizard.stateOne, wizard.getCurrentState());

        action = new WizardAction();
        action.setWizard(TwoStepWizard.class.getName());
        action.validate();
        assertFalse(action.hasErrors());
        assertEquals("two", action.execute());
        assertEquals(wizard.stateTwo, wizard.getCurrentState());

        assertTrue(wizard.stateOne.isValidated());
        assertTrue(wizard.stateOne.isExecuted());
        assertFalse(wizard.isComplete());

        assertTrue(wizard.stateTwo.isInitialised());

        action = new WizardAction();
        action.setWizard(TwoStepWizard.class.getName());
        action.validate();
        assertFalse(action.hasErrors());
        assertEquals(ActionSupport.SUCCESS, action.execute());

        assertTrue(wizard.stateTwo.isValidated());
        assertTrue(wizard.stateTwo.isExecuted());
        assertTrue(wizard.isComplete());
    }
}
