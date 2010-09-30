// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.toolbar.Toolbar = Ext.extend(Ext.Toolbar, {
    initComponent: function()
    {
        var config = {
            layout: 'xztoolbar'
        };
        Ext.apply(this, config);
        Ext.apply(this.initialConfig, config);

        Zutubi.toolbar.Toolbar.superclass.initComponent.call(this);
    }
});
Ext.reg('xztoolbar', Zutubi.toolbar.Toolbar);
