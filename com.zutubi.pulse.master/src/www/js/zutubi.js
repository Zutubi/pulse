// dependency: ext/package.js
// dependency: ext/StatusBar.js
// dependency: widget/treegrid/package.js
// dependency: zutubi/FloatManager.js
// dependency: zutubi/MenuManager.js
// dependency: zutubi/form/package.js
// dependency: zutubi/fs/package.js
// dependency: zutubi/layout/package.js
// dependency: zutubi/table/package.js
// dependency: zutubi/toolbar/package.js
// dependency: zutubi/tree/package.js

function getBuildMenuItem(buildLink, itemId, image)
{
    return {
        id: itemId,
        url: buildLink + itemId + '/',
        image: image
    };
}

function getBuildMenuLinks(buildLink)
{
    return [
        getBuildMenuItem(buildLink, 'summary', 'information.gif'),
        getBuildMenuItem(buildLink, 'logs', 'script.gif'),
        getBuildMenuItem(buildLink, 'details', 'magnifier.gif', 'details'),
        getBuildMenuItem(buildLink, 'changes', 'page_code.gif'),
        getBuildMenuItem(buildLink, 'artifacts', 'folder_page.gif')
    ];
}

function getBuildMenuItems(buildModel)
{
    var items, onclick;
    items = getBuildMenuLinks(buildModel.link);
    if (buildModel.cancelPermitted)
    {
        items.push({
            id: 'cancel',
            image: 'cancel.gif',
            onclick: 'cancelBuild(' + buildModel.id + ', false); return false'
        });
    }

    return items;
}

function getCommentsTooltip(model)
{
    var commentsTip, i, recentComments, comment;
    commentsTip = '';
    if (model.comments && model.comments.commentCount > 0)
    {
        recentComments = model.comments.recentComments;
        for (i = 0; i < recentComments.length; i++)
        {
            comment = recentComments[i];
            if (commentsTip)
            {
                commentsTip += '<br/>';
            }

            commentsTip += '<b>' + Ext.util.Format.htmlEncode(comment.author) + '</b>: ' + Ext.util.Format.htmlEncode(comment.snippet);
        }

        if (recentComments.length < model.comments.commentCount)
        {
            commentsTip += '<br/><br/><em>' + recentComments.length + ' of ' + model.comments.commentCount + ' comments shown</em>';
        }
    }

    return commentsTip;
}

function agentAction(id, action)
{
    Zutubi.FloatManager.hideAll();

    runAjaxRequest({
        url: window.baseUrl + '/ajax/agentAction.action',
        params: {
            agentId: id,
            actionName: action
        },
        callback: handleActionResponse
    });
}

Zutubi.DetailPanel = function(config)
{
    Zutubi.DetailPanel.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.DetailPanel, Ext.Panel, {
    helpPath: "",
    helpType: "",

    initComponent: function()
    {
        Ext.apply(this, {
            layout: 'fit',
            id: 'detail-panel',
            contentEl: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'padding: 10px'
        });

        Zutubi.DetailPanel.superclass.initComponent.call(this);
    },

    clearHelp: function()
    {
        this.helpPath = '';
        this.helpType = '';
    },

    getHelp: function()
    {
        return {path: this.helpPath, type: this.helpType};
    },

    setHelp: function(path, type)
    {
        this.helpPath = path;
        this.helpType = type || '';
    },

    load: function(o)
    {
        this.clearHelp();
        this.body.load(o);
    },

    update: function(html)
    {
        this.clearHelp();
        this.body.update(html, true);
    }
});

