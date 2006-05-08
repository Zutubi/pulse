/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;
import junit.framework.Assert;

/**
 * <class-comment/>
 */
public abstract class BaseForm
{
    protected final WebTester tester;
    protected static final int TEXTFIELD = 3;
    protected static final int CHECKBOX = 4;
    protected static final int RADIOBOX = 5;
    protected static final int SELECT = 6;

    public BaseForm(WebTester tester)
    {
        this.tester = tester;
    }

    public void assertFormPresent()
    {
        tester.assertFormPresent(getFormName());
        String[] names = getFieldNames();
        for (String name : names)
        {
            String[] selectValues = getSelectOptions(name);
            if (selectValues != null)
            {
                tester.assertOptionsEqual(name, selectValues);
            }
        }
    }

    public void assertFormNotPresent()
    {
        tester.assertFormNotPresent(getFormName());
    }

    public abstract String getFormName();

    public abstract String[] getFieldNames();

    /**
     * Returns the type identifiers for the form fields. The default implementation
     * returns an array of TEXTFIELD identifiers.
     *
     * @return an array of form field identifiers.
     *
     * @see BaseForm#TEXTFIELD
     * @see BaseForm#CHECKBOX
     * @see BaseForm#RADIOBOX
     * @see BaseForm#SELECT
     */
    public int[] getFieldTypes()
    {
        int[] types = new int[getFieldNames().length];
        for (int i = 0; i < types.length; i++)
        {
            types[i] = TEXTFIELD;
        }
        return types;
    }

    public String[] getSelectOptions(String name)
    {
        return null;
    }

    public void saveFormElements(String... args)
    {
        setFormElements(args);
        tester.submit("save");
    }

    public void nextFormElements(String... args)
    {
        setFormElements(args);
        tester.submit("next");
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

        int[] types = getFieldTypes();
        for (int i = 0; i < types.length; i++)
        {
            switch (types[i])
            {
                case TEXTFIELD:
                case SELECT:
                    if (values[i] != null)
                    {
                        tester.setFormElement(getFieldNames()[i], values[i]);
                    }
                    break;
                case CHECKBOX:
                    setCheckboxChecked(getFieldNames()[i], Boolean.valueOf(values[i]));
                    break;
                case RADIOBOX:
                    if (values[i] != null)
                    {
                        setRadioboxSelected(getFieldNames()[i], values[i]);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void assertFormElements(String... values)
    {
        tester.assertFormPresent(getFormName());
        tester.setWorkingForm(getFormName());

        int[] types = getFieldTypes();
        Assert.assertEquals(values.length, types.length);

        for (int i = 0; i < types.length; i++)
        {
            switch (types[i])
            {
                case TEXTFIELD:
                    tester.assertFormElementEquals(getFieldNames()[i], values[i]);
                    break;
                case CHECKBOX:
                    assertCheckboxChecked(getFieldNames()[i], Boolean.valueOf(values[i]));
                    break;
                case RADIOBOX:
                    assertRadioboxSelected(getFieldNames()[i], values[i]);
                    break;
                case SELECT:
                    // Can set to null to ignore (e.g. multiselect where this doesn't work)
                    if (values[i] != null)
                    {
                        tester.assertFormElementEquals(getFieldNames()[i], values[i]);
                    }
                default:
                    break;
            }
        }
    }

    public void setRadioboxSelected(String fieldName, String selectedOption)
    {
        tester.setFormElement(fieldName, selectedOption);
    }

    public void assertRadioboxSelected(String fieldName, String option)
    {
        tester.assertRadioOptionSelected(fieldName, option);
    }

    public void setCheckboxChecked(String name, boolean b)
    {
        if (b)
        {
            tester.setFormElement(name, "true");
        }
        else
        {
            tester.uncheckCheckbox(name);
        }
    }

    public void assertCheckboxChecked(String name, boolean b)
    {
        if (b)
        {
            tester.assertFormElementEquals(name, "true");
        }
        else
        {
            tester.assertFormElementEquals(name, null);
        }
    }
}
