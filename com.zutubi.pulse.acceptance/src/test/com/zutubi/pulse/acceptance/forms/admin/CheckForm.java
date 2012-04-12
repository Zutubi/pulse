package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import org.openqa.selenium.By;

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
        return (Boolean) browser.evaluateScript("return checkOK");
    }

    public String getResultMessage()
    {
        return browser.getText(By.id("check.result"));
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
