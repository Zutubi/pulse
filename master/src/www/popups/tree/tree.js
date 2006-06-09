/**
 * Simple node object that allows us to create a node tree.
 */
MyNode = function(){};
MyNode.prototype = {

    /**
     * Constructor.
     */
    initialize: function() {
        this.children = new $A(),
        this.parent = null,
        this.name = null,
        this.type = null,
        this.id
    },

    getId: function()
    {
        return this.id;
    },

    getName: function()
    {
        return this.name;
    },

    getType: function()
    {
        return this.type;
    },

    /**
     * Get the nodes children.
     */
    getChildren: function()
    {
        return this.children;
    },

    /**
     * Add a new child to this node.
     */
    addChild: function(child)
    {
        // check that the child is not already a child.
        this.children.push(child);
        child.parent = this;
    },

    /**
     * Returns true if this node instance has children, false otherwise.
     */
    hasChildren: function()
    {
        return this.children.length > 0;
    },

    /**
     * Returns the parent of this node, or null if this is the root node.
     */
    getParent: function()
    {
        return this.parent;
    },

    /**
     * Returns the path of this node from the root.
     */
    getPath: function()
    {
        var path = "";
        if (this.getParent())
        {
            path = this.getParent().getPath();
        }
        //TODO: replace hard wired '/' with file system specific value.
        path = path + "/" + this.getName();
        return path;
    }
};

/**
 * Event router allows us to attach multiple event handlers to a single dom event callback.
 *
 * (adapted from Ajax in Action, pg 142.)
 */
EventRouter =function(){};
EventRouter.prototype = {

    initialize: function(el, eventType)
    {
        this.lsnrs = new Array();
        this.el = el;
        el.eventRouter = this;
        el[eventType] = this.callback;
    },

    addListener: function(lsnr)
    {
        this.lsnrs.push(lsnr);
    },

    removeListener: function(lsnr)
    {
        this.lsnrs.remove(lsnr);
    },

    notify: function(src, event)
    {
        var lsnrs = $A(this.lsnrs);
        lsnrs.each(function(lsnr)
        {
            lsnr.call(src, event);
        });
    },

    callback: function(event)
    {
        var router = this.eventRouter;
        router.notify(this, event);
    }
}

/**
 * Initialise the tree control.
 *
 */
function init(event)
{
    // Initialise the model root.
    //TODO: use a root value specific to the file system.
    var newRoot = new MyNode();
    newRoot.initialize();
    newRoot.name = "ROOT";
    newRoot.id = "";
    newRoot.type = "";
    setRoot(newRoot);

    var anchorId = getConfig().anchor;

    // need to find a way to extract the id of the root tree node from this file.
    var anchorDiv = document.getElementById(anchorId);

    // LOADING FEEDBACK.
    var ul = document.createElement("ul");
    ul.appendChild(createDomNode(createVirtualNode("Loading...", "loading", "")));
    anchorDiv.appendChild(ul);

    // TRIGGER LOAD OF THE ROOT NODE.
    requestUpdate(getConfig().initUid);
}

/**
 * Simple encapsulation of the configuration object. This should simplify the fixing how
 * the configuration details are passed around.
 */
function getConfig()
{
    return window.myTree;
}

function requestUpdate(uids)
{
    var params = "";
    var sep = "";
    uids.each(function(uid)
    {
        params = params + sep + "uid=" + uid;
        sep = "&";
    });

    var url = getConfig().url;

    var ajax = new Ajax.Request(
        url,
        {
            method: 'get',
            onComplete: updateModel,
            onFailure: handleFailure,
            onException: handleException,
            parameters:params
        }
    );
}

/**
 * Update the data model.
 */
