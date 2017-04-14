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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.tove.annotations.Dropdown;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * The manually configured dependencies.
 */
@SymbolicName("zutubi.dependenciesConfiguration")
@Form(fieldOrder = {"version", "status", "publicationPattern", "retrievalPattern", "syncDestination"})
public class DependenciesConfiguration extends AbstractConfiguration
{
    private List<DependencyConfiguration> dependencies = new LinkedList<DependencyConfiguration>();

    @Required
    private String version = "$(build.number)";
    @Required
    @Constraint("com.zutubi.pulse.core.dependency.ivy.IvyPatternValidator")
    private String retrievalPattern = "lib/[artifact](.[ext])";
    private boolean unzipRetrievedArchives;
    private boolean syncDestination = BuildProperties.DEFAULT_SYNC_DESTINATION;  
    @Required
    @Constraint("com.zutubi.pulse.core.dependency.StatusValidator")
    @Dropdown(optionProvider = "com.zutubi.pulse.master.tove.config.project.BuildStatusOptionProvider")
    private String status = IvyStatus.STATUS_INTEGRATION;

    public DependenciesConfiguration()
    {
        setPermanent(true);
    }

    public List<DependencyConfiguration> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(List<DependencyConfiguration> dependencies)
    {
        this.dependencies = dependencies;
    }

    public String getRetrievalPattern()
    {
        return retrievalPattern;
    }

    public void setRetrievalPattern(String retrievalPattern)
    {
        this.retrievalPattern = retrievalPattern;
    }

    public boolean isUnzipRetrievedArchives()
    {
        return unzipRetrievedArchives;
    }

    public void setUnzipRetrievedArchives(boolean unzipRetrievedArchives)
    {
        this.unzipRetrievedArchives = unzipRetrievedArchives;
    }

    public boolean isSyncDestination()
    {
        return syncDestination;
    }

    public void setSyncDestination(boolean syncDestination)
    {
        this.syncDestination = syncDestination;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
