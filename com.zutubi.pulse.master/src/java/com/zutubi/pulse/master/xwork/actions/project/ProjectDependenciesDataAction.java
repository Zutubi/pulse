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

import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;

/**
 * Action to display project dependencies page - upstream and downstream
 * dependency trees.
 */
public class ProjectDependenciesDataAction extends ProjectDependenciesAction
{
    private ProjectDependencyGraphBuilder projectDependencyGraphBuilder;
    private ProjectDepenenciesModel model;
    
    private ConfigurationManager configurationManager;

    public ProjectDepenenciesModel getModel()
    {
        return model;
    }

    public String execute()
    {
        ProjectDependencyGraphBuilder.TransitiveMode mode = lookupTransitiveMode();

        Project project = getRequiredProject();
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        ProjectDependencyGraph dependencyGraph = projectDependencyGraphBuilder.build(project, mode);
        ProjectDependencyGraphRenderer renderer = new ProjectDependencyGraphRenderer(buildManager, urls);
        model = new ProjectDepenenciesModel(renderer.renderUpstream(dependencyGraph), renderer.renderDownstream(dependencyGraph));
        
        return SUCCESS;
    }

    public void setProjectDependencyGraphBuilder(ProjectDependencyGraphBuilder projectDependencyGraphBuilder)
    {
        this.projectDependencyGraphBuilder = projectDependencyGraphBuilder;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