function updateModel(originalRequest)
{
    // the jsonObjs contains an array of listings.
    var jsonObjs = eval("(" + originalRequest.responseText + ")");

    var results = $A(jsonObjs.results)
    results.each(function(jsonObj)
    {
        // locate where in the tree this update belongs.
        var rootNode = getRoot();
        var node = locateNode(rootNode, jsonObj.uid);
        if (!node)
        {
            // the default.
            node = rootNode;
        }

        // UPDATE THE MODEL WITH THE NEW DATA.
        for (var i = 0; i < jsonObj.listing.length; i++)
        {
            // sanity check that we do not add components to the model a second time. This should be
            // caught at an earlier stage.
            var existingNode = node.getChildren().find(function(child)
            {
                return (child.getId() == jsonObj.listing[i].uid);
            });
            if (existingNode)
            {
                console.log("skipping adding node a second time.");
                continue;
            }

            var childNode = new MyNode();
            childNode.initialize();
            childNode.id = jsonObj.listing[i].uid;
            childNode.name = jsonObj.listing[i].file;
            childNode.type = jsonObj.listing[i].type;
            node.addChild(childNode);
        }

        onModelUpdate(jsonObj.uid);
    });
}

/**
 * Handler for when the model is updated.
 *
 *   updateId: the id of the model element updated.
 */
function onModelUpdate(updateId)
{
    // TRIGGER AN UPDATE OF THE UI. SHOULD THIS BE HANDLED VIA AN EVENT?
    updateTree(updateId);

}

//TODO: this traversal is too slow. Should generate a map of uid to nodes and use that instead.
function locateNode(root, uid)
{
    var node = null;
    root.getChildren().each(function(childNode)
    {
        if (childNode.getId() == uid)
        {
            node = childNode;
        }
        var n = locateNode(childNode, uid);
        if (n)
        {
            node = n;
        }
    });
    return node;
}

/**
 * A callback handler to process the response from a 'listing' request to the
 * server. This handler constructs a full navigable directory tree.
 *
 */
function updateTree(id)
{
    // LOCATE THE POINT IN THE DOM THAT WE WILL BE UPDATING.
    var target = document.getElementById(id);
    if (!target)
    {
        target = document.getElementById(getConfig().anchor);
    }

    // REMOVE ANY EXISTING LOADING MESSAGE
    var ul = locateFirstChild(target, "UL");
    if (ul) // if there are children to be removed.
    {
        removeAllChildren(ul);
    }
    else
    {
        // need to create the appropriate UL element.
        ul = document.createElement("ul");
        target.appendChild(ul);

        // now we change the onclick handler so that it handles toggling instead of loading.
        target.onclick = onToggle;
    }

    var rootNode = locateNode(getRoot(), id);
    if (!rootNode)
    {
        rootNode = getRoot();

        // add the '.' directory so that it can be selected. However, we do not want it to be
        // reloaded since it is a special case that clears out all existing content...
        var thisDirectory = createDomNode(createVirtualNode(".", "root", ""));
        ul.appendChild(thisDirectory);
    }

    // convert the data into a dom tree representation.
    rootNode.getChildren().each(function(child)
    {
        var listItem = createDomNode(child);
        ul.appendChild(listItem);
    });
}

function updateFlat(id)
{
    var folder = document.getElementById(getConfig().anchor);

    // lookup the root node.
    var rootNode = locateNode(getConfig().root, id);
    if (!rootNode)
    {
        rootNode = getConfig().root;
    }

    removeChild(folder);
    clearSelection();

    var ul = document.createElement("ul");
    folder.appendChild(ul);

    // add the links to the current directory.
    var uid = rootNode.getId();

    var thisDirectory = createDomNode(createVirtualNode(".", "folder", uid));
    ul.appendChild(thisDirectory);

    // show link to the parent whenever we are not at the root.
    if (rootNode.getParent())
    {
        var puid = ""; // value for the root.
        if (rootNode.getParent().getId())
        {
            puid = rootNode.getParent().getId();
        }
        var parentDirectory = createDomNode(createVirtualNode("..", "folder", puid));
        ul.appendChild(parentDirectory);
    }

    // convert the data into a dom tree representation.
    rootNode.getChildren().each(function(child)
    {
        var listItem = createDomNode(child);
        ul.appendChild(listItem);
    });
}

