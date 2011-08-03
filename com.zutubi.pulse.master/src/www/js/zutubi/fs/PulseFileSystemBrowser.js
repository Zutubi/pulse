// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./FSTreeLoader.js
// dependency: zutubi/tree/ConfigTree.js

/**
 * A tree browser window that supports navigation of the Pulse File System.
 *
 * @cfg fs              the file system to be used. Defaults to 'pulse'.
 * @cfg showFiles       if true, files will be shown.  Defaults to true.
 * @cfg showHidden      if true, hidden files will be shown.  Defaults to false.
 * @cfg autoExpandRoot  if true, the root node is automatically expanded.  Defaults to true.
 * @cfg baseUrl         the base url for communicating with the Pulse server.  All requests
 *                      to the server will be prefixed with this base url.  Defaults to window.baseUrl.
 * @cfg basePath        the base file system path from which browsing will begin.
 * @cfg prefix          the prefix path is applied after the base path, and is used to filter
 *                      any nodes that do not contain that prefix
 */
Zutubi.fs.PulseFileSystemBrowser = Ext.extend(Ext.Window, {

    // window configuration defaults.
    id: 'pulse-file-system-browser',
    layout: 'fit',
    width: 300,
    height: 500,
    closeAction: 'close',
    modal: true,
    plain: true,

    // tree configuration defaults
    fs: 'pulse',
    showFiles: true,
    showHidden: false,
    showRoot: false,
    autoExpandRoot: true,
    rootBaseName: '',
    basePath: '',

    defaultTreeConfig: {},

    initComponent: function()
    {
        var statusBar;

        Zutubi.fs.PulseFileSystemBrowser.superclass.initComponent.apply(this, arguments);

        this.target = Ext.getCmp(this.target);

        statusBar = new Ext.ux.StatusBar({
            defaultText: '',
            useDefaults: true
        });

        this.loader = new Zutubi.fs.FSTreeLoader({
            baseUrl: this.baseUrl,
            fs: this.fs,
            basePath: this.basePath,
            showFiles: this.showFiles,
            showHidden: this.showHidden
        });

        this.loader.on('beforeload', function()
        {
            this.loading = true;
            statusBar.setStatus({text: 'Loading...'});
        }, this);
        this.loader.on('load', function(self, node, response)
        {
            var data;

            data = Ext.util.JSON.decode(response.responseText);
            if (data.actionErrors && data.actionErrors.length > 0)
            {
                statusBar.setStatus({
                    text: data.actionErrors[0],
                    iconCls: 'x-status-error',
                    clear: true
                });
            }
            else
            {
                statusBar.clearStatus();
            }
            this.loading = false;
        }, this);
        this.loader.on('loadexception', function()
        {
            statusBar.setStatus({
                text: 'An error has occured',
                iconCls: 'x-status-error',
                clear: true
            });
            this.loading = false;
        }, this);

        this.tree = new Zutubi.tree.ConfigTree(Ext.apply({
            loader: this.loader,
            layout: 'fit',
            border: false,
            animate: false,
            autoScroll: true,
            bbar: statusBar,
            rootVisible: this.showRoot,
            bodyStyle: 'padding: 10px'
        }, this.defaultTreeConfig));

        this.tree.setRootNode(new Ext.tree.AsyncTreeNode({
            baseName: this.rootBaseName,
            expanded: this.autoExpandRoot,
            allowDrag: false,
            allowDrop: false
        }));

        this.tree.on('afterlayout', this.showMask, this, {single:true});
        this.loader.on('load', this.hideMask, this, {single:true});
        this.loader.on('loadexception', this.hideMask, this, {single:true});

        this.add(this.tree);

        if (this.target)
        {
            this.submitButton = new Ext.Button({
                text: 'ok',
                disabled: true,
                handler: this.onSubmit.createDelegate(this)
            });
            this.addButton(this.submitButton);

            this.tree.getSelectionModel().on('selectionchange', this.onSelectionChange.createDelegate(this));
        }

        this.closeButton = new Ext.Button({
            text: 'cancel',
            handler: function()
            {
                this.close();
            }.createDelegate(this)
        });
        this.addButton(this.closeButton);
    },

    onSubmit: function()
    {
        var node, p;

        node = this.tree.getSelectionModel().getSelectedNode();
        p = node.getPath('baseName');
        if (!this.tree.rootVisible)
        {
            p = p.substring(this.tree.root.attributes.baseName.length + 1);
        }

        if(p.length > 0 && p.substring(0, 1) == '/')
        {
            p = p.substring(1);
        }

        this.target.setValue(p);
        this.close();
    },

    showMask: function()
    {
        this.initialLoadingMask = new Ext.LoadMask(this.tree.getEl(), { msg: "Loading..." });
        this.initialLoadingMask.show();
    },

    hideMask: function()
    {
        this.initialLoadingMask.hide();
    },

    onSelectionChange: function(selectionModel, node)
    {
        if (node)
        {
            if (this.submitButton.disabled)
            {
                this.submitButton.enable();
            }
            node.ensureVisible();
        }
        else
        {
            if (!this.submitButton.disabled)
            {
                this.submitButton.disable();
            }
        }
    },

    show: function()
    {
        Zutubi.fs.PulseFileSystemBrowser.superclass.show.apply(this, arguments);

        if (this.target)
        {
            var initVal = this.target.getValue();
            if (initVal)
            {
                this.tree.selectConfigPath(initVal);
            }
        }
    }
});


