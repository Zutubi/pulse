package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.PulseActionMapper;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.ui.ToveUiUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;

/**
 * Webwork environment specific tove utility methods.
 */
public class ToveUtils
{
    public static String getConfigURL(String path, String action, String submitField, String namespace)
    {
        String result = (namespace != null) ? namespace : PulseActionMapper.ADMIN_NAMESPACE;
        if (path != null)
        {
            String[] elements = PathUtils.getPathElements(path);
            for (String element : elements)
            {
                result += "/";
                result += WebUtils.uriComponentEncode(element);
            }
        }

        result = PathUtils.normalisePath(result);
        if (action != null && !action.equals("display") || submitField != null)
        {
            result = result + "?" + action;
        }

        if (submitField != null)
        {
            result = result + "=" + submitField;
        }

        return result;
    }

    /**
     * Creates an action link for a given action on the given data.  The link
     * includes extra UI decoration, e.g. the icon and potential
     * transformation of "delete" into "hide" for display purposes.  In order
     * to determine if the "delete" action should be "hide", the parent record
     * (i.e. the map) is required when getting action links for map items.
     *
     * @param actionName   action to create the link for
     * @param parentRecord if the parent record is a map, that map record,
     *                     otherwise null
     * @param key          if the parent record is a map, the map key of the
     *                     item that the action applies to, otherwise null
     * @param messages     used to format UI labels
     * @param systemPaths  used to locate icons
     * @return details of an action link for UI display
     */
    public static ActionLink getActionLink(String actionName, Record parentRecord, String key, Messages messages, SystemPaths systemPaths)
    {
        String action = actionName;
        actionName = ToveUiUtils.resolveActionName(actionName, parentRecord, key);
        File contentRoot = systemPaths.getContentRoot();
        return getActionLink(action, actionName, messages, contentRoot);
    }

    public static ActionLink getActionLink(String action, Messages messages, File contentRoot)
    {
        return getActionLink(action, action, messages, contentRoot);
    }

    private static ActionLink getActionLink(String action, String actionName, Messages messages, File contentRoot)
    {
        return new ActionLink(action, ToveUiUtils.format(messages, actionName + ConventionSupport.I18N_KEY_SUFFIX_LABEL), getActionIconName(actionName, contentRoot));
    }

    public static String getActionIconName(String actionName, File contentRoot)
    {
        File iconFile = new File(contentRoot, FileSystemUtils.composeFilename("images", "config", "actions", actionName + ".gif"));
        return iconFile.exists() ? actionName : "generic";
    }

    public static String getStatusIcon(BuildResult result)
    {
        return getStatusIcon(result.getState());
    }

    public static String getStatusIcon(ResultState state)
    {
        switch (state)
        {
            case SUCCESS:
                return "accept.gif";
            case WARNINGS:
                return "error.gif";
            case ERROR:
            case FAILURE:
            case TERMINATED:
                return "exclamation.gif";
            case IN_PROGRESS:
                return "inprogress.gif";
            case PENDING:
                return "hourglass.gif";
            case TERMINATING:
                return "stop.gif";
            default:
                return "help.gif";
        }
    }

    public static String getStatusClass(BuildResult result)
    {
        return getStatusClass(result.getState());
    }

    public static String getStatusClass(ResultState state)
    {
        switch (state)
        {
            case SUCCESS:
                return "success";
            case WARNINGS:
                return "warning";
            case ERROR:
            case FAILURE:
                return "failure";
            default:
                return "content";
        }
    }
}
