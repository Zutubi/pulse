package com.zutubi.pulse.master.api;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.util.time.TimeStamps;

import java.util.*;

/**
 * Utility methods shared by XML-RPC API classes.
 */
public class ApiUtils
{
    /**
     * Maps a list of build results into an array of structs.
     *
     * @param builds        the builds to convert
     * @param includeStages if true, include details of build stages in the
     *                      returned structs, if false omit these details
     * @return the builds ready for automatic conversion to an array of structs
     */
    public static Vector<Hashtable<String, Object>> mapBuilds(List<BuildResult> builds, boolean includeStages)
    {
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for (BuildResult build : builds)
        {
            result.add(convertBuild(build, includeStages));
        }
        return result;
    }

    /**
     * Converts a single build result to its XML-RPC struct equivalent.
     *
     * @param build         the build to convert
     * @param includeStages if true, include details of all stages in the
     *                      returned struct, if false omit these details
     * @return the build details ready for automatic struct conversion
     */
    public static Hashtable<String, Object> convertBuild(BuildResult build, boolean includeStages)
    {
        Hashtable<String, Object> buildDetails = new Hashtable<String, Object>();
        buildDetails.put("id", (int) build.getNumber());
        buildDetails.put("project", build.getProject().getName());
        buildDetails.put("owner", getOwner(build));
        buildDetails.put("personal", build.isPersonal());
        buildDetails.put("revision", getBuildRevision(build));
        buildDetails.put("tests", convertTests(build.getTestSummary()));
        buildDetails.put("version", getBuildVersion(build));
        buildDetails.put("reason", build.getReason().getSummary());
        buildDetails.put("maturity", build.getStatus());
        buildDetails.put("pinned", build.isPinned());
        addResultFields(build, buildDetails);

        if (includeStages)
        {
            Vector<Hashtable<String, Object>> stages = new Vector<Hashtable<String, Object>>();
            for (RecipeResultNode rrn : build.getStages())
            {
                stages.add(convertStage(rrn));
            }
            buildDetails.put("stages", stages);
        }

        return buildDetails;
    }

    private static String getOwner(BuildResult build)
    {
        NamedEntity owner = build.getOwner();
        return owner == null ? "" : owner.getName();
    }

    /**
     * Extract the build version string from the build result, returning an
     * empty string if no build version is available.
     *
     * @param build from which the build version is being retrieved.
     * @return a string representing the build version, or an empty string if no
     *         build version is available.
     */
    public static String getBuildVersion(BuildResult build)
    {
        if (build.getVersion() != null)
        {
            return build.getVersion();
        }
        return "";
    }

    private static String getBuildRevision(BuildResult build)
    {
        Revision revision = build.getRevision();
        if (revision != null)
        {
            return revision.getRevisionString();
        }

        return "";
    }

    private static Hashtable<String, Object> convertStage(RecipeResultNode recipeResultNode)
    {
        Hashtable<String, Object> stage = new Hashtable<String, Object>();
        stage.put("name", recipeResultNode.getStageName());
        stage.put("recipe", recipeResultNode.getResult().getRecipeNameSafe());
        stage.put("agent", recipeResultNode.getAgentNameSafe());
        stage.put("tests", convertTests(recipeResultNode.getResult().getTestSummary()));
        stage.put("commands", convertCommands(recipeResultNode));

        addResultFields(recipeResultNode.getResult(), stage);

        return stage;
    }

    private static Vector<Hashtable<String, Object>> convertCommands(final RecipeResultNode node)
    {
        Vector<Hashtable<String, Object>> commands = new Vector<Hashtable<String, Object>>();
        for (CommandResult command : node.getResult().getCommandResults())
        {
            Hashtable<String, Object> detail = new Hashtable<String, Object>();
            detail.put("name", command.getCommandName());
            addResultFields(command, detail);

            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            Enumeration propertyNames = command.getProperties().propertyNames();
            while(propertyNames.hasMoreElements())
            {
                String propertyName = (String) propertyNames.nextElement();
                properties.put(propertyName, command.getProperties().get(propertyName));
            }
            detail.put("properties", properties);

            commands.add(detail);
        }
        return commands;
    }

    private static void addResultFields(Result result, Hashtable<String, Object> details)
    {
        details.put("status", result.getState().getPrettyString());
        details.put("completed", result.completed());
        details.put("succeeded", result.healthy());
        details.put("errorCount", result.getErrorFeatureCount());
        details.put("warningCount", result.getWarningFeatureCount());

        TimeStamps timeStamps = result.getStamps();
        details.put("startTime", new Date(timeStamps.getStartTime()));
        details.put("startTimeMillis", Long.toString(timeStamps.getStartTime()));
        details.put("endTime", new Date(timeStamps.getEndTime()));
        details.put("endTimeMillis", Long.toString(timeStamps.getEndTime()));
        if (timeStamps.hasEstimatedTimeRemaining())
        {
            details.put("progress", timeStamps.getEstimatedPercentComplete());
        }
        else
        {
            details.put("progress", -1);
        }
    }

    private static Hashtable<String, Object> convertTests(TestResultSummary testSummary)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put("total", testSummary.getTotal());
        result.put("passed", testSummary.getPassed());
        result.put("skipped", testSummary.getSkipped());
        result.put("expectedFailures", testSummary.getExpectedFailures());
        result.put("failures", testSummary.getFailures());
        result.put("errors", testSummary.getErrors());
        return result;
    }
}