Zutubi.HelpPanel = function(config)
{
    Zutubi.HelpPanel.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.HelpPanel, Ext.Panel, {
    shownPath: "",
    shownType: "",
    syncOnExpand: true,

    initComponent: function()
    {
        Ext.apply(this, {
            tbar:  [{
                icon: window.baseUrl + '/images/arrow_left_right.gif',
                cls: 'x-btn-icon',
                tooltip: 'synchronise help',
                onClick: this.synchronise.createDelegate(this)
            }, '-', {
                icon: window.baseUrl + '/images/expand.gif',
                cls: 'x-btn-icon',
                tooltip: 'expand all',
                onClick: this.expandAll.createDelegate(this)
            }, {
                icon: window.baseUrl + '/images/collapse.gif',
                cls: 'x-btn-icon',
                tooltip: 'collapse all',
                onClick: this.collapseAll.createDelegate(this)
            }, '->', {
                icon: window.baseUrl + '/images/close.gif',
                cls: 'x-btn-icon',
                tooltip: 'hide help',
                onClick: this.collapse.createDelegate(this)
            }]
        });

        Zutubi.HelpPanel.superclass.initComponent.call(this);

        this.on('expand', this.expanded.createDelegate(this));
    },

    expanded: function()
    {
        if (this.syncOnExpand)
        {
            this.syncOnExpand = false;
            this.synchronise();
        }
    },

    synchronise: function(field)
    {
        var location;

        location = detailPanel.getHelp();
        this.showHelp(location.path, location.type, field);
    },

    showHelp: function(path, type, field)
    {
        if(this.collapsed)
        {
            this.syncOnExpand = false;
            this.expand(false);
        }

        this.loadPath(path, type, this.gotoField.createDelegate(this, [field]));
    },

    loadPath: function(path, type, cb)
    {
        var panel;

        if(!path)
        {
            path = '';
        }

        if(!type)
        {
            type = '';
        }

        if(path !== this.shownPath || type !== this.shownType || type === 'wizard')
        {
            if(path)
            {
                panel = this;
                this.body.load({
                    url: window.baseUrl + '/ajax/help/' + encodeURIPath(path) + '?' + type + '=',
                    scripts: true,
                    callback: function() {
                        var helpEl, fieldHeaders;

                        panel.shownPath = path;
                        panel.shownType = type;
                        helpEl = Ext.get('config-help');
                        fieldHeaders = helpEl.select('.field-expandable .field-header', true);
                        fieldHeaders.on('click', function(e, el) {
                            var expandableEl;
                            
                            expandableEl = Ext.fly(el).parent('.field-expandable');
                            if(expandableEl)
                            {
                                expandableEl.toggleClass('field-expanded');
                            }
                        });

                        fieldHeaders.addClassOnOver('field-highlighted');

                        if(cb)
                        {
                            cb();
                        }
                    }
                });
            }
            else
            {
                this.body.update('No help available.', false, cb);
            }
        }
        else
        {
            if(cb)
            {
                cb();
            }
        }
    },

    gotoField: function(field)
    {
        var rowEl, top;
        
        if(field)
        {
            rowEl = Ext.get('field-row-' + field);
            if(rowEl)
            {
                if(rowEl.hasClass('field-expandable'))
                {
                    this.expandField(rowEl);
                }

                top = (rowEl.getOffsetsTo(this.body)[1]) + this.body.dom.scrollTop;
                this.body.scrollTo('top', top - 10);
                rowEl.highlight();
            }
        }
    },

    expandField: function(el)
    {
        el.addClass('field-expanded');
    },

    expandAll: function()
    {
        this.expandField(this.selectExpandableFields());
    },

    collapseField: function(el)
    {
        el.removeClass('field-expanded');
    },

    collapseAll: function()
    {
        this.collapseField(this.selectExpandableFields());
    },

    selectExpandableFields: function()
    {
        return this.body.select('.field-expandable');
    }
});

Ext.reg('xzhelppanel', Zutubi.HelpPanel);

Ext.form.Checkbox.prototype.onResize = function()
{
    Ext.form.Checkbox.superclass.onResize.apply(this, arguments);
};

Ext.form.ComboBox.prototype.isDirty = function()
{
    if (this.editable && !this.disabled)
    {
        return this.startValue !== this.getRawValue();
    }
    else
    {
        return Ext.form.ComboBox.superclass.isDirty.apply(this, arguments);
    }
};

