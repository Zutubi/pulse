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

package com.zutubi.pulse.master.xwork.actions.ajax;

import com.opensymphony.xwork.ActionSupport;

import java.util.Collection;
import java.util.Map;

/**
 * An object that exposes an actions errors, messages and
 * fields to a JSON serialiser.
 *
 * @see com.zutubi.pulse.master.webwork.dispatcher.FlexJsonResult
 */
public class ActionResult
{
    private ActionSupport action;

    public ActionResult(ActionSupport action)
    {
        this.action = action;
    }

    public Collection getActionErrors()
    {
        return action.getActionErrors();
    }

    public Collection getActionMessages()
    {
        return action.getActionMessages();
    }

    public Map getFieldErrors()
    {
        return action.getFieldErrors();
    }
}
