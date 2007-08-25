package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public abstract class SeleniumForm
{
    protected static final int TEXTFIELD = 3;
    protected static final int CHECKBOX = 4;
    protected static final int RADIOBOX = 5;
    protected static final int COMBOBOX = 6;
    protected static final int MULTI_CHECKBOX = 7;
    protected static final int MULTI_SELECT = 8;

    protected Selenium selenium;
    protected boolean ajax = true;

    public SeleniumForm(Selenium selenium)
    {
        this.selenium = selenium;
    }

    protected SeleniumForm(Selenium selenium, boolean ajax)
    {
        this.selenium = selenium;
        this.ajax = ajax;
    }

    public void waitFor()
    {
        SeleniumUtils.waitForElement(selenium, getFormName());
    }

    public void assertFormPresent()
    {
        TestCase.assertTrue(isFormPresent());
    }

    public void assertFormNotPresent()
    {
        TestCase.assertFalse(isFormPresent());
    }

    private boolean isFormPresent()
    {
        String formLocator = getLocator(null);
        return selenium.isElementPresent(formLocator);
    }

    private String getLocator(String rest)
    {
        return "//td[@id='" + getFormName() + "']/form" + (rest == null ? "" : rest);
    }

    private String getFieldLocator(String name)
    {
        return getLocator("//*[@name='" + name + "']");
    }

    /**
     * Returns the type identifiers for the form fields. The default implementation
     * returns an array of TEXTFIELD identifiers.
     *
     * @return an array of form field identifiers.
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
        return selenium.getSelectOptions(getFieldLocator(name));
    }

    public void submitFormElements(String id, String... args)
    {
        setFormElements(args);
        submit(id);
    }

    public void nextFormElements(String... args)
    {
        submitFormElements("next", args);
    }

    public void saveFormElements(String... args)
    {
        submitFormElements("save", args);
    }

    public void finishFormElements(String... args)
    {
        submitFormElements("finish", args);
    }

    public void cancelFormElements(String... args)
    {
        submitFormElements("cancel", args);
    }

    private void submit(String id)
    {
        selenium.click("zfid." + id);
        if (!ajax)
        {
            selenium.waitForPageToLoad("30000");
        }
    }

    public String getFieldValue(String name)
    {
        return selenium.getValue(getFieldLocator(name));
    }

    public void setFormElements(String... values)
    {
        int[] types = getFieldTypes();
        Assert.assertEquals(values.length, types.length);

        for (int i = 0; i < types.length; i++)
        {
            String locator = getFieldLocator(getFieldNames()[i]);
            switch (types[i])
            {
                case TEXTFIELD:
                    if (values[i] != null)
                    {
                        selenium.type(locator, values[i]);
                    }
                    break;
                case COMBOBOX:
                    if (values[i] != null)
                    {
                        // Combos are custom ext widgets, so we just poke a
                        // value into the underlying hidden input field.
                        selenium.getEval("selenium.browserbot.getCurrentWindow().document.getElementById('" + getFieldNames()[i] + "').value = '" + values[i] + "';");
                    }
                    break;
                case CHECKBOX:
                    if(Boolean.valueOf(values[i]))
                    {
                        selenium.check(locator);
                    }
                    else
                    {
                        selenium.uncheck(locator);
                    }
                    break;
                case RADIOBOX:
                    if (values[i] != null)
                    {
                        setRadioboxSelected(getFieldNames()[i], values[i]);
                    }
                    break;
                case MULTI_CHECKBOX:
                case MULTI_SELECT:
                    if (values[i] != null)
                    {
                        setMultiValues(getFieldNames()[i], values[i]);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void assertFormElements(String... values)
    {
        assertFormPresent();

        int[] types = getFieldTypes();
        Assert.assertEquals(values.length, types.length);

        for (int i = 0; i < types.length; i++)
        {
            String fieldName = getFieldNames()[i];
            switch (types[i])
            {
                case TEXTFIELD:
                    TestCase.assertEquals(values[i], getFieldValue(fieldName));
                    break;
                case CHECKBOX:
                    TestCase.assertEquals(Boolean.valueOf(values[i]) ? "on" : "off", getFieldValue(fieldName));
                    break;
                case RADIOBOX:
                    // FIXME
                    TestCase.fail();
                    break;
                case COMBOBOX:
                    TestCase.assertEquals(values[i], getFieldValue(fieldName));
                    break;
                case MULTI_CHECKBOX:
                case MULTI_SELECT:
                    if (values[i] != null)
                    {
                        String[] expected;

                        if (values[i].length() > 0)
                        {
                            expected = values[i].split(",");
                        }
                        else
                        {
                            expected = new String[0];
                        }

                        assertMultiValues(getFieldNames()[i], expected);
                    }
                default:
                    break;
            }
        }
    }

    public void assertFormReset()
    {
        assertFormElements(getDefaultValues());
    }

    public String[] getDefaultValues()
    {
        int[] types = getFieldTypes();

        String[] defaultValues = new String[types.length];
        for (int i = 0; i < types.length; i++)
        {
            defaultValues[i] = "";
        }
        return defaultValues;
    }

    public String[] getFormValues()
    {
        String[] fieldNames = getFieldNames();
        String[] formValues = new String[fieldNames.length];
        CollectionUtils.mapToArray(fieldNames, new Mapping<String, String>()
        {
            public String map(String fieldName)
            {
                return getFieldValue(fieldName);
            }
        }, formValues);
        return formValues;
    }

    public void assertMultiValues(String name, String... values)
    {
        String[] gotValues = selenium.getSelectedValues(getFieldLocator(name));
        Assert.assertEquals(values.length, gotValues.length);
        for (int i = 0; i < values.length; i++)
        {
            Assert.assertEquals(values[i], gotValues[i]);
        }
    }

    public void setMultiValues(String name, String values)
    {
        String[] set;
        if (values.length() > 0)
        {
            set = values.split(",");
        }
        else
        {
            set = new String[0];
        }

        String fieldLocator = getFieldLocator(name);
        for(String value: set)
        {
            selenium.addSelection(fieldLocator, "value=" + value);
        }
    }

    public void setRadioboxSelected(String fieldName, String selectedOption)
    {
        TestCase.fail();
    }

    public void assertRadioboxSelected(String fieldName, String option)
    {
        TestCase.fail();
    }

    public void setCheckboxChecked(String name, boolean b)
    {
        if (b)
        {
            selenium.check(getFieldLocator(name));
        }
        else
        {
            selenium.uncheck(getFieldLocator(name));
        }
    }

    public abstract String getFormName();
    public abstract String[] getFieldNames();
}