Zutubi.TailSettingsWindow = function(config)
{
    Zutubi.TailSettingsWindow.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.TailSettingsWindow, Ext.Window, {
    modal: true,
    title: 'tail view settings',
    closeAction: 'close',

    initComponent: function()
    {
        var tailWindow;

        tailWindow = this;
        this.form = new Ext.FormPanel({
            method: 'POST',
            labelWidth: 180,
            width: 255,
            labelAlign: 'right',
            bodyStyle: 'padding: 10px; background: transparent;',
            border: false,
            items: [{
                xtype: 'hidden',
                name: 'pulse-session-token',
                value: window.sessionToken
            }, {
                xtype: 'textfield',
                name: 'maxLines',
                id: 'settings-max-lines',
                fieldLabel: 'maximum lines to show',
                value: tailWindow.initialMaxLines,
                width: 50
            }, {
                xtype: 'textfield',
                name: 'refreshInterval',
                id: 'settings-refresh-interval',
                fieldLabel: 'refresh interval (seconds)',
                value: tailWindow.initialRefreshInterval,
                width: 50
            }],
            buttons: [{
                text: 'apply',
                handler: function() {
                    tailWindow.apply();
                }
            }, {
                text: 'cancel',
                handler: function() {
                    tailWindow.close();
                }
            }],
            listeners: {
                afterLayout: {
                    fn: function() {
                        var nav;

                        nav = new Ext.KeyNav(this.getForm().getEl(), {
                            'enter': function() {
                                tailWindow.apply();
                            },
                            scope: this
                        });
                    },
                    single: true
                }
            }
        });

        Ext.apply(this, {
            layout: 'form',
            autoHeight: true,
            items: [this.form],
            focus: function() {
                this.form.items.get(0).focus(true);
            }
        });

        Zutubi.TailSettingsWindow.superclass.initComponent.call(this);
    },

    apply: function()
    {
        var tailWindow;

        tailWindow = this;
        this.form.getForm().submit({
            clientValidation: true,
            url: window.baseUrl + '/ajax/saveTailSettings.action',
            success: function()
            {
                var mask;

                tailWindow.close();
                mask = new Ext.LoadMask(Ext.getBody(), {msg:"Applying..."});
                mask.show();
                window.location.reload(true);
            },
            failure: function(form, action)
            {
                tailWindow.close();
                switch (action.failureType) {
                    case Ext.form.Action.CONNECT_FAILURE:
                        Ext.Msg.alert('Ajax communication failed.', 'failure');
                        break;
                    case Ext.form.Action.SERVER_INVALID:
                       Ext.Msg.alert('Server error.', 'failure');
                       break;
               }
            }
        });
    }
});

/**
 * Displays a content panel on a build page, with a heading and scrollable
 * content.
 */
Zutubi.ContentPanel = function(config)
{
    Zutubi.ContentPanel.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.ContentPanel, Ext.Panel,
{
    layout: 'fit',
    border: false,
    animate: false,
    autoScroll: true,
    id: 'content-panel',

    initComponent: function()
    {
        Zutubi.ContentPanel.superclass.initComponent.apply(this, arguments);
    }
});
Ext.reg('xzcontentpanel', Zutubi.ContentPanel);


