package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * The check form.
 */
public class CheckForm extends SeleniumForm
{
    private String name;

    public CheckForm(SeleniumForm mainForm)
    {
        super(mainForm.getSelenium());
        name = mainForm.getFormName() + "CheckHandler";
    }

    public String getFormName()
    {
        return name;
    }

    public String[] getFieldNames()
    {
        return new String[0];
    }

    public void checkFormElements(String... args)
    {
        submitFormElements("check", args);
    }

    public boolean isResultOk()
    {
        String checkOK = SeleniumUtils.evalVariable(selenium, "checkOK");
        return Boolean.valueOf(checkOK);
    }

    public String getResultMessage()
    {
        return selenium.getText("check.result");
    }

    public void waitForCheck()
    {
        SeleniumUtils.waitForVariable(selenium, "checkComplete", 30000);
    }

    public void checkFormElementsAndWait(String... args)
    {
        checkFormElements(args);
        waitForCheck();
    }
}
