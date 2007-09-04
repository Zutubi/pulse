package com.zutubi.pulse.webwork.mapping;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.util.StringUtils;

/**
 * Helper object that computes consistent URLs for use in velocity templates
 * and such like.
 */
public class Urls
{
    private String baseUrl;

    public Urls(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String login()
    {
        return baseUrl + "/login.action";
    }
    
    public String browse()
    {
        return baseUrl + "/browse/";
    }

    public String projects()
    {
        return browse() + "projects/";
    }

    public String project(Object project)
    {
        String encodedName;
        if(project instanceof String)
        {
            // Should be pre-encoded name.
            encodedName = (String) project;
        }
        else if(project instanceof Project)
        {
            encodedName = StringUtils.uriComponentEncode(((Project)project).getName());
        }
        else
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

    private String projectBuilds(Object project)
    {
        return project(project) + "builds/";
    }

    public String projectHistory(Object project)
    {
        return project(project) + "history/";
    }

    public String projectChanges(Object project)
    {
        return project(project) + "changes/";
    }

    public String projectChangelist(Object project, long id)
    {
        return projectChanges(project) + Long.toString(id) + "/";
    }

    public String build(BuildResult build)
    {
        return projectBuilds(build.getProject()) + Long.toString(build.getNumber()) + "/";
    }

    public String build(Object project, String number)
    {
        return projectBuilds(project) + number + "/";
    }

    public String buildSummary(BuildResult build)
    {
        return build(build) + "summary/";
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
        return stageDetails(build, node) + StringUtils.uriComponentEncode(command.getCommandName()) + "/";
    }

    public String buildLogs(BuildResult build)
    {
        return build(build) + "logs/";
    }

    public String stageLogs(BuildResult build, RecipeResultNode node)
    {
        return buildLogs(build) + getStageComponent(node);
    }

    public String buildChanges(BuildResult build)
    {
        return build(build) + "changes/";
    }

    public String buildChangelist(BuildResult build, long id)
    {
        return buildChanges(build) + Long.toString(id) + "/";
    }

    public String buildTests(BuildResult build)
    {
        return build(build) + "tests/";
    }

    public String stageTests(BuildResult build, RecipeResultNode node)
    {
        return buildTests(build) + getStageComponent(node);
    }

    public String buildFile(BuildResult build)
    {
        return build(build) + "file/";
    }

    public String buildFileDownload(BuildResult build)
    {
        return buildFile(build) + "raw/";
    }

    public String buildArtifacts(BuildResult build)
    {
        return build(build) + "artifacts/";
    }

    public String stageArtifacts(BuildResult build, RecipeResultNode node)
    {
        return buildArtifacts(build) + getStageComponent(node);
    }

    public String commandArtifacts(BuildResult build, CommandResult commandResult)
    {
        RecipeResultNode recipeResultNode = build.findResultNode(commandResult);
        return buildArtifacts(build) + getStageComponent(recipeResultNode) + StringUtils.uriComponentEncode(commandResult.getCommandName()) + "/";
    }

    public String buildWorkingCopy(BuildResult build)
    {
        return build(build) + "wc/";
    }

    private String getStageComponent(RecipeResultNode node)
    {
        return StringUtils.uriComponentEncode(node.getStageName()) + "/";
    }

    public String agents()
    {
        return baseUrl + "/agents/";
    }

    public String agent(Object agent)
    {
        String encodedName;
        if(agent instanceof String)
        {
            // Should be pre-encoded name.
            encodedName = (String) agent;
        }
        else if(agent instanceof Agent)
        {
            encodedName = StringUtils.uriComponentEncode(((Agent)agent).getConfig().getName());
        }
        else
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
        return agentActions(agent) + action + "/";
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

    public String adminProject(String project)
    {
        return adminProjects() + project + "/";
    }
}
