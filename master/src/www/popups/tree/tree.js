/**
 * Simple node object that allows us to create a node tree.
 */
MyNode = Class.create();
Object.extend(MyNode.prototype = {

    /**
     * The not loaded state.
     */
    NOT_LOADED: 3,

    /**
     * The loading state.
     */
    LOADING: 4,

    /**
     * The loaded state.
     */
    LOADED: 5,

    /**
     * Constructor.
     */
    initialize: function() {
        this.children = new $A(),
        this.parent = null,
        this.name = null,
        this.type = null,
        this.state = NOT_LOADED,
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
    },

    getState: function()
    {
        return this.state;
    },

    isLoaded: function()
    {
        return this.getState() == this.LOADED;
    },

    /**
     * Returns true if the type represented by this node is a folder, false otherwise.
     */
    isFolder: function()
    {
        return this.getType() == "folder";
    }
});

/**
 * Event router allows us to attach multiple event handlers to a single dom event callback.
 *
 * (adapted from Ajax in Action, pg 142.)
 */
EventRouter = Class.create();
Object.extend(EventRouter.prototype = {
    initialize: function(el, eventType)
    {
        this.lsnrs = new Array();
        this.el = el;
        this.eventType = eventType;
        el[eventType + 'Router'] = this;
        el[eventType] = this.generateCallback(eventType);
    },

/*
    extendEl: function()
    {
        this.el['add' + this.eventType + 'Listener'] = eval('function(lsnr){this.'+this.eventType+'Router.lsnrs.push(lsnr);}');
        this.el['remove' + this.eventType + 'Listener'] = eval('function(lsnr){this.'+this.eventType+'Router.lsnrs.remove(lsnr);}');
    },

*/
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

    generateCallback: function(eventType)
    {
        // you have to love javascript :)
        // The following eval generates a callback function that 'knows' the variable name that contains
        // this object. The problem being solved is that when the callback method is registered with the
        // element (during init), it loses its context, and in particular, its reference to 'this'. We need
        // 'this' to get access to the lsnrs. By dynamically creating this function, we are able to use multiple
        // event handlers on the same object. Cool.
        return eval('function(event){'+
                        'var e = event || window.event; '+
                        'var router = this.'+eventType+'Router; '+
                        'router.notify(this, e);'+
                    '}')
    }
});

Core = Class.create();
Object.extend(Core.prototype = {
    initialize: function()
    {
        // initialize the model.
        this.root = new MyNode();
        this.root.name = "ROOT";
        this.root.id = "";
        this.root.type = "";

        this.updateRouter = new EventRouter(this, "onupdate");
        this.selectRouter = new EventRouter(this, "onselect");

        this.selectedNode = null;

        this.loadedNodes = {};
    },

    addOnUpdateListener: function(lsnr)
    {
        this.updateRouter.addListener(lsnr);
    },

    addOnSelectListener: function(lsnr)
    {
        this.selectRouter.addListener(lsnr);
    },

    /**
     * Retrieve the root node of the node tree.
     */
    getModel: function()
    {
        return this.root;
    },

    /**
     * Retrieve the node specified by the node id.
     *
     */
    getNodeById: function(nodeId)
    {
        // lookup the node in the internal node cache.
        return this.loadedNodes.nodeId;
    },

    getSelectedNode: function()
    {
        return this.selectedNode;
    },

    setSelectedNodeById: function(nodeId)
    {
        var node = null;
        if (nodeId)
        {
            node = getNodeById(nodeId);
        }
        this.setSelectedNode(node);
    },

    setSelectedNode: function(node)
    {
        if (node == this.selectedNode)
        {
            // no change.
            return;
        }
        this.selectedNode = node;

        // generate the onselect event.
        this.onselect(this.selectedNode);
    },

    loadNodeById:function(nodeId)
    {
        var nodeIds = new Array();
        nodeIds.push(nodeId);
        this.loadNodesById(nodeIds);
    },

    loadNodesById: function(nodeIds)
    {
        var nodesToLoad = new Array();
        $A(nodeIds).each(function(nodeId)
        {
            var node = this.getNodeById(nodeId);
            if (node)
            {
                if (node.getState() == node.NOT_LOADED)
                {
                    node.state = node.LOADING;
                    nodesToLoad.push(nodeId);
                }
            }
            else
            {
                // a node that we know nothing about. Must be during initialization.
                nodesToLoad.push(nodeId);
            }
        });

        if (nodesToLoad.length > 0)
        {
            requestUpdate(nodesToLoad);
        }
    },

    requestUpdate: function(uids)
    {
        console.log("requestUpdate()");
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
    },

    handleComplete: function(resp)
    {
        // the jsonObjs contains an array of listings.
        var jsonObjs = eval("(" + resp.responseText + ")");

        var results = $A(jsonObjs.results)
        results.each(function(jsonObj)
        {
            // locate where in the tree this update belongs.
            var rootNode = getCore().getModel();
            var node = getCore().getNodeById(jsonObj.uid);
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
                childNode.id = jsonObj.listing[i].uid;
                childNode.name = jsonObj.listing[i].file;
                childNode.type = jsonObj.listing[i].type;
                node.addChild(childNode);
            }

            getCore().onupdate(jsonObj.uid);
        });
    },

    /**
     * Basic failure handler.
     *
     */
    handleFailure: function(resp)
    {
        alert("onFailure");
    },

    /**
     * Basic exception handler.
     *
     */
    handleException: function(resp, e)
    {
        alert("onException: " + e);
    }
});

