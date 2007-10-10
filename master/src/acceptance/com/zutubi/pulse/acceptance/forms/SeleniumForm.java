package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.List;

/**
 * <class-comment/>
 */
public abstract class SeleniumForm
{
    protected static final int TEXTFIELD      = 3;
    protected static final int CHECKBOX       = 4;
    protected static final int RADIOBOX       = 5;
    protected static final int COMBOBOX       = 6;
    protected static final int MULTI_CHECKBOX = 7;
    protected static final int MULTI_SELECT   = 8;
    protected static final int ITEM_PICKER    = 9;

    protected Selenium selenium;
    protected boolean inherited = false;
    protected boolean ajax = true;

    protected SeleniumForm(Selenium selenium)
    {
        this.selenium = selenium;
    }

    protected SeleniumForm(Selenium selenium, boolean ajax)
    {
        this.selenium = selenium;
        this.ajax = ajax;
    }

    public SeleniumForm(Selenium selenium, boolean ajax, boolean inherited)
    {
        this.selenium = selenium;
        this.ajax = ajax;
        this.inherited = inherited;
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
        // Wait for the last field as the forms are lazily rendered
        String[] fields = getActualFieldNames();
        SeleniumUtils.waitForElement(selenium, getFieldId(fields[fields.length - 1]));
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

    private String getFieldId(String name)
    {
        return "zfid." + name;
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
        return selenium.getSelectOptions(getFieldId(name));
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

    public void applyFormElements(String... args)
    {
        submitFormElements("apply", args);
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
        if (ajax)
        {
            SeleniumUtils.waitForVariable(selenium, "formSubmitting", SeleniumUtils.DEFAULT_TIMEOUT, true);            
        }
        else
        {
            selenium.waitForPageToLoad("60000");
        }
    }

    public String getFieldValue(String name)
    {
        int type = getFieldType(name);
        switch(type)
        {
            case ITEM_PICKER:
            case MULTI_SELECT:
                return selenium.getEval("selenium.browserbot.getCurrentWindow().Ext.getCmp('" + getFieldId(name) + "').getValue();");
            default:
                return selenium.getValue(getFieldId(name));
        }
    }

    public void setFormElement(String name, String value)
    {
        int type = getFieldType(name);
        setFormElement(name, value, type);
    }

    private void setFormElement(String name, String value, int type)
    {
        String id = getFieldId(name);
        switch (type)
        {
            case TEXTFIELD:
                if (value != null)
                {
                    selenium.type(id, value);
                }
                break;
            case COMBOBOX:
                if (value != null)
                {
                    setComponentValue(id, "'" + value + "'");
                }
                break;
            case ITEM_PICKER:
            case MULTI_SELECT:
                if (value != null)
                {
                    String[] values = convertMultiValue(value);
                    List<String> quotedValues = CollectionUtils.map(values, new Mapping<String, String>()
                    {
                        public String map(String s)
                        {
                            return "'" + s + "'";
                        }
                    });
                    setComponentValue(id, quotedValues.toString());
                }
                break;
            case CHECKBOX:
                if(Boolean.valueOf(value))
                {
                    selenium.check(id);
                }
                else
                {
                    selenium.uncheck(id);
                }
                break;
            case RADIOBOX:
                if (value != null)
                {
                    setRadioboxSelected(name, value);
                }
                break;
            case MULTI_CHECKBOX:
                if (value != null)
                {
                    setMultiValues(name, value);
                }
                break;
            default:
                break;
        }
    }

    private void setComponentValue(String id, String value)
    {
        // Custom Ext widgets are tricky to manage.  Since we are
        // not testing the widgets themselves, just go direct to
        // the setValue method.
        selenium.getEval("var field = selenium.browserbot.getCurrentWindow().Ext.getCmp('" + id + "'); field.setValue(" + value + "); field.form.updateButtons()");
    }

    private int getFieldType(String name)
    {
        int i;
        String[] names = getActualFieldNames();
        for(i = 0; i < names.length; i++)
        {
            if(names[i].equals(name))
            {
                break;
            }
        }

        return getActualFieldTypes()[i];
    }

    public void setFormElements(String... values)
    {
        int[] types = getActualFieldTypes();
        Assert.assertEquals(values.length, types.length);

        for (int i = 0; i < types.length; i++)
        {
            String name = getActualFieldNames()[i];
            String value = values[i];
            setFormElement(name, value, types[i]);
        }
    }

    public void assertFormElements(String... values)
    {
        assertFormPresent();

        int[] types = getActualFieldTypes();
        Assert.assertEquals(values.length, types.length);

        for (int i = 0; i < types.length; i++)
        {
            String fieldName = getActualFieldNames()[i];
            switch (types[i])
            {
                case TEXTFIELD:
                    TestCase.assertEquals(StringUtils.stripLineBreaks(values[i]), StringUtils.stripLineBreaks(getFieldValue(fieldName)));
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
                case ITEM_PICKER:
                case MULTI_CHECKBOX:
                case MULTI_SELECT:
                    if (values[i] != null)
                    {
                        String[] expected = convertMultiValue(values[i]);
                        assertMultiValues(fieldName, expected);
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
        int[] types = getActualFieldTypes();

        String[] defaultValues = new String[types.length];
        for (int i = 0; i < types.length; i++)
        {
            defaultValues[i] = "";
        }
        return defaultValues;
    }

    public String[] getFormValues()
    {
        String[] fieldNames = getActualFieldNames();
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
        String fieldValue = getFieldValue(name);
        String[] gotValues = fieldValue.length() == 0 ? new String[0] : fieldValue.split(",");
        Assert.assertEquals(values.length, gotValues.length);
        for (int i = 0; i < values.length; i++)
        {
            Assert.assertEquals(values[i], gotValues[i]);
        }
    }

    public void setMultiValues(String name, String values)
    {
        String[] set = convertMultiValue(values);

        String fieldLocator = getFieldId(name);
        for(String value: set)
        {
            selenium.addSelection(fieldLocator, "value=" + value);
        }
    }

    private String[] convertMultiValue(String values)
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
        return set;
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
            selenium.check(getFieldId(name));
        }
        else
        {
            selenium.uncheck(getFieldId(name));
        }
    }

    private String[] getActualFieldNames()
    {
        String[] fieldNames = getFieldNames();
        if(inherited)
        {
            String[] temp = fieldNames;
            fieldNames = new String[fieldNames.length - 1];
            System.arraycopy(temp, 1, fieldNames, 0, temp.length - 1);
        }
        return fieldNames;
    }

    private int[] getActualFieldTypes()
    {
        int[] fieldTypes = getFieldTypes();
        if(inherited)
        {
            int[] temp = fieldTypes;
            fieldTypes = new int[fieldTypes.length - 1];
            System.arraycopy(temp, 1, fieldTypes, 0, temp.length - 1);
        }
        return fieldTypes;
    }

    public abstract String getFormName();
    public abstract String[] getFieldNames();
}
