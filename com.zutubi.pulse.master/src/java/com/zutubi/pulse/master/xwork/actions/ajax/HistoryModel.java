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

import com.zutubi.pulse.master.xwork.actions.project.BuildModel;
import com.zutubi.pulse.master.xwork.actions.project.PagerModel;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * Model for JSON data used to render the project history page.
 */
public class HistoryModel
{
    private List<BuildModel> builds = new LinkedList<BuildModel>();
    private PagerModel pager;

    public HistoryModel(List<BuildModel> builds, PagerModel pager)
    {
        this.builds = builds;
        this.pager = pager;
    }

    @JSON
    public List<BuildModel> getBuilds()
    {
        return builds;
    }

    public PagerModel getPager()
    {
        return pager;
    }
}
