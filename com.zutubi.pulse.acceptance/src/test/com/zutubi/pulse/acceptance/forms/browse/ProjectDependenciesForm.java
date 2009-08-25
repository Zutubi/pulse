package com.zutubi.pulse.acceptance.forms.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;

/**
 * The options form on a project dependencies tab.
 */
public class ProjectDependenciesForm extends SeleniumForm
{
    public static final String FIELD_MODE = "mode";

    public ProjectDependenciesForm(SeleniumBrowser browser)
    {
        super(browser, true);
    }

    public String getFormName()
    {
        return "dependencies-form";
    }

    public String[] getFieldNames()
    {
        return new String[]{FIELD_MODE};
    }

    public int[] getFieldTypes()
    {
        return new int[]{COMBOBOX};
    }

    @Override
    public void setFieldValue(String name, String value)
    {
        // This form has special behaviour to submit on select, which
        // unfortunately we need to emulate to use in tests.
        browser.evalExpression("var field = selenium.browserbot.getCurrentWindow().Ext.getCmp('" + getFieldId(name) + "'); field.setValue('" + value + "'); field.fireEvent('select');");
    }

    public void submitMode(ProjectDependencyGraphBuilder.TransitiveMode mode)
    {
        setFieldValue(FIELD_MODE, mode.name());
    }
}
