package com.zutubi.pulse.acceptance.forms;

import com.google.common.base.Function;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.ObjectUtils;
import com.zutubi.util.StringUtils;
import static com.zutubi.util.StringUtils.stripLineBreaks;
import com.zutubi.util.WebUtils;
import com.zutubi.util.adt.Pair;
import junit.framework.Assert;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Base for form classes: supports methods for reading and writing fields and
 * submitting forms.
 */
public abstract class SeleniumForm
{
    public static final int TEXTFIELD       = 3;
    public static final int CHECKBOX        = 4;
    public static final int COMBOBOX        = 6;
    public static final int MULTI_CHECKBOX  = 7;
    public static final int MULTI_SELECT    = 8;
    public static final int ITEM_PICKER     = 9;

    public static final String BUTTON_APPLY = "apply";
    public static final String BUTTON_NEXT = "next";
    public static final String BUTTON_SAVE = "save";
    public static final String BUTTON_FINISH = "finish";
    public static final String BUTTON_CANCEL = "cancel";
    public static final String BUTTON_RESET = "reset";
    
    protected SeleniumBrowser browser;
    protected boolean inherited = false;
    protected boolean ajax = true;

    protected SeleniumForm(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    protected SeleniumForm(SeleniumBrowser browser, boolean ajax)
    {
        this.browser = browser;
        this.ajax = ajax;
    }

    public SeleniumForm(SeleniumBrowser browser, boolean ajax, boolean inherited)
    {
        this.browser = browser;
        this.ajax = ajax;
        this.inherited = inherited;
    }

    public SeleniumBrowser getSelenium()
    {
        return browser;
    }

    public boolean isAjax()
    {
        return ajax;
    }

    public void waitFor()
    {
        // Wait for the last field as the forms are lazily rendered
        String[] fields = getActualFieldNames();
        browser.waitForElement(getFieldId(fields[fields.length - 1]));
    }

    public boolean isFormPresent()
    {
        return browser.isElementIdPresent(getFormName());
    }

    public String getFieldId(String name)
    {
        return "zfid." + name;
    }

    public String getBrowseLinkId(String field)
    {
        return getFieldId(field) + ".browse";
    }

    public boolean isAnnotationPresent(String fieldName, String annotation)
    {
        return browser.isElementIdPresent(getFieldId(fieldName) + "." + annotation);
    }

    public boolean isEditable(String fieldName)
    {
        return browser.isEditable(getFieldId(fieldName));
    }

    public boolean isMarkedRequired(String fieldName)
    {
        return isAnnotationPresent(fieldName, "required");
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
        return browser.getSelectOptions(getFieldId(name));
    }

    public List<String> getComboBoxOptions(String name)
    {
        return browser.getComboOptions(getFieldId(name));
    }

    public List<String> getComboBoxDisplays(String name)
    {
        return browser.getComboDisplays(getFieldId(name));
    }

    public void submitNamedFormElements(String submitValue, Pair<String, String>... fieldValues)
    {
        if (fieldValues != null)
        {
            for (Pair<String, String> fv : fieldValues)
            {
                setFieldValue(fv.first, fv.second);
            }
        }

        submit(submitValue);
    }

    public void submitFormElements(String submitValue, String... fieldValues)
    {
        setFormElements(fieldValues);
        submit(submitValue);
    }

    public void nextNamedFormElements(Pair<String, String>... fieldValues)
    {
        submitNamedFormElements(BUTTON_NEXT, fieldValues);
    }

    public void nextFormElements(String... args)
    {
        submitFormElements(BUTTON_NEXT, args);
    }

    public void applyNamedFormElements(Pair<String, String>... fieldValues)
    {
        submitNamedFormElements(BUTTON_APPLY, fieldValues);
    }

    public void applyFormElements(String... args)
    {
        submitFormElements(BUTTON_APPLY, args);
    }

    public void saveNamedFormElements(Pair<String, String>... fieldValues)
    {
        submitNamedFormElements(BUTTON_SAVE, fieldValues);
    }

    public void saveFormElements(String... args)
    {
        submitFormElements(BUTTON_SAVE, args);
    }

    public void finishNamedFormElements(Pair<String, String>... fieldValues)
    {
        submitNamedFormElements(BUTTON_FINISH, fieldValues);
    }

    public void finishFormElements(String... args)
    {
        submitFormElements(BUTTON_FINISH, args);
    }

    public void cancelNamedFormElements(Pair<String, String>... fieldValues)
    {
        submitNamedFormElements(BUTTON_CANCEL, fieldValues);
    }

    public void cancelFormElements(String... args)
    {
        submitFormElements(BUTTON_CANCEL, args);
    }

    public void resetFormElements(String... args)
    {
        submitFormElements(BUTTON_RESET, args);
    }

    private void submit(String id)
    {
        browser.click(By.id("zfid." + id));
        if (ajax)
        {
            browser.waitForVariable("formSubmitting", true);
        }
    }

    /**
     * Returns an array of values of the same size as the form which is null-
     * filled.  When used to set or submit the form, no values in the form will
     * be changed.
     *
     * @return a value array that will leave the form unchanged
     */
    public String[] getUnchangedValues()
    {
        return new String[getFieldNames().length];
    }

    public String getFieldErrorMessage(String name)
    {
        return browser.getText(By.xpath("//div[@id='x-form-el-zfid."+name+"']"));
    }

    public boolean hasFieldError(String name)
    {
        return getFieldErrorMessage(name) != null;
    }

    public Object getFieldValue(String name)
    {
        int type = getFieldType(name);
        switch (type)
        {
            case ITEM_PICKER:
            case MULTI_SELECT:
                return browser.evaluateScript("return Ext.getCmp('" + getFieldId(name) + "').getValue();");
            default:
                return browser.getValue(getFieldId(name));
        }
    }

    public void setFieldValue(String name, String value)
    {
        int type = getFieldType(name);
        setFormElement(name, value, type);

        // Hack: make sure buttons are updated.  Typing in selenium doesn't
        // work :|.
        if (type == TEXTFIELD)
        {
            forceButtonUpdate(name);
        }
    }

    private void setFormElement(String name, String value, int type)
    {
        if (value != null)
        {
            String id = getFieldId(name);
            switch (type)
            {
                case TEXTFIELD:
                    browser.type(By.id(id), value);
                    break;
                case COMBOBOX:
                    setComponentValue(id, "'" + value + "'");
                    break;
                case ITEM_PICKER:
                case MULTI_SELECT:
                    List<String> values = convertMultiValue(value);
                    List<String> quotedValues = CollectionUtils.map(values, new Function<String, String>()
                    {
                        public String apply(String s)
                        {
                            return "'" + s + "'";
                        }
                    });
                    setComponentValue(id, quotedValues.toString());
                    break;
                case CHECKBOX:
                    setComponentValue(id, value);
                    break;
                case MULTI_CHECKBOX:
                    setMultiValues(name, value);
                    break;
                default:
                    break;
            }
        }
    }

    private void setComponentValue(String id, String value)
    {
        // Custom Ext widgets are tricky to manage.  Since we are
        // not testing the widgets themselves, just go direct to
        // the setValue method.
        browser.evaluateScript("var field = Ext.getCmp('" + id + "'); field.setValue(" + value + "); field.form.updateButtons()");
    }

    public boolean isFieldNotEmpty(String id)
    {
        String value = browser.getValue(WebUtils.toValidHtmlName(id));
        return StringUtils.stringSet(value);
    }

    private int getFieldType(String name)
    {
        int i;
        String[] names = getActualFieldNames();
        for (i = 0; i < names.length; i++)
        {
            if (names[i].equals(name))
            {
                break;
            }
        }

        if (i == names.length)
        {
            throw new IllegalArgumentException("Unknown field '" + name + "'");
        }
        
        return getActualFieldTypes()[i];
    }

    public void setFormElements(String... values)
    {
        int[] types = getActualFieldTypes();
        Assert.assertEquals(types.length, values.length);

        String[] names = getActualFieldNames();
        for (int i = 0; i < types.length; i++)
        {
            String name = names[i];
            String value = values[i];
            setFormElement(name, value, types[i]);
        }

        // Hack: make sure buttons are updated.  Typing something empty in
        // selenium doesn't work :|.
        if (names.length > 0)
        {
            forceButtonUpdate(names[0]);
        }
    }

    private void forceButtonUpdate(String fieldName)
    {
        browser.evaluateScript("var field = Ext.getCmp('" + getFieldId(fieldName) + "'); field.form.updateButtons()");
    }

    /**
     * Returns true if the forms field values match the given set of values.
     *
     * @param values the values being matched against the forms field values.
     *
     * @return true if the values match, false otherwise.
     */
    public boolean checkFormValues(String... values)
    {
        if (!isFormPresent())
        {
            return false;
        }

        int[] types = getActualFieldTypes();
        if (values.length != types.length)
        {
            return false;
        }

        for (int i = 0; i < types.length; i++)
        {
            if (values[i] != null)
            {
                String fieldName = getActualFieldNames()[i];
                switch (types[i])
                {
                    case SeleniumForm.TEXTFIELD:
                        if (!ObjectUtils.equals(stripLineBreaks(values[i]), stripLineBreaks((String) getFieldValue(fieldName))))
                        {
                            return false;
                        }
                        break;

                    case SeleniumForm.CHECKBOX:
                        String expectedValue = Boolean.valueOf(values[i]) ? "on" : "off";
                        if (!ObjectUtils.equals(expectedValue, getFieldValue(fieldName)))
                        {
                             return false;
                        }
                        break;

                    case SeleniumForm.COMBOBOX:
                        if (!ObjectUtils.equals(values[i], getFieldValue(fieldName)))
                        {
                            return false;
                        }
                        break;

                    case SeleniumForm.ITEM_PICKER:
                    case SeleniumForm.MULTI_CHECKBOX:
                    case SeleniumForm.MULTI_SELECT:
                        if (values[i] != null)
                        {
                            List<String> expected = convertMultiValue(values[i]);
                            @SuppressWarnings("unchecked")
                            List<String> got = (List<String>) getFieldValue(fieldName);
                            if (expected.size() != got.size())
                            {
                                return false;
                            }
                            for (int j = 0; j < expected.size(); j++)
                            {
                                if (!ObjectUtils.equals(expected.get(j), got.get(j)))
                                {
                                    return false;
                                }
                            }
                        }
                    default:
                        break;
                }
            }
        }
        return true;
    }

    public void setMultiValues(String name, String values)
    {
        List<String> set = convertMultiValue(values);

        String fieldId = getFieldId(name);
        for (String value : set)
        {
            browser.addSelection(fieldId, "value=" + value);
        }
    }

    public List<String> convertMultiValue(String values)
    {
        List<String> set = new LinkedList<String>();
        if (values.length() > 0)
        {
            set.addAll(Arrays.asList(values.split(",")));
        }

        return set;
    }

    public String[] getActualFieldNames()
    {
        String[] fieldNames = getFieldNames();
        if (inherited)
        {
            String[] temp = fieldNames;
            fieldNames = new String[fieldNames.length - 1];
            System.arraycopy(temp, 1, fieldNames, 0, temp.length - 1);
        }
        return fieldNames;
    }

    public int[] getActualFieldTypes()
    {
        int[] fieldTypes = getFieldTypes();
        if (inherited)
        {
            int[] temp = fieldTypes;
            fieldTypes = new int[fieldTypes.length - 1];
            System.arraycopy(temp, 1, fieldTypes, 0, temp.length - 1);
        }
        return fieldTypes;
    }

    public void triggerEvent(String fieldName, String eventType)
    {
        browser.evaluateScript("var field = Ext.getCmp('" + getFieldId(fieldName) + "'); field.fireEvent('" + eventType + "', field);");
    }

    public abstract String getFormName();

    public abstract String[] getFieldNames();
}
