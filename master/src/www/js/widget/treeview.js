
ZUTUBI.widget.TreeView = YAHOO.widget.TreeView;

ZUTUBI.widget.TreeView.prototype = new YAHOO.widget.TreeView();

/**
 * Reference to the currently selected node.
 */
ZUTUBI.widget.TreeView.prototype.selected = null;

/**
 * Add an on select callback to the treeview.
 */
ZUTUBI.widget.TreeView.prototype.onSelect = function(node)
{
};

/**
 * Handle the selection of a tree node.
 */
ZUTUBI.widget.TreeView.prototype.select = function(node)
{
    if (this.selected)
    {
        Element.removeClassName(this.selected.getLabelEl(), "selected");
    }
    this.selected = node;
    if (this.selected)
    {
        Element.addClassName(this.selected.getLabelEl(), "selected");
        this.onSelect(this.selected);
    }
};

// --- (on activate) ---

/**
 * Callback triggered when a node is 'activated'... ie: double clicked.
 *
 * WARNING: STILL TO BE IMPLEMENTED.
 */
ZUTUBI.widget.TreeView.prototype.onActivate = function(node)
{

};

// --- ( path separator ) ---

/**
 * The separator character is used when node paths are generated.
 */
ZUTUBI.widget.TreeView.prototype.separator = '/';

/**
 * Specify the path separator character.
 */
ZUTUBI.widget.TreeView.prototype.setSeparator = function(sep)
{
    this.separator = sep;
};

/**
 * Implement the TreeView onExpand callback handler.
 *
 */
ZUTUBI.widget.TreeView.prototype.onExpand = function(node)
{
    // the onexpand callback is triggered before the this.expanded is set to true
    if (node.getFileEl() && node.data)
    {
        node.getFileEl().className = node.data.type + "_o";
    }
}

/**
 * Implement the TreeView onCollapse callback handler.
 *
 */
ZUTUBI.widget.TreeView.prototype.onCollapse = function(node)
{
    if (node.getFileEl() && node.data)
    {
        node.getFileEl().className = node.data.type;
    }
}

// --- ( utility methods. ) ---

/**
 * Select the currently selected nodes parent. If there is no selected node
 * or the node does not have a parent, no change is made.
 */
ZUTUBI.widget.TreeView.prototype.goUp = function()
{
    // is there a selected node?
    if (this.selected)
    {
        // is it possible to go up from the currently selected node?
        if (this.selected.parent && !this.selected.parent.isRoot())
        {
            this.select(this.selected.parent);
        }
    }
};

/**
 * Expand the tree to the node defined by the requested path.
 *
 * If this tree is dynamically loaded (which it is), there may be a delay in expanding
 * to the requested path due to loading of the data. As a result, this method returns
 * 3 response codes.
 *
 * code 0: expansion to the requested path is complete.
 * code 1: expansion to the requested path has failed, the path is invalid.
 * code 2: expansion to the request path is in progress. One of the nodes in the path is being loaded.
 */
ZUTUBI.widget.TreeView.prototype.expandTo = function(requestPath)
{
    var node = this.getRoot();

    // search for node with name initPath.
    var sep = this.separator;
    requestPath = requestPath.split(sep);

    var p = null; // previous node.
    for (var i = 0; i < requestPath.length; i++)
    {
        var path = requestPath[i];
        if (!node.expanded)
        {
            node.toggle();
        }

        if (!node.isDynamic() || node.dynamicLoadComplete)
        {
            // node is loaded, so lets go to  the next one.
            node = $A(node.children).find(function(child)
            {
                var n = child.data.name;
                // ensure that trailing '\\' are removed.
                if (n.indexOf(sep) == n.length - 1)
                {
                    n = n.substring(0, n.length - 1);
                }
                return n == path;
            });
            if (!node)
            {
                // select the parent.
                if (p)
                {
                    this.select(p);
                }
                return 1; // path does not exist.
            }
            p = node;
        }
        else
        {
            return 2; // path is loading.
        }
    }

    // found path and loaded.
    if (node.isContainer() && !node.expanded)
    {
        node.toggle();
    }
    
    // always mark the open node as selected.
    this.select(node);
    return 0;
};

