// hack in a namespace
var ZUTUBI = new Object();
ZUTUBI.widget = new Object();

/**
 * The ZUTUBI.widget.View is a global registry for the node views.
 */
ZUTUBI.widget.View = new Object();

/**
 * Keep a count of all of the nodes for the tree view. This node count is used to
 * create the unique ids for each of the nodes.
 */
ZUTUBI.widget.View.nodeCount = 0;

/**
 * A private array for the ZUTUBI.widget.View registry that contains a reference to all
 * of the nodes.
 */
ZUTUBI.widget.View._nodes = [];

ZUTUBI.widget.View._trees = [];

/**
 * Register a newly created node with the global view registry.
 */
ZUTUBI.widget.View.regNode = function(node)
{
    ZUTUBI.widget.View._nodes[node.index] = node;
};

/**
 * Lookup the node identified by the specified node index.
 */
ZUTUBI.widget.View.getNodeByIndex = function(nodeIndex)
{
    return ZUTUBI.widget.View._nodes[nodeIndex];
};

/**
 * Lookup the node that is identified by the specified function.
 *
 * An example of the matching function is:
 *     function(node)
 *     {
 *          return node.property == 'something';
 *     }
 * With the above example function, all of the registered nodes where the
 * property field equals 'something' will be returned.
 */
ZUTUBI.widget.View.getNodeByProperty = function(match)
{
    return $A(ZUTUBI.widget.View._nodes).find(function(node, index)
    {
        // after deleting nodes, we end up with nulls in this list. Lets filter these
        // out before they cause problems with the client function.
        if (node)
        {
            return match(node, index);
        }
        return false;
    });
};

ZUTUBI.widget.View.regTree = function(tree)
{
    ZUTUBI.widget.View._trees[tree.id] = tree;
}

ZUTUBI.widget.View.getTreeById = function(id)
{
    return ZUTUBI.widget.View._trees[id];
}

/**
 *
 *
 */
ZUTUBI.widget.TreeView = Class.create();

Object.extend(ZUTUBI.widget.TreeView.prototype = {

    /**
     *
     */
    id: null,

    /**
     * The root node of this view.
     */
    root: null,

    /**
     * The data loader, allows the node data to be dynamically loaded.
     */
    dataLoader:null,

    /**
     * Currently selected node.
     */
    selectedNode: null,

    /**
     * id: the id of the dom element where this TreeView will be attached.
     */
    initialize: function(id)
    {
        this.id = id;
        this.root = new ZUTUBI.widget.RootNode(this);
        ZUTUBI.widget.View.regTree(this);
    },

    /**
     * Draw this tree view to the document.
     *
     * It is intended that draw is only called once.
     */
    draw: function()
    {
        var anchorPoint = document.getElementById(this.id);

        // remove any children at the anchor point.
        $A(anchorPoint.childNodes).each(function(childNode)
        {
            Element.remove(childNode);
        });
        // attach the newly rendered tree view to the dom.
        var renderedTree = this.root._render();
    },

    /**
     * Select the parent node of the currently selected node.
     */
    goUp: function()
    {
        // is there a selected node?
        if (this.selectedNode)
        {
            // is it possible to go up from the currently selected node?
            if (this.selectedNode.parent && !this.selectedNode.parent.isRoot())
            {
                this._select(this.selectedNode.parent);
            }
        }
    },

    /**
     * todo
     */
    collapseAll: function()
    {

    },

    /**
     * todo
     */
    expandAll: function()
    {

    },

    /**
     * todo
     */
    expandTo: function(node)
    {

    },

    /**
     * todo
     */
    refresh: function()
    {

    },

    /**
     * The selection handler is a callback function that should be attached to any elements that,
     * when clicked, select the node they represent.
     */
    _selectionHandler: function(event)
    {
        var src = Event.element(getCurrentEvent(event));
        if (src == this)
        {
            console.log("_selectionHandler.");
            // NOTE: this requires that all ids are in the format xyz_index
            var index = src.getAttribute("id").substring(src.getAttribute("id").indexOf('_') + 1);
            var node = ZUTUBI.widget.View.getNodeByIndex(index);
            if (node)
            {
                // record the selection.
                node.tree._select(node);
            }
            else
            {
                // either the index refers to a none existant node or the format
                // of the selected elements id is different from expected.
                console.log("WARNING: selected node '%s' was not identified. Original id: %s", index, src.getAttribute("id"));
            }
        }
    },

    /**
     * Select the specified node. Specifying a null node will 'clear' the selection.
     */
    _select: function(node)
    {
        // record the selection.
        node.tree.selectedNode = node;

        // if we are not clearing the selection (node == null)
        if (node)
        {
            node.onSelect();
        }

        // trigger the onSelect handler.
        node.tree.onSelect(node);
    },

    removeNode: function(node)
    {
        console.log("removing node: %s", node.getPath());
        // can not remove the root node.
        if (node.isRoot())
        {
            return false;
        }

        var p = node.parent;

        console.log("this._deleteNode(%s)", node);
        this._deleteNode(node);

        if (p)
        {
            p._invalidate();
        }

        return true;
    },

    removeChildren: function(node)
    {
        console.log("removeChildren: %s", node.getPath());
        var self = this;
        $A(node.children).each(function(child)
        {
            console.log("this._deleteNode(%s)", child);
            self._deleteNode(child);
        });
    },

    _deleteNode: function(node)
    {
        // first remove all of the children.
        console.log("_deleteNode: %s", node);
        this.removeChildren(node);

        // remove this node from its parent.
        var p = node.parent;
        p.removeChild(node);

        node._unrender();

        // free the memory.
        console.log("delete ZUTUBI.widget.View._nodes[%s]", node.index);
        delete ZUTUBI.widget.View._nodes[node.index];
    },

    isSelected: function(node)
    {
        return (this.selectedNode == node);
    },

    /**
     * On expand callback. Triggered when a node is expanded.
     */
    onExpand: function(node) { },

    /**
     * On collapse callback. Triggered when a node is collapsed.
     */
    onCollapse: function(node) { },

    /**
     * On select callback. Triggered when a node is selected.
     */
    onSelect: function(node) { },

    /**
     * On update callback. Triggered when the data model is updated.
     */
    onUpdate: function(node) {}
});