if(Ext.ux.tree) { Zutubi.ArtifactsTree = Ext.extend(Ext.ux.tree.TreeGrid,
{
    MAX_COLUMN_WIDTH: 600,

    border: false,
    layout: 'fit',
    enableHdMenu: false,

    tooltips: {
        archive: 'download a zip archive of this artifact',
        decorate: 'go to a decorated view of this file',
        download: 'download this file',
        link: 'navigate to the external link',
        view: 'view this html artifact'
    },

    initComponent: function()
    {
        var tree, config;

        tree = this;
        config = {
            loader: new Zutubi.fs.FSTreeLoader({
                baseUrl: window.baseUrl,
                fs: 'pulse',
                basePath: 'projects/' + this.initialConfig.projectId + '/builds/' + this.initialConfig.buildId + '/artifacts',
                showFiles: true,
                preloadDepth: 3,
                filterFlag: this.initialConfig.filter
            }),

            selModel: new Ext.tree.DefaultSelectionModel({onNodeClick: Ext.emptyFn}),

            tbar: {
                id: 'build-toolbar',
                items: [{
                    xtype: 'label',
                    text: 'filter:'
                }, ' ', {
                    xtype: 'combo',
                    id: 'filter-combo',
                    width: 200,
                    mode: 'local',
                    triggerAction: 'all',
                    editable: false,
                    store: new Ext.data.ArrayStore({
                        idIndex: 0,
                        fields: [
                            'filter',
                            'text'
                        ],
                        data: [
                            ['', 'all artifacts'],
                            ['explicit', 'explicit artifacts only'],
                            ['featured', 'featured artifacts only']
                        ]
                    }),
                    valueField: 'filter',
                    displayField: 'text',
                    value: this.initialConfig.filter,
                    listeners: {
                        select: function(combo, record) {
                            tree.setFilterFlag(record.get('filter'));
                        }
                    }
                }, {
                    xtype: 'xztblink',
                    id: 'save-filter-link',
                    text: 'save',
                    icon: window.baseUrl + '/images/save.gif',
                    listeners: {
                        click: function() {
                            tree.saveFilterFlag(Ext.getCmp('filter-combo').getValue());
                        }
                    }
                }, {
                    xtype: 'tbtext',
                    text: '<span class="understated">//</span>'
                }, {
                    xtype: 'xztblink',
                    id: 'expand-all-link',
                    text: 'expand all',
                    icon: window.baseUrl + '/images/expand.gif',
                    listeners: {
                        click: function() {
                            tree.expandAll();
                        }
                    }
                }, {
                    xtype: 'xztblink',
                    id: 'collapse-all-link',
                    text: 'collapse all',
                    icon: window.baseUrl + '/images/collapse.gif',
                    listeners: {
                        click: function() {
                            tree.collapseAll();
                        }
                    }
                }]
            },

            columns: [{
                header: 'name',
                tpl: '{text}',
                width: 400,
                sortType: function(node) {
                    if (node.getDepth() > 2)
                    {
                        // Artifacts and below are sorted by name.
                        return node.attributes.text;
                    }
                    else
                    {
                        // Stages and commands are not sorted.
                        return 0;
                    }
                }
            }, {
                header: 'size',
                width: 100,
                dataIndex: 'size',
                tpl: '<tpl if="extraAttributes.size">{[Ext.util.Format.fileSize(values.extraAttributes.size)]}</tpl>',
                align: 'right',
                sortType: function(node) {
                    var extraAttributes;

                    extraAttributes = node.attributes.extraAttributes;
                    if (extraAttributes && extraAttributes.size)
                    {
                        return parseInt(extraAttributes.size, 10);
                    }
                    else
                    {
                        return 0;
                    }
                }
            }, {
                header: 'hash',
                cls: 'artifact-hash',
                width: 300,
                tpl: '<tpl if="extraAttributes.hash">{values.extraAttributes.hash}</tpl>',
                align: 'right',
                sortType: function(node) {
                    var extraAttributes;

                    extraAttributes = node.attributes.extraAttributes;
                    if (extraAttributes && extraAttributes.hash)
                    {
                        return extraAttributes.hash;
                    }
                    else
                    {
                        return 0;
                    }
                }
            }, {
                header: 'actions',
                width: 120,
                sortable: false,
                tpl: '<tpl if="extraAttributes.actions">' +
                         '<tpl for="extraAttributes.actions">' +
                             '&nbsp;<a href="' + window.baseUrl + '{url}">' +
                                 '<img alt="{type}" src="'+ window.baseUrl + '/images/artifacts/{type}.gif" ext:qtip="{[Zutubi.ArtifactsTree.prototype.tooltips[values.type]]}"/>' +
                             '</a>' +
                         '</tpl>' +
                     '</tpl>'
            }]
        };

        Ext.apply(this, config);
        Ext.apply(this.initialConfig, config);

        Zutubi.ArtifactsTree.superclass.initComponent.apply(this, arguments);

        this.loading = true;
        this.on('beforerender', this.setInitialColumnWidths, this, {single: true});
        this.on('expandnode', this.initialExpand, this, {single: true});
    },

    onRender: function()
    {
        Zutubi.ArtifactsTree.superclass.onRender.apply(this, arguments);
        this.loadingEl = this.innerBody.createChild({
            tag: 'div',
            style: 'padding: 5px',
            children: [{
                tag: 'img',
                alt: 'loading',
                src: window.baseUrl + '/images/default/tree/loading.gif'
            }, ' ', {
                tag: 'span',
                html: 'Loading...'
            }]
        });
    },
    
    setInitialColumnWidths: function()
    {
        var availableWidth, columns, firstWidth, remainingWidth, count, i, buffer, newWidth;

        // If there is more than enough width for our columns,
        // stretch the first one to fill.
        availableWidth = this.ownerCt.getSize().width;
        columns = this.columns;
        firstWidth = columns[0].width;
        remainingWidth = 0;
        count = columns.length;
        for (i = 1; i < count; i++)
        {
            remainingWidth += columns[i].width;
        }

        buffer = Ext.getScrollBarWidth() + 20;
        if (availableWidth > firstWidth + remainingWidth + buffer)
        {
            newWidth = availableWidth - remainingWidth - buffer;
            if (newWidth > this.MAX_COLUMN_WIDTH)
            {
                newWidth = this.MAX_COLUMN_WIDTH;
            }
            this.columns[0].width = newWidth;
        }
    },

    smallEnough: function(node)
    {
        var children;

        children = node.attributes.children;
        return children && children.length < 9;
    },

    initialExpand: function(node)
    {
        var depth, children, count, i, child;

        depth = node.getDepth();
        if (depth < 3)
        {
            children = node.childNodes;
            count = children.length;
            for (i = 0; i < count; i++)
            {
                child = children[i];
                if (this.selectedId !== 0 && this.selectedId === child.attributes.baseName)
                {
                    child.select();
                }
                
                if (depth < 2 || this.smallEnough(child))
                {
                    child.expand(false, false, this.initialExpand, this);
                }
            }

            if (depth === 0)
            {
                this.loading = false;
                this.loadingEl.remove();
            }
        }
    },

    setFilterFlag: function(flag)
    {
        this.loader.baseParams.filterFlag = flag;
        this.loading = true;
        this.getEl().mask('Reloading...');
        this.on('expandnode', this.initialExpand, this, {single: true});
        this.getRootNode().reload(function() {
            this.getOwnerTree().getEl().unmask();
        });
    },

    saveFilterFlag: function(flag)
    {
        runAjaxRequest({
           url: window.baseUrl + '/ajax/saveArtifactsFilter.action',
           success: function() { showStatus('Filter saved.','success'); },
           failure: function() { showStatus('Unable to save filter.','failure'); },
           params: { filter: flag }
        });
    }
}); }