ZUTUBI.widget.TreeView.prototype.expandToPath = function(requestPath)
{
    if (!requestPath)
    {
        return;
    }

    // split the request path into its components.

    var status = this.expandTo(requestPath);
    if (status == 1)
    {
        // node does not exist.
    }
    else if (status == 2)
    {
        // still loading.
        var self = this;
        setTimeout(function()
        {
            self.expandToPath(requestPath);
        }, 250);
    }
    else if (status == 0)
    {
        // complete.
    }
};

/**
 * Construct the human readable path that identifies the specified node.
 *
 */
ZUTUBI.widget.TreeView.prototype.getDisplayPath = function(node)
{
    return this._getPath(node, function(data)
    {
        return data.name;
    });
}

/**
 * Construct the id path that uniquely identifies the specified node.
 *
 */
ZUTUBI.widget.TreeView.prototype.getIdPath = function(node)
{
    return this._getPath(node, function(data)
    {
        return data.id;
    });
}

/**
 * Construct a path for the specified node, using the getValue function to define the data
 * used to construct the path components.
 */
ZUTUBI.widget.TreeView.prototype._getPath = function(node, getValue)
{
    var sep = "";
    var path = "";

    while (node)
    {
        // only traverse nodes that carry the ZUTUBI data packets. This should be all of the
        // nodes, except maybe the root node itself.
        if (node.data)
        {
            var name = getValue(node.data);
            if (name.indexOf(this.separator) != -1)
            {
                path = name + path
            }
            else
            {
                path = name + sep + path
            }
            sep = this.separator;
        }
        node = node.parent;
    }
    return path;
}

// ---( ROOT NODE )---

ZUTUBI.widget.RootNode = YAHOO.widget.RootNode;

/**
 * The root node is a virtual node that is not displayed in the UI.
 *
 */
ZUTUBI.widget.RootNode.prototype = new YAHOO.widget.RootNode();


ZUTUBI.widget.RootNode.prototype.isRoot = function()
{
    return true;
};

ZUTUBI.widget.RootNode.prototype.getPath = function()
{
    return "";
};

//---( FILE NODE )---

/**
 * Constructor.
 */
ZUTUBI.widget.FileNode = function(oData, oParent, expanded) {

    this.type = "FileNode";
	if (oParent) {
		this.init(oData, oParent, expanded);
	}
    this.setUpLabel(oData);
};

ZUTUBI.widget.FileNode.prototype = new YAHOO.widget.TextNode();

ZUTUBI.widget.FileNode.prototype.isRoot = function()
{
    return false;
};

ZUTUBI.widget.FileNode.prototype.getName = function()
{
    return this.data.name;
};

ZUTUBI.widget.FileNode.prototype.getPath = function()
{
    return this.tree.getDisplayPath(this);
};

ZUTUBI.widget.FileNode.prototype.getIdPath = function()
{
    return this.tree.getIdPath(this);
};

/**
 * Override the base hasChildren method to add knowledge of the file type. Only folders
 * and root nodes (containers) can be opened.
 */
ZUTUBI.widget.FileNode.prototype.hasChildren = function(checkForLazyLoad) {

    if (!this.isContainer())
    {
        return false;
    }

    return ( this.children.length > 0 ||
            (checkForLazyLoad && this.isDynamic() && !this.dynamicLoadComplete) );
};

/**
 * Check if this node represents a container, a node that may contain children.
 */
ZUTUBI.widget.FileNode.prototype.isContainer = function()
{
    return this.data.container == true;
};

ZUTUBI.widget.FileNode.prototype.getFileElId = function()
{
    return "ygtvfile" + this.index;
};

