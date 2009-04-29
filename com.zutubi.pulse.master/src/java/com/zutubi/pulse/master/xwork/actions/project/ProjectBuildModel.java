package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.util.TextUtils;
import static com.opensymphony.util.TextUtils.htmlEncode;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.BuildColumns;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Mapping;
import com.zutubi.util.Pair;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON-encodable object representing a single build result.  The configurable
 * build columns are pre-rendered as HTML, as an optimisation (less rendering
 * time on the client side, and significantly simpler data format).
 */
public class ProjectBuildModel
{
    private static final String LABEL_REMAINING = "remaining";
    private static final String LABEL_TIME = "time";

    private static final String REVISION_PERSONAL = "personal";
    private static final String REVISION_NONE = "none";
    private static final String TESTS_NONE = "none";
    private static final String VERSION_NONE = "none";

    private static final String TAG_ANCHOR = "a";
    private static final String TAG_IMAGE = "img";
    private static final String TAG_SPAN = "span";

    private static final String ATTRIBUTE_ALTERNATIVE = "alt";
    private static final String ATTRIBUTE_CLASS = "class";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_ONCLICK = "onclick";
    private static final String ATTRIBUTE_SOURCE = "src";
    private static final String ATTRIBUTE_STYLE = "style";
    private static final String ATTRIBUTE_TITLE = "title";

    private long number;
    private ResultState state;
    private String status;
    private String statusIcon;
    private List<String> columns = new LinkedList<String>();

    public ProjectBuildModel(final BuildResult buildResult, ProjectsSummaryConfiguration configuration, final Urls urls)
    {
        number = buildResult.getNumber();
        state = buildResult.getState();
        status = buildResult.getState().getPrettyString();
        statusIcon = ToveUtils.getStatusIcon(buildResult);
        columns = CollectionUtils.map(configuration.getColumns(), new Mapping<String, String>()
        {
            public String map(String column)
            {
                return renderColumn(buildResult, column, urls);
            }
        });
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

    @JSON
    public List<String> getColumns()
    {
        return columns;
    }

    private String renderColumn(BuildResult buildResult, String column, Urls urls)
    {
        String label = column;
        String content;
        if (column.equals(BuildColumns.KEY_VERSION))
        {
            String version = buildResult.getVersion();
            if (!TextUtils.stringSet(version))
            {
                version = VERSION_NONE;
            }

            content = htmlEncode(version);
        }
        else if (column.equals(BuildColumns.KEY_ERRORS))
        {
            content = Integer.toString(buildResult.getErrorFeatureCount());
        }
        else if (column.equals(BuildColumns.KEY_REASON))
        {
            content = htmlEncode(buildResult.getReason().getSummary());
        }
        else if (column.equals(BuildColumns.KEY_REVISION))
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
                    ChangeViewerConfiguration changeViewer = buildResult.getProject().getConfig().getChangeViewer();
                    String revisionString = renderRevisionString(revision);
                    if (changeViewer == null)
                    {
                        content = revisionString;
                    }
                    else
                    {
                        content = link(revisionString, changeViewer.getRevisionURL(revision));
                    }
                }
            }
        }
        else if (column.equals(BuildColumns.KEY_ELAPSED))
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
        else if (column.equals(BuildColumns.KEY_WHEN))
        {
            String timeId = "time." + buildResult.getId();
            String dateId = "date." + buildResult.getId();

            String date = buildResult.getStamps().getPrettyStartDate(ActionContext.getContext().getLocale());

            String image = wrap(null, TAG_IMAGE, asPair(ATTRIBUTE_ALTERNATIVE, "toggle format"), asPair(ATTRIBUTE_SOURCE, urls.image("calendar.gif")));
            content = wrap(image, TAG_ANCHOR, asPair(ATTRIBUTE_HREF, "#"), asPair(ATTRIBUTE_CLASS, "unadorned"), asPair(ATTRIBUTE_TITLE, date) , asPair(ATTRIBUTE_ONCLICK, "toggleDisplay('" + timeId + "'); toggleDisplay('" + dateId + "'); return false;"));
            content += " " + wrap(buildResult.getStamps().getPrettyStartTime(), TAG_SPAN, asPair(ATTRIBUTE_ID, timeId));
            content += wrap(date, TAG_SPAN, asPair(ATTRIBUTE_ID, dateId), asPair(ATTRIBUTE_STYLE, "display: none"));
        }
        else if (column.equals(BuildColumns.KEY_TESTS))
        {
            TestResultSummary summary = buildResult.getTestSummary();
            if (summary.getTotal() == 0)
            {
                content = TESTS_NONE;
            }
            else
            {
                if (summary.hasBroken())
                {
                    content = Integer.toString(summary.getBroken()) + " of " + summary.getTotal() + " broken";
                }
                else
                {
                    content = Integer.toString(summary.getTotal()) + " passed";
                }

                if (summary.hasSkipped())
                {
                    content += " (" + summary.getSkipped() + " skipped)";
                }

                content = link(content, urls.buildTests(buildResult));
            }
        }
        else if (column.equals(BuildColumns.KEY_WARNINGS))
        {
            content = Integer.toString(buildResult.getWarningFeatureCount());
        }
        else
        {
            content = "unknown";
        }

        return label + ": " + content;
    }

    private String renderRevisionString(Revision revision)
    {
        if (revision.isAbbreviated())
        {
            return wrap(htmlEncode(revision.getAbbreviatedRevisionString()), TAG_SPAN, asPair(ATTRIBUTE_TITLE, htmlEncode(revision.getRevisionString())));
        }
        else
        {
            return htmlEncode(revision.getRevisionString());
        }
    }

    private String link(String content, String url)
    {
        return wrap(content, TAG_ANCHOR, asPair(ATTRIBUTE_HREF, url));
    }

    private String wrap(String content, String tag, Pair<String, String>... attributes)
    {
        StringBuilder builder = new StringBuilder();
        builder.append('<');
        builder.append(tag);
        for (Pair<String, String> attribute: attributes)
        {
            builder.append(' ');
            builder.append(attribute.first);
            builder.append("=\"");
            builder.append(attribute.second);
            builder.append('"');
        }

        if (content == null)
        {
            builder.append("/>");
        }
        else
        {
            builder.append('>');
            builder.append(content);
            builder.append("</");
            builder.append(tag);
            builder.append('>');
        }

        return builder.toString();
    }
}