/**
 *
 *
 */
ZUTUBI.widget.FlatView = Class.create();

Object.extend(ZUTUBI.widget.FlatView.prototype = {

    initialize: function(id)
    {
        this.id = id;
        ZUTUBI.widget.View.regTree(this);
    }
});

ZUTUBI.widget.Node = Class.create();

Object.extend(ZUTUBI.widget.Node.prototype = {

    /**
     * Unique identifier of this node.
     */
    index: null,

    /**
     * The parent of this node.
     */
    parent: null,

    /**
     * The children of this node.
     */
    children: null,

    /**
     * The tree to which this node belongs.
     */
    tree: null,

    /**
     * The depth of this node from the root. The depth of the root node is -1.
     */
    depth: -1,

    /**
     * The function callback that can be used to load the data dynamically.
     */
    dataLoader: null,

    /**
     * This variable is true if this node is expanded.
     */
    expanded: false,

    /**
     * This variable is true if this node has no children, ie: it represents a leaf node in the tree.
     */
    isLeaf: false,

    /**
     * This variable is true if the children of this node has been loaded.
     */
    isLoaded: false,

    /**
     * Indicates whether or not the children of this node have been rendered to the dom.
     */
    renderedChildren: false,

    /**
     * Indicates whether or not the rendered representation of this node is up to date.
     */
    invalidated: true,

    /**
     * Initialise this node.
     */
    initialize: function(parent, expanded)
    {
        this.expanded = expanded;
        this.parent = parent;
        this.children = [];
        this.index = ZUTUBI.widget.View.nodeCount;
        ZUTUBI.widget.View.nodeCount++;
        ZUTUBI.widget.View.regNode(this);

        if (parent)
        {
            this.depth = parent.depth + 1;
            this.tree = parent.tree;
            this.parent.appendChild(this);
        }
    },

    /**
     * Get this nodes parent node.
     */
    getParent: function()
    {
        return this.parent;
    },

    /**
     * Get this nodes sibling nodes (all of its parents children). This node is not
     * part of the returned siblings.
     */
    getSiblings: function()
    {
        var siblings = [];
        var self = this;
        $A(this.parent.children).each(function(sibling)
        {
            if (sibling.index != self.index)
            {
                siblings[siblings.length] = sibling;
            }
        });
        return siblings;
    },

    /**
     * Is this the trees root node? Root nodes are a special case that are not rendered
     * and should be handled specifically when working with nodes.
     */
    isRoot: function()
    {
        return this.tree.root == this;
    },

    /**
     * Get this nodes children.
     */
    getChildren: function()
    {
        return this.children;
    },

    /**
     * Add a child to this nodes list of children.
     */
    appendChild: function(newChild)
    {
        if (newChild.parent && newChild.parent != this)
        {
            // the new child is already attached to another parent.
            return false;
        }

        if (!newChild.parent)
        {
            newChild.parent = this;
        }

        // quick check to ensure we do not accidentally add a node twice.
        var existingNode = $A(this.children).find(function(child)
        {
            return child.index == newChild.index;
        });
        // if it is not already registered, lets add it to the children array.
        if (existingNode)
        {
            return false;
        }

        this.children[this.children.length] = newChild;
        return true;
    },

    /**
     *
     */
    removeChild: function(node)
    {
        // reconstruct the children array without the child to be removed.
        a = [];
        $A(this.children).each(function(child)
        {
            if (child != node)
            {
                a[a.length] = child;
            }
        });

        var nodeRemoved = this.children.length != a.length;
        this.children = a;

        return nodeRemoved;
    },

    /**
     * Check whether or not this node has children. This method takes into account whether or not this
     * node has been loaded. Before a node has had its children loaded, we have to assume that children do
     * exist.
     */
    hasChildren: function()
    {
        // if we have not loaded this node, then we do not know whether or not it has
        // any children. In that case, we assume that we do.
        if (this.isLeaf)
        {
            return false;
        }
        return (!this.isLoaded || this.children.length > 0);
    },

    // =========================================
    // BEGIN TreeView SPECIFIC IMPLEMENTATIONS.

    /**
     * Returns true if all of this nodes ancestors are expanded.
     */
    isVisible: function()
    {
        // special case
        if (this.isRoot())
        {
            return true;
        }
        return this.parent.isVisible() && this.parent.expanded;
    },

    /**
     * Returns true if this node has been rendered to the dom.
     * This does not make any assumptions about whether or not the dom view is up to date. This is
     * handled by the invalidated property.
     */
    _isRendered: function()
    {
        return this.getEl() != null;
    },

    /**
     * Indicates that this nodes rendered view requires a refresh.
     */
    _invalidate: function()
    {
        this.invalidated = true;

        // is this node visible? if so, we need to render it.
        if (this.isVisible())
        {
            this._render();
        }
    },

    _unrender: function()
    {
        if (this._isRendered())
        {
            Element.remove(this.getEl());
        }
    },

    /**
     * Render this node to the dom, creating a dom representation or updating an existing
     * one as necessary.
     *
     * NOTE: rendering a validated node will result in no action being taken.
     */
    _render: function()
    {
        if (!this.invalidated)
        {
            return;
        }

        if (!this.isVisible())
        {
            return;
        }

        if (!this._isRendered())
        {
            // render structure to the dom.
            this._renderStructure();
        }

        // render the state of this node.
        this._renderState();

        // render the data.
        this._renderNode();

        // only render children if they are visible.
        if (this.hasChildren() && this.expanded)
        {
            this._renderChildren();
        }

        this.invalidated = false;
    },

    _renderStructure: function()
    {
        var anchorPoint = this.parent.getChildrenEl();

        var li = document.createElement("li");
        li.setAttribute("id", this.getElId());
        Event.observe(li, 'click', this._onclickHandler.bindAsEventListener(li), true);

        var span = document.createElement("span");
        span.setAttribute("id", this.getNodeElId());

        var ul = document.createElement("ul");
        ul.setAttribute("id", this.getChildrenElId());

        li.appendChild(span);
        li.appendChild(ul);

        anchorPoint.appendChild(li);
    },

    _renderState: function()
    {
        var li = this.getEl();

        // firstly, clean up.
        if (Element.hasClassName(li, "closed"))
        {
            Element.removeClassName(li, "closed");
        }

        if (Element.hasClassName(li, "opened"))
        {
            Element.removeClassName(li, "opened");
        }

        if (Element.hasClassName(li, "empty"))
        {
            Element.removeClassName(li, "empty");
        }

        // now we render the +/- state indicator.
        if (this.hasChildren())
        {
            if (this.expanded)
            {
                Element.show(this.getChildrenEl());
                Element.addClassName(li, "opened");
            }
            else
            {
                Element.hide(this.getChildrenEl());
                Element.addClassName(li, "closed");
            }
        }
        else
        {
            Element.addClassName(li, "empty");
        }
    },

    _renderNode: function()
    {
        var anchorPoint = this.getNodeEl();
        // replace any existing content.
        $A(anchorPoint.childNodes).each(function(element)
        {
            Element.remove(element);
        });
        // write the new content.
        anchorPoint.appendChild(document.createTextNode("node_" + this.index));
    },

    _renderChildren: function()
    {
        if (!this.hasChildren() || !this.expanded)
        {
            console.log("WARNING: attempting to render children when there are none to render OR this node is not expanded.")
        }

        if (!this.isLoaded)
        {
            console.log("INFO: Triggering a load for node(%s)", this.index);
            // trigger a load and render 'Loading...' feedback.
            var ul = this.getChildrenEl();
            var li = document.createElement("li");
            li.appendChild(document.createTextNode("Loading..."));
            ul.appendChild(li);

            // if a data loader is configured for this node, then use it. Else, default to the data loader
            // configured for the tree.
            if (this.dataLoader)
            {
                this.dataLoader(this);
            }
            else
            {
                this.tree.dataLoader(this);
            }
        }
        else
        {
            this.children.sort(function(a, b)
            {
                if (a.index > b.index)
                    return 1;
                if (a.index < b.index)
                    return -1;
                return 0;
            });

            $A(this.children).each(function(child)
            {
                child._render();
            });
        }
    },

    _onclickHandler: function(event)
    {
        var src = Event.element(getCurrentEvent(event));
        if (src == this)
        {
            console.log("_ONCLICKHANDLER:");
            var id = src.getAttribute("id").substring(src.getAttribute("id").indexOf('_') + 1);
            var node = ZUTUBI.widget.View.getNodeByIndex(id);
            if (node.hasChildren())
            {
                node.toggle();
            }
        }
    },

    /**
     * Function that needs to be called when the data loader has completed
     * loading the data for this node.
     *
     * This method should also be called when a model update is complete as it triggers
     * a refresh / sync of the visible state.
     */
    loadComplete: function()
    {
        console.log("INFO: loadComplete for node(%s)", this.index);
        // record the fact that we have loaded.
        this.isLoaded = true;

        this.invalidated = true;

        // quick hack: remove the 'Loading' from the children.
        $A(this.getChildrenEl().childNodes).each(function(element)
        {
            Element.remove(element);
        });

        // if after loading we have no children, then we collapse this node.
        if (!this.hasChildren())
        {
            this._collapse();
        }
        this._render();
    },

    refresh: function()
    {
        console.log("INFO: refresh triggered on node(%s)", this.index);
        this.invalidate();
        this._render();
    },

    /**
     * Get the id of the dom element that represents the anchor point for this node.
     */
    getElId: function()
    {
        return "bid_" + this.index;
    },

    /**
     * Get the dom element that represents the anchor point for this node.
     */
    getEl: function()
    {
        return document.getElementById(this.getElId());
    },

    /**
     * Get the id of the dom element that contains the node display.
     */
    getNodeElId: function()
    {
        return "nid_" + this.index;
    },

    /**
     * Get the dom element that contains the node display
     */
    getNodeEl: function()
    {
        return document.getElementById(this.getNodeElId());
    },

    /**
     * Get the id of the dom element that contains the children of this node.
     */
    getChildrenElId: function()
    {
        return "cid_" + this.index;
    },

    /**
     * Get the dom element that contains the children of this node.
     */
    getChildrenEl: function()
    {
        return document.getElementById(this.getChildrenElId());
    },

    _expand: function()
    {
        if (this.expanded)
        {
            return;
        }

        this.expanded = true;

        // we have a state change for this node, therefore invalidate it to
        // trigger a render.
        this._invalidate();

        // On expand callback.
        this.tree.onExpand(this);
        this.onExpand();
    },

    _collapse: function()
    {
        if (!this.expanded)
        {
            return;
        }

        this.expanded = false;

        this._invalidate();

        // trigger the onCollapse callback.
        this.tree.onCollapse(this);
        this.onCollapse();
    },

    toggle: function()
    {
        // we need to check whether or not this node has children since we are currently
        // failing to remove the onclick handler that triggers the toggle. It is also safer
        // to do so.

        if (this.hasChildren())
        {
            if (this.expanded)
            {
                this._collapse();
            }
            else
            {
                this._expand();
            }
        }
    },

    /**
     * On collapse callback. Triggered when this node is collapsed.
     */
    onCollapse: function() { },

    /**
     * On expand callback. Triggered when this node is expanded.
     */
    onExpand: function() { },

    /**
     * On select callback. This method is triggered when this node is selected.
     */
    onSelect: function()
    {
        var selectedEls = document.getElementsByClassName("selected");
        $A(selectedEls).each(function(element)
        {
            Element.removeClassName(element, "selected");
        });

        // select this node.
        Element.addClassName(this.getNodeEl(), "selected");
    },

    getPath: function()
    {
        return "";
    }
});


