/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance;

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

    public String[] getSelectionValues(String formElementName)
    {
        return tester.getDialog().getForm().getParameterValues(formElementName);
    }

    public void selectMultipleValues(String formElementName, String[] values)
    {
        tester.getDialog().getForm().setParameter(formElementName, values);
    }

    public void assertSelectionValues(String formElementName, String[] expected)
    {
        String[] got = getSelectionValues(formElementName);
        assertEquals(expected.length, got.length);
        for(int i = 0; i < got.length; i++)
        {
            assertEquals(expected[i], got[i]);
        }
    }
}
