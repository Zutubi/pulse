ZUTUBI.widget.TreeView = function(id)
{
    if (id)
    {
        this.init(id);
    }
};

YAHOO.extend(ZUTUBI.widget.TreeView, YAHOO.widget.TreeView, {

    /**
     * Reference to the currently selected node.
     */
    selected: null,

    /**
     * Add an on select callback to the treeview.
     */
    onSelect: function(node)
    {
    },

    /**
     * Handle the selection of a tree node.
     */
    select: function(node)
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
    },

    // --- (on activate) ---

    /**
     * Callback triggered when a node is 'activated'... ie: double clicked.
     *
     * WARNING: STILL TO BE IMPLEMENTED.
     */
    onActivate: function(node)
    {

    },

    // --- ( path separator ) ---

    /**
     * The separator character is used when node paths are generated.
     */
    separator: '/',

    /**
     * Specify the path separator character.
     */
    setSeparator: function(sep)
    {
        this.separator = sep;
    },

    /**
     * Implement the TreeView onExpand callback handler.
     *
     */
    onExpand: function(node)
    {
        // the onexpand callback is triggered before the this.expanded is set to true
        if (node.getFileEl() && node.data)
        {
            node.getFileEl().className = node.data.type + "_o";
        }
    },

    /**
     * Implement the TreeView onCollapse callback handler.
     *
     */
    onCollapse: function(node)
    {
        if (node.getFileEl() && node.data)
        {
            node.getFileEl().className = node.data.type;
        }
    },

    /**
     * Override this method to receive onAction callbacks.
     *
     * node:    the node on which the action is being executed.
     * action:  the action string identifying the action
     *
     */
    onAction: function(node, action)
    {

    },

    // ---( utility methods. )---

    /**
     * Retrieve the node defined by the path relative to the base node.
     *
     * baseNode:        the starting point of the path search. If no base node is specified, the trees root node is used.
     * requestPath:     the path specifying the node being retrieved.
     *
     * This method will return the requested node, or null if the node could not be located. This method will
     * not dynamically load nodes.
     */
    getNodeByPath: function(baseNode, requestPath)
    {
        var node = baseNode;
        if (!node)
        {
            node = this.getRoot();
        }

        // a: normalize the path so that it does not matter which separator is being used.
        requestPath = requestPath.replace('\\', '/');

        // search for node with name initPath.
        requestPath = requestPath.split('/');

        var p = null; // previous node.
        for (var i = 0; i < requestPath.length; i++)
        {
            var path = requestPath[i];
            if (path == "")
            {
                // happens when the path begins with a '/'
                continue;
            }

            // we are only interested in nodes that are available locally.
            if (!node.isDynamic() || node.dynamicLoadComplete)
            {
                // Attempt to locate the child node specified by the current path element.
                node = $A(node.children).find(function(child)
                {
                    var n = child.data.name;
                    // normalise the name to ensure we are using '/' as the path separator.
                    n = n.replace('\\', '/');

                    // ensure that trailing '\\' are removed.
                    if (n.indexOf('/') == n.length - 1)
                    {
                        n = n.substring(0, n.length - 1);
                    }
                    return n == path;
                });
                
                if (!node)
                {
                    // the requested path node does not exist.
                    return null;
                }
                p = node;
            }
            else
            {
                // this node has not been loaded, so we can not traverse any further.
                return null;
            }
        }

        return node;
    },

    /**
     * Select the currently selected nodes parent. If there is no selected node
     * or the node does not have a parent, no change is made.
     */
    goUp: function()
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
    },

    /**
     * Expand the tree to the node defined by the requested path.
     *
     * If this tree is dynamically loaded, there may be a delay in expanding
     * to the requested path due to loading of the data. As a result, this method returns
     * 3 response codes.
     *
     * code 0: expansion to the requested path is complete.
     * code 1: expansion to the requested path has failed, the path is invalid.
     * code 2: expansion to the request path is in progress. One of the nodes in the path is being loaded.
     */
    expandTo: function(requestPath)
    {
        var node = this.getRoot();

        // search for node with name initPath.
        var sep = this.separator;
        requestPath = requestPath.split(sep);

        var p = null; // previous node.
        for (var i = 0; i < requestPath.length; i++)
        {
            var path = requestPath[i];
            if (path == "")
            {
                // happens when the path begins with a '/'
                continue;
            }
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
    },

    expandToPath: function(requestPath)
    {
        if (!requestPath)
        {
            return;
        }

        // split the request path into its components.

        var status = this.expandTo(requestPath);
        if (status == 0)
        {
            // complete.
            return;
        }

        if (status == 1)
        {
            // node does not exist.
            return;
        }

        if (status == 2)
        {
            // still loading, so lets try again in 250 milliseconds
            var self = this;
            setTimeout(function()
            {
                self.expandToPath(requestPath);
            }, 250);
        }
    },

    /**
     * Construct the human readable path that identifies the specified node.
     *
     */
    getDisplayPath: function(node)
    {
        return this._getPath(node, function(data)
        {
            return data.name;
        });
    },

    /**
     * Construct the id path that uniquely identifies the specified node.
     *
     */
    getIdPath: function(node)
    {
        return this._getPath(node, function(data)
        {
            return data.id;
        });
    },

    /**
     * Construct a path for the specified node, using the getValue function to define the data
     * used to construct the path components.
     */
    _getPath: function(node, getValue)
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
});

