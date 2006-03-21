package com.cinnamonbob.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public abstract class BaseForm
{
    protected final WebTester tester;
    protected static final int TEXTFIELD = 3;
    protected static final int CHECKBOX = 4;
    protected static final int RADIOBOX = 5;

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

    public int[] getFieldTypes()
    {
        int[] types = new int[getFieldNames().length];
        for (int i = 0; i < types.length; i++)
        {
            types[i] = TEXTFIELD;
        }
        return types;
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