/**
 * The tree view is a full node tree representation of the model.
 *
 */
MyTreeView = Class.create();
Object.extend(MyTreeView.prototype = {

    initialize: function(anchorId)
    {
        this.anchorEl = document.getElementById(anchorId);

        var ul = document.createElement("ul");
        ul.appendChild(createDomNode(createVirtualNode("Loading...", "loading", "")));
        this.anchorEl.appendChild(ul);

        // initialize event listeners.
    }
});

/**
 * The flat view is a representation that shows the current level of the tree. This view
 * also contains virtual nodes that represent the '.' and '..' directories.
 *
 */
MyFlatView = Class.create();
Object.extend(MyFlatView.prototype = {
    initialize: function(anchorId)
    {

    }
});

/**
 * Initialise the tree control.
 *
 */
function init(event)
{
    // Initialise the model root.
    //TODO: use a root value specific to the file system.
    var core = new Core();
    window.myTree.core = core;

    // node select event listener.
    core.addOnSelectListener(onSelectionUpdateDomNode)
    core.addOnSelectListener(onSelectionUpdatePathDisplay)
    core.addOnSelectListener(onSelectionUpdateSelectedDisplayValue);

    // configure the update event listeners.
    core.addOnUpdateListener(onModelUpdate);

    var view = new MyTreeView(getConfig().anchor);

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
    console.log("requestUpdate()");
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
    console.log("RESPONSE CALLBACK FROM SERVER: updateModel.");

    // the jsonObjs contains an array of listings.
    var jsonObjs = eval("(" + originalRequest.responseText + ")");

    var results = $A(jsonObjs.results)
    results.each(function(jsonObj)
    {
        // locate where in the tree this update belongs.
        var rootNode = getCore().getModel();
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
            childNode.id = jsonObj.listing[i].uid;
            childNode.name = jsonObj.listing[i].file;
            childNode.type = jsonObj.listing[i].type;
            node.addChild(childNode);
        }

        getCore().onupdate(jsonObj.uid);
    });
}

/**
 * Handler for when the model is updated.
 *
 *   updateId: the id of the model element updated.
 */
function onModelUpdate(updateId)
{
    console.log("----onModelUpdate(%s)", updateId);

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
    console.log("----updateTree(%s)", id);

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

    var rootNode = locateNode(getCore().getModel(), id);
    if (!rootNode)
    {
        rootNode = getCore().getModel();

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
function onSelectionUpdatePathDisplay(event)
{
    console.log("+onSelectionUpdatePathDisplay")
    var selectedNode = event;

    // A: the path element at shows the path to the currently selected node.
    var currentPathDisplay = document.getElementById('path');
    if (currentPathDisplay)
    {
        // clear out the existing content.
        removeAllChildren(currentPathDisplay);

        // If the current selection is a folder, show the path of that folder.
        // If the current selection is a file, then show the path of its containing folder.
        var pathToDisplay = null;
        if (selectedNode.isFolder())
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
}

function onSelectionUpdateSelectedDisplayValue(event)
{
    console.log("+onSelectionUpdateSelectedDisplayValue")

    var selectedNode = event;

    // B: The selected element display, that shows the name of the currently selected node.
    var selectedDisplay = document.getElementById('selected');
    if (selectedDisplay)
    {
        // clear out the existing content.
        removeAllChildren(selectedDisplay);
        if (!selectedNode.isFolder())
        {
            selectedDisplay.value = selectedNode.getName();
        }
    }
}

/**
 * Event handler that listens for node selection change events. When a node is selected,
 * this event handler updates the dom tree to ensure that the node is displayed as 'selected'
 * ie: sets the selected class on the correct node.
 *
 */
function onSelectionUpdateDomNode(event)
{
    console.log("+onSelectionUpdateDomNode")
    var selectedNode = event;

    // C: The dom tree UI classes.
    clearSelection();
    // locate and update the dom tree node that should be appearing as selected.
    var selectedDomNode = document.getElementById(selectedNode.getId());
    Element.addClassName(selectedDomNode, "selected");

    //TODO: Move this into a different event handler, one that manages the toggling of nodes.
    // if the selected dom node is a folder, then toggle it.
    if (selectedNode.isFolder())
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
    if (node.isFolder())
    {
        var eventRouter = new EventRouter(domNode, "onclick");
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
        getCore().setSelectedNodeById(currentTarget.id);
    }
}

function getCore()
{
    return window.myTree.core;
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
    var selectedNode = getCore().getSelectedNode();
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

