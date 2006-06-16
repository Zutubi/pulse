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
function submitenter(field, evt)
{
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
        field.form.submit.value = "next";
        field.form.submit();
        return false;
    }
    else
    {
        return true;
    }
}

// Function for opening an SCM browse window used on several forms\
//   - selectDir: set to true for selecting a directory, false for a file
//   - elementId: ID of textbox to set the value of to the selected path
//   - extraArgs: optional extra arguments for param string (e.g. "&prefix=foo")
function openBrowseWindow(selectDir, elementId, extraArgs)
{
    var browseWindow = window.open("/popups/browseScm.action?selectDir=" + selectDir + "&elementId=" + elementId + extraArgs, "browse scm", 'status=yes,resizable=yes,top=100,left=100,width=600,height=600,scrollbars=yes');
    browseWindow.opener = self;
    browseWindow.focus();
}

// Toggles the display attribute of an element between '' and 'none' to
// show/hide it.
function toggleElementDisplay(element)
{
    if(!element.style.display)
    {
        element.style.display = 'none';
    }
    else
    {
        element.style.display = '';
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
    var image = getElement(id + '_image');

    if(!element.style.display)
    {
        var closedImage = '/images/resultset_next.gif';
        if(arguments.length > 1)
        {
            closedImage = arguments[1];
        }

        element.style.display = 'none';
        image.src = closedImage;
    }
    else
    {
        var openImage = '/images/resultset_down.gif';
        if(arguments.length > 2)
        {
            openImage = arguments[2];
        }

        element.style.display = '';
        image.src = openImage;
    }
}

function toggleFolderList(id)
{
    toggleList(id, 'images/tree/foldericon.png', 'images/tree/openfoldericon.png');
}

// Changes display style for all lists under the given node
function styleAllLists(id, style, imageSource)
{
    var node = getElement(id);
    var childLists = node.getElementsByTagName("ul");

    for(var i = 0; i < childLists.length; i++)
    {
        if(childLists[i].id != '')
        {
            childLists[i].style.display = style;
            var image = getElement(childLists[i].id + '_image');
            if(image)
            {
                image.src = imageSource(childLists[i]);
            }
        }
    }
}

// Expands all lists under the given node
function expandAllLists(id)
{
    styleAllLists(id, '', function(element) { if(element.parentNode.className == "dir-artifact") { return '/images/tree/openfoldericon.png'; } else { return '/images/resultset_down.gif'; } });
}

// Collapses all lists under the given node
function collapseAllLists(id)
{
    styleAllLists(id, 'none', function(element) { if(element.parentNode.className == "dir-artifact") { return '/images/tree/foldericon.png'; } else { return '/images/resultset_next.gif'; } });
}

// Hides all children of the element with the given id
function hideChildren(id)
{
    var element = getElement(id);

    for(var i = 0; i < element.childNodes.length; i++)
    {
        var child = element.childNodes[i];
        if(child.nodeType == 1)
        {
            if(child.style.display == '')
            {
                child.style.display = 'none';
            }
            else
            {
                child.style.display = '';
            }
        }
    }
}

// Toggle display for all rows under the given table with a first cell of the given class
function toggleRowsWithClass(tableId, className, visible)
{
    var table = getElement(tableId);
    var rows = table.getElementsByTagName("tr");

    for(var i = 0; i < rows.length; i++)
    {
        var row = rows[i];
        var cells = row.getElementsByTagName("td");

        if(!row.collapsed && cells.length > 0 && cells[0].className.indexOf(className) == 0)
        {
            row.style.display = visible ? '' : 'none';
        }
    }
}

function toggleTests(id)
{
    var row = getElement(id);
    if(row)
    {
        expandCollapseSuite(row, row.childrenCollapsed);
    }
}

function expandCollapseSuite(row, expand)
{
    var cells = row.getElementsByTagName('td');

    if(cells.length > 0)
    {
        var firstCell = cells[0];

        row.childrenCollapsed = !expand;        
        if(expand)
        {
            firstCell.className = firstCell.className.replace('expand', 'collapse');
        }
        else
        {
            firstCell.className = firstCell.className.replace('collapse', 'expand');
        }

        var sibling = row.nextSibling;
        while(sibling)
        {
            if(sibling.id)
            {
                if(sibling.id.indexOf(row.id) == 0)
                {
                    cells = sibling.getElementsByTagName('td');

                    if(cells.length > 0)
                    {
                        sibling.collapsed = !expand;

                        if(successfulShowing || (cells[0].className.indexOf('success') < 0))
                        {
                            if(expand)
                            {
                                sibling.style.display = '';
                            }
                            else
                            {
                                sibling.style.display = 'none';
                            }
                        }
                    }
                }
                else
                {
                    break;
                }
            }

            sibling = sibling.nextSibling;
        }
    }
}

function toggleAllTests(tableId, expand)
{
    var table = getElement(tableId);
    var rows = table.getElementsByTagName('tr');

    for(var i = 0; i < rows.length; i++)
    {
        if(rows[i].id && rows[i].className && rows[i].className.indexOf('suite') == 0)
        {
            expandCollapseSuite(rows[i], expand);
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
