package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.vfs.provider.pulse.file.FileInfoRootFileObject;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;

/**
 * <class comment/>
 */
public class WorkingCopyStageFileObject extends FileInfoRootFileObject implements RecipeResultProvider
{
    private AgentManager agentManager;

    private static final String NO_WORKING_COPY_AVAILABLE = "no working copy available";

    private final String STAGE_FORMAT = "stage :: %s :: %s@%s";

    private final long recipeId;

    public WorkingCopyStageFileObject(final FileName name, final long recipeId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.recipeId = recipeId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        String childName = fileName.getBaseName();
        if (childName.equals(NO_WORKING_COPY_AVAILABLE))
        {
            return objectFactory.buildBean(TextMessageFileObject.class,
                    new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                    new Object[]{fileName, FileTypeConstants.MESSAGE, pfs}
            );
        }

        return super.createFile(fileName);
    }

    protected String[] doListChildren() throws FileSystemException
    {
        String[] children = super.doListChildren();
        if (children.length == 0)
        {
            return new String[]{NO_WORKING_COPY_AVAILABLE};
        }
        return children;
    }

    public List<FileInfo> getFileInfos(String path)
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        BuildResult buildResult = buildManager.getByRecipeId(recipeId);
        ProjectConfiguration projectConfig = buildResult.getProject().getConfig();

        Agent agent = agentManager.getAgent(node.getHost());
        AgentConfiguration agentConfig = agent.getConfig();

        AgentRecipeDetails details = getAgentRecipeDetails(node, buildResult, projectConfig, agentConfig);

        return agent.getService().getFileInfos(details, path);
    }

    public FileInfo getFileInfo(String path)
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        BuildResult buildResult = buildManager.getByRecipeId(recipeId);
        ProjectConfiguration projectConfig = buildResult.getProject().getConfig();

        Agent agent = agentManager.getAgent(node.getHost());
        AgentConfiguration agentConfig = agent.getConfig();

        AgentRecipeDetails details = getAgentRecipeDetails(node, buildResult, projectConfig, agentConfig);

        return agent.getService().getFileInfo(details, path);
    }

    private AgentRecipeDetails getAgentRecipeDetails(RecipeResultNode node, BuildResult buildResult, ProjectConfiguration projectConfig, AgentConfiguration agentConfig)
    {
        AgentRecipeDetails details = new AgentRecipeDetails();
        details.setAgent(agentConfig.getName());
        details.setAgentDataPattern(agentConfig.getDataDirectory());
        details.setAgentHandle(agentConfig.getHandle());

        details.setIncremental(!buildResult.isPersonal() && projectConfig.getScm().getCheckoutScheme() == CheckoutScheme.INCREMENTAL_UPDATE);
        details.setProject(projectConfig.getName());
        details.setProjectHandle(projectConfig.getHandle());
        details.setProjectPersistentPattern(projectConfig.getOptions().getPersistentWorkDir());

        details.setRecipeId(recipeId);
        details.setStage(node.getStageName());
        details.setStageHandle(node.getStageHandle());
        return details;
    }

    public String getDisplayName()
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        return String.format(STAGE_FORMAT, node.getStageName(), node.getResult().getRecipeNameSafe(), node.getHostSafe());
    }

    public RecipeResult getRecipeResult()
    {
        return buildManager.getRecipeResult(getRecipeResultId());
    }

    public long getRecipeResultId()
    {
        return recipeId;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