/**
 * Button used to select a node in a tree.
 *
 * @cfg path        the tree path to be selected when this button is clicked.
 * @cfg tree        the tree in which the path will be selected.
 */
Zutubi.fs.SelectNodeButton = Ext.extend(Ext.Button, {

    cls: 'x-btn-icon',

    initComponent: function()
    {
        if (this.path === undefined)
        {
            this.disabled = true;
        }

        Zutubi.fs.SelectNodeButton.superclass.initComponent.apply(this, arguments);
    },

    onClick: function()
    {
        this.tree.selectConfigPath(this.path);
    },

    setPath: function(path)
    {
        this.path = path;
        if (this.path !== undefined)
        {
            this.enable();
        }
        else
        {
            this.disable();
        }
    }
});


/**
 * @cfg tree    the tree in which the selected node (or root if no node is selected)
 *              will be reloaded.
 */
Zutubi.fs.ReloadSelectedNodeButton = Ext.extend(Ext.Button, {

    cls: 'x-btn-icon',
    disabled: true,

    initComponent: function()
    {
        Zutubi.fs.ReloadSelectedNodeButton.superclass.initComponent.apply(this, arguments);

        this.tree.getSelectionModel().on('selectionchange', this.onNodeSelectionChange.createDelegate(this));
    },

    onNodeSelectionChange: function(selectionModel, node)
    {
        if (this.canReload(node) && this.disabled)
        {
            this.enable();
        }
        else if (!this.canReload(node) && !this.disabled)
        {
            this.disable();
        }
    },

    canReload: function(node)
    {
        return !node || node.reload;
    },

    onClick: function()
    {
        var node;

        if (!this.disabled)
        {
            node = this.tree.getSelectionModel().getSelectedNode();
            if (node === null)
            {
                node = this.tree.getRootNode();
            }
            node.reload();
        }
    }
});


