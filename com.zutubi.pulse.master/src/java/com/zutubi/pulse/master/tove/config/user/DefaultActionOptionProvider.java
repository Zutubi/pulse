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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.xwork.actions.DefaultAction;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.ListOptionProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides the list of possible default pages: the page the user is taken to
 * when the login or click the "pulse" link.
 */
public class DefaultActionOptionProvider extends ListOptionProvider
{
    private static final List<String> options = new LinkedList<String>();

    static
    {
        options.add(DefaultAction.WELCOME_ACTION);
        options.add(DefaultAction.DASHBOARD_ACTION);
        options.add(DefaultAction.BROWSE_ACTION);
    }

    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        return options;
    }
}
