// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/table/LinkTable.js
// dependency: zutubi/table/PropertyTable.js
// dependency: zutubi/table/SummaryTable.js
// dependency: zutubi/pulse/project/ResponsibilityBox.js
// dependency: zutubi/pulse/project/CommentList.js
// dependency: zutubi/pulse/project/FeatureList.js
// dependency: zutubi/pulse/project/StatusBox.js
// dependency: zutubi/pulse/project/TestFailuresTable.js

/**
 * The content of the build summary tab.
 *
 * @cfg {String} id          Id for this component.
 * @cfg {String} projectId   Id of the project the build belongs to.
 * @cfg {String} projectName Name of the project the build belongs to.
 * @cfg {String} buildsUrl   URL for builds for the project.
 * @cfg {String} buildId     Id of the build to display.
 * @cfg {String} buildNumber Number of the build to display.
 * @cfg {String} personal    True if the displayed build is personal, false otherwise.
 */
Zutubi.pulse.project.browse.BuildSummaryPanel = Ext.extend(Zutubi.ActivePanel, {
    layout: 'border',
    border: false,
    personal: false,
    
    dataMapping: {
        responsibility :'responsibility',
        status: 'build',
        details: 'build',
        comments: 'comments',
        stages: 'build.stages',
        errors: 'errors',
        warnings: 'warnings',
        failures: 'failures',
        actions: 'actions',
        links: 'links',
        artifacts: 'build.stages',
        hooks: 'hooks'
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
                        titleTemplate: 'build {number} <tpl if="pinned">[pinned]</tpl>',
                        fields: [
                            Zutubi.pulse.project.configs.result.status,
                            Zutubi.pulse.project.configs.build.tests,
                            Zutubi.pulse.project.configs.result.errors,
                            Zutubi.pulse.project.configs.result.warnings
                        ]
                    }, {
                        xtype: 'box'
                    }, {
                        xtype: 'xzpropertytable',
                        id: this.id + '-details',
                        title: 'build details',
                        rows: [
                            Zutubi.pulse.project.configs.build.reason,
                            Zutubi.pulse.project.configs.build.revision,
                            Zutubi.pulse.project.configs.build.maturity,
                            Zutubi.pulse.project.configs.build.version,
                            Zutubi.pulse.project.configs.result.when,
                            Zutubi.pulse.project.configs.result.completed,
                            Zutubi.pulse.project.configs.result.elapsedStatic
                        ]
                    }]
                }, {
                    xtype: 'xzcommentlist',
                    id: this.id + '-comments',
                    title: 'comments',
                    buildId: this.buildId,
                    cellCls: 'hpad'
                }, {
                    xtype: 'xzsummarytable',
                    id: this.id + '-stages',
                    title: 'build stages',
                    cellCls: 'hpad',
                    columns: [
                        Zutubi.pulse.project.configs.stage.name,
                        Zutubi.pulse.project.configs.stage.recipe,
                        Zutubi.pulse.project.configs.stage.agent,
                        Zutubi.pulse.project.configs.result.status,
                        Zutubi.pulse.project.configs.result.errors,
                        Zutubi.pulse.project.configs.result.warnings,
                        Zutubi.pulse.project.configs.stage.tests,
                        Zutubi.pulse.project.configs.result.when,
                        Zutubi.pulse.project.configs.result.elapsed,
                        Zutubi.pulse.project.configs.stage.logs
                    ]
                }, {
                    xtype: 'xzfeaturelist',
                    id: this.id + '-errors',
                    level: 'error',
                    cellCls: 'hpad'
                }, {
                    xtype: 'xzfeaturelist',
                    id: this.id + '-warnings',
                    level: 'warning',
                    cellCls: 'hpad'
                }, {
                    xtype: 'xztestfailurestable',
                    id: this.id + '-failures',
                    title: 'test failures',
                    buildsUrl: this.buildsUrl,
                    cellCls: 'hpad',
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
                    xtype: 'xzlinktable',
                    id: this.id + '-actions',
                    title: 'actions',
                    handlers: {
                        cancel: cancelBuild.createDelegate(window, [this.buildId, false]),
                        kill: cancelBuild.createDelegate(window, [this.buildId, true]),
                        'delete': this.deleteBuild.createDelegate(this),
                        pin: this.togglePin.createDelegate(this, [true]),
                        unpin: this.togglePin.createDelegate(this, [false]),
                        clearResponsibility: clearResponsibility.createDelegate(window, [this.projectId]),
                        takeResponsibility: takeResponsibility.createDelegate(window, [this.projectId]),
                        addComment:this.addComment.createDelegate(this)
                    }
                }, {
                    xtype: 'xzlinktable',
                    id: this.id + '-links',
                    title: 'links',
                    iconTemplate: 'images/link_go.gif',
                    idProperty: 'label'
                }, {
                    xtype: 'xzlinktable',
                    id: this.id + '-artifacts',
                    title: 'featured artifacts',
                    iconTemplate: 'images/artifacts/{icon}.gif',
                    categorised: true,
                    categoryPrefix: 'stage :: ',
                    linksProperty: 'featuredArtifacts',
                    idProperty: 'label'
                }, {
                    xtype: 'xzlinktable',
                    id: this.id + '-hooks',
                    title: 'build hooks',
                    iconTemplate: 'images/lightning.gif',
                    idProperty: 'label',
                    handlers: {
                        'triggerHook': this.triggerHook.createDelegate(this)
                    },
                    listeners: {
                        afterrender: function() {
                            panel.updateRows();
                        }
                    }
                }]
            }]
        });
        
        Zutubi.pulse.project.browse.BuildSummaryPanel.superclass.initComponent.apply(this, arguments);
    },
        
    update: function(data)
    {
        Zutubi.pulse.project.browse.BuildSummaryPanel.superclass.update.apply(this, arguments);

        this.checkPanelForContent('right', function(table) {
            return table.dataExists();
        });
        
        if (this.rendered)
        {
            this.updateRows();
            this.el.unmask();
        }
    },

    updateRows: function()
    {
        Ext.getCmp(this.id + '-main').getLayout().checkRows();
        Ext.getCmp(this.id + '-right').getLayout().checkRows();
    },

    deleteBuild: function()
    {
        var newLocation,
            params;

        params = {
            buildVID: this.buildNumber
        };

        if (this.personal)
        {
            newLocation = '/dashboard/my/';
            params.personal = true;
        }
        else
        {
            newLocation = '/browse/projects/' + encodeURIComponent(this.projectName) + '/';
            params.projectName = this.projectName;
        }

        if (confirm('Are you sure you wish to delete this build?'))
        {
            runAjaxRequest({
               url: window.baseUrl + '/ajax/deleteBuild.action',
               params: params,
               callback: function() {
                   window.location = window.baseUrl + newLocation;
               }
            });
        }
    },

    togglePin: function(pin)
    {
        this.getEl().mask((pin ? 'Pinning' : 'Unpinning') + ' build...', 'working');

        runAjaxRequest({
            url: window.baseUrl + '/ajax/togglePin.action',
            params: {
                buildId: this.buildId,
                pin: pin
            },
            callback: handleDialogResponse
        });
    },

    addComment: function()
    {
        showPromptDialog('Add Comment',
                         'Comment:',
                         true,
                         true,
                         'Adding comment...',
                         '/ajax/addComment.action',
                         { buildId: this.buildId });
    },
    
    triggerHook: function(hookHandle)
    {
        var params;

        hideStatus(false);
        
        params = {
            buildVID: this.buildNumber,
            hook: hookHandle
        };
        
        if (this.personal)
        {
            params.personal = true;
        }
        else
        {
             params.projectName = this.projectName;
        }

        runAjaxRequest({
            url: window.baseUrl + '/ajax/triggerBuildHook.action',
            params: params,
            success: function(transport, options)
            {
                var response;

                response = eval("(" + transport.responseText + ")");
                if(response.success)
                {
                    showStatus(response.detail, 'success');
                }
                else
                {
                    showStatus('Unable to trigger hook: ' + response.detail, 'failure');
                }
            },

            failure: function(transport, options)
            {
                showStatus.update('Hook trigger failed.', 'failure');
            }
        });
    }
});