// ---( ROOT NODE )---

/**
 * The root node is a virtual node that is not displayed in the UI.
 *
 */
/**
YAHOO.extend(ZUTUBI.widget.RootNode, YAHOO.widget.RootNode, {

    isRoot: function()
    {
        return true;
    },

    getPath: function()
    {
        return "";        
    }
});
*/

//---( FILE NODE )---

/**
 * Constructor.
 */
ZUTUBI.widget.FileNode = function(oData, oParent, expanded) {

    this.type = "FileNode";
	if (oParent)
    {
		this.init(oData, oParent, expanded);
	}
    this.setUpLabel(oData);
};

YAHOO.extend(ZUTUBI.widget.FileNode, YAHOO.widget.TextNode, {

    getActions: function()
    {
        return this.data.actions;    
    },

    isRoot: function()
    {
        return false;
    },

    getName: function()
    {
        return this.data.name;
    },

    getPath: function()
    {
        return this.tree.getDisplayPath(this);
    },

    getIdPath: function()
    {
        return this.tree.getIdPath(this);
    },

    /**
     * Override the base hasChildren method to add knowledge of the file type. Only folders
     * and root nodes (containers) can be opened.
     */
    hasChildren: function(checkForLazyLoad) {

        if (!this.isContainer())
        {
            return false;
        }

        return ( this.children.length > 0 ||
                (checkForLazyLoad && this.isDynamic() && !this.dynamicLoadComplete) );
    },

    /**
     * Check if this node represents a container, a node that may contain children.
     */
    isContainer: function()
    {
        return this.data.container == true;
    },

    getFileElId: function()
    {
        return "ygtvfile" + this.index;
    },

    getFileEl: function()
    {
        return document.getElementById(this.getFileElId());
    },

    getFileStyle: function(openIfLoading)
    {
        var type = this.data.type;

        // check if we are open.
        if (this.hasChildren(true) || (this.isDynamic() && !this.getIconMode()))
        {
            return (this.expanded || (openIfLoading && this.isDynamic && !this.dynamicLoadComplete)) ? type + "_o" : type;
        }

        return type
    },

    select: function()
    {
        this.tree.select(this);
    },

    onLabelClick: function(me)
    {
        this.select();
        return false;
    },

    onLabelDblClick: function(me)
    {
        this.tree.onActivate(this);
        return false;
    },

    onActionClick: function(action)
    {
        this.tree.onAction(this, action);
        return false;
    },

    getActionLink: function(action)
    {
        return "YAHOO.widget.TreeView.getNode(\'" + this.tree.id + "\'," +
            this.index + ").onActionClick(\'"+action+"\')";
    },

    // overrides YAHOO.widget.TextNode
    getNodeHtml: function()
    {
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
        sb[sb.length] = '&nbsp;';
        sb[sb.length] = '</td>';

        // render the actions.
        var actions = $A(this.getActions());
        var self = this;
        actions.each(function(action)
        {
            sb[sb.length] = '<td class="'+action+'"';
            sb[sb.length] = ' onclick="javascript:' + self.getActionLink(action) + '"';
            sb[sb.length] = '>';
            sb[sb.length] = '&nbsp;';
            sb[sb.length] = '</td>';
        });

        sb[sb.length] = '</tr>';
        sb[sb.length] = '</table>';

        return sb.join("");
    }
});