/**
 * Displays all plugins in a tree, handling selection and actions performed on
 * them.
 *
 * @cfg detailPanel panel to load plugin details into
 */
Zutubi.PluginsTree = function(config)
{
    Zutubi.PluginsTree.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.PluginsTree, Zutubi.tree.ConfigTree,
{
    layout: 'fit',
    border: false,
    animate: false,
    autoScroll: true,
    bodyStyle: 'padding: 10px',

    initComponent: function()
    {
        var config;
        
        config = {
            loader: new Zutubi.fs.FSTreeLoader({
                baseUrl: window.baseUrl
            }),

            root: new Ext.tree.AsyncTreeNode({
                id: 'plugins',
                baseName: 'plugins',
                text: 'plugins',
                iconCls: 'plugins-icon'
            })
        };

        Ext.apply(this, config);
        Ext.apply(this.initialConfig, config);

        this.getSelectionModel().on('selectionchange', this.onPluginSelect);

        Zutubi.PluginsTree.superclass.initComponent.apply(this, arguments);
    },

    selectPlugin: function(id)
    {
        this.getSelectionModel().select(this.getRootNode().findChild('baseName', id));
    },

    pluginAction: function(id, action)
    {
        var model, selectedNode, selectedId, pluginsTree;

        model = this.getSelectionModel();
        selectedNode = model.getSelectedNode();
        selectedId = '';
        if (selectedNode && selectedNode.parentNode)
        {
            selectedId = selectedNode.attributes.baseName;
        }

        pluginsTree = this;
        runAjaxRequest({
            url: window.baseUrl + '/ajax/admin/' + action + 'Plugin.action?id=' + id,

            success: function()
            {
                pluginsTree.getRootNode().reload(function() {
                    model.clearSelections();
                    if (selectedId)
                    {
                        pluginsTree.selectPlugin(selectedId);
                    }
                    else
                    {
                        model.select(pluginsTree.getRootNode());
                    }
                });
            },

            failure: function()
            {
                showStatus('Unable to perform plugin action.', 'failure');
            }
        });
    },

    onPluginSelect: function(model, node)
    {
        var url;
        
        if (model.tree.detailPanel && node)
        {
            if(node.parentNode)
            {
                url = window.baseUrl + '/ajax/admin/viewPlugin.action?id=' + node.attributes.baseName;
            }
            else
            {
                url = window.baseUrl + '/ajax/admin/allPlugins.action';
            }

            model.tree.detailPanel.load({
                url: url,
                scripts: true,
                callback: function(element, success) {
                    if(!success)
                    {
                        showStatus('Could not get plugin details.', 'failure');
                    }
                }
            });
        }
    }
});