/**
 * The Root node is the base node for the TreeView. This node is not displayed, but rather
 * is used to hold and render the top level children.
 *
 *
 */
ZUTUBI.widget.RootNode = function(tree)
{
    this.initialize(null, true);
	this.tree = tree;
};

ZUTUBI.widget.RootNode.prototype = new ZUTUBI.widget.Node();

/**
 * Override the default implementation of getHtml(), the root node
 * only displays its children, not itself.
 */
ZUTUBI.widget.RootNode.prototype._render = function()
{
    // create the children structural anchor point and then render the children.
    if (!this._isRendered())
    {
        // render structure to the dom.
        this._renderStructure();
    }

    this._renderChildren();
};

ZUTUBI.widget.RootNode.prototype.isRendered = function()
{
    return this.getChildrenEl() != null;
}

ZUTUBI.widget.RootNode.prototype._renderStructure = function()
{
    var anchorPoint = document.getElementById(this.tree.id);

    var ul = document.createElement("ul");
    ul.setAttribute("id", this.getChildrenElId());

    anchorPoint.appendChild(ul);
}

/**
 *
 *
 *
 */
ZUTUBI.widget.FileNode = function(parent)
{
    this.initialize(parent);
}

ZUTUBI.widget.FileNode.prototype = new ZUTUBI.widget.Node();

