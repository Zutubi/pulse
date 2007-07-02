// setup the namespace for the zutubi components.
var ZUTUBI = window.ZUTUBI || {};

// if it is already defined, great, otherwise create it.
ZUTUBI.widget = ZUTUBI.widget || {};


ZUTUBI.ConfigTreeLoader = function(base)
{
    ZUTUBI.ConfigTreeLoader.superclass.constructor.call(this, {
        dataUrl: base,
        requestMethod: 'get'
    });
};

Ext.extend(ZUTUBI.ConfigTreeLoader, Ext.tree.TreeLoader, {
    getNodePath : function(node)
    {
        var tree = node.getOwnerTree();
        return this.dataUrl + '/' + tree.getNodeConfigPath(node);
    },

    requestData : function(node, callback)
    {
        if(this.fireEvent("beforeload", this, node, callback))
        {
            var params = this.getParams(node);
            var cb = {
                success: this.handleResponse,
                failure: this.handleFailure,
                scope: this,
                argument: {callback: callback, node: node}
            };

            this.transId = Ext.lib.Ajax.request(this.requestMethod, this.getNodePath(node) + '?ls', cb, params);
        }
        else
        {
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if(typeof callback == "function")
            {
                callback();
            }
        }
    }
});


ZUTUBI.ConfigTree = function(id, config)
{
    ZUTUBI.ConfigTree.superclass.constructor.call(this, id, config);
}

Ext.extend(ZUTUBI.ConfigTree, Ext.tree.TreePanel, {
    configPathToTreePath: function(configPath)
    {
        var treePath = configPath;

        if (this.pathPrefix && treePath.indexOf(this.pathPrefix) == 0)
        {
            treePath = treePath.substring(this.pathPrefix.length);
        }

        if(treePath.length > 0 && treePath[0] != '/')
        {
            treePath = '/' + treePath;
        }

        if (!this.rootVisible)
        {
            treePath = this.pathSeparator + this.root.id + treePath;
        }

        // Workaround odd Ext behaviour: selecting needs a leading slash
        // except when selecting the root.
        if (treePath.lastIndexOf('/') == 0)
        {
            treePath = treePath.substring(1);
        }

        //console.log('cpttp(' + configPath + ') -> ' + treePath);
        return treePath;
    },

    treePathToConfigPath: function(treePath)
    {
        var configPath = treePath;
        
        if (!this.rootVisible)
        {
            configPath = configPath.substring(this.root.id.length + 1);
        }

        if(configPath.length > 0 && configPath[0] == '/')
        {
            configPath = configPath.substring(1);    
        }

        if (this.pathPrefix)
        {
            configPath = this.pathPrefix + this.pathSeparator + configPath;
        }

        //console.log('tptcp(' + treePath + ') -> ' + configPath);
        return configPath;
    },

    selectConfigPath: function(configPath)
    {
        this.getSelectionModel().clearSelections();
        this.selectPath(this.configPathToTreePath(configPath));
    },

    getNodeConfigPath: function(node)
    {
        return this.treePathToConfigPath(node.getPath());
    },

    getNodeByConfigPath: function(configPath)
    {
        var treePath = this.configPathToTreePath(configPath);
        if(treePath[0] == '/')
        {
            treePath = treePath.substring(1);
        }
        
        var keys = treePath.split(this.pathSeparator);

        if (keys[0] != this.root.id)
        {
            return null;
        }

        var node = this.root;
        for(var i = 1; node && i < keys.length; i++)
        {
            node = node.findChild('id', keys[i]);
        }
        
        return node;
    },

    handleResponse: function(response)
    {
        var tree = this;

        if(response.addedFiles)
        {
            each(response.addedFiles, function(addition) { tree.addNode(addition.parentPath, {id: addition.baseName, text: addition.displayName, leaf: addition.leaf}); });
        }

        if(response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(rename.oldPath, rename.newName); });
        }

        if(response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(path) });
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
        if (parentPath)
        {
            var parentNode = this.getNodeByConfigPath(parentPath);
            if (parentNode)
            {
                var newNode = this.getLoader().createNode(config);
                parentNode.appendChild(newNode);
            }
        }
    },

    renameNode: function(oldPath, newName)
    {
        if(oldPath)
        {
            var node = this.getNodeByConfigPath(oldPath);
            if(node)
            {
                this.setNodeId(node, newName);
                node.setText(newName);
            }
        }
    },

    removeNode: function(path)
    {
        if (path)
        {
            var node = this.getNodeByConfigPath(path);
            if (node && !node.isRoot)
            {
                node.parentNode.removeChild(node);
            }
        }
    },

    setNodeId: function(node, id)
    {
        this.unregisterNode(node);
        node.id = id;
        node.attributes.id = id;
        this.registerNode(node);
    }
});


ZUTUBI.TemplateTree = function(scope, id, config)
{
    this.scope = scope;
    ZUTUBI.TemplateTree.superclass.constructor.call(this, id, config);
}

Ext.extend(ZUTUBI.TemplateTree, ZUTUBI.ConfigTree, {
    handleResponse: function(response)
    {
        var tree = this;

        if (response.addedFiles)
        {
            each(response.addedFiles, function(addition) {
                if (addition.parentTemplatePath && addition.parentPath == tree.scope)
                {
                    tree.addNode(addition.parentTemplatePath, {id: addition.baseName, text: addition.displayName, leaf: addition.leaf});
                }
            });
        }

        if (response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(tree.translatePath(rename.oldPath), rename.newName); });
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

    translatePath: function(path)
    {
        var pieces = path.split(this.pathSeparator);
        if (pieces.length == 2 && pieces[0] == this.scope)
        {
            var id = pieces[1];
            var node = this.getNodeById(id);
            if (node)
            {
                return this.getNodeConfigPath(node);
            }
        }
        
        return null;
    }
});

ZUTUBI.CheckForm = function(mainForm, options)
{
    ZUTUBI.CheckForm.superclass.constructor.call(this, options);
    this.mainForm = mainForm;
};

Ext.extend(ZUTUBI.CheckForm, Ext.form.Form, {
    isValid: function()
    {
        // Call both, they have side-effects.
        var mainValid = this.mainForm.isValid();
        var valid = ZUTUBI.CheckForm.superclass.isValid.call(this);
        return mainValid && valid;
    },

    markInvalid: function(errors)
    {
        for(var i = 0; i < errors.length; i++)
        {
            var fieldError = errors[i];
            var id = fieldError.id;
            var field;

            if(id.lastIndexOf('_check') == id.length - 6)
            {
                field = this.mainForm.findField(id.substr(0, id.length - 6));
            }
            else
            {
                field = this.findField(id);
            }
            if(field)
            {
                field.markInvalid(fieldError.msg);
            }
        }
    },

    submit: function(options)
    {
        var params = options.params || {};
        var mainParams = this.mainForm.getValues(false);

        for(var param in mainParams)
        {
           params[param + '_check'] = mainParams[param];
        }

        options.params = params;
        ZUTUBI.CheckForm.superclass.submit.call(this, options);
    }
});

Ext.form.Field.prototype.isDirty = function()
{
    if(this.disabled)
    {
        return false;
    }
    return String(this.getValue()) !== String(this.originalValue);
}
