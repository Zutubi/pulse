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

    public Selenium getSelenium()
    {
        return selenium;
    }

    public boolean isAjax()
    {
        return ajax;
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
        return "//*[@id='" + getFormName() + "']/form" + (rest == null ? "" : rest);
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

    public String[] getComboBoxOptions(String name)
    {
        String js = "var result = function() { " +
                        "var combo = selenium.browserbot.getCurrentWindow().Ext.getCmp('zfid." + name + "'); " +
                        "var values = []; " +
                        "combo.store.each(function(r) { values.push(r.get(combo.valueField)); }); " +
                        "return values; " +
                    "}(); " +
                    "result";
        return selenium.getEval(js).split(",");
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

    public void setFormElement(String name, String value)
    {
        int type = getFieldType(name);
        setFormElement(name, value, type);
    }

    private void setFormElement(String name, String value, int type)
    {
        String locator = getFieldLocator(name);
        switch (type)
        {
            case TEXTFIELD:
                if (value != null)
                {
                    selenium.type(locator, value);
                }
                break;
            case COMBOBOX:
                if (value != null)
                {
                    // Combos are custom ext widgets, so we just poke a
                    // value into the underlying hidden input field.
                    selenium.getEval("selenium.browserbot.getCurrentWindow().document.getElementById('" + name + "').value = '" + value + "';");
                }
                break;
            case CHECKBOX:
                if(Boolean.valueOf(value))
                {
                    selenium.check(locator);
                }
                else
                {
                    selenium.uncheck(locator);
                }
                break;
            case RADIOBOX:
                if (value != null)
                {
                    setRadioboxSelected(name, value);
                }
                break;
            case MULTI_CHECKBOX:
            case MULTI_SELECT:
                if (value != null)
                {
                    setMultiValues(name, value);
                }
                break;
            default:
                break;
        }
    }

    private int getFieldType(String name)
    {
        int i;
        String[] names = getFieldNames();
        for(i = 0; i < names.length; i++)
        {
            if(names[i].equals(name))
            {
                break;
            }
        }

        return getFieldTypes()[i];
    }

    public void setFormElements(String... values)
    {
        int[] types = getFieldTypes();
        Assert.assertEquals(values.length, types.length);

        for (int i = 0; i < types.length; i++)
        {
            String name = getFieldNames()[i];
            String value = values[i];
            setFormElement(name, value, types[i]);
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
