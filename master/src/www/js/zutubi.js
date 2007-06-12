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
        var path = node.getPath().substring(2);
        if(path.length == 0)
        {
            path = '/';
        }

        return this.dataUrl + path;
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
    getNodeByPath: function(path)
    {
        var keys = path.split(this.pathSeparator);
        var node = this.root;
        for(var i = 0; node && i < keys.length; i++)
        {
            node = node.findChild('id', keys[i]);
        }
        return node;
    },

    invalidatePaths: function(paths, callback, index)
    {
        if(!index)
        {
            index = 0;
        }

        if(paths && paths.length - index > 0)
        {
            callback = paths.length - index == 1 ? callback : function() { invalidatePaths(paths, callback, index + 1); };
            var node = this.getNodeByPath(paths[index]);
            if(node && node.isLoaded())
            {
                node.reload(callback);
                return;
            }
        }

        callback();
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
