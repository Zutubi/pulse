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

package com.zutubi.tove.ui.model;

import java.util.Set;

/**
 * Information about a type in a wizard. Wraps a {@link CompositeTypeModel} with extra
 * wizard-specific details.
 */
public class WizardTypeModel
{
    private CompositeTypeModel type;
    private String label;
    private String help;
    private WizardTypeFilter filter;

    public WizardTypeModel(CompositeTypeModel type, String label)
    {
        this.type = type;
        this.label = label;
    }

    public CompositeTypeModel getType()
    {
        return type;
    }

    public String getLabel()
    {
        return label;
    }

    public String getHelp()
    {
        return help;
    }

    public void setHelp(String help)
    {
        this.help = help;
    }

    public WizardTypeFilter getFilter()
    {
        return filter;
    }

    public void setTypeFilter(String stepKey, Set<String> compatibleTypes)
    {
        filter = new WizardTypeFilter(stepKey, compatibleTypes);
    }

}
