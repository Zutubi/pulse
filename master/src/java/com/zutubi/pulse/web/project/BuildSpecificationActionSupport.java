package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;
import java.util.LinkedList;
import java.io.ByteArrayInputStream;

/**
 */
public class BuildSpecificationActionSupport extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(BuildSpecificationActionSupport.class);

    protected List<String> recipes = new LinkedList<String>();
    protected ResourceRepository resourceRepository;
    protected long projectId;
    protected Project project;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        if (project == null)
        {
            project = getProjectManager().getProject(projectId);
        }
        return project;
    }

    public List<String> getRecipes()
    {
        return recipes;
    }

    protected void populateRecipes()
    {
        recipes.add("");
        FileLoader fileLoader = new PulseFileLoader(new ObjectFactory(), resourceRepository);
        try
        {
            PulseFileDetails details = getProject().getPulseFileDetails();
            ComponentContext.autowire(details);
            String pulseFile = details.getPulseFile(0, project, null);

            PulseFile file = new PulseFile();
            fileLoader.load(new ByteArrayInputStream(pulseFile.getBytes()), file, null, new RecipeListingPredicate());
            for(Recipe r: file.getRecipes())
            {
                recipes.add(r.getName());
            }
        }
        catch(Exception e)
        {
            // Ignore...we just don't show recipes
            LOG.warning("Unable to load pulse file for project '" + project.getName() + "': " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
