// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.tree.ConfigTreeLoader = function(base)
{
    Zutubi.tree.ConfigTreeLoader.superclass.constructor.call(this, {
        dataUrl: base,
        requestMethod: 'get'
    });
};

Ext.extend(Zutubi.tree.ConfigTreeLoader, Ext.tree.TreeLoader, {
    getNodeURL: function(node)
    {
        var tree, path;

        tree = node.getOwnerTree();
        path = tree.toConfigPathPrefix(node.getPath('baseName'));
        return this.dataUrl + '/' + encodeURIPath(path);
    },

    requestData: function(node, callback)
    {
        var params, cb;

        if(this.fireEvent("beforeload", this, node, callback))
        {
            params = this.getParams(node);
            cb = {
                success: this.handleResponse,
                failure: this.handleFailure,
                scope: this,
                argument: {callback: callback, node: node}
            };

            this.transId = Ext.lib.Ajax.request(this.requestMethod, this.getNodeURL(node) + '?ls', cb, params);
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
