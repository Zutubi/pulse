package com.cinnamonbob.acceptance;

import net.sourceforge.jwebunit.WebTestCase;

/**
 *
 */
public abstract class ExtendedWebTestCase extends WebTestCase
{
    public ExtendedWebTestCase()
    {
    }

    public ExtendedWebTestCase(String name)
    {
        super(name);
    }


    public void assertFormElementNotEmpty(String formElementName) {
        assertFormElementPresent(formElementName);
        String formElementValue = tester.getDialog().getFormParameterValue(formElementName);
        assertNotNull(formElementValue);
        assertFalse(formElementValue.equals(""));
    }
}
