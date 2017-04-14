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
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.List;

/**
 * A form for a type selection state in a wizard.
 */
public class SelectTypeState extends SeleniumForm
{
    private static final String FIELD_NAME_SELECT = "wizard.select";

    public SelectTypeState(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return "select.state";
    }

    public String[] getFieldNames()
    {
        return new String[]{FIELD_NAME_SELECT};
    }

    public int[] getFieldTypes()
    {
        return new int[]{ COMBOBOX };
    }

    public List<String> getOptions()
    {
        return getComboBoxOptions(FIELD_NAME_SELECT);
    }

    public List<String> getSortedOptionList()
    {
        List<String> options = getComboBoxOptions(FIELD_NAME_SELECT);
        Collections.sort(options, new Sort.StringComparator());
        return options;
    }
}
