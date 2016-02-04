// dependency: ext/package.js

function each(a, f)
{
    var i;
    for(i = 0; i < a.length; i++)
    {
        f(a[i]);
    }
}

function stopEventPropagation(e)
{
    if (typeof e.stopPropagation === 'function')
    {
        e.stopPropagation();
    }
    else
    {
        e.cancelBubble = true;
    }
}

// Function to encode the components of a path using encodeURIComponent.  This
// effectively works like enodeURIComponent on the whole path but without
// encoding the slashes.
function encodeURIPath(path)
{
    var pieces, encodedPath, i;

    pieces = path.split('/');
    encodedPath = '';

    for (i = 0; i < pieces.length; i++)
    {
        if (encodedPath.length > 0)
        {
            encodedPath += '/';
        }

        encodedPath += encodeURIComponent(pieces[i]);
    }

    return encodedPath;
}

// Converts the given string to a valid HTML name by replacing any invalid
// characters with '.'.
function toHtmlName(s)
{
    return s.replace(/[^\w-:.]/g, '.');
}

// Function for opening a resource browse window used on several forms.
//   - resourceId: ID of textbox to receive the resource name
//   - versionId: ID of textbox to receive the resource version
//   - defaultVersionId: ID of textbox to receive the resource default version flag.
function openResourceBrowser(contextPath, resourceId, versionId, defaultVersionId)
{
    var browseWindow;
    browseWindow = window.open(contextPath + "/popups/browseResources.action?resourceId=" + resourceId + "&versionId=" + versionId + "&defaultVersionId=" + defaultVersionId, "resources", 'status=yes,resizable=yes,top=100,left=100,width=600,height=600,scrollbars=yes');
    browseWindow.focus();
}

function toggleDisplay(id)
{
    var element;

    element = Ext.getDom(id);
    if (element.style.display === 'none')
    {
        element.style.display = '';
    }
    else
    {
        element.style.display = 'none';
    }
}

// Toggle display for all success rows under the given table, identified by
// CSS class "successful".
function toggleSuccessfulTestRows(tableId, successfulShowing)
{
    var table, rows, i, successfulRow;

    table = Ext.getDom(tableId);
    if (table)
    {
        rows = table.getElementsByTagName('tr');

        for(i = 0; i < rows.length; i++)
        {
            successfulRow = rows[i].className.indexOf('successful') === 0;
            if(successfulRow)
            {
                rows[i].style.display = successfulShowing ? '' : 'none';
            }
        }
    }
}

/*===========================================================================
 * Functions used for configuration UI
 *=========================================================================*/

function handleConfigurationResponse(result)
{
    if (result.success)
    {
        if (result.newPanel)
        {
            detailPanel.update(result.newPanel);
        }

        if (typeof handleSuccessfulConfigurationResponse === 'function')
        {
            handleSuccessfulConfigurationResponse(result);
        }

        if (result.status)
        {
            showStatus(result.status.message, result.status.type);
        }
    }
    else
    {
        if (result.actionErrors && result.actionErrors.length > 0)
        {
            showStatus(result.actionErrors[0], 'failure');
        }
    }
}

function getParentPath(path)
{
    var index;

    index = path.lastIndexOf('/');
    if(index >= 0)
    {
        return path.slice(0, index);
    }

    return null;
}

function onSelectFailure(element, response)
{
    var message;

    if(response.status === 0)
    {
        showStatus(response.statusText, 'failure');
    }
    else
    {
        message = 'Pulse server returned status ' + String(response.status);
        if(response.statusText)
        {
            message = message + ' (' + response.statusText + ')';
        }

        showStatus(message, 'failure');
    }

    if(response.responseText)
    {
        element.update(response.responseText);
    }
    else
    {
        element.update('');
    }
}

function getAjaxCallbacks(maskedElement)
{
    return {
        success: function(response)
        {
            try
            {
                handleConfigurationResponse(Ext.decode(response.responseText));
            }
            finally
            {
                if(maskedElement)
                {
                    maskedElement.unmask();
                }

                window.actionInProgress = false;
            }
        },

        failure: function(/*response*/)
        {
            try
            {
                if(maskedElement)
                {
                    maskedElement.unmask();
                }
                showStatus('Unable to contact Pulse server', 'failure');
            }
            finally
            {
                window.actionInProgress = false;
            }
        }
    };
}

function runAjaxRequest(urlOrConfig)
{
    var config,
        pane;

    if (typeof urlOrConfig === typeof '')
    {
        config = {url: urlOrConfig};
    }
    else
    {
        config = urlOrConfig;
    }

    config.method = 'POST';
    config.params = config.params || {};
    config.params['pulse-session-token'] = window.sessionToken;

    if (!config.callback && !config.success)
    {
        pane = Ext.get('nested-layout');
        Ext.apply(config, getAjaxCallbacks(pane));
        pane.mask('Please wait...');
    }

    window.actionInProgress = true;
    Ext.Ajax.request(config);
}

function handleDialogResponse(options, success, response)
{
    var result;

    if (success)
    {
        result = Ext.util.JSON.decode(response.responseText);
        if (result.success)
        {
            if (window.refresh)
            {
                refresh(function() {
                    hideStatus();
                });
            }
            else
            {
                window.location.reload(true);
            }
        }
        else
        {
            showStatus(Ext.util.Format.htmlEncode(result.detail), 'failure');
        }

    }
    else
    {
        showStatus('Cannot contact server', 'failure');
    }
}

