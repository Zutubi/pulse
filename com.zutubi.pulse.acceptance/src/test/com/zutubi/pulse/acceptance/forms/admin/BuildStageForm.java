package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;

import java.util.List;

/**
 * Project build stage form (suits wizard too).
 */
public class BuildStageForm extends ConfigurationForm
{
    private static final int LAZY_LOAD_TIMEOUT = 30000;

    public BuildStageForm(SeleniumBrowser browser, boolean inherited)
    {
        super(browser, BuildStageConfiguration.class, true, inherited);
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, COMBOBOX};
    }

    @Override
    public List<String> getComboBoxOptions(String name)
    {
        // Lazily-loaded options require some effort: simulate a click and
        // ensure we wait for the load to complete.
        browser.evaluateScript("var combo = Ext.getCmp('zfid." + name + "');" +
                "combo.store.on('load', function() { combo.loaded = true; });" +
                "combo.store.on('loadexception', function() { combo.loaded = true; });" +
                "combo.onTriggerClick();");
        browser.waitForCondition("return Ext.getCmp('zfid." + name + "').loaded", LAZY_LOAD_TIMEOUT);
        return super.getComboBoxOptions(name);
    }
}
