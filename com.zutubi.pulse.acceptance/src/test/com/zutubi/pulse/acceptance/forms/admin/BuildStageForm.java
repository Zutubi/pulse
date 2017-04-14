/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
