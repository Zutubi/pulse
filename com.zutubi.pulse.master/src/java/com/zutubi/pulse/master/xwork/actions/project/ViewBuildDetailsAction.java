package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.DependencyManager;
import com.zutubi.pulse.master.model.StageRetrievedArtifacts;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
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
        return dependencyDetails != null && CollectionUtils.contains(dependencyDetails, new Predicate<StageRetrievedArtifacts>()
        {
            public boolean satisfied(StageRetrievedArtifacts stageDependencyDetails)
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
            CollectionUtils.filter(artifacts, new Predicate<StoredArtifact>()
            {
                public boolean satisfied(StoredArtifact storedArtifact)
                {
                    return !storedArtifact.isExplicit();
                }
            }, implicitArtifacts);
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