/**
 * Selected node change event handler.
 *
 */
function onSelectionChange()
{
    // update two locations. The path display at the top, and the selected display at the bottom.

    var selectedNode = getSelection();
    if (!selectedNode)
    {
        console.log("WARNING: onSelectionChange triggered but no selectedNode is available. :(")
        return;
    }

    // A: the path element at shows the path to the currently selected node.
    var currentPathDisplay = document.getElementById('path');
    if (currentPathDisplay)
    {
        // clear out the existing content.
        removeAllChildren(currentPathDisplay);

        // If the current selection is a folder, show the path of that folder.
        // If the current selection is a file, then show the path of its containing folder.
        var pathToDisplay = null;
        if (selectedNode.getType() == "folder")
        {
            pathToDisplay = selectedNode.getPath();
        }
        else
        {
            if (selectedNode.getParent())
            {
                pathToDisplay = selectedNode.getParent().getPath();
            }
        }
        if (pathToDisplay)
        {
            currentPathDisplay.appendChild(document.createTextNode(pathToDisplay));
        }
    }

    // B: The selected element display, that shows the name of the currently selected node.
    var selectedDisplay = document.getElementById('selected');
    if (selectedDisplay)
    {
        // clear out the existing content.
        removeAllChildren(selectedDisplay);
        selectedDisplay.value = selectedNode.getName();
    }

    // C: The dom tree UI classes.
    clearSelection();
    // locate and update the dom tree node that should be appearing as selected.
    var selectedDomNode = document.getElementById(selectedNode.getId());
    Element.addClassName(selectedDomNode, "selected");

    // if the selected dom node is a folder, then toggle it.
    if (selectedNode.getType() == "folder")
    {
        if (Element.hasClassName(selectedDomNode, "folder"))
        {
            replaceClassName(selectedDomNode, "folder", "openfolder");
        }
        else
        {
            replaceClassName(selectedDomNode, "openfolder", "folder");
        }
    }
}

function createVirtualNode(file, type, uid)
{
    var tmpNode = new MyNode();
    tmpNode.id = uid;
    tmpNode.type = type;
    tmpNode.name = file;
    return tmpNode;
}

/**
 * Create a visual representation of a node.
 *
 *    data: an associative array with fields id, file and type.
 *
 */
function createDomNode(node)
{
    var domNode = document.createElement("li");
    domNode.appendChild(document.createTextNode(node.getName()));
    domNode.setAttribute("id", node.getId());

    Element.addClassName(domNode, node.getType());
    if (node.getType() == "folder")
    {
        var eventRouter = new EventRouter();
        eventRouter.initialize(domNode, "onclick");
        eventRouter.addListener(onSelect);
        eventRouter.addListener(onLoad);
    }
    else if (node.getType() == "loading")
    {
        // do nothing here..
    }
    else
    {
        domNode.onclick = onSelect;
    }
    return domNode;
}

/**
 * Select the element that is the target of this event.
 *
 * Selecting an element will add the 'selected' class to its list of classes.
 * Only a single element can be selected at a time.
 */
function onSelect(event)
{
    var currentTarget = getCurrentTarget(event);
    if (this == currentTarget)
    {
        // record selection.
        setSelectionById(currentTarget.id);
    }
}

/**
 * Set the selected node, identified by the specified id.
 */
function setSelectionById(id)
{
    var newSelection = locateNode(getRoot(), id);
    if (!newSelection)
    {
        console.log("WARNING: failed to locate newly selected node '%s'", id);
    }
    setSelection(newSelection);

    // trigger the selected node change event listeners.
    onSelectionChange();
}

function setSelection(newSelection)
{
    getConfig().selectedNode = newSelection;
}

function getSelection()
{
    return getConfig().selectedNode;
}

function getRoot()
{
    return getConfig().root;
}

function setRoot(newRoot)
{
    getConfig().root = newRoot;
}

/**
 * The onLoad event handler is used to trigger an ajax list request to load a part of the
 * data model that has not been loaded.
 *
 */
