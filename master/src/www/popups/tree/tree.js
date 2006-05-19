function init ()
{
    // need to find a way to extract the id of the root tree node from this file.
    var rootDiv = document.getElementById("browse");

    // add loading place holder.
    var ul = document.createElement("ul");
    ul.appendChild(createNewNode({"file":"Loading...", "type":"loading", "id":""}));
    rootDiv.appendChild(ul);
    rootDiv.subList = ul;

    // trigger an initial load.
    requestUpdate("root");
}

function handleFailure(resp)
{
    alert("onFailure");
}

function handleException(resp, e)
{
    alert("onException: " + e);
}

function load(event)
{
    var currentTarget = Event.element(event);

    if (this == currentTarget)
    {
        // WARNING: using innerHTML directly clears out any event handlers.
        // insert another level of the tree. <ul>loading...</ul>

        var ul = document.createElement("ul");
        ul.appendChild(createNewNode({"file":"Loading...", "type":"loading", "id":""}));

        currentTarget.appendChild(ul);
        currentTarget.subList = ul;

        // now we change the onclick handler so that it handles toggling instead of loading.
        currentTarget.onclick = toggle;

        // open current target.
        Element.removeClassName(currentTarget, "folder");
        Element.addClassName(currentTarget, "openfolder");

        // send off the xml http request.
        requestUpdate(currentTarget.id);
    }
}

function requestUpdate(id)
{
//    enterMethod("requestUpdate: " + id);
    var ajax = new Ajax.Request(
        "http://localhost:8080/ajax/list.action",
        //"file:///C:/tmp/tree/listing.json",
        {
            method: 'get',
            onComplete: updateFlat,
            onFailure: handleFailure,
            onException: handleException,
            parameters:"encodedPath=" + id
        }
    );
}

/**
 * A callback handler to process the response from a 'listing' request to the
 * server. This handler constructs a full navigable directory tree.
 *
 * This handler expected a response in the format:
 *
 *    listing:  {file, type, id}
 *    path: 'parent id'
 *
 * where
 *    file - is the name of the file
 *    type - is the type of the file (folder, file, txt etc)
 *    id - is the unique identifier for this file.
 *
 * and
 *
 *    path - represents the unique id of the parent.
 */
function updateTree(originalRequest)
{
    var jsonText = originalRequest.responseText;
    var jsonObj = eval("(" + jsonText + ")");
    var listing = jsonObj.listing;

    var target = document.getElementById(jsonObj.path);

    // clean the "loading..." out of the list.
    var ul = target.subList;
    var children = ul.childNodes;
    for (var j = 0; j < children.length; j++)
    {
        var child = children[j];
        ul.removeChild(child);
    }

    for (var i = 0; i < listing.length; i++)
    {
        var listItem = createNewNode(listing[i]);
        ul.appendChild(listItem);
    }
}

function updateFlat(originalRequest)
{
//    enterMethod("updateFlat");
    var folder = document.getElementById('browse');

    var jsonText = originalRequest.responseText;
    var jsonObj = eval("(" + jsonText + ")");
    var listing = jsonObj.listing;

    removeChild(folder);

    var ul = document.createElement("ul");
    folder.appendChild(ul);

    // add the links to the current directory.
    if (jsonObj.path != "root")
    {
        var path = jsonObj.path;
        var thisDirectory = createNewNode({"file":".", "type":"folder", "id":path});
        ul.appendChild(thisDirectory);
    }

    if (jsonObj.parentPath)
    {
        var parentPath = jsonObj.parentPath;
        var parentDirectory = createNewNode({"file":"..", "type":"folder", "id":parentPath});
        ul.appendChild(parentDirectory);
    }

    for (var i = 0; i < listing.length; i++)
    {
        var listItem = createNewNode(listing[i]);
        ul.appendChild(listItem);
    }

    // display path if it is available.
    var currentPathDisplay = document.getElementById('path');
    if (currentPathDisplay)
    {
        removeAllChildren(currentPathDisplay);
        // update the current node status.
        if (jsonObj.displayPath)
        {
            currentPathDisplay.appendChild(document.createTextNode(jsonObj.displayPath));
        }
    }


//    exitMethod("updateFlat");
}

