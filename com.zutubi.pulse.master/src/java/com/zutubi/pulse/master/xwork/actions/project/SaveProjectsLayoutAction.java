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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.LabelProjectTuple;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.record.PathUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

/**
 * An action used to save the expanded/collapsed state of groups/templates
 * on a projects view.
 */
public class SaveProjectsLayoutAction extends ActionSupport
{
    private static final String PROPERTY_GROUP = "group";
    private static final String PROPERTY_COLLAPSED = "collapsed";
    private static final String PROPERTY_LAYOUT = "layout";

    /**
     * Sent as an array of objects, one per group, where objects have layout:
     *
     * <pre>{@code {
     *     group: &lt;group name&gt;,
     *     collapsed: true/false,
     *     layout: { &lt;template name&gt;: true/false , ...}
     * } }</pre>
     *
     * Where the nested layout property is a mapping of the collapsed state
     * of all of the templates visible within the group.
     */
    private String layout;
    private boolean dashboard;

    private ConfigurationProvider configurationProvider;

    public void setLayout(String layout)
    {
        this.layout = layout;
    }

    public void setDashboard(boolean dashboard)
    {
        this.dashboard = dashboard;
    }

    @Override
    public String execute() throws Exception
    {
        User user = getLoggedInUser();
        if (user != null)
        {
            Set<LabelProjectTuple> tuples = dashboard ? user.getDashboardCollapsed() : user.getBrowseViewCollapsed();
            tuples.clear();

            JSONArray array = new JSONArray(layout);
            for (int i = 0; i < array.length(); i++)
            {
                JSONObject group = array.getJSONObject(i);
                String label = group.getString(PROPERTY_GROUP);
                boolean collapsed = group.getBoolean(PROPERTY_COLLAPSED);
                if (collapsed)
                {
                    tuples.add(new LabelProjectTuple(label));
                }

                JSONObject templates = group.getJSONObject(PROPERTY_LAYOUT);
                Iterator it = templates.keys();
                while (it.hasNext())
                {
                    String template = (String) it.next();
                    long handle = getProjectHandle(template);

                    if (handle != 0)
                    {
                        boolean templateCollapsed = templates.getBoolean(template);
                        if (templateCollapsed)
                        {
                            tuples.add(new LabelProjectTuple(label, handle));
                        }
                    }
                }
            }

            userManager.save(user);
        }

        return SUCCESS;
    }

    private long getProjectHandle(String template)
    {
        ProjectConfiguration projectConfig = configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, template), ProjectConfiguration.class);
        if (projectConfig == null)
        {
            return 0;
        }
        else
        {
            return projectConfig.getHandle();
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