function onLoad(event)
{
    var currentTarget = getCurrentTarget(event);
    if (this == currentTarget)
    {
        // WARNING: using innerHTML directly clears out any event handlers.
        // insert another level of the tree. <ul>loading...</ul>

        var ul = document.createElement("ul");
        ul.appendChild(createDomNode(createVirtualNode("Loading...", "loading", "")));

        currentTarget.appendChild(ul);

        // now we change the onclick handler so that it handles toggling instead of loading.
        currentTarget.onclick = onToggle;

        // open current target.
        Element.removeClassName(currentTarget, "folder");
        Element.addClassName(currentTarget, "openfolder");

        // send off the xml http request.
        var uids = new Array();
        uids.push(currentTarget.id)
        requestUpdate(uids);
    }
}

/**
 *
 */
function clearSelection()
{
    var selectedNodes = document.getElementsByClassName("selected");

    // remove 'selected' from the list of classes.
    if (selectedNodes)
    {
        for (var i = 0; i < selectedNodes.length; i++)
        {
            var node = selectedNodes[i];
            Element.removeClassName(node, "selected");
        }
    }
    clearBrowserTextSelection();
}

/**
 * Go back one level in the hierarchy. This means that:
 * a) if the current node is a file, go to its folders parent.
 * b) if the current node is a folder, go to its parent.
 */
function selectParentNode()
{
    var selectedNode = getSelection();
    if (selectedNode)
    {

        var parentNode = selectedNode.getParent();
        if (parentNode)
        {
            setSelectionById(parentNode.getId());
        }
        else
        {
            //clear the selection...
            setSelectionById("");
        }
    }
}

/**
 * Toggle the state of the element that is the target of this event.
 *
 * NOTE: It only makes sense for the target node to represent a 'folder'.
 */
function onToggle(event)
{
    var currentTarget = getCurrentTarget(event);
    if (this == currentTarget)
    {
        var node = this;
        var ul = locateFirstChild(node, "UL");
        Element.toggle(ul);
        if (Element.visible(ul))
        {
            replaceClassName(node, "folder", "openfolder");
        }
        else
        {
            replaceClassName(node, "openfolder", "folder");
        }
    }
}

function replaceClassName(element, oldClassName, newClassName)
{
    Element.removeClassName(element, oldClassName);
    Element.addClassName(element, newClassName);
}

function removeChild(element)
{
    var children = element.childNodes;
    for (var j = 0; j < children.length; j++)
    {
        var child = children[j];
        if (child.nodeType == 1 && (child.tagName.toUpperCase() == "UL"))
        {
            Element.remove(child);
        }
    }
}

/**
 * Remove all of the child nodes from the specified element.
 */
function removeAllChildren(element)
{
    var children = $A(element.childNodes);
    children.each(function(child)
    {
        // remove the child element from the document.
        Element.remove(child);
    });
}

function getCurrentTarget(event)
{
    return Event.element(getCurrentEvent(event));
}

function getCurrentEvent(event)
{
    return event || window.event;
}

function clearBrowserTextSelection()
{
    if (document.selection)
    {
        document.selection.empty();
    }
}

/**
 * Locate and return the first child of the specified element that has a nodeName property
 * that matches the specified nodeName.
 *
 */
function locateFirstChild(elem, nodeName)
{
    nodeName = nodeName.toUpperCase();

    // add Enumerable to childNodes
    var children = $A(elem.childNodes);

/* old implementation
    var res = null;
    children.each(function(child) {
        var name = child.nodeName;
        if (name && name.toUpperCase() == nodeName)
        {
            res = child;
            return;
        }
    });
    return res;
*/
    var res = children.find(function(child)
    {
        var name = child.nodeName;
        return (name && name.toUpperCase() == nodeName);
    });
    return res;
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

/**
 * Basic failure handler.
 *
 */
function handleFailure(resp)
{
    alert("onFailure");
}

/**
 * Basic exception handler.
 *
 */
function handleException(resp, e)
{
    alert("onException: " + e);
}