// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.fs.FSTreeLoader = function(config)
{
    var baseUrl;

    baseUrl = config.baseUrl;
    this.preloadDepth = config.preloadDepth || 0;

    Zutubi.fs.FSTreeLoader.superclass.constructor.call(this, {
        dataUrl: baseUrl + '/ajax/xls.action',
        baseParams: config
    });
};

Ext.extend(Zutubi.fs.FSTreeLoader, Ext.tree.TreeLoader, {
    getParams: function(node)
    {
        var buf, bp, key;

        buf = [];
        bp = this.baseParams;

        for (key in bp)
        {
            if (typeof bp[key] !== "function")
            {
                buf.push(encodeURIComponent(key), "=", encodeURIComponent(bp[key]), "&");
            }
        }
        buf.push("path=", encodeURIComponent(node.getPath("baseName")));
        if (this.preloadDepth && node.getDepth() === 0)
        {
            buf.push("&depth=", this.preloadDepth);
        }
        return buf.join("");
    }
});
