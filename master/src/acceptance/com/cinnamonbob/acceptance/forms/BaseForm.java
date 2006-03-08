package com.cinnamonbob.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public abstract class BaseForm
{
    protected final WebTester tester;

    public BaseForm(WebTester tester)
    {
        this.tester = tester;
    }

    public void assertFormPresent()
    {
        tester.assertFormPresent(getFormName());
    }

    public void assertFormNotPresent()
    {
        tester.assertFormNotPresent(getFormName());
    }

    public abstract String getFormName();
    public abstract String[] getFieldNames();

    public void saveFormElements(String... args)
    {
        setFormElements(args);
        tester.submit("save");
    }

    public void cancelFormElements(String... args)
    {
        setFormElements(args);
        tester.submit("cancel");
    }

    /**
     * @param values
     */
    public void setFormElements(String... values)
    {
        tester.assertFormPresent(getFormName());
        tester.setWorkingForm(getFormName());
        for (int i = 0; i < getFieldNames().length; i++)
        {
            String field = getFieldNames()[i];
            String value = values[i];
            if (value != null)
            {
                tester.setFormElement(field, value);
            }
        }
    }

    public void assertFormElements(String... values)
    {
        tester.assertFormPresent(getFormName());
        tester.setWorkingForm(getFormName());
        for (int i = 0; i < getFieldNames().length; i++)
        {
            String field = getFieldNames()[i];
            String value = values[i];
            tester.assertFormElementEquals(field, value);
        }
    }
}