/**
 * Generates the pulse header bar, which includes the bread crumbs, user details
 * and more.
 *
 * @cfg buildId         the id of current build, used for rendering the breadcrumbs
 * @cfg projectName     the name of the current project, if available, used for rendering the breadcrumbs
 * @cfg projectUrl      the url to the current projects home page, if available, used for rendering
 *                      the breadcrumbs.
 * @cfg agentName       the name of the current agent, if available, used for rendering the breadcrumbs.
 * @cfg agentUrl        the url to the current agents home page, if available, used for rendering
 *                      the breadcrumbs.
 * @cfg userName        the name of the logged in user, if any.
 * @cfg canUserLogout   indicates whether or not to render the logout link,
 *                      requires that a userName be specified to render.
 *
 * Note that the project and agent portions of the breadcrumbs are mutually exclusive, with
 * the project taking precedence.
 */
Zutubi.PulseHeader = Ext.extend(Ext.Toolbar, {

    id: 'pulse-toolbar',
    
    initComponent: function()
    {
        Zutubi.PulseHeader.superclass.initComponent.apply(this, arguments);

        this.builds = new Ext.util.MixedCollection();
        if (this.data)
        {
            this.builds.addAll(this.data.builds);
        }
    },

    afterRender: function()
    {
        Zutubi.PulseHeader.superclass.afterRender.apply(this, arguments);

        // Remove the x-toolbar class to avoid clashing with the default
        // toolbar styling.
        this.getEl().removeClass('x-toolbar');
    },

    onRender: function()
    {
        var currentItems, menuConfig;

        Zutubi.PulseHeader.superclass.onRender.apply(this, arguments);

        // clear the existing items.
        currentItems = (this.items) ? this.items.clone() : new Ext.util.MixedCollection();
        currentItems.each(function(item) {
            this.items.remove(item);
            item.destroy();
        }, this);

        this.addItem({xtype: 'tbtext', html: '&nbsp;::&nbsp;', tag: 'span'});
        this.addItem({xtype: 'xztblink', text:"pulse 2.7", url: window.baseUrl + '/default.action'});
        this.addItem({xtype: 'tbtext', html: '&nbsp;::&nbsp;', tag: 'span'});

        if (this.projectName)
        {
            this.addItem({id: 'pulse-toolbar-project-link', xtype: 'xztblink', text: this.projectName, url: this.projectUrl});
            this.addItem({xtype: 'tbtext', html: '&nbsp;::&nbsp;', tag: 'span'});
        }
        else if (this.agentName)
        {
            this.addItem({id: 'pulse-toolbar-agent-link', xtype: 'xztblink', text: this.agentName, url: this.agentUrl});
            this.addItem({xtype: 'tbtext', html: '&nbsp;::&nbsp;', tag: 'span'});
        }

        if (this.buildId)
        {
            this.builds.each(function(build) {
                var selected, url, tooltip;

                selected = build.id === this.buildId;
                if (selected)
                {
                    url = null;
                    if (this.personalBuild)
                    {
                        url = window.baseUrl + '/dashboard/my/' + build.number;
                    }
                    else
                    {
                        url = window.baseUrl + '/browse/projects/' + encodeURIComponent(build.name) + '/builds/' + build.number;
                    }
                    this.addItem({
                        id: 'pulse-toolbar-build-link',
                        xtype: 'xztblink',
                        text: 'build ' + build.number,
                        url: url
                    });
                }
                else
                {
                    if (build.id < this.buildId)
                    {
                        tooltip = 'step back to build ' + build.number;
                    }
                    else
                    {
                        tooltip = 'step forward to build ' + build.number;
                    }
                    this.addItem(new Zutubi.BuildNavToolbarItem({
                        id: 'pulse-toolbar-build-item-' + build.number,
                        build: build,
                        tooltip: tooltip,
                        selectedTab: this.selectedTab,
                        personalBuild: this.personalBuild
                    }));
                }
            }, this);

            if (this.hasMenuItems())
            {
                menuConfig = {};
                Ext.apply(menuConfig, this.data);
                Ext.apply(menuConfig, {
                    id: this.id,
                    personalBuild : this.personalBuild,
                    selectedTab: this.selectedTab,
                    imgcls: 'popdown-small'
                });

                this.addItem(new Zutubi.BuildNavToolbarMenu(menuConfig));
            }
            this.addItem({xtype: 'tbtext', html: '&nbsp;::&nbsp;', tag: 'span'});
        }

        if (this.stageName)
        {
            this.addItem({id: 'pulse-toolbar-stage-link', xtype: 'xztblink', text: 'stage ' + this.stageName, url: this.stageUrl});
            this.addItem({xtype: 'tbtext', html: '&nbsp;::&nbsp;', tag: 'span'});
        }

        this.addFill();

        if (this.userName)
        {
            this.addItem({xtype: 'tbtext', html: this.userName + ' [', tag: 'span', cls: 'userToolbar'});
            this.addItem({xtype: 'xztblink', id: 'prefs', text: 'preferences', url: window.baseUrl + '/dashboard/preferences/', cls: 'userToolbar'});
            if (this.userCanLogout)
            {
                this.addItem({xtype: 'tbtext', html: '|', tag: 'span', cls: 'userToolbar'});
                this.addItem({xtype: 'xztblink', id: 'logout', text: "logout", url: window.baseUrl + '/j_spring_security_logout', cls: 'userToolbar'});
            }
            this.addItem({xtype: 'tbtext', html: ']', tag: 'span', cls: 'userToolbar'});
        }
        else
        {
            this.addItem({xtype: 'xztblink', id: 'login', text: 'login', url:window.baseUrl + '/login!input.action', cls: 'user'});
        }

        this.addItem({xtype: 'xztblink',
            icon: window.baseUrl + "/images/manual.gif",
            cls: 'unadorned',
            listeners: {
                click: function()
                {
                    var popup = window.open(window.baseUrl + '/popups/reference.action', '_pulsereference', 'status=yes,resizable=yes,top=100,left=100,width=900,height=600,scrollbars=yes');
                    popup.focus();
                }
            }
        });
    },

    hasMenuItems: function()
    {
        return this.data && (this.data.nextSuccessful || this.data.nextBroken || this.data.previousSuccessful || this.data.previousBroken);
    }
});


