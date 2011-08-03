// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.tree.ConfigTree = function(config)
{
    Zutubi.tree.ConfigTree.superclass.constructor.call(this, config);
    this.dead = false;
};

Ext.extend(Zutubi.tree.ConfigTree, Ext.tree.TreePanel, {

    getSelectedConfigPath: function()
    {
        var treePath;

        treePath = this.getSelectedTreePath();
        return treePath == null ? null : this.toConfigPathPrefix(treePath);
    },

    getSelectedTreePath: function()
    {
        var node;

        node = this.getSelectionModel().getSelectedNode();
        if (node)
        {
            return node.getPath('baseName');
        }
        return null;
    },

    toTreePathPrefix: function(configPath)
    {
        var treePath;

        treePath = configPath;

        if (this.pathPrefix && treePath.indexOf(this.pathPrefix) == 0)
        {
            treePath = treePath.substring(this.pathPrefix.length);
        }

        if(treePath.length > 0 && treePath.substring(0, 1) != '/')
        {
            treePath = '/' + treePath;
        }

        if (!this.rootVisible)
        {
            treePath = this.pathSeparator + this.root.attributes.baseName + treePath;
        }

        if (treePath.substring(0, 1) == '/')
        {
            treePath = treePath.substring(1);
        }

        return treePath;
    },

    toConfigPathPrefix: function(treePath)
    {
        var configPath;

        configPath = treePath;

        if (!this.rootVisible)
        {
            configPath = configPath.substring(this.root.attributes.baseName.length + 1);
        }

        if(configPath.length > 0 && configPath.substring(0, 1) == '/')
        {
            configPath = configPath.substring(1);
        }

        if (this.pathPrefix)
        {
            configPath = this.pathPrefix + this.pathSeparator + configPath;
        }

        return configPath;
    },

    selectConfigPath: function(configPath, callback)
    {
        this.getSelectionModel().clearSelections();
        this.expandToPath(configPath, function(found, node) {
            if (found)
            {
                node.select();
            }
            
            if (callback)
            {
                callback();
            }
        });
    },

    expandToPath: function(path, callback)
    {
        var keys, current, index, skippedLast, f;

        path = this.toTreePathPrefix(path);
        keys = path.split(this.pathSeparator);
        current = this.root;
        if (current.attributes['baseName'] != keys[0])
        {
            if(callback)
            {
                callback(false, null);
            }
            return;
        }

        index = 0;
        skippedLast = false;
        f = function() {
            var c;

            if (++index == keys.length)
            {
                if (callback)
                {
                    callback(true, current);
                }
                return;
            }

            if (!skippedLast && current.attributes.extraAttributes && current.attributes.extraAttributes.collapsedCollection)
            {
                skippedLast = true;
                f();
            }
            else
            {
                skippedLast = false;
                current.expand(false, false, function() {
                    c = current.findChild('baseName', keys[index]);
                    if (!c)
                    {
                        if(callback)
                        {
                            callback(false, current);
                        }
                        return;
                    }

                    current = c;
                    f();
                });
            }
        };

        f();
    },

    getNodeConfigPath: function(node)
    {
        var p, b;

        p = node.parentNode;
        b = [node.attributes['baseName']];
        while (p)
        {
            if (p.attributes.extraAttributes && p.attributes.extraAttributes.collapsedCollection)
            {
                b.unshift(p.attributes.extraAttributes.collapsedCollection);
            }
            b.unshift(p.attributes['baseName']);
            p = p.parentNode;
        }

        return this.toConfigPathPrefix('/' + b.join('/'));
    },

    getNodeByConfigPath: function(configPath)
    {
        var path, keys, current, skippedLast, i;

        path = this.toTreePathPrefix(configPath);
        keys = path.split(this.pathSeparator);
        current = this.root;
        if (current.attributes['baseName'] != keys[0])
        {
            return null;
        }

        skippedLast = false;
        for(i = 1; current && i < keys.length; i++)
        {
            if (!skippedLast && current.attributes.extraAttributes && current.attributes.extraAttributes.collapsedCollection)
            {
                skippedLast = true;
            }
            else
            {
                skippedLast = false;
                current = current.findChild('baseName', keys[i]);
            }
        }
        return current;
    },

    handleResponse: function(response)
    {
        var tree;

        tree = this;

        if(response.addedFiles)
        {
            each(response.addedFiles, function(addition) { tree.addNode(addition.parentPath, {baseName: addition.baseName, text: addition.displayName, iconCls: addition.iconCls, leaf: addition.leaf, extraAttributes: {collapsedCollection: addition.collapsedCollection}}); });
        }

        if(response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(rename.oldPath, rename.newName, rename.newDisplayName, rename.collapsedCollection); });
        }

        if(response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(path); });
        }
    },

    redirectToNewPath: function(response)
    {
        if (response.newPath)
        {
            this.selectConfigPath(response.newPath);
        }
    },

    addNode: function(parentPath, config)
    {
        var parentNode, newNode;

        if (parentPath)
        {
            parentNode = this.getNodeByConfigPath(parentPath);
            if (parentNode)
            {
                newNode = this.getLoader().createNode(config);
                parentNode.leaf = false;
                parentNode.appendChild(newNode);
            }
        }
    },

    renameNode: function(oldPath, newName, newDisplayName, collapsedCollection)
    {
        var node;

        if(oldPath)
        {
            node = this.getNodeByConfigPath(oldPath);
            if(node)
            {
                node.attributes.baseName = newName;
                node.setText(newDisplayName);
                if (!node.attributes.extraAttributes)
                {
                    node.attributes.extraAttributes = {};
                }

                node.attributes.extraAttributes.collapsedCollection = collapsedCollection;
            }
        }
    },

    removeNode: function(path)
    {
        var node;

        if (path)
        {
            node = this.getNodeByConfigPath(path);
            if (node)
            {
                if(node.isRoot)
                {
                    this.dead = true;
                }
                else
                {
                    node.parentNode.removeChild(node);
                }
            }
        }
    }
});
