// dependency: ext/package.js

function each(a, f)
{
    for(var i = 0; i < a.length; i++)
    {
        f(a[i]);
    }
}

function stopEventPropagation(e)
{
    if (typeof e.stopPropagation == 'function')
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
    var pieces = path.split('/');
    var encodedPath = '';
    for (var i = 0; i < pieces.length; i++)
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

// Function to toggle the enable state of a control based on the state of a
// checkbox.
function setEnableState(id, checkboxId, inverse)
{
    var element = Ext.getDom(id);
    var disabled = !Ext.getDom(checkboxId).checked;

    if(inverse)
    {
        disabled = !disabled;
    }

    element.disabled = disabled;
}

// Sets the enabled state of all controls in a form (excepting the checkbox
// itself and submits (buttons)) based on the state of a checkbox.
function setFormEnableState(formId, checkboxId, includeSubmit, inverse)
{
    var disabled = !Ext.getDom(checkboxId).checked;

    if(inverse)
    {
        disabled = !disabled;
    }

    var form = Ext.getDom(formId);
    var fields = form.elements;

    for(var i = 0; i < fields.length; i++)
    {
        var field = fields[i];
        if(field.id != checkboxId && (includeSubmit || field.type && field.type != "submit"))
        {
            fields[i].disabled = disabled;
        }
    }
}

function confirmUrl(message, url)
{
    if (confirm(message))
    {
        location.href = url;
    }
}

// Function for opening a resource browse window used on several forms.
//   - resourceId: ID of textbox to receive the resource name
//   - versionId: ID of textbox to receive the resource version
//   - defaultVersionId: ID of textbox to receive the resource default version flag.
function openResourceBrowser(contextPath, resourceId, versionId, defaultVersionId)
{
    var browseWindow = window.open(contextPath + "/popups/browseResources.action?resourceId=" + resourceId + "&versionId=" + versionId + "&defaultVersionId=" + defaultVersionId, "resources", 'status=yes,resizable=yes,top=100,left=100,width=600,height=600,scrollbars=yes');
    browseWindow.opener = self;
    browseWindow.focus();
}

// @deprecated. Use the Prototype function Element.toggle instead.
function toggleElementDisplay(element)
{
    if(element.style.display == 'none')
    {
        element.style.display = '';
    }
    else
    {
        element.style.display = 'none';
    }
}

function toggleDisplay(id)
{
    toggleElementDisplay(Ext.getDom(id));
}

// Toggle display for all success rows under the given table, identified by
// CSS class "successful".
function toggleSuccessfulTestRows(tableId, successfulShowing)
{
    var table = Ext.getDom(tableId);
    if (table)
    {
        var rows = table.getElementsByTagName('tr');

        for(var i = 0; i < rows.length; i++)
        {
            var successfulRow = rows[i].className.indexOf('successful') == 0;
            if(successfulRow)
            {
                rows[i].style.display = successfulShowing ? '' : 'none';
            }
        }
    }
}

function setClass(id, className)
{
    var element = Ext.getDom(id);
    element.className = className;
}

// Used in left/right pane navigation to handle selection of a new node in
// the left pane.
function selectNode(id)
{
    setClass("nav_" + selectedNode, "");
    setClass("nav_" + id, "active");

    var rightPane = Ext.getDom("node_" + selectedNode);
    rightPane.style.display = "none";
    rightPane = Ext.getDom("node_" + id);
    rightPane.style.display = "block";
    selectedNode = id;
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

        if (typeof handleSuccessfulConfigurationResponse == 'function')
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
        if(result.actionErrors && result.actionErrors.length > 0)
        {
            showStatus(result.actionErrors[0], 'failure');
        }
    }
}

function getParentPath(path)
{
    var index = path.lastIndexOf('/');
    if(index >= 0)
    {
        return path.slice(0, index);
    }

    return null;
};

function onSelectFailure(element, response)
{
    if(response.status == 0)
    {
        showStatus(response.statusText, 'failure');
    }
    else
    {
        var message = 'Pulse server returned status ' + response.status;
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

function onConfigSelect(sm, node)
{
    if(treesInitialised && node)
    {
        detailPanel.load({
            url: configTree.loader.dataUrl + '/' + encodeURIPath(configTree.getNodeConfigPath(node)),
            scripts: true,
            callback: function(element, success, response) {
                if(!success)
                {
                    onSelectFailure(element, response);
                }
            }
        });
    }
}

function getAjaxCallback(maskedElement)
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

function runAjaxRequest(url)
{
    var pane = Ext.get('nested-layout');
    pane.mask('Please wait...');
    window.actionInProgress = true;
    Ext.lib.Ajax.request('get', url, getAjaxCallback(pane));
}

function selectPath(path)
{
    configTree.getSelectionModel().clearSelections();
    configTree.selectConfigPath(path);
}

function editPath(path)
{
    detailPanel.load({url: window.baseUrl + '/ajax/config/' + encodeURIPath(path), scripts:true});
}

function addToPath(path, template)
{
    runAjaxRequest(window.baseUrl + '/ajax/config/' + encodeURIPath(path) + '?wizard' + (template ? '=template' : ''));
}

function actionPath(path, action, fromParent, onDescendants)
{
    var url = window.baseUrl + '/ajax/config/' + encodeURIPath(path) + '?' + action + '=input';
    if (fromParent)
    {
        url += '&newPath=' + getParentPath(path);
    }
    if (onDescendants)
    {
        url += '&descendants=true';
    }
    runAjaxRequest(url);
}

function deletePath(path)
{
    runAjaxRequest(window.baseUrl + '/ajax/config/' + encodeURIPath(path) + '?delete=confirm');
}

function showHelp(path, type)
{
    var helpPanel = Ext.getCmp('nested-east');
    // Only show help when there is a panel for it (there is none during
    // setup, for example).
    if(helpPanel)
    {
        helpPanel.showHelp(path, type);
    }
}

function showFieldHelp(field)
{
    var helpPanel = Ext.getCmp('nested-east');
    // Only show help when there is a panel for it (there is none during
    // setup, for example).
    if(helpPanel)
    {
        helpPanel.synchronise(field);
    }
}

function addFieldAnnotations(form, field, required, noOverride, inheritedFrom, overriddenOwner)
{
    if (required)
    {
        form.markRequired(field.getId(), 'field is required');
    }

    if (noOverride)
    {
        field.getEl().addClass('field-no-override');
    }

    if (inheritedFrom)
    {
        form.annotateField(field.getId(), 'inherited', window.baseUrl + '/images/inherited.gif', 'value inherited from ' + inheritedFrom);
    }

    if (overriddenOwner)
    {
        form.annotateField(field.getId(), 'overridden', window.baseUrl + '/images/overridden.gif', 'overrides value defined by ' + overriddenOwner);
    }
}

function addFieldHelp(form, field, message)
{
    var helpEl = form.annotateField(field.getId(), 'help', window.baseUrl + '/images/help.gif', message);
    helpEl.on('click', function() { showFieldHelp(field.getName()); });
}

function handleDialogResponse(options, success, response)
{
    if (success)
    {
        var result = Ext.util.JSON.decode(response.responseText);
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
                if (btn == 'ok')
                {
                    if (prompt)
                    {
                        params['message'] = text;
                    }

                    showStatus(statusMesage , 'working');
                    Ext.Ajax.request({
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
    Ext.Ajax.request({
        url: window.baseUrl + '/ajax/clearResponsibility.action',
        params: { projectId: projectId },
        callback: handleDialogResponse
    });
}

function deleteComment(buildId, commentId)
{
    showPromptDialog('Delete Comment',
                     'Are you sure you want to delete this comment?',
                     false,
                     false,
                     'Deleting comment...',
                     '/ajax/deleteComment.action',
                     { buildId: buildId, commentId: commentId });
}

function toggleStateList(e)
{
    var target = e.target || e.srcElement;
    Ext.get(target).findParent('ul.top-level', document.body, true).toggleClass('expanded');
}

function indentImage(size)
{
    if (size == 0)
    {
        return '';
    }
    else
    {
        return '<img src="' + Ext.BLANK_IMAGE_URL + '" width="' + (size * 10) +  '"/>';
    }
}

function refreshPanel(id, url, callback)
{
    var panel = Ext.get(id);
    panel.mask('Refreshing...');
    var updater = new Ext.Updater(id);
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