Zutubi.BuildNavToolbarItem = Ext.extend(Ext.Toolbar.Item, {

    cls: 'x-build-nav-item',

    initComponent: function()
    {
        Zutubi.BuildNavToolbarItem.superclass.initComponent.apply(this, arguments);

        this.addClass('x-build-nav-item-' + this.build.status);
        this.autoEl = {
            tag: 'div'
        };
        this.autoEl.children = ['&nbsp;'];
    },

    afterRender: function()
    {
        Zutubi.BuildNavToolbarItem.superclass.afterRender.apply(this, arguments);

        Ext.QuickTips.register({
            target: this.getEl(),
            text: this.tooltip
        });

        this.mon(this.getEl(), {
           "click": this.onClick.createDelegate(this)
        });
    },

    onClick: function()
    {
        if (this.personalBuild)
        {
            window.location.href = window.baseUrl + '/dashboard/my/' + this.build.number + '/' + this.selectedTab;
        }
        else
        {
            window.location.href = window.baseUrl + '/browse/projects/' + encodeURIComponent(this.build.name) + '/builds/' + this.build.number + '/' + this.selectedTab;
        }
    }
});

Zutubi.BuildNavToolbarMenu = Ext.extend(Ext.Toolbar.Item, {
    cls: 'popdown',
    imgcls: 'popdown',

    initComponent: function()
    {
        Zutubi.BuildNavToolbarMenu.superclass.initComponent.apply(this, arguments);

        this.autoEl = {
            id: this.id + "-actions-link",
            tag: 'a',
            cls: 'unadorned',
            style: 'position:relative; top:2px;line-height:11px;font-size:11px',
            onclick: 'Zutubi.MenuManager.toggleMenu(this); return false',
            children: [{
                tag: 'img',
                cls: this.imgcls + ' floating-widget',
                id: this.id + '-actions-button',
                src: window.baseUrl + '/images/default/s.gif'
            }]
        };
    },

    afterRender: function()
    {
        Zutubi.BuildNavToolbarMenu.superclass.afterRender.apply(this, arguments);
        Zutubi.MenuManager.registerMenu(this.id + '-actions', this.getMenuItems.createDelegate(this), this.imgcls);
    },

    getMenuItems: function()
    {
        var items = [];

        if (this.nextSuccessful)
        {
            items.push({
                id: 'next-successful',
                image: this.getImage(this.nextSuccessful),
                title: 'next healthy (build ' + this.nextSuccessful.number + ')',
                url: this.getUrl(this.personalBuild, this.nextSuccessful)
            });
        }
        if (this.nextBroken)
        {
            items.push({
                id: 'next-broken',
                image: this.getImage(this.nextBroken),
                title: 'next broken (build ' + this.nextBroken.number + ')',
                url: this.getUrl(this.personalBuild, this.nextBroken)
            });
        }
        if (this.previousSuccessful)
        {
            items.push({
                id: 'previous-successful',
                image: this.getImage(this.previousSuccessful),
                title: 'previous healthy (build ' + this.previousSuccessful.number + ')',
                url: this.getUrl(this.personalBuild, this.previousSuccessful)
            });
        }
        if (this.previousBroken)
        {
            items.push({
                id: 'previous-broken',
                image: this.getImage(this.previousBroken),
                title: 'previous broken (build ' + this.previousBroken.number + ')',
                url: this.getUrl(this.personalBuild, this.previousBroken)
            });
        }

        if (this.latest)
        {
            items.push({
                id: 'latest',
                image: this.getImage(this.latest),
                title: 'latest (build ' + this.latest.number + ')',
                url: this.getUrl(this.personalBuild, this.latest)
            });
        }

        return items;
    },

    getImage: function(build)
    {
        if (build.status === 'success')
        {
            return 'health/ok.gif';    
        }
        else if (build.status === 'warnings')
        {
            return 'health/warnings.gif';
        }
        else if (build.status === 'failure' || build.status === 'error' || build.status === 'cancelled' || build.status === 'terminated')
        {
            return 'health/broken.gif';
        }
        else
        {
            return 'health/unknown.gif';
        }
    },

    getUrl: function(isPersonalBuild, build)
    {
        if (isPersonalBuild)
        {
            return 'dashboard/my/' + build.number + '/' + this.selectedTab;
        }
        else
        {
            return 'browse/projects/' + encodeURIComponent(build.name) + '/builds/' + build.number + '/' + this.selectedTab;
        }
    }
});

Ext.util.Format.plainToHtml = function(s)
{
    return Ext.util.Format.htmlEncode(s).replace(/(\s) /g, '$1&nbsp;').replace(/\r?\n/g, '<br/>');
};
