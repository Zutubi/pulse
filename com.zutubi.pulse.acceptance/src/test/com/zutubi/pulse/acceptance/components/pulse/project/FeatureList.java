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

package com.zutubi.pulse.acceptance.components.pulse.project;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Component;

/**
 * Corresponds to the Zutubi.pulse.project.FeatureList JS component.
 */
public class FeatureList extends Component
{
    public FeatureList(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    @Override
    protected String getPresentScript()
    {
        return "return " + getComponentJS() + ".data != null;";
    }
}
