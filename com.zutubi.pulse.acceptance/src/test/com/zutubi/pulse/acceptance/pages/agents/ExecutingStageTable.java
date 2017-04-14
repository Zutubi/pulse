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

package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.PropertyTable;

/**
 * A property table that shows a build stage executing on an agent.
 */
public class ExecutingStageTable extends PropertyTable
{
    public ExecutingStageTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }
    
    public long getNumber()
    {
        return Long.parseLong(getValue("number").replace("build ", ""));
    }
    
    public String getProject()
    {
        return getValue("project");
    }
    
    public String getOwner()
    {
        return getValue("owner");
    }
    
    public String getStage()
    {
        return getValue("name");
    }
    
    public String getRecipe()
    {
        return getValue("recipe");
    }
}
