// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./PulseFileSystemBrowser.js

/**
 * A PulseFileSystemBrowser with a toolbar and some buttons.
 */
Zutubi.fs.LocalFileSystemBrowser = Ext.extend(Zutubi.fs.PulseFileSystemBrowser, {

    isWindows: false,

    initComponent: function() {

        this.fs = 'local';

        this.defaultTreeConfig = {
            tbar: new Ext.Toolbar()
        };

        Zutubi.fs.LocalFileSystemBrowser.superclass.initComponent.apply(this, arguments);

        var toolbar = this.tree.getTopToolbar();
        var statusBar = this.tree.getBottomToolbar();

        var userHomeButton = new Zutubi.fs.SelectNodeButton({
            icon: this.baseUrl + '/images/house.gif',
            tooltip: 'go to user home',
            tree: this.tree
        });
        var reloadButton = new Zutubi.fs.ReloadSelectedNodeButton({
            icon: this.baseUrl + '/images/arrow_refresh.gif',
            tooltip: 'refresh folder',
            tree: this.tree
        });
        var createFolderButton = new Zutubi.fs.CreateFolderButton({
            icon: this.baseUrl + '/images/folder_add.gif',
            tooltip: 'create new folder',
            baseUrl:this.baseUrl,
            basePath:this.basePath,
            tree: this.tree,
            sbar: statusBar
        });
        var deleteFolderButton = new Zutubi.fs.DeleteFolderButton({
            icon: this.baseUrl + '/images/folder_delete.gif',
            cls: 'x-btn-icon',
            tooltip: 'delete folder',
            baseUrl: this.baseUrl,
            basePath: this.basePath,
            tree: this.tree,
            sbar: statusBar
        });

        toolbar.add(userHomeButton);
        toolbar.add('-');
        toolbar.add(createFolderButton);
        toolbar.add(deleteFolderButton);
        toolbar.add(reloadButton);

        Ext.Ajax.request({
            url: this.baseUrl + '/ajax/getHome.action',
            success: function(rspObj)
            {
                var data = Ext.util.JSON.decode(rspObj.responseText);
                userHomeButton.setPath(data.userHome);
            },
            failure: function(rspObj)
            {
                statusBar.setStatus({
                    text: 'Failed to contact server.',
                    iconCls: 'x-status-error',
                    clear: true // auto-clear after a set interval
                });
            }
        });
    },

    onSubmit: function()
    {
        var node = this.tree.getSelectionModel().getSelectedNode();
        var p = node.getPath('baseName');
        if (!this.tree.rootVisible)
        {
            p = p.substring(this.tree.root.attributes.baseName.length + 1);
        }

        if (this.isWindows)
        {
            if(p.length > 0 && p.substring(0, 1) == '/')
            {
                p = p.substring(1);
            }
        }

        this.target.setValue(p);
        this.close();
    }
});
