// dependency: ext/package.js
// dependency: ext/StatusBar.js
// dependency: widget/treegrid/package.js
// dependency: zutubi/FloatManager.js
// dependency: zutubi/MenuManager.js
// dependency: zutubi/fs/package.js
// dependency: zutubi/layout/package.js
// dependency: zutubi/table/package.js
// dependency: zutubi/toolbar/package.js

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
    var items;
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
