// Function to toggle the enable state of a control based on the state of a
// checkbox.
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

function getPosition(element)
{
    var x = 0;
    var y = 0;

    while(element != null)
    {
        x += element.offsetLeft;
        y += element.offsetTop;
        element = element.offsetParent;
    }

    return [x, y];
}

function setEnableState(id, checkboxId)
{
    var element = getElement(id);
    element.disabled = !document.getElementById(checkboxId).checked;
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
function toggleDisplay(id)
{
    var element = getElement(id);

    if(!element.style.display)
    {
        element.style.display = 'none';
    }
    else
    {
        element.style.display = '';
    }
}

// Toggles the display of a nested list and switched the correcsponding image
function toggleList(id)
{
    var element = getElement(id);
    var image = getElement(id + '_image');

    if(!element.style.display)
    {
        element.style.display = 'none';
        image.src = '/images/resultset_next.gif';
    }
    else
    {
        element.style.display = '';
        image.src = '/images/resultset_down.gif';
    }
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
                image.src = imageSource;
            }
        }
    }
}

// Expands all lists under the given node
function expandAllLists(id)
{
    styleAllLists(id, '', '/images/resultset_down.gif');
}

// Collapses all lists under the given node
function collapseAllLists(id)
{
    styleAllLists(id, 'none', '/images/resultset_next.gif');
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