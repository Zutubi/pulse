package com.zutubi.pulse.master.webwork;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.util.StringUtils.uriComponentEncode;

/**
 * Helper object that computes consistent URLs for use in velocity templates
 * and such like.
 */
public class Urls
{
    private static final Urls BASELESS_INSTANCE = new Urls("");

    private String baseUrl;

    /**
     * @return a singleton Urls instance for relative URLs, i.e. those that
     *         have an empty base url
     */
    public static Urls getBaselessInstance()
    {
        return BASELESS_INSTANCE;
    }

    public Urls(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String base()
    {
        return baseUrl + "/";
    }

    public String login()
    {
        return baseUrl + "/login!input.action";
    }
    
    public String dashboard()
    {
        return base() + "dashboard/";
    }

    public String dashboardMyBuilds()
    {
        return dashboard() + "my/";
    }

    public String dashboardMyBuild(String number)
    {
        return dashboardMyBuilds() + number + "/";
    }

    public String dashboardMyBuildChanges(String number)
    {
        return dashboardMyBuild(number) + "changes/";
    }

    public String dashboardMyBuildDetails(String number)
    {
        return dashboardMyBuild(number) + "details/";
    }

    public String dashboardMyBuildStageDetails(String number, String stageName)
    {
        return dashboardMyBuildDetails(number) + stageName + "/";
    }

    public String dashboardMyBuildLog(String number)
    {
        return dashboardMyBuild(number) + "log/";
    }

    public String dashboardMyBuildTests(String number)
    {
        return dashboardMyBuild(number) + "tests/";
    }

    public String dashboardMyStageLog(String number, String stage)
    {
        return dashboardMyBuild(number) + "logs/" + uriComponentEncode(stage) + "/";
    }

    public String dashboardMyBuildFile(String number)
    {
        return dashboardMyBuild(number) + "file/";
    }

    public String dashboardMyBuildArtifacts(String number)
    {
        return dashboardMyBuild(number) + "artifacts/";
    }

    public String dashboardMyCommandArtifacts(String number, String stage, String command)
    {
        return dashboardMyBuildArtifacts(number) + uriComponentEncode(stage) + "/" + uriComponentEncode(command) + "/";
    }

    public String dashboardMyBuildWorkingCopy(String number)
    {
        return dashboardMyBuild(number) + "wc/";
    }

    public String dashboardPreferences()
    {
        return dashboard() + "preferences/";
    }

    public String browse()
    {
        return baseUrl + "/browse/";
    }

    public String projects()
    {
        return browse() + "projects/";
    }

    private String getEncodedProjectName(Object project)
    {
        String encodedName = null;
        if(project instanceof String)
        {
            // Should be pre-encoded name.
            encodedName = (String) project;
        }
        else if(project instanceof Project)
        {
            encodedName = uriComponentEncode(((Project)project).getName());
        }

        return encodedName;
    }

    public String project(Object project)
    {
        String encodedName = getEncodedProjectName(project);
        if (encodedName == null)
        {
            return projects();
        }

        return projects() + encodedName + "/";
    }

    public String projectHome(Object project)
    {
        return project(project) + "home/";
    }

    public String projectReports(Object project)
    {
        return project(project) + "reports/";
    }

    public String projectReports(Object project, String groupName)
    {
        return projectReports(project) + groupName + "/";
    }

    private String projectBuilds(Object project)
    {
        return project(project) + "builds/";
    }

    public String projectHistory(Object project)
    {
        return project(project) + "history/";
    }

    public String projectLog(Object project)
    {
        return project(project) + "log/";
    }

    public String projectChanges(Object project)
    {
        return project(project) + "changes/";
    }

    public String projectChangelist(Object project, long id)
    {
        return projectChanges(project) + Long.toString(id) + "/";
    }

    public String projectActions(Object project)
    {
        return project(project) + "actions/";
    }

    public String projectAction(Object project, String action)
    {
        if (action.equals(AccessManager.ACTION_WRITE))
        {
            return adminProject(project);
        }
        else
        {
            return projectActions(project) + action + "/";
        }
    }
    
    public String build(BuildResult build)
    {
        String prefix;
        if(build.isPersonal())
        {
            prefix = dashboardMyBuilds();
        }
        else
        {
            prefix = projectBuilds(build.getProject());
        }
        
        return prefix + Long.toString(build.getNumber()) + "/";
    }

    public String build(Object project, String number)
    {
        return projectBuilds(project) + number + "/";
    }

    public String buildSummary(BuildResult build)
    {
        return build(build) + "summary/";
    }

    public String buildSummary(Object project, String number)
    {
        return build(project, number) + "summary/";
    }

    public String buildDetails(BuildResult build)
    {
        return build(build) + "details/";
    }

    public String buildDetails(Object project, String number)
    {
        return build(project, number) + "details/";
    }

    public String stageDetails(BuildResult build, RecipeResultNode node)
    {
        return buildDetails(build) + getStageComponent(node);
    }

    public String stageDetails(Object project, String number, String stageName)
    {
        return buildDetails(project, number) + stageName + "/";
    }

    public String commandDetails(BuildResult build, RecipeResultNode node, CommandResult command)
    {
        return stageDetails(build, node) + uriComponentEncode(command.getCommandName()) + "/";
    }

    public String buildLogs(BuildResult build)
    {
        return build(build) + "logs/";
    }

    public String buildLog(BuildResult build)
    {
        return build(build) + "log/";
    }

    public String buildLog(Object project, String number)
    {
        return build(project, number) + "log/";
    }

    public String stageLogs(BuildResult build, RecipeResultNode node)
    {
        return buildLogs(build) + getStageComponent(node);
    }

    public String stageLogs(Object project, String number, Object stage)
    {
        return buildLog(project, number) + getStageComponent(stage);
    }

    public String buildChanges(BuildResult build)
    {
        return build(build) + "changes/";
    }

    public String buildChanges(Object project, String number)
    {
        return build(project, number) + "changes/";
    }

    public String buildChangelist(BuildResult build, long id)
    {
        return buildChanges(build) + Long.toString(id) + "/";
    }

    public String buildChangelist(Object project, String number, long id)
    {
        return buildChanges(project, number) + Long.toString(id) + "/";
    }

    public String buildTests(BuildResult build)
    {
        return build(build) + "tests/";
    }

    public String buildTests(Object project, String number)
    {
        return build(project, number) + "tests/";
    }

    public String stageTests(BuildResult build, RecipeResultNode node)
    {
        return buildTests(build) + getStageComponent(node);
    }

    public String stageTests(Object project, String number, String stage)
    {
        return buildTests(project, number) + uriComponentEncode(stage) + "/";
    }

    public String buildFile(BuildResult build)
    {
        return build(build) + "file/";
    }

    public String buildFile(Object project, String number)
    {
        return build(project, number) + "file/";
    }

    public String buildFileDownload(BuildResult build)
    {
        return buildFile(build) + "raw/";
    }

    public String buildArtifacts(BuildResult build)
    {
        return build(build) + "artifacts/";
    }

    public String buildArtifacts(Object project, String number)
    {
        return build(project, number) + "artifacts/";
    }

    public String stageArtifacts(BuildResult build, RecipeResultNode node)
    {
        return buildArtifacts(build) + getStageComponent(node);
    }

    public String commandArtifacts(BuildResult build, CommandResult commandResult)
    {
        RecipeResultNode recipeResultNode = build.findResultNode(commandResult);
        return buildArtifacts(build) + getStageComponent(recipeResultNode) + uriComponentEncode(commandResult.getCommandName()) + "/";
    }

    public String commandArtifacts(Object project, String number, String stage, String command)
    {
        return buildArtifacts(project, number) + uriComponentEncode(stage) + "/" + uriComponentEncode(command) + "/";
    }

    public String buildDownloads(BuildResult build)
    {
        return build(build) + "downloads/";
    }

    public String buildDownloads(Object project, String number)
    {
        return build(project, number) + "downloads/";
    }

    public String stageDownloads(BuildResult build, RecipeResultNode node)
    {
        return buildDownloads(build) + getStageComponent(node);
    }

    public String commandDownloads(BuildResult build, CommandResult commandResult)
    {
        RecipeResultNode recipeResultNode = build.findResultNode(commandResult);
        return buildDownloads(build) + getStageComponent(recipeResultNode) + uriComponentEncode(commandResult.getCommandName()) + "/";
    }

    public String commandDownloads(Object project, String number, String stage, String command)
    {
        return buildDownloads(project, number) + uriComponentEncode(stage) + "/" + uriComponentEncode(command) + "/";
    }

    public String commandDownload(Object project, String number, String stage, String command, String artifact)
    {
        return buildDownloads(project, number) + uriComponentEncode(stage) + "/" + uriComponentEncode(command) + "/" + uriComponentEncode(artifact) + "/";
    }

    public String buildWorkingCopy(BuildResult build)
    {
        return build(build) + "wc/";
    }

    public String buildWorkingCopy(Object project, String number)
    {
        return build(project, number) + "wc/";
    }

    private String getStageComponent(Object stage)
    {
        if (stage instanceof RecipeResultNode)
        {
            return uriComponentEncode(((RecipeResultNode)stage).getStageName()) + "/";
        }
        else if (stage instanceof String)
        {
            return uriComponentEncode((String)stage) + "/";
        }
        return null;
    }

    public String server()
    {
        return baseUrl + "/server/";
    }

    public String serverActivity()
    {
        return server() + "activity/";
    }

    public String serverMessages()
    {
        return server() + "messages/";
    }

    public String serverInfo()
    {
        return server() + "info/";
    }

    public String agents()
    {
        return baseUrl + "/agents/";
    }

    private String getEncodedAgentName(Object agent)
    {
        String encodedName = null;
        if(agent instanceof String)
        {
            // Should be pre-encoded name.
            encodedName = (String) agent;
        }
        else if(agent instanceof Agent)
        {
            encodedName = uriComponentEncode(((Agent)agent).getConfig().getName());
        }
        else if (agent instanceof AgentConfiguration)
        {
            encodedName = uriComponentEncode(((AgentConfiguration) agent).getName());
        }
        return encodedName;
    }

    public String agent(Object agent)
    {
        String encodedName = getEncodedAgentName(agent);
        if (encodedName == null)
        {
            return agents();
        }

        return agents() + encodedName + "/";
    }

    public String agentActions(Object agent)
    {
        return agent(agent) + "actions/";
    }

    public String agentAction(Object agent, String action)
    {
        if (action.equals(AccessManager.ACTION_WRITE))
        {
            return adminAgent(agent);
        }
        else
        {
            return agentActions(agent) + action + "/";
        }
    }

    public String agentStatus(Object agent)
    {
        return agent(agent) + "status/";
    }

    public String agentMessages(Object agent)
    {
        return agent(agent) + "messages/";
    }

    public String agentInfo(Object agent)
    {
        return agent(agent) + "info/";
    }

    public String admin()
    {
        return baseUrl + "/admin/";
    }

    public String adminProjects()
    {
        return admin() + "projects/";
    }

    public String adminProject(Object project)
    {
        return adminProjects() + getEncodedProjectName(project) + "/";
    }

    public String adminAgents()
    {
        return admin() + "agents/";
    }

    public String adminAgent(Object agent)
    {
        return adminAgents() + getEncodedAgentName(agent) + "/";
    }

    public String adminSettings()
    {
        return admin() + "settings/";
    }

    public String adminGroups()
    {
        return admin() + "groups/";
    }

    public String adminGroup(String group)
    {
        return adminGroups() + group + "/";
    }

    public String adminUsers()
    {
        return admin() + "users/";
    }

    public String adminUser(String user)
    {
        return adminUsers() + user + "/";
    }

    public String adminPlugins()
    {
        return admin() + "plugins/";
    }

    public String adminPlugin(String id)
    {
        return adminPlugins() + id + "/";
    }

    public String file()
    {
        return base() + "file/";
    }

    public String fileArtifacts()
    {
        return file() + "artifacts/";
    }

    public String fileArtifact(String id)
    {
        return fileArtifacts() + id + "/";
    }

    public String fileArtifact(StoredArtifact artifact)
    {
        return fileArtifact(Long.toString(artifact.getId()));
    }

    public String fileFileArtifact(String id, String filePath)
    {
        return fileArtifact(id) + filePath;
    }

    public String fileFileArtifact(StoredArtifact artifact, StoredFileArtifact file)
    {
        String pathPart = file.getPathUrl();
        // Strip the artifact name, it is implied by the id.
        pathPart = PathUtils.getPath(1, PathUtils.getPathElements(pathPart));
        return fileFileArtifact(Long.toString(artifact.getId()), pathPart);
    }

    public String image(String imageFile)
    {
        return base() + "images/" + imageFile;
    }
}
