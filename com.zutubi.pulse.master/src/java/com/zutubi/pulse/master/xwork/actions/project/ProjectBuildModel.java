package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Function;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.BuildReason;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.variables.GenericVariable;
import com.zutubi.tove.variables.HashVariableMap;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.ResolutionException;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.time.TimeStamps;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.opensymphony.util.TextUtils.htmlEncode;
import static com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration.*;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * JSON-encodable object representing a single build result.  The configurable
 * build columns are pre-rendered as HTML, as an optimisation (less rendering
 * time on the client side, and significantly simpler data format).
 */
public class ProjectBuildModel
{
    private static final String LABEL_REMAINING = "remaining";
    private static final String LABEL_TIME = "time";

    private static final String REASON_NONE = "none";
    private static final String MATURITY_NONE = "none";
    private static final String REVISION_PERSONAL = "personal";
    private static final String REVISION_NONE = "none";
    private static final String TESTS_NONE = "none";
    private static final String VERSION_NONE = "none";

    private long number;
    private ResultState state;
    private String status;
    private String statusIcon;
    private CommentSummaryModel comments;
    private List<String> columns = new LinkedList<String>();
    private boolean absoluteTimestamps;

    public ProjectBuildModel(final BuildResult buildResult, ProjectsSummaryConfiguration configuration, final Urls urls, boolean absoluteTimestamps)
    {
        this.absoluteTimestamps = absoluteTimestamps;
        number = buildResult.getNumber();
        state = buildResult.getState();
        status = formatStatus(buildResult, urls);
        statusIcon = ToveUtils.getStatusIcon(buildResult);
        comments = new CommentSummaryModel(buildResult.getComments());
        columns = newArrayList(transform(configuration.getColumns(), new Function<String, String>()
        {
            public String apply(String column)
            {
                return renderColumn(buildResult, column, urls);
            }
        }));
    }

    private String formatStatus(BuildResult buildResult, Urls urls)
    {
        String result;
        TimeStamps stamps = buildResult.getStamps();
        if (buildResult.inProgress() && stamps.hasEstimatedTimeRemaining())
        {
            // Show a progress bar.
            int percentComplete = stamps.getEstimatedPercentComplete();
            int percentRemaining = 100 - stamps.getEstimatedPercentComplete();

            result = "";
            if (percentComplete > 0)
            {
                result += formatBar(urls.base(), percentComplete, stamps.getPrettyElapsed(), "elapsed");
            }

            if (percentRemaining > 0)
            {
                result += formatBar(urls.base(), percentRemaining, stamps.getPrettyEstimatedTimeRemaining(), "remaining");
            }
        }
        else
        {
            result = buildResult.getState().getPrettyString();
        }

        return result;
    }

    private String formatBar(String base, int percent, String pretty, String type)
    {
        return merge("<img class='centre' title='${pretty} (${percent}%) ${type}' src='${base}images/box-${type}.gif' height='10' width='${percent}'/>",
                        asPair("base", base),
                        asPair("percent", Integer.toString(percent)),
                        asPair("type", type),
                        asPair("pretty", pretty)
                );
    }

    public long getNumber()
    {
        return number;
    }

    @JSON(include = false)
    public ResultState getState()
    {
        return state;
    }

    public String getStatus()
    {
        return status;
    }

    public String getStatusIcon()
    {
        return statusIcon;
    }

    public CommentSummaryModel getComments()
    {
        return comments;
    }

    @JSON
    public List<String> getColumns()
    {
        return columns;
    }

