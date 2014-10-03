package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildType;
import com.zutubi.pulse.master.tove.config.project.CheckoutType;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.vfs.provider.pulse.file.FileInfoRootFileObject;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;

/**
 * A file object that represents the root of a working copy within the pulse file system.
 */
public class WorkingCopyStageFileObject extends FileInfoRootFileObject implements RecipeResultProvider
{
    private static final Messages I18N = Messages.getInstance(WorkingCopyStageFileObject.class);

    private AgentManager agentManager;

    private static final String NO_WORKING_COPY_AVAILABLE = I18N.format("no.working.copy.available");

    private static final String STAGE_FORMAT = "stage :: %s :: %s@%s";

    private final long recipeId;

    public WorkingCopyStageFileObject(final FileName name, final long recipeId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.recipeId = recipeId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String childName = fileName.getBaseName();
        if (childName.equals(NO_WORKING_COPY_AVAILABLE))
        {
            return objectFactory.buildBean(TextMessageFileObject.class, fileName, null, pfs);
        }

        FileInfo child = getFileInfo(fileName.getBaseName());

        return objectFactory.buildBean(WorkingCopyFileInfoFileObject.class, child, fileName, pfs);
    }

    protected String[] doListChildren() throws Exception
    {
        String[] children = super.doListChildren();
        if (children.length == 0)
        {
            return new String[]{NO_WORKING_COPY_AVAILABLE};
        }
        return children;
    }

    public List<FileInfo> getFileInfos(String path) throws Exception
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        BuildResult buildResult = buildManager.getByRecipeId(recipeId);
        ProjectConfiguration projectConfig = buildResult.getProject().getConfig();

        if (node.getAgentName() == null)
        {
            throw new FileSystemException("host.not.assigned");
        }

        Agent agent = agentManager.getAgent(node.getAgentName());
        if (!agent.isOnline())
        {
            throw new FileSystemException("host.not.available");
        }

        AgentConfiguration agentConfig = agent.getConfig();
        AgentRecipeDetails details = getAgentRecipeDetails(node, buildResult, projectConfig, agentConfig);

        try
        {
            return agent.getService().getFileListing(details, path);
        }
        catch (Exception e)
        {
            throw new FileSystemException(e);
        }
    }

    public FileInfo getFileInfo(String path)
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        BuildResult buildResult = buildManager.getByRecipeId(recipeId);
        ProjectConfiguration projectConfig = buildResult.getProject().getConfig();

        Agent agent = agentManager.getAgent(node.getAgentName());
        AgentConfiguration agentConfig = agent.getConfig();

        AgentRecipeDetails details = getAgentRecipeDetails(node, buildResult, projectConfig, agentConfig);

        return agent.getService().getFile(details, path);
    }

    private AgentRecipeDetails getAgentRecipeDetails(RecipeResultNode node, BuildResult buildResult, ProjectConfiguration projectConfig, AgentConfiguration agentConfig)
    {
        AgentRecipeDetails details = new AgentRecipeDetails();
        details.setAgent(agentConfig.getName());
        details.setAgentDataPattern(agentConfig.getStorage().getDataDirectory());
        details.setAgentHandle(agentConfig.getHandle());

        details.setIncremental(!buildResult.isPersonal() && projectConfig.getBootstrap().getBuildType() == BuildType.INCREMENTAL_BUILD);
        details.setUpdate(!buildResult.isPersonal() && projectConfig.getBootstrap().getCheckoutType() == CheckoutType.INCREMENTAL_CHECKOUT);
        details.setProject(projectConfig.getName());
        details.setProjectHandle(projectConfig.getHandle());
        details.setProjectPersistentPattern(projectConfig.getBootstrap().getPersistentDirPattern());
        details.setProjectTempPattern(projectConfig.getBootstrap().getTempDirPattern());

        details.setRecipeId(recipeId);
        details.setStage(node.getStageName());
        details.setStageHandle(node.getStageHandle());
        return details;
    }

    public String getDisplayName()
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        return String.format(STAGE_FORMAT, node.getStageName(), node.getResult().getRecipeNameSafe(), node.getAgentNameSafe());
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
