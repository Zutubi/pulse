package com.zutubi.pulse.acceptance.forms.admin;

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
        String checkOK = browser.evalVariable("checkOK");
        return Boolean.valueOf(checkOK);
    }

    public String getResultMessage()
    {
        return browser.getText("check.result");
    }

    public void waitForCheck()
    {
        browser.waitForVariable("checkComplete");
    }

    public void checkFormElementsAndWait(String... args)
    {
        checkFormElements(args);
        waitForCheck();
    }
}