/**
 * Create a new node.
 *
 *    data: an associative array with fields id, file and type.
 *
 */
function createNewNode(data)
{
//    enterMethod("createNewNode");
    var node = document.createElement("li");
    node.appendChild(document.createTextNode(data.file));
    node.setAttribute("id", data.id);
    Element.addClassName(node, data.type);
    if (data.type == "folder")
    {
        node.ondblclick = load;
        node.onclick = select;
    }
    else if (data.type == "loading")
    {
        // do nothing here..
    }
    else
    {
        node.onclick = select;
    }
//    exitMethod("createNewNode");
    return node;
}

/**
 * Select the element that is the target of this event.
 *
 * Selecting an element will add the 'selected' class to its list of classes.
 * Only a single element can be selected at a time.
 */
function select(event)
{
    var currentTarget = Event.element(event);
    if (this == currentTarget)
    {
        // locate the selected class.
        var selectedNodes = document.getElementsByClassName("selected");

        // remove 'selected' from the list of classes.
        var currentNodeSelected = false;
        if (selectedNodes)
        {
            for (var i = 0; i < selectedNodes.length; i++)
            {
                var node = selectedNodes[i];
                Element.removeClassName(node, "selected");
                if (node == currentTarget)
                {
                    currentNodeSelected = true;
                }
            }
        }

        Element.addClassName(currentTarget, "selected");

        if (Element.hasClassName(currentTarget, "folder"))
        {
            return;
        }

        // update selected display.
        // - what is the currently selected name?
        var selectedDisplay = document.getElementById('selected');
        if (selectedDisplay)
        {
            removeAllChildren(selectedDisplay);

            //selectedDisplay.appendChild(document.createTextNode(currentTarget.id));
            selectedDisplay.value = extractText(currentTarget);
        }
    }
}

function extractText(element)
{
    return element.innerHTML;
}

/**
 * Toggle the state of the element that is the target of this event.
 *
 * NOTE: It only makes sense for the target node to represent a 'folder'.
 */
function toggle(event)
{
//    enterMethod("toggle");
    var currentTarget = Event.element(event);
    if (this == currentTarget)
    {
        var node = this;
        Element.toggle(node.subList);
        if (Element.visible(node.subList))
        {
            replaceClassName(node, "folder", "openfolder");
        }
        else
        {
            replaceClassName(node, "openfolder", "folder");
        }
    }
//    exitMethod("toggle");
}

function replaceClassName(element, oldClassName, newClassName)
{
//    enterMethod("replaceClassName");
    Element.removeClassName(element, oldClassName);
    Element.addClassName(element, newClassName);
//    exitMethod("replaceClassName");
}

function removeChild(element)
{
//    enterMethod("removeChild")
    var children = element.childNodes;
    for (var j = 0; j < children.length; j++)
    {
        var child = children[j];
        if (child.nodeType == 1 && (child.tagName == "ul" || child.tagName == "UL"))
        {
            Element.remove(child);
        }
    }
//    exitMethod("removeChild")
}

function removeAllChildren(element)
{
//    enterMethod("removeAllChildren");
    var children = element.childNodes;
    for (var j = 0; j < children.length; j++)
    {
        var child = children[j];
        element.removeChild(child);
    }
}


function enterMethod(method)
{
    alert(method + ": enter");
}

function exitMethod(method)
{
    alert(method + ": exit");
}

function debug(element)
{
    var debugging = "";
    var i = 0;
    for (var propName in element)
    {
        var sep = "\n"
        if (i < 5)
        {
            sep = ", "
        }
        else
        {
            i = 0;
        }
        debugging += sep + propName;
        i++;
    }
    alert(debugging);
}