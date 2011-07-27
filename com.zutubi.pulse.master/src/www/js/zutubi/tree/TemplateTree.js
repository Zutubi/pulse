// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ConfigTree.js

Zutubi.tree.TemplateTree = function(scope, config)
{
    this.scope = scope;
    this.dead = false;
    Zutubi.tree.TemplateTree.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.tree.TemplateTree, Zutubi.tree.ConfigTree, {
    handleResponse: function(response)
    {
        var tree = this;

        if (response.addedFiles)
        {
            each(response.addedFiles, function(addition) {
                if (addition.parentTemplatePath && addition.parentPath == tree.scope)
                {
                    tree.addNode(addition.parentTemplatePath, { baseName: addition.baseName, text: addition.displayName, iconCls: addition.iconCls, leaf: addition.templateLeaf});
                }
            });
        }

        if (response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(tree.translatePath(rename.oldPath), rename.newName, rename.newDisplayName); });
        }

        if (response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(tree.translatePath(path)); } );
        }
    },

    redirectToNewPath: function(response)
    {
        if (response.newTemplatePath)
        {
            this.selectConfigPath(response.newTemplatePath);
        }
    },

    findNodeByAttribute: function(attribute, value, node)
    {
        node = node || this.root;
        if (node.attributes[attribute] == value)
        {
            return node;
        }

        var cs = node.childNodes;
        var i, len;
        for(i = 0, len = cs.length; i < len; i++)
        {
            var found = this.findNodeByAttribute(attribute, value, cs[i]);
            if (found)
            {
                return found;
            }
        }

        return null;
    },

    translatePath: function(path)
    {
        var pieces = path.split(this.pathSeparator);
        if (pieces.length == 2 && pieces[0] == this.scope)
        {
            var baseName = pieces[1];
            var node = this.findNodeByAttribute('baseName', baseName);
            if (node)
            {
                return this.getNodeConfigPath(node);
            }
        }

        return null;
    }
});
