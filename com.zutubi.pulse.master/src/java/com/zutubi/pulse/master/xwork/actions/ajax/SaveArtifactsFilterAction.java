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

import com.google.common.base.Objects;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Action to save tail view settings to a user's preferences.  Invalid settings
 * are ignored.
 */
public class SaveArtifactsFilterAction extends ActionSupport
{
    private String filter;
    private SimpleResult result;

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    @Override
    public String execute() throws Exception
    {
        Object principle = getPrinciple();
        if (principle != null)
        {
            User user = userManager.getUser((String) principle);
            if (user != null && !Objects.equal(user.getArtifactsFilter(), filter))
            {
                user.setArtifactsFilter(filter);
                userManager.save(user);
            }
        }

        result = new SimpleResult(true, "filter saved");
        return SUCCESS;
    }
}