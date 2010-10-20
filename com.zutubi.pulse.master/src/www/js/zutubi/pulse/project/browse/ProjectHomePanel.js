// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/LinkTable.js
// dependency: zutubi/table/PropertyTable.js
// dependency: zutubi/pulse/SectionHeading.js
// dependency: zutubi/pulse/project/ResponsibilityBox.js
// dependency: zutubi/pulse/project/StatusBox.js

/**
 * The content of the project home page.
 */
Zutubi.pulse.project.browse.ProjectHomePanel = Ext.extend(Ext.Panel, {
    layout: 'border',
    border: false,
    
    dataKeys: ['responsibility', 'status', 'activity', 'latest', 'recent', 'changes', 'actions', 'links'],
    
    initComponent: function(container, position) {
        var panel = this;
        Ext.apply(this, {
            defaults: {
                layout: 'fit',
                border: false,
                autoScroll: true
            },
            contentEl: 'center',
            items: [{
                region: 'center',
                id: 'project-main',
                split: false,
                contentEl: this.loadingEl,
                layout: 'vtable',
                items: [{
                    xtype: 'xzresponsibilitybox',
                    id: 'project-responsibility',
                    data: this.data.responsibility,
                    projectId: this.projectId,
                    style: 'margin: 0 17px'
                }, {
                    xtype: 'container',
                    layout: 'htable',
                    items: [{
                        xtype: 'xzstatusbox',
                        id: 'project-status',
                        titleTemplate: '{name:htmlEncode}',
                        fields: [
                            {name: 'health'},
                            {name: 'state', renderer: function(state) { return state + ' [<a href="#">pause</a>]'; }},
                            {name: 'successRate', key: 'success rate'},
                            {name: 'statistics', renderer: function() { return '<img class="centre" title="7 (88%) successful" src="/images/box-success.gif" height="10" width="88"><img class="centre" title="1 (13%) failed" src="/images/box-failure.gif" height="10" width="13"><img class="centre" title="0 (0%) errors" src="/images/box-error.gif" height="10" width="0"><br/>8 builds (s: 7, f: 1, e: 0)'; }}
                        ],
                        data: this.data.status
                    }, {
                        xtype: 'box',
                    },
                    {
                        xtype: 'xzsummarytable',
                        id: 'project-activity',
                        title: 'current activity',
                        columns: [
                            'id',
                            {name: 'status', renderer: Zutubi.pulse.project.cells.status},
                            'reason',
                            'revision'
                        ],
                        data: this.data.activity,
                        emptyMessage: 'no current build activity'
                    }]
                }, {
                    xtype: 'xzsectionheading',
                    text: 'builds'
                }, {
                    xtype: 'container',
                    layout: 'htable',
                    items: [{
                        xtype: 'xzpropertytable',
                        id: 'project-latest',
                        title: 'latest completed build',
                        rows: [
                            'id',
                            {name: 'status', renderer: Zutubi.pulse.project.cells.status}
                        ],
                        data: this.data.latest,
                        emptyMessage: 'no completed builds found'
                    }, {
                        xtype: 'box',
                    }, {
                        xtype: 'xzsummarytable',
                        id: 'project-recent',
                        title: 'recently completed builds',
                        columns: [
                            'id',
                            {name: 'status', renderer: Zutubi.pulse.project.cells.status},
                            'reason',
                            'revision'
                        ],
                        data: this.data.recent,
                        emptyMessage: 'no historic builds found'
                    }]
                }, {
                    xtype: 'xzsectionheading',
                    text: 'changes'
                }, {
                    xtype: 'xzsummarytable',
                    id: 'project-changes',
                    title: 'latest changes',
                    cellCls: 'hpad',
                    columns: [
                        'revision',
                        'who',
                        'when',
                        'comment'
                    ],
                    data: this.data.changes,
                    emptyMessage: 'no changes found'
                }]
            }, {
                region: 'east',
                id: 'project-right',
                bodyStyle: 'padding: 0 17px',
                split: true,
                collapsible: true,
                collapseMode: 'mini',
                hideCollapseTool: true,
                width: 300,
                layout: 'vtable',
                items: [{
                    xtype: 'xzlinktable',
                    id: 'project-actions',
                    data: this.data.actions,
                    title: 'actions'
                }, {
                    xtype: 'xzlinktable',
                    id: 'project-links',
                    title: 'links',
                    iconTemplate: 'images/config/links/{icon}.gif',
                    data: this.data.links,
                    listeners: {
                        afterrender: function() {
                            panel.updateRows();
                        }
                    }
                }]
            }]
        });
        
        Zutubi.pulse.project.browse.ProjectHomePanel.superclass.initComponent.apply(this, arguments);
    },
        
    update: function(data) {
        this.data = data;
        for (var i = 0, l = this.dataKeys.length; i < l; i++)
        {
            var key = this.dataKeys[i];
            Ext.getCmp('project-' + key).update(data[key]);    
        }
        
        this.updateRows();
    },

    updateRows: function() {
        Ext.getCmp('project-main').getLayout().checkRows();
        Ext.getCmp('project-right').getLayout().checkRows();
    }
});