    private String renderColumn(BuildResult buildResult, String column, Urls urls)
    {
        String label = column;
        String content;
        if (column.equals(KEY_VERSION))
        {
            String version = buildResult.getVersion();
            if (!TextUtils.stringSet(version))
            {
                version = VERSION_NONE;
            }

            content = htmlEncode(version);
        }
        else if (column.equals(KEY_ERRORS))
        {
            content = Integer.toString(buildResult.getErrorFeatureCount());
        }
        else if (column.equals(KEY_REASON))
        {
            BuildReason reason = buildResult.getReason();
            if (reason == null)
            {
                content = REASON_NONE;
            }
            else
            {
                content = htmlEncode(reason.getSummary());
            }
        }
        else if (column.equals(KEY_REVISION))
        {
            if (buildResult.isPersonal())
            {
                content = REVISION_PERSONAL;
            }
            else
            {
                Revision revision = buildResult.getRevision();
                if (revision == null)
                {
                    content = REVISION_NONE;
                }
                else
                {
                    ProjectConfiguration projectConfig = buildResult.getProject().getConfig();
                    ChangeViewerConfiguration changeViewer = projectConfig.getChangeViewer();
                    String revisionString = renderRevisionString(revision);
                    if (changeViewer == null)
                    {
                        content = revisionString;
                    }
                    else
                    {
                        content = link(revisionString, changeViewer.getRevisionURL(projectConfig, revision));
                    }
                }
            }
        }
        else if (column.equals(KEY_ELAPSED))
        {
            if (buildResult.completed())
            {
                label = LABEL_TIME;
                content = buildResult.getStamps().getPrettyElapsed();
            }
            else
            {
                label = LABEL_REMAINING;
                content = buildResult.getStamps().getPrettyEstimatedTimeRemaining();
            }
        }
        else if (column.equals(KEY_WHEN))
        {
            content = renderTime(buildResult, buildResult.getStamps().getStartTime(), "start", urls);
        }
        else if (column.equals(KEY_COMPLETED))
        {
            content = renderTime(buildResult, buildResult.getStamps().getEndTime(), "end", urls);
        }
        else if (column.equals(KEY_TESTS))
        {
            TestResultSummary summary = buildResult.getTestSummary();
            if (summary == null || summary.getTotal() == 0)
            {
                content = TESTS_NONE;
            }
            else
            {
                int run = summary.getTotal() - summary.getSkipped();
                if (summary.hasBroken())
                {
                    content = Integer.toString(summary.getBroken()) + " of " + run + " broken";
                }
                else
                {
                    content = Integer.toString(run) + " passed";
                }

                if (summary.hasSkipped())
                {
                    content += " (" + summary.getSkipped() + " skipped)";
                }

                content = link(content, urls.buildTests(buildResult));
            }
        }
        else if (column.equals(KEY_WARNINGS))
        {
            content = Integer.toString(buildResult.getWarningFeatureCount());
        }
        else if (column.equals(KEY_MATURITY))
        {
            content = buildResult.getStatus();
            if (content == null)
            {
                content = MATURITY_NONE;
            }
        }
        else
        {
            content = "unknown";
        }

        return label + ": " + content;
    }

    private String renderTime(BuildResult buildResult, long time, String type, Urls urls)
    {
        if (time == TimeStamps.UNINITIALISED_TIME)
        {
            return "n/a";
        }
        else
        {
            String idSuffix = type + "." + buildResult.getId();
            return merge("<a href='#' class='unadorned' title='${date}' onclick=\"toggleDisplay('${timeId}'); toggleDisplay('${dateId}'); return false;\">" +
                                "<img alt='toggle format' src='${base}images/calendar.gif'/>" +
                            "</a> " +
                            "<span id='${timeId}' style='display: ${relativeDisplay}'>${time}</span>" +
                            "<span id='${dateId}' style='display: ${absoluteDisplay}'>${date}</span>",
                    asPair("base", urls.base()),
                    asPair("timeId", "time." + idSuffix),
                    asPair("dateId", "date." + idSuffix),
                    asPair("date", TimeStamps.getPrettyDate(time, ActionContext.getContext().getLocale())),
                    asPair("time", TimeStamps.getPrettyTime(time)),
                    asPair("absoluteDisplay", absoluteTimestamps ? "inline" : "none"),
                    asPair("relativeDisplay", absoluteTimestamps ? "none" : "inline"));
        }
    }

    private String renderRevisionString(Revision revision)
    {
        if (revision.isAbbreviated())
        {
            return merge("<span title='${rev}'>${shortRev}</span>",
                    asPair("rev", htmlEncode(revision.getRevisionString())),
                    asPair("shortRev", htmlEncode(revision.getAbbreviatedRevisionString())));
        }
        else
        {
            return htmlEncode(revision.getRevisionString());
        }
    }

    private String merge(String template, Pair<String, String>... properties)
    {
        VariableMap variableMap = new HashVariableMap();
        for (Pair<String, String> p: properties)
        {
            variableMap.add(new GenericVariable<String>(p.first, p.second));
        }

        try
        {
            return VariableResolver.resolveVariables(template, variableMap, VariableResolver.ResolutionStrategy.RESOLVE_NON_STRICT);
        }
        catch (ResolutionException e)
        {
            // Never happens
            return template;
        }
    }

    private String link(String content, String url)
    {
        return merge("<a href='${url}'>${content}</a>", asPair("content", content), asPair("url", url));
    }
}
