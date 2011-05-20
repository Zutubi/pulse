// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/pulse/Graph.js

/**
 * The content of the project reports page.
 */
Zutubi.pulse.project.browse.ProjectReportsPanel = Ext.extend(Ext.Panel, {
    layout: 'fit',
    border: false,
    loaded: false,
    
    initComponent: function(container, position)
    {
        var panel = this;
        Ext.apply(this, {
            defaults: {
                layout: 'fit',
                border: false,
                autoScroll: true
            },
            style: 'padding: 17px',
            contentEl: 'center',
            tbar: {
                id: 'build-toolbar',
                style: 'margin-top: 0',
                items: [{
                    xtype: 'label',
                    text: 'report group:'
                }, ' ', {
                    xtype: 'combo',
                    id: 'report-group',
                    width: 200,
                    mode: 'local',
                    triggerAction: 'all',
                    editable: false,
                    store: this.data.groupNames,
                    value: this.data.group
                }, ' ', ' ', ' ', {
                    xtype: 'label',
                    text: 'time frame:'
                }, ' ', {
                    xtype: 'combo',
                    id: 'report-time-frame',
                    editable: true,
                    triggerAction: 'all',
                    store: ['15', '30', '45', '90'],
                    value: this.data.timeFrame,
                    width: 100
                }, ' ', {
                    xtype: 'combo',
                    id: 'report-time-unit',
                    editable: false,
                    forceSelection: true,
                    triggerAction: 'all',
                    store: ['builds', 'days'],
                    value: this.data.timeUnit,
                    width: 100
                }, ' ', {
                    xtype: 'xztblink',
                    id: 'reports-apply',
                    icon: window.baseUrl + '/images/arrow_refresh.gif',
                    text: 'apply',
                    listeners: {
                        click: function() {
                            panel.data.group = Ext.getCmp('report-group').getValue();
                            panel.data.timeFrame = Ext.getCmp('report-time-frame').getValue();
                            panel.data.timeUnit = Ext.getCmp('report-time-unit').getValue();
                            panel.load();
                        }
                    }
                }]
            }
        });

        Zutubi.pulse.project.browse.ProjectReportsPanel.superclass.initComponent.apply(this, arguments);
    },

    renderReports: function()
    {
        if (!this.data.group || !this.data.groupNames || this.data.groupNames.length == 0)
        {
            this.showMessage('No report groups defined.');
        }
        else if (this.data.buildCount == 0)
        {
            this.showMessage('No builds found in time frame.');
        }
        else
        {
            var items = [];
            for (var i = 0, l = this.data.reports.length; i < l; i++)
            {
                items.push({
                    xtype: 'xzgraph',
                    data: this.data.reports[i]
                });
            }

            this.add({
                id: 'reports-main',
                layout: 'table',
                border: true,
                bodyStyle: {
                    'border-color': '#bbb'
                },
                layoutConfig: {
                    columns: 2,
                    tableAttrs: {
                        cls: 'chart'
                    }
                },
                items: items
            });

            this.doLayout();
        }
    },

    clear: function()
    {
        this.removeAll();
    },

    showMessage: function(html)
    {
        this.add({xtype: 'box', autoEl: {tag: 'p', html: html}});
        this.doLayout();
    },

    update: function(data)
    {
        this.data = data;
        this.renderReports();
    },

    load: function()
    {
        this.loaded = false;
        this.clear();
        this.showMessage('<img alt="loading" src="' + window.baseUrl + '/images/inprogress.gif"/> Loading reports...');
        
        Ext.Ajax.request({
            url: window.baseUrl + '/ajax/projectReportsData.action',
            params: {
                projectId: this.projectId,
                group: this.data.group,
                timeFrame: this.data.timeFrame,
                timeUnit: this.data.timeUnit
            },
            
            scope: this,

            success: function(transport) {
                this.clear();
                this.update(eval('(' + transport.responseText + ')'));
                this.loaded = true;
            },

            failure: function() {
                this.clear();
                this.showMessage('Unable to load report data.');
                this.loaded = true;
            }
        });
    }
});