Zutubi.fs.DeleteFolderButton = Ext.extend(Ext.Button, {

    initComponent: function()
    {
        if (this.path === undefined)
        {
            this.disabled = true;
        }

        Zutubi.fs.DeleteFolderButton.superclass.initComponent.apply(this, arguments);

        this.tree.getSelectionModel().on('selectionchange', this.onNodeSelectionChange.createDelegate(this));
    },

    onNodeSelectionChange: function(selectionModel, node)
    {
        if (this.isFolder(node) && this.disabled)
        {
            this.enable();
        }
        else if (!this.isFolder(node) && !this.disabled)
        {
            this.disable();
        }
    },

    isFolder: function(node)
    {
        return node && node.reload;
    },

    onClick: function()
    {
        var that;

        that = this;
        Ext.MessageBox.confirm('confirm', 'Are you sure you want to delete the folder?', function(btn)
        {
            if (btn == 'yes')
            {
                that.onDelete();
            }
        });
    },

    onDelete: function()
    {
        var path;

        this.sbar.setStatus({
            text: 'Deleting folder...'
        });

        path = this.tree.getSelectedConfigPath();
        Ext.Ajax.request({
            url: this.baseUrl + '/ajax/rmdir.action',
            params: {
                path: path,
                basePath: this.basePath
            },
            success: this.onSuccess,
            failure: this.onFailure,
            scope: this
        });
    },

    onFailure: function(response, options)
    {
        this.sbar.setStatus({
            text: 'Failed to delete folder.',
            iconCls: 'x-status-error',
            clear: true // auto-clear after a set interval
        });
    },

    onSuccess: function(response, options)
    {
        // check for errors.
        var decodedResponse, deletedNode, deletedPath;

        decodedResponse = Ext.util.JSON.decode(response.responseText);
        if (decodedResponse.actionErrors[0])
        {
            this.sbar.setStatus({
                text: decodedResponse.actionErrors[0],
                iconCls: 'x-status-error',
                clear: true // auto-clear after a set interval
            });
            return;
        }

        this.sbar.setStatus({
            text: 'Folder deleted.',
            clear: true // auto-clear after a set interval
        });

        deletedNode = this.tree.getSelectionModel().getSelectedNode();
        if (deletedNode.previousSibling)
        {
            deletedNode.previousSibling.select();
        }
        else if (deletedNode.nextSibling)
        {
            deletedNode.nextSibling.select();
        }
        else
        {
            deletedNode.parentNode.select();
        }

        deletedPath = this.tree.toConfigPathPrefix(deletedNode.getPath('baseName'));
        this.tree.removeNode(deletedPath);
    }
});


/**
 * @cfg tree    the tree to which the new folder will be added.
 */
Zutubi.fs.CreateFolderButton = Ext.extend(Ext.Button, {

    win: undefined,

    initComponent: function()
    {
        if (this.path === undefined)
        {
            this.disabled = true;
        }

        Zutubi.fs.CreateFolderButton.superclass.initComponent.apply(this, arguments);

        this.tree.getSelectionModel().on('selectionchange', this.onNodeSelectionChange.createDelegate(this));
    },

    onClick: function()
    {
        var that;

        that = this;
        Ext.MessageBox.prompt('create folder', 'folder name:', function(btn, txt)
        {
            if (btn == 'ok')
            {
                that.onOk(txt);
            }
        });
    },

    onOk: function(name)
    {
        var path;

        this.sbar.setStatus({
            text: 'Creating folder...'
        });
        this.newFolderName = name;
        path = this.tree.getSelectedConfigPath();

        Ext.Ajax.request({
            url: this.baseUrl + '/ajax/mkdir.action',
            params: {
                path:path,
                name:name,
                basePath:this.basePath
            },
            success: this.onSuccess,
            failure: this.onFailure,
            scope: this
        });
    },

    onFailure: function(response, options)
    {
        this.sbar.setStatus({
            text: 'Failed to create folder.',
            clear: true // auto-clear after a set interval
        });
    },

    onSuccess: function(response, options)
    {
        // check for errors.
        var decodedResponse, name, selected, newFolder;

        decodedResponse = Ext.util.JSON.decode(response.responseText);
        if (decodedResponse.actionErrors[0])
        {
            this.sbar.setStatus({
                text: decodedResponse.actionErrors[0],
                iconCls: 'x-status-error',
                clear: true // auto-clear after a set interval
            });
            return;
        }

        this.sbar.setStatus({
            text: 'Folder created.',
            clear: true // auto-clear after a set interval
        });

        name = this.newFolderName;

        selected = this.tree.getSelectionModel().getSelectedNode();
        if (!selected.expanded)
        {
            selected.expand(false, true, function(node){
                newFolder = node.findChild('baseName', name);
                newFolder.select();
            });
        }
        else
        {
            this.tree.addNode(this.tree.getSelectedConfigPath(), { baseName: name, text: name, leaf: false });
            newFolder = selected.findChild('baseName', name);
            newFolder.attributes['baseName'] = name; // since everything else uses baseName, lets add it here.
            newFolder.select();
        }
    },

    onNodeSelectionChange: function(selectionModel, node)
    {
        if (this.isFolder(node) && this.disabled)
        {
            this.enable();
        }
        else if (!this.isFolder(node) && !this.disabled)
        {
            this.disable();
        }
    },

    isFolder: function(node)
    {
        return node && !node.leaf;
    }
});