ZUTUBI.widget.FileNode.prototype.getFileEl = function()
{
    return document.getElementById(this.getFileElId());
}

ZUTUBI.widget.FileNode.prototype.getFileStyle = function(openIfLoading)
{
    var type = this.data.type;

    // check if we are open.
    if (this.hasChildren(true) || (this.isDynamic() && !this.getIconMode()))
    {
        return (this.expanded || (openIfLoading && this.isDynamic && !this.dynamicLoadComplete)) ? type + "_o" : type;
    }

    return type
}

ZUTUBI.widget.FileNode.prototype.select = function()
{
    this.tree.select(this);
}

ZUTUBI.widget.FileNode.prototype.onLabelClick = function(me)
{
    this.select();
    return false;
};

ZUTUBI.widget.FileNode.prototype.onLabelDblClick = function(me)
{
    this.tree.onActivate(this);
    return false;
}

ZUTUBI.widget.FileNode.prototype.getNodeHtml = function() {
	var sb = [];

	sb[sb.length] = '<table border="0" cellpadding="0" cellspacing="0">';
	sb[sb.length] = '<tr>';

    // depth rendering.
    for (i=0;i<this.depth;++i)
    {
		sb[sb.length] = '<td class="' + this.getDepthStyle(i) + '">&nbsp;</td>';
	}

	var getNode = 'YAHOO.widget.TreeView.getNode(\'' + this.tree.id + '\',' + this.index + ')';

    // toggle link rendering.
    sb[sb.length] = '<td';
	sb[sb.length] = ' id="' + this.getToggleElId() + '"';
	sb[sb.length] = ' class="' + this.getStyle() + '"';
	if (this.hasChildren(true)) {
		sb[sb.length] = ' onmouseover="this.className=';
		sb[sb.length] = getNode + '.getHoverStyle()"';
		sb[sb.length] = ' onmouseout="this.className=';
		sb[sb.length] = getNode + '.getStyle()"';
	}
	sb[sb.length] = ' onclick="javascript:' + this.getToggleLink() + '">';

	sb[sb.length] = '&nbsp;';

	sb[sb.length] = '</td>';

    // type image rendering.
    sb[sb.length] = '<td';
    sb[sb.length] = ' id="' + this.getFileElId() + '"';
    sb[sb.length] = ' class="' + this.getFileStyle(false) + '"';
    sb[sb.length] = ' onclick="javascript:' + this.getToggleLink(true) + '"';
    if (this.hasChildren(true))
    {
        sb[sb.length] = ' onmouseover="document.getElementById(\'';
        sb[sb.length] = this.getToggleElId() + '\').className=';
        sb[sb.length] = getNode + '.getHoverStyle()"';
        sb[sb.length] = ' onmouseout="document.getElementById(\'';
        sb[sb.length] = this.getToggleElId() + '\').className=';
        sb[sb.length] = getNode + '.getStyle()"';
    }
    sb[sb.length] = '>';
    sb[sb.length] = '</td>';

    // label rendering.
    sb[sb.length] = '<td>';
	sb[sb.length] = '<a';
	sb[sb.length] = ' id="' + this.labelElId + '"';
	sb[sb.length] = ' class="' + this.labelStyle + '"';
	sb[sb.length] = ' href="' + this.href + '"';
	sb[sb.length] = ' target="' + this.target + '"';
	sb[sb.length] = ' onclick="return ' + getNode + '.onLabelClick(' + getNode +')"';
	sb[sb.length] = ' ondblclick="return ' + getNode + '.onLabelDblClick(' + getNode +')"';
	sb[sb.length] = ' >';
	sb[sb.length] = this.label;
	sb[sb.length] = '</a>';
	sb[sb.length] = '</td>';
	sb[sb.length] = '</tr>';
	sb[sb.length] = '</table>';

	return sb.join("");
};


