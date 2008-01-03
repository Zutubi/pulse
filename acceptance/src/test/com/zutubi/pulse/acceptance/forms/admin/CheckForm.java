package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import junit.framework.Assert;

/**
 * The Subversion SCM form.
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

    public void assertResult(boolean ok, String message)
    {
        String checkOK = SeleniumUtils.evalVariable(selenium, "checkOK");
        Assert.assertEquals(Boolean.toString(ok), checkOK);
        String got = selenium.getText("check.result");
        Assert.assertTrue("Check result '" + got + "' does not contain expected message '" + message + "'", got.contains(message));
    }

    public void waitForCheck()
    {
        SeleniumUtils.waitForVariable(selenium, "checkComplete", 30000);
    }

    public void checkAndAssertResult(boolean ok, String message, String... args)
    {
        checkFormElements(args);
        waitForCheck();
        assertResult(ok, message);
    }
}
