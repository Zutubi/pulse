// dependency: ./namespace.js
// dependency: ext/package.js

ZUTUBI.tree.ConfigTreeLoader = function(base)
{
    ZUTUBI.tree.ConfigTreeLoader.superclass.constructor.call(this, {
        dataUrl: base,
        requestMethod: 'get'
    });
};

Ext.extend(ZUTUBI.tree.ConfigTreeLoader, Ext.tree.TreeLoader, {
    getNodeURL: function(node)
    {
        var tree = node.getOwnerTree();
        var path = tree.toConfigPathPrefix(node.getPath('baseName'));
        return this.dataUrl + '/' + encodeURIPath(path);
    },

    requestData: function(node, callback)
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
