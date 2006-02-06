package com.cinnamonbob.web.wizard;

import junit.framework.*;

/**
 * <class-comment/>
 */
public class BaseWizardTest extends TestCase
{
    private BaseWizard wizard = null;

    public BaseWizardTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        wizard = new BaseWizard();
    }

    public void tearDown() throws Exception
    {
        wizard = null;

        super.tearDown();
    }

    public void testInitialise()
    {
        TestWizardState testState = new TestWizardState(wizard, "a");
        wizard.initialState = testState;
        wizard.addState(testState);
        wizard.initialise();

        assertFalse(testState.isValidated());
        assertFalse(testState.isExecuted());

        assertEquals("a", wizard.traverseForward());
        assertTrue(testState.hasErrors()); // attempted to traverse forward when no action was available.
        assertTrue(testState.isInitialised());
        assertTrue(testState.isValidated());
        assertTrue(testState.isExecuted());
    }

    public void testTraverseForwardBackward()
    {
        TestWizardState testStateA = new TestWizardState(wizard, "a");
        testStateA.setNextState("b");
        TestWizardState testStateB = new TestWizardState(wizard, "b");
        wizard.initialState = testStateA;
        wizard.addState(testStateA);
        wizard.addState(testStateB);
        wizard.initialise();

        assertEquals("b", wizard.traverseForward());
        assertTrue(testStateA.isExecuted());
        assertTrue(testStateA.isValidated());
        assertTrue(testStateB.isInitialised());

        testStateA.reset();
        testStateB.reset();
        assertEquals("a", wizard.traverseBackward());
        assertFalse(testStateA.isInitialised());
        assertFalse(testStateA.isValidated());
        assertFalse(testStateA.isExecuted());

        assertEquals("b", wizard.traverseForward());
        assertTrue(testStateA.isExecuted());
        assertTrue(testStateA.isValidated());
        assertTrue(testStateB.isInitialised());
    }

}