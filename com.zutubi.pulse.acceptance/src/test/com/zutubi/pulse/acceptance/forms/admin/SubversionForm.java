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
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * The Subversion SCM form.
 */
public class SubversionForm extends SeleniumForm
{
    public SubversionForm(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return "com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "url", "username", "password", "keyfile", "keyfilePassphrase", "externalsMonitoring", "externalMonitorPaths", "verifyExternals", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, COMBOBOX, ITEM_PICKER, CHECKBOX, CHECKBOX, CHECKBOX, TEXTFIELD, CHECKBOX, TEXTFIELD};
    }
}