function showPromptDialog(title, message, prompt, multiline, statusMesage, url, params)
{
    window.dialogBox = Ext.Msg.show({
        title: title,
        msg: message,
        fn: function(btn, text) {
                window.dialogBox = null;
                if (btn === 'ok')
                {
                    if (prompt)
                    {
                        params.message = text;
                    }

                    showStatus(statusMesage , 'working');
                    runAjaxRequest({
                        url: window.baseUrl + url,
                        params: params,
                        callback: handleDialogResponse
                    });
                }
        },
        prompt: prompt,
        width: 400,
        multiline: multiline,
        buttons: Ext.Msg.OKCANCEL
    });
}

function takeResponsibility(projectId)
{
    showPromptDialog('Take Responsibility',
                     'Comment (optional):',
                     true,
                     false,
                     'Taking responsibility...',
                     '/ajax/takeResponsibility.action',
                     { projectId: projectId });
}

function clearResponsibility(projectId)
{
    showStatus('Clearing responsibility...', 'working');
    runAjaxRequest({
        url: window.baseUrl + '/ajax/clearResponsibility.action',
        params: { projectId: projectId },
        callback: handleDialogResponse
    });
}

function triggerBuild(projectName, triggerName, prompt, successCb)
{
    var actionWindow;

    function handleResult(data)
    {
        if (data.success)
        {
            Zutubi.core.reportSuccess(data.message);
            if (successCb)
            {
                successCb();
            }
        }
        else
        {
            Zutubi.core.reportError(data.message);
        }
    }

    if (prompt)
    {
        actionWindow = new Zutubi.config.ActionWindow({
            path: "projects/" + projectName,
            action: {
                action: 'trigger',
                variant: triggerName,
                label: triggerName
            },
            executed: handleResult
        });

        actionWindow.show();
    }
    else
    {
        showStatus('Triggering build...', 'working');
        Zutubi.core.ajax({
            type: "POST",
            url: "/api/action/single/trigger:" + Zutubi.config.encodePath(triggerName + "/projects/" + projectName),
            success: handleResult,
            error: function (jqXHR)
            {
                Zutubi.core.reportError("Could not trigger build: " + Zutubi.core.ajaxError(jqXHR));
            }
        });

    }
}

function deleteComment(agentId, buildId, commentId)
{
    showPromptDialog('Delete Comment',
                     'Are you sure you want to delete this comment?',
                     false,
                     false,
                     'Deleting comment...',
                     '/ajax/deleteComment.action',
                     { agentId: agentId, buildId: buildId, commentId: commentId });
}

function refreshPanel(id, url, callback)
{
    var panel, updater;

    panel = Ext.get(id);
    panel.mask('Refreshing...');
    updater = new Ext.Updater(id);
    updater.showLoadIndicator = false;
    updater.update({
        url: url,
        scripts: true,
        callback: function() {
            panel.unmask();
            if (callback)
            {
                callback();
            }
        }
    });
}

function handleCancelResponse(options, success, response)
{
    var result;

    if (success)
    {
        result = Ext.util.JSON.decode(response.responseText);
        refresh(function() {
            if (result.success)
            {
                showStatus(result.detail, 'success');
            }
            else
            {
                showStatus(Ext.util.Format.htmlEncode(result.detail), 'failure');
            }
        });
    }
    else
    {
        showStatus('Cannot contact server', 'failure');
    }
}

function cancelQueuedBuild(id)
{
    var url, params, confirm, status;
    url = window.baseUrl + '/ajax/cancelQueuedBuild.action';
    params = { id: id };

    if (id === -1)
    {
        confirm = 'all queued builds';
        status = 'queued builds';
    }
    else
    {
        confirm = 'this build';
        status = 'queued build';
    }

    window.dialogBox = Ext.Msg.show({
        title: 'Confirm',
        msg: 'Are you sure you want to cancel ' + confirm + '?',
        fn: function(btn, text) {
                window.dialogBox = null;
                if (btn === 'yes')
                {
                    showStatus('Cancelling ' + status + '...', 'working');
                    runAjaxRequest({
                        url: url,
                        params: params,
                        callback: handleCancelResponse
                    });
                }
        },
        width: 400,
        buttons: Ext.Msg.YESNO
    });
}

function cancelBuild(id, kill)
{
    var url, params, confirm, status;
    url = window.baseUrl + '/ajax/cancelBuild.action';
    params = { buildId: id, kill: kill };

    if (id === -1)
    {
        confirm = 'all running builds';
        status = 'all builds';
    }
    else
    {
        confirm = 'this build';
        status = 'build';
    }

    window.dialogBox = Ext.Msg.show({
        title: 'Confirm',
        msg: 'Are you sure you want to terminate ' + confirm + '?',
        fn: function(btn, text) {
                window.dialogBox = null;
                if (btn === 'yes')
                {
                    showStatus('Terminating ' + status + '...', 'working');
                    runAjaxRequest({
                        url: url,
                        params: params,
                        callback: handleCancelResponse
                    });
                }
        },
        width: 400,
        buttons: Ext.Msg.YESNO
    });
}
