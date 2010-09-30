// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./PulseFileSystemBrowser.js

Zutubi.fs.WorkingCopyFileSystemBrowser = Ext.extend(Zutubi.fs.PulseFileSystemBrowser, {

    initComponent: function() {

        this.defaultTreeConfig = {
            tbar: new Ext.Toolbar()
        };

        Zutubi.fs.WorkingCopyFileSystemBrowser.superclass.initComponent.apply(this, arguments);

        var toolbar = this.tree.getTopToolbar();

        var reloadButton = new Zutubi.fs.ReloadSelectedNodeButton({
            icon: this.baseUrl + '/images/arrow_refresh.gif',
            tooltip: 'refresh',
            disabled: false,
            tree: this.tree
        });

        toolbar.add(reloadButton);
    }
});