ZUTUBI.widget.FileNode.prototype.isFolder = function()
{
    return this.data.type == "folder";
}

ZUTUBI.widget.FileNode.prototype._renderNode = function()
{
    console.log("_renderNode:");
    // the element is the element that would be returned by this.getNodeEl();
    var anchorPoint = this.getNodeEl();
    // replace any existing content.
    $A(anchorPoint.childNodes).each(function(element)
    {
        Element.remove(element);
    });

    // render image.
    if (this.isFolder())
    {
        if (this.expanded)
        {
            Element.addClassName(anchorPoint, "openfolder");
        }
        else
        {
            Element.addClassName(anchorPoint, "folder");
        }
    }
    else
    {
        Element.addClassName(anchorPoint, this.data.type);
    }
    Element.addClassName(anchorPoint, "node");

    // render text.
    var span = document.createElement("span");
    span.appendChild(document.createTextNode(this.data.name));
    span.setAttribute("id", "fn_" + this.index);

    anchorPoint.appendChild(span);

    // is this node selected?
    if (this.tree.isSelected(this))
    {
        Element.addClassName(span, "selected");
    }

    Event.observe(span, 'click', this.tree._selectionHandler.bindAsEventListener(span), true);
}

ZUTUBI.widget.FileNode.prototype.onExpand = function()
{
    console.log("onExpand:");
    // open the folder.
    var element = this.getNodeEl();

    Element.removeClassName(element, "folder");
    Element.addClassName(element, "openfolder");
}

