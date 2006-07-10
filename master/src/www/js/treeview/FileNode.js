/**
 * Reference to the currently selected node.
 */
YAHOO.widget.TreeView.prototype.selected = null;

/**
 * Add an on select callback to the treeview.
 */
YAHOO.widget.TreeView.prototype.onSelect = function(node)
{
};

/**
 * Handle the selection of a tree node.
 */
YAHOO.widget.TreeView.prototype.select = function(node)
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

YAHOO.widget.TreeView.prototype.onActivate = function(node)
{

};

YAHOO.widget.TreeView.prototype.goUp = function()
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
YAHOO.widget.TreeView.prototype.expandTo = function(requestPath)
{
    var node = this.getRoot();

    // search for node with name initPath.
    //TODO: remove hard coding of '\' separator char.
    requestPath = requestPath.split('\\');
    for (var i = 0; i < requestPath.length; i++)
    {
        var path = requestPath[i];
        if (!node.expanded)
        {
            node.fileToggle(true);
        }

        if (!node.isDynamic() || node.dynamicLoadComplete)
        {
            // node is loaded, so lets go to  the next one.
            node = $A(node.children).find(function(child)
            {
                var n = child.data.name;

                // ensure that trailing '\\' are removed.
                if (n.indexOf('\\') == n.length - 1)
                {
                    n = n.substring(0, n.length - 1);
                }
                return n == path;
            });
            if (!node)
            {
                return 1; // path does not exist.
            }
        }
        else
        {
            return 2; // path is loading.
        }
    }

    // found path and loaded.
    if (node.isContainer() && !node.expanded)
    {
        node.fileToggle(true);
    }
    else
    {
        this.select(node);
    }
    return 0;
};

YAHOO.widget.TreeView.prototype.expandToPath = function(requestPath)
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



YAHOO.widget.RootNode.prototype.isRoot = function()
{
    return true;
};

YAHOO.widget.RootNode.prototype.getPath = function()
{
    return "";
};

//---( FILE NODE )---

/**
 * Constructor.
 */
var FileNode = function(oData, oParent, expanded) {

    this.type = "FileNode";
	if (oParent) {
		this.init(oData, oParent, expanded);
		this.setUpLabel(oData);
        this.href = "javascript:" + this.getFileToggleLink();
	}
};

FileNode.prototype = new YAHOO.widget.TextNode();

FileNode.prototype.isRoot = function()
{
    return false;
};

FileNode.prototype.getSeparator = function()
{
    return this.data.separator;
};

FileNode.prototype.getName = function()
{
    return this.data.name;
};

FileNode.prototype.getPath = function()
{
    var path = this.getName();

    // need to be a little careful. the root node does not have a path function.
    if (this.parent && !this.parent.isRoot())
    {
        // ensure that the parent path does not end with the separator char.
        var sep = this.getSeparator();
        var parentPath = this.parent.getPath();

        if (parentPath.indexOf(sep) == parentPath.length - 1)
        {
            path = parentPath + path;
        }
        else
        {
            path = parentPath + sep + path;
        }
    }
    return path;
};

/**
 * Override the base hasChildren method to add knowledge of the file type. Only folders
 * and root nodes (containers) can be opened.
 */
FileNode.prototype.hasChildren = function(checkForLazyLoad) {

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
FileNode.prototype.isContainer = function()
{
    return this.data.type == "folder" || this.data.type == "root";
};

FileNode.prototype.getFileElId = function()
{
    return "ygtvfile" + this.index;
};

FileNode.prototype.getFileEl = function()
{
    return document.getElementById(this.getFileElId());
}

FileNode.prototype.getFileStyle = function(openIfLoading)
{
    if (this.data.type == "folder")
    {
        // check if we are open.
        if (this.hasChildren(true) || (this.isDynamic() && !this.getIconMode())) {
            return (this.expanded || (openIfLoading && this.isDynamic && !this.dynamicLoadComplete)) ? "openfolder" : "folder";
        }
    }
    return this.data.type
}

/**
 * If the node has been rendered, update the html to reflect the current
 * state of the node.
 */
FileNode.prototype.updateFileHtml = function()
{
	if (this.parent && this.parent.childrenRendered)
    {
		this.getFileEl().className = this.getFileStyle(true);
	}
};

FileNode.prototype.getFileToggleLink = function(selectNode)
{
    return "YAHOO.widget.TreeView.getNode(\'" + this.tree.id + "\'," +
        this.index + ").fileToggle("+(selectNode || "")+")";
},

FileNode.prototype.fileToggle = function(selectNode)
{
    if (selectNode)
    {
        this.select();
    }
    this.toggle();
    this.updateFileHtml();
}

FileNode.prototype.select = function()
{
    this.tree.select(this);
}

FileNode.prototype.onLabelClick = function(me)
{
    this.select();
    return false;
};

FileNode.prototype.onLabelDblClick = function(me)
{
    this.tree.onActivate(this);
    return false;
}

FileNode.prototype.getNodeHtml = function() {
	var sb = [];

	sb[sb.length] = '<table border="0" cellpadding="0" cellspacing="0">';
	sb[sb.length] = '<tr>';

    // depth rendering.
    for (i=0;i<this.depth;++i)
    {
		sb[sb.length] = '<td class="' + this.getDepthStyle(i) + '">&nbsp;</td>';
	}

	var getNode = 'YAHOO.widget.TreeView.getNode(\'' +
					this.tree.id + '\',' + this.index + ')';

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
	sb[sb.length] = ' onclick="javascript:' + this.getFileToggleLink() + '">';

	sb[sb.length] = '&nbsp;';

	sb[sb.length] = '</td>';

    // type image rendering.
    sb[sb.length] = '<td';
    sb[sb.length] = ' id="' + this.getFileElId() + '"';
    sb[sb.length] = ' class="' + this.getFileStyle(false) + '"';
    sb[sb.length] = ' onclick="javascript:' + this.getFileToggleLink(true) + '"';
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