ZUTUBI.widget.PulseTreeView = function(id)
{
    if (id)
    {
        this.init(id);
        var self = this;
        this.setDynamicLoad(function(node, onCompleteCallback)
        {
            self.ls(node, onCompleteCallback, true, false);
        }, 1);
    }
};

YAHOO.extend(ZUTUBI.widget.PulseTreeView, ZUTUBI.widget.TreeView, {

    fsRoot:null,

    base:null,

    error:"error",

    setFSRoot: function(root)
    {
        this.fsRoot = root;
    },

    /**
     * Specify the base context path, needed for any links that are generated.
     */
    setBase: function(base)
    {
        this.base = base;
    },

    setErrorId: function(id)
    {
        this.error = id;
    },

    onAction: function(node, action)
    {
        if (action == "download")
        {
            document.location = this.base+"/cat.action?path=" + this.fsRoot + node.getIdPath();
        }
        if (action == "decorate")
        {
            document.location = this.base+"/viewArtifact.action?path=" + this.fsRoot + node.getIdPath();
        }
        if (action == "archive")
        {
            document.location = this.base+"/zip.action?path=" + this.fsRoot + node.getIdPath();
        }
    },

    ls: function(node, onCompleteCallback, showFiles, showHidden)
    {
        this.hideActionErrors();
            
        // generate id path.
        var p = "";
        if (node.tree.getIdPath)
        {
            // not available for Yahoo.widget.RootNodes
            p = node.tree.getIdPath(node);
        }

        var ajax = new Ajax.Request(
            this.base+"/ajax/ls.action",
            {
                method: 'post',
                onComplete: this.lsResponse(node, onCompleteCallback),
                onFailure: this.handleFailure,
                onException: this.handleException,
                parameters: "path=" + this.fsRoot + p +
                             (showFiles && "&showFiles=" + showFiles || "") +
                             (showHidden && "&showHidden=" + showHidden || "")
            }
        );
    },

    lsResponse: function(parentNode, callback)
    {
        var self = this; // does this cause a js memory leak?...
        return function(response)
        {
            var jsonObj = eval("(" + response.responseText + ")");

            if (jsonObj.actionErrors && $A(jsonObj.actionErrors).length > 0)
            {
                // display the action error messages.
                self.showActionErrors(jsonObj.actionErrors);
            }

            var results = $A(jsonObj.listing);
            results.each(function(obj)
            {
                var data = {
                    "id":obj.id,
                    "name":obj.file,
                    "label":obj.file,
                    "type":obj.type,
                    "container":obj.container,
                    "actions":obj.actions
                };
                var node = new ZUTUBI.widget.FileNode(data, parentNode, false);

            });
            if (callback)
            {
                callback();
            }
        };
    },

    /**
     * Display the provided list of action error messages within the configured error div.
     *
     * actionErrors:    an array of error messages.
     */
    showActionErrors: function(actionErrors)
    {
        var sb = [];
        sb[sb.length] = "<ul class=\"error\">";
        $A(actionErrors).each(function(msg)
        {
            sb[sb.length] = "<li class=\"error\">";
            sb[sb.length] = msg;
            sb[sb.length] = "</li>";
        });
        sb[sb.length] = "</ul>";

        var element = $(this.error);
        element.innerHTML = sb.join("");
        Element.show(element);
    },

    hideActionErrors: function()
    {
        var element = $(this.error);
        if (element)
        {
            Element.hide(element);
        }
    },

    handleFailure: function(e, e2)
    {
        openDebugAlert(e2);
    },

    handleException: function(e, e2)
    {
        openDebugAlert(e2);
    }
})
