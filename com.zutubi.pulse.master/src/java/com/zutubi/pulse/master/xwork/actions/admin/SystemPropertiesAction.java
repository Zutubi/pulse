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

package com.zutubi.pulse.master.xwork.actions.admin;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

import java.util.Map;
import java.util.Properties;

/**
 * A simple helper action that helps administrators to tweak the configuration of a running pulse
 * installation by allowing them to change the system properties.
 *
 * 
 */
public class SystemPropertiesAction extends ActionSupport
{
    public Properties getSystemProperties()
    {
        return System.getProperties();
    }

    public String doAdd()
    {
        // apply the parameters to the system properties.
        Map<String, String[]> params = ActionContext.getContext().getParameters();

        for (String key : params.keySet())
        {
            String[] values = params.get(key);
            if (values.length > 0)
            {
                System.setProperty(key, values[0]);
            }
        }

        return SUCCESS;
    }

    public String doRemove()
    {
        // apply the parameters to the system properties.
        Map<String, String[]> params = ActionContext.getContext().getParameters();

        for (String key : params.keySet())
        {
            System.clearProperty(key);
        }
        
        return SUCCESS;
    }
}