ZUTUBI.widget.FileNode.prototype.onCollapse = function()
{
    console.log("onCollapse:")
    // open the folder.
    var element = this.getNodeEl();

    Element.removeClassName(element, "openfolder");
    Element.addClassName(element, "folder");
}

ZUTUBI.widget.FileNode.prototype.onSelect = function()
{
    var selectedEls = document.getElementsByClassName("selected");
    $A(selectedEls).each(function(element)
    {
        Element.removeClassName(element, "selected");
    });

    var text = document.getElementById("fn_" + this.index);
    Element.addClassName(text, "selected");
}

ZUTUBI.widget.FileNode.prototype.getPath = function()
{
    var path = "";
    // need to be a little careful. the root node does not have a path function.
    if (this.getParent() && this.getParent().getPath)
    {
        path = this.getParent().getPath();
    }
    //TODO: replace hard wired '/' with file system specific value.
    path = path + "/" + this.data.name;
    return path;
}

function getCurrentEvent(event)
{
    return event || window.event;
}

function dump(obj)
{
    var dump = "Object dump: [";
    var i = 0;
    for (property in obj)
    {
        i++;
        dump = dump + " " + property;
        if (i > 5)
        {
            dump = dump + "\n";
            i = 0;
        }
    }
    dump = dump + "]";
    console.log(dump);
}
