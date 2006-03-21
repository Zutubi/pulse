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


    public void assertFormElementNotEmpty(String formElementName)
    {
        assertFormElementPresent(formElementName);
        String formElementValue = getFormValue(formElementName);
        assertNotNull(formElementValue);
        assertFalse(formElementValue.equals(""));
    }

    public String getFormValue(String formElementName)
    {
        return tester.getDialog().getFormParameterValue(formElementName);
    }
}
