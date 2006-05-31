package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.ByteArrayInputStream;

/**
 */
public class BuildSpecificationActionSupport extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(BuildSpecificationActionSupport.class);

    protected List<String> recipes;
    protected ResourceRepository resourceRepository;
    protected Project project;
    private SlaveManager slaveManager;
    private Map<Long, String> buildHosts;

    protected String name;
    protected Long buildHost = 0L;
    protected BuildStage stage = new BuildStage();

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
        if(recipes == null)
        {
            recipes = new LinkedList<String>();
            populateRecipes();
        }
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

    public BuildStage getStage()
    {
        return stage;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<Long, String> getBuildHosts()
    {
        if(buildHosts == null)
        {
            List<Slave> slaves = slaveManager.getAll();

            buildHosts = new LinkedHashMap<Long, String>();
            buildHosts.put(0L, "[any]");
            buildHosts.put(1L, "[master]");

            for (Slave slave : slaves)
            {
                buildHosts.put(slave.getId(), slave.getName());
            }
        }

        return buildHosts;
    }

    public Long getBuildHost()
    {
        return buildHost;
    }

    public void setBuildHost(Long buildHost)
    {
        this.buildHost = buildHost;
    }

    protected void lookupAgent()
    {
        if (buildHost != 0 && buildHost != 1 && slaveManager.getSlave(buildHost) == null)
        {
            addActionError("Unknown agent [" + buildHost + "]");
        }
    }

    protected void addFieldsToStage(BuildSpecification spec, BuildSpecificationNode node)
    {
        stage.setName(name);

        if (buildHost == 0L)
        {
            stage.setHostRequirements(new AnyCapableBuildHostRequirements(spec, node));
        }
        else if(buildHost == 1L)
        {
            stage.setHostRequirements(new MasterBuildHostRequirements());
        }
        else
        {
            Slave slave = slaveManager.getSlave(buildHost);
            stage.setHostRequirements(new SlaveBuildHostRequirements(slave));
        }

        if(!TextUtils.stringSet(stage.getRecipe()))
        {
            stage.setRecipe(null);
        }
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }
}
