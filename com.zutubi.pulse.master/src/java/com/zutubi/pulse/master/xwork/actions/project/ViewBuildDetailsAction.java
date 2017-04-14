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

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.DependencyManager;
import com.zutubi.pulse.master.model.StageRetrievedArtifacts;
import com.zutubi.util.Sort;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Action for rendering the build details tab.
 */
public class ViewBuildDetailsAction extends BuildStatusActionBase
{
    private List<StageRetrievedArtifacts> dependencyDetails;
    private List<StoredArtifact> implicitArtifacts = new LinkedList<StoredArtifact>();

    private DependencyManager dependencyManager;

    public boolean isDependencyDetailsPresent()
    {
        return dependencyDetails != null && any(dependencyDetails, new Predicate<StageRetrievedArtifacts>()
        {
            public boolean apply(StageRetrievedArtifacts stageDependencyDetails)
            {
                return stageDependencyDetails.isArtifactInformationAvailable() && stageDependencyDetails.getRetrievedArtifacts().size() > 0;
            }
        });
    }

    public List<StageRetrievedArtifacts> getDependencyDetails()
    {
        return dependencyDetails;
    }

    public boolean fileArtifactAvailable(StoredFileArtifact fileArtifact)
    {
        CommandResult commandResult = getCommandResult();
        final File outputDir = commandResult.getAbsoluteOutputDir(configurationManager.getDataDirectory());
        return new File(outputDir, fileArtifact.getPath()).exists();
    }

    public List<StoredArtifact> getImplicitArtifacts()
    {
        return implicitArtifacts;
    }

    public String execute()
    {
        super.execute();

        BuildResult result = getRequiredBuildResult();
        if (result.completed())
        {
            dependencyDetails = dependencyManager.loadRetrievedArtifacts(result);
        }
        
        CommandResult commandResult = getCommandResult();
        if (commandResult != null)
        {
            List<StoredArtifact> artifacts = commandResult.getArtifacts();
            addAll(implicitArtifacts, filter(artifacts, new Predicate<StoredArtifact>()
            {
                public boolean apply(StoredArtifact storedArtifact)
                {
                    return !storedArtifact.isExplicit();
                }
            }));
            final Sort.StringComparator stringComparator = new Sort.StringComparator();
            Collections.sort(implicitArtifacts, new Comparator<StoredArtifact>()
            {
                public int compare(StoredArtifact o1, StoredArtifact o2)
                {
                    return stringComparator.compare(o1.getName(), o2.getName());
                }
            });
        }

        return SUCCESS;
    }

    public void setDependencyManager(DependencyManager dependencyManager)
    {
        this.dependencyManager = dependencyManager;
    }
}