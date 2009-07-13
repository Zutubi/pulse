function each(a, f)
{
    for(var i = 0; i < a.length; i++)
    {
        f(a[i]);
    }
}

function map(a, mapping)
{
    var out = new Array(a.length);
    for(var i = 0; i < a.length; i++)
    {
        out[i] = mapping(a[i]);
    }

    return out;
}

function getElement(id)
{
    var element;

    if (document.getElementById)
    {
        element = document.getElementById(id);
    }
    else if (document.all)
    {
        element = document.all[id];
    }
    else
    {
        element = document.layers[id];
    }

    return element;
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
    var element = getElement(id);
    var disabled = !getElement(checkboxId).checked;

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
    var disabled = !getElement(checkboxId).checked;

    if(inverse)
    {
        disabled = !disabled;
    }

    var form = getElement(formId);
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

// function to select the 'next' submit action in a wizard when
// enter is pressed in a form field. Without this, the first submit (previous)
// button would always be selected.
// How?: it sends a hidden field called submit with the details. 
function submitenter(field, evt, value)
{
    // provide backward compatibility. The value may not be specified, in which case default to 'next'.
    if (!value)
    {
        value = "next"; // the default value.
    }

    var keycode;
    if (window.event)
    {
        keycode = window.event.keyCode;
    }
    else if (evt)
    {
        keycode = evt.which;
    }
    else
    {
        return true;
    }

    if (keycode == 13)
    {
        // submit the next button.
        field.form.submit.value = value;
        field.form.submit();
        return false;
    }
    else
    {
        return true;
    }
}

// Function for opening an SCM browse window used on several forms.
//   - contextPath: the web app context path.
//   - selectDir: set to true for selecting a directory, false for a file
//   - elementId: ID of textbox to set the value of to the selected path
//   - extraArgs: optional extra arguments for param string (e.g. "&prefix=foo")
function openBrowseWindow(contextPath, selectDir, elementId, extraArgs)
{
    var browseWindow = window.open(contextPath + "/popups/browseScm.action?selectDir=" + selectDir + "&elementId=" + elementId + extraArgs, "scm", 'status=yes,resizable=yes,top=100,left=100,width=600,height=600,scrollbars=yes');
    browseWindow.opener = self;
    browseWindow.focus();
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

function openFileDialog(path, formname, fieldname, root, prefix, showFiles, showHidden, showToolbar)
{
    var browseWindow = window.open(path + '?formname=' + formname +
                                   '&fieldname=' + fieldname + '&root=' + root +
                                   '&prefix=' + prefix + '&showFiles=' + showFiles +
                                   '&showHidden=' + showHidden + '&showToolbar=' + showToolbar,
            'files', 'width=400, height=550, resizable=yes', false);
    browseWindow.opener = self;
    browseWindow.focus();
}

function openResourceDialog(base, agent, formname, fieldname)
{
    var root;

    if (agent == -1)
    {
        root = 'local:///';
    }
    else
    {
        root = 'agent://$agentId/';
    }

    openFileDialog(base + '/popups/fileDialog.action', formname, fieldname, root, '', false, false, false);
}

function openSCMSelectDialog(base, showFiles, formName, fieldName, projectPath, path, prefix)
{
    if(path)
    {
        var pieces = path.split("/");

        // Reassemble, removing empty pieces
        path = "";
        for(var i = 0; i < pieces.length; i++)
        {
            if(pieces[i])
            {
                if(path)
                {
                    path += "/";
                }

                path += pieces[i];
            }
        }

        if(path)
        {
            path += "/";
        }
    }

    openFileDialog(base + '/popups/scmFileDialog.action', formName, fieldName, 'pulse:///' + projectPath + '/scm/' + path, prefix, showFiles, false, false);
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
    toggleElementDisplay(getElement(id));
}

// Toggles the display of a nested list and switched the correcsponding image
//   - if a second argument is given, it is used as the image for a "closed"
//     element
//   - if a third argument is given, it is used as the image for a "open"
//     element
function toggleList(id)
{
    var element = getElement(id);
    var header = getElement(id + '_header');

    if(!element.style.display)
    {
        var closedClass = 'collapsed-list';
        if(arguments.length > 1)
        {
            closedClass = arguments[1];
        }

        element.style.display = 'none';
        header.className = closedClass;
    }
    else
    {
        var openClass = 'expanded-list';
        if(arguments.length > 2)
        {
            openClass = arguments[2];
        }

        element.style.display = '';
        header.className = openClass;
    }
}

function toggleFolderList(id)
{
    toggleList(id, 'closed-folder', 'open-folder');
}

// Changes display style for all lists under the given node
function styleAllLists(id, style, headerClass)
{
    var node = getElement(id);
    var childLists = node.getElementsByTagName("ul");

    for(var i = 0; i < childLists.length; i++)
    {
        if(childLists[i].id != '')
        {
            childLists[i].style.display = style;
            var header = getElement(childLists[i].id + '_header');
            if(header)
            {
                header.className = headerClass(childLists[i]);
            }
        }
    }
}

// Expands all lists under the given node
function expandAllLists(id)
{
    styleAllLists(id, '', function(element) { if(element.parentNode.className == "dir-artifact") { return 'open-folder'; } else { return 'expanded-list'; } });
}

// Collapses all lists under the given node
function collapseAllLists(id)
{
    styleAllLists(id, 'none', function(element) { if(element.parentNode.className == "dir-artifact") { return 'closed-folder'; } else { return 'collapsed-list'; } });
}

// Hides all children of the element with the given id
function hideChildren(id)
{
    var element = getElement(id);

    $A(element.childNodes).each(function(child)
    {
        if (child.nodeType == 1)
        {
            Element.hide(child);
        }
    });
}

// Toggle display for all success rows under the given table, identified by
// CSS class "successful".
function toggleSuccessfulTestRows(tableId, successfulShowing)
{
    var table = getElement(tableId);
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

function setText(id, text)
{
    var element = getElement(id);
    element.innerHTML = text;
}

function setClass(id, className)
{
    var element = getElement(id);
    element.className = className;
}

// Used in left/right pane navigation to handle selection of a new node in
// the left pane.
function selectNode(id)
{
    setClass("nav_" + selectedNode, "");
    setClass("nav_" + id, "active");

    var rightPane = getElement("node_" + selectedNode);
    rightPane.style.display = "none";
    rightPane = getElement("node_" + id);
    rightPane.style.display = "block";
    selectedNode = id;
}

/*
 * Return the dimensions of the window.
 *
 *    width: the window width.
 *    height: the window height.
 */
function windowSize()
{
    var myWidth = 0, myHeight = 0;
    if (typeof( window.innerWidth ) == 'number')
    {
        //Non-IE
        myWidth = window.innerWidth;
        myHeight = window.innerHeight;
    }
    else if (document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ))
    {
        //IE 6+ in 'standards compliant mode'
        myWidth = document.documentElement.clientWidth;
        myHeight = document.documentElement.clientHeight;
    }
    else if (document.body && ( document.body.clientWidth || document.body.clientHeight ))
    {
        //IE 4 compatible
        myWidth = document.body.clientWidth;
        myHeight = document.body.clientHeight;
    }
    return {"width":myWidth, "height":myHeight};
}

/**
 * Return an array containing the query string parameters.
 *
 *
 */
function qs()
{
    var qsParm = new Array();
    var query = window.location.search.substring(1);
    var parms = query.split('&');
    for (var i=0; i<parms.length; i++) {
        var pos = parms[i].indexOf('=');
        if (pos > 0) {
            var key = parms[i].substring(0,pos);
            qsParm[key] = parms[i].substring(pos+1);
        }
    }
    return qsParm;
}


function openDebugAlert(obj)
{
    if (obj)
    {
        var temp = "";
        for (var x in obj)
        {
            temp += x + ": " + obj[x] + "\n";
        }
        alert (temp);
    }
}

// Selects the given range of text in a text area
function setSelectionRange(id, start, end)
{
    var t = getElement(id);
    if(t.setSelectionRange)
    {
        // FF
        t.setSelectionRange(start, end);
    }
    else if(t.createTextRange)
    {
        // IE
        var r = t.createTextRange();
        r.move("character", start);
        r.moveEnd("character", end - start);
        r.select();
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
    detailPanel.load({url: window.baseUrl + '/aconfig/' + encodeURIPath(path), scripts:true});
}

function addToPath(path, template)
{
    runAjaxRequest(window.baseUrl + '/aconfig/' + encodeURIPath(path) + '?wizard' + (template ? '=template' : ''));
}

function actionPath(path, action, fromParent)
{
    var url = window.baseUrl + '/aconfig/' + encodeURIPath(path) + '?' + action + '=input';
    if(fromParent)
    {
        url += '&newPath=' + getParentPath(path);
    }
    runAjaxRequest(url);
}

function deletePath(path)
{
    runAjaxRequest(window.baseUrl + '/aconfig/' + encodeURIPath(path) + '?delete=confirm');
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

function handleResponsibilityResponse(options, success, response)
{
    if (success)
    {
        var result = Ext.util.JSON.decode(response.responseText);
        if (result.success)
        {
            window.location.reload(true);
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

function takeResponsibility(projectId)
{
    window.dialogBox = Ext.Msg.show({
        title: 'Take Responsibility',
        msg: 'Comment (optional):',
        fn: function(btn, text) {
                window.dialogBox = null;
                if (btn == 'ok')
                {
                    showStatus('Taking responsibility...', 'working');
                    Ext.Ajax.request({
                        url: window.baseUrl + '/ajax/takeResponsibility.action',
                        params: { projectId: projectId, comment: text },
                        callback: handleResponsibilityResponse
                    });
                }
        },
        prompt: true,
        width: 400,
        buttons: Ext.Msg.OKCANCEL
    });
}

function clearResponsibility(projectId)
{
    showStatus('Clearing responsibility...', 'working');
    Ext.Ajax.request({
        url: window.baseUrl + '/ajax/clearResponsibility.action',
        params: { projectId: projectId },
        callback: handleResponsibilityResponse
    });
}

function handleMarkForCleanResponse(options, success, response)
{
    if (success)
    {
        var result = Ext.util.JSON.decode(response.responseText);
        if (result.success)
        {
            if (result.status)
            {
                showStatus(result.status.message, result.status.type);
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

function markForClean(projectName)
{
    showStatus('Marking for clean build...', 'working');
    Ext.Ajax.request({
        url: window.baseUrl + '/aconfig/projects/'+projectName+'?clean=clean',
        callback: handleMarkForCleanResponse
    });
}
