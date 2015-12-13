// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/TextBox.js
// dependency: zutubi/table/LinkTable.js
// dependency: zutubi/table/PropertyTable.js
// dependency: zutubi/pulse/SectionHeading.js
// dependency: zutubi/pulse/project/BuildSummaryTable.js
// dependency: zutubi/pulse/project/ResponsibilityBox.js
// dependency: zutubi/pulse/project/StatusBox.js

/**
 * The content of the project home page.
 */
Zutubi.pulse.project.browse.ProjectHomePanel = Ext.extend(Zutubi.ActivePanel, {
    layout: 'border',
    border: false,
    
    dataMapping: {
        responsibility: 'responsibility',
        status: 'status',
        activity: 'activity',
        recent: 'recent',
        changes: 'changes',
        description: 'description',
        actions: 'actions',
        links: 'links',
        artifacts: 'recent.0.stages'
    },
    
    initComponent: function(container, position)
    {
        var panel;

        panel = this;
        Ext.apply(this, {
            defaults: {
                layout: 'fit',
                border: false,
                autoScroll: true
            },
            contentEl: 'center',
            items: [{
                region: 'center',
                id: this.id + '-main',
                split: false,
                layout: 'vtable',
                items: [{
                    xtype: 'xzresponsibilitybox',
                    id: this.id + '-responsibility',
                    projectId: this.projectId,
                    style: 'margin: 0 17px'
                }, {
                    xtype: 'container',
                    layout: 'htable',
                    items: [{
                        xtype: 'xzstatusbox',
                        id: this.id + '-status',
                        titleTemplate: '{name:htmlEncode}',
                        fields: [
                            {name: 'health'},
                            {name: 'brokenStages', renderer: Zutubi.pulse.project.renderers.buildStages},
                            {name: 'state', renderer: Zutubi.pulse.project.renderers.projectState},
                            {name: 'successRate', renderer: Zutubi.pulse.project.renderers.projectSuccessRate},
                            {name: 'statistics', renderer: Zutubi.pulse.project.renderers.projectStatistics}
                        ]
                    }, {
                        xtype: 'box'
                    }, {
                        xtype: 'xzsummarytable',
                        id: this.id + '-activity',
                        title: 'current activity',
                        columns: [
                            Zutubi.pulse.project.configs.build.number,
                            Zutubi.pulse.project.configs.result.status,
                            Zutubi.pulse.project.configs.build.reason,
                            Zutubi.pulse.project.configs.build.revision
                        ],
                        emptyMessage: 'no current build activity'
                    }]
                }, {
                    xtype: 'xzsectionheading',
                    text: 'builds'
                }, {
                    xtype: 'xzbuildsummarytable',
                    id: this.id + '-recent',
                    cellCls: 'hpad',
                    title: 'recently completed builds',
                    selectedColumns: this.recentColumns,
                    emptyMessage: 'no historic builds found',
                    customisable: !this.anonymous
                }, {
                    xtype: 'xzsectionheading',
                    text: 'changes'
                }, {
                    xtype: 'xzsummarytable',
                    id: this.id + '-changes',
                    title: 'latest changes',
                    cellCls: 'hpad',
                    columns: [
                        Zutubi.pulse.project.configs.changelist.rev,
                        'who',
                        Zutubi.pulse.project.configs.changelist.when,
                        Zutubi.pulse.project.configs.changelist.comment,
                        Zutubi.pulse.project.configs.changelist.actions
                    ],
                    emptyMessage: 'no changes found',
                    listeners: {
                        afterrender: function() {
                            panel.updateRows();
                        }
                    }
                }]
            }, {
                region: 'east',
                id: this.id + '-right',
                bodyStyle: 'padding: 0 17px',
                split: true,
                collapsible: true,
                collapseMode: 'mini',
                hideCollapseTool: true,
                width: 300,
                layout: 'vtable',
                items: [{
                    xtype: 'xztextbox',
                    id: this.id + '-description',
                    title: 'description'
                }, {
                    xtype: 'xzlinktable',
                    id: this.id + '-actions',
                    idProperty: 'label',
                    title: 'actions',
                    handlers: {
                        clean: this.markForClean.createDelegate(this),
                        clearResponsibility: clearResponsibility.createDelegate(window, [this.projectId]),
                        takeResponsibility: takeResponsibility.createDelegate(window, [this.projectId]),
                        trigger: this.triggerBuild.createDelegate(this),
                        triggerWithPrompt: this.triggerBuildWithPrompt.createDelegate(this),
                        viewSource: Zutubi.fs.viewWorkingCopy.createDelegate(window, [this.projectId])
                    }
                }, {
                    xtype: 'xzlinktable',
                    id: this.id + '-links',
                    title: 'links',
                    iconTemplate: 'images/config/links/{icon}.gif'
                }, {
                    xtype: 'xzlinktable',
                    id: this.id + '-artifacts',
                    title: 'latest featured artifacts',
                    iconTemplate: 'images/artifacts/{icon}.gif',
                    categorised: true,
                    categoryPrefix: 'stage :: ',
                    linksProperty: 'featuredArtifacts',
                    idProperty: 'label',
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
        
    update: function(data)
    {
        var latest, title;

        Zutubi.pulse.project.browse.ProjectHomePanel.superclass.update.apply(this, arguments);

        this.checkPanelForContent('right', function(table) {
            return table.dataExists();
        });

        if (data.recent && data.recent.length > 0)
        {
            latest = data.recent[0];
            title = 'latest featured artifacts :: <a href="' + window.baseUrl + '/' + latest.link + '">build ' + latest.number + '</a>';
            Ext.getCmp(this.id + '-artifacts').setTitle(title);
        }
        
        if (this.rendered)
        {            
            this.updateRows();
        }
    },

    updateRows: function()
    {
        Ext.getCmp(this.id + '-main').getLayout().checkRows();
        Ext.getCmp(this.id + '-right').getLayout().checkRows();
    },

    markForClean: function()
    {
        var panel = this;
        window.dialogBox = Ext.Msg.show({
            title: 'Confirm',
            msg: 'Are you sure you want to delete all build directories for this project?',
            fn: function(btn, text) {
                    window.dialogBox = null;
                    if (btn === 'yes')
                    {
                        showStatus('Cleaning up build directories...', 'working');
                        Zutubi.core.ajax({
                            type: "POST",
                            url: '/api/action/single/clean/projects/' + encodeURIComponent(panel.data.status.name),
                            success: function(data)
                            {
                                showStatus(data.message, data.success ? 'success' : 'failure');
                                panel.load();
                            }
                        });
                    }
            },
            width: 400,
            buttons: Ext.Msg.YESNO
        });
    },

    triggerBuildWithPrompt: function(triggerName)
    {
        triggerBuild(this.data.status.name, triggerName, true, jQuery.proxy(this.load, this));
    },

    triggerBuild: function(triggerName)
    {
        triggerBuild(this.data.status.name, triggerName, false, jQuery.proxy(this.load, this));
    }
});
