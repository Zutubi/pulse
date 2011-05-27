// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./AgentSummaryTable.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/pulse/InvalidList.js
// dependency: zutubi/table/package.js

/**
 * The content of the main page for the agents section.  Expects data of the form:
 *
 * {
 *     agents: [ AgentRowModel ],
 *     invalidAgents: [ String ]
 * }
 */
Zutubi.pulse.agent.AgentsPanel = Ext.extend(Zutubi.ActivePanel, {
    layout: 'border',
    border: false,
    
    dataKeys: ['invalidAgents', 'agents'],
    
    initComponent: function(container, position)
    {
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
                bodyStyle: 'padding: 0 17px',
                id: this.id + '-main',
                split: false,
                layout: 'vtable',
                items: [{
                    xtype: 'xzinvalidlist',
                    id: this.id + '-invalidAgents',
                    blurb: 'The following agents are invalid, and cannot be used for building until rectified:',
                    scopeUrl: window.baseUrl + '/admin/agents/'
                }, {
                    xtype: 'xzagentsummarytable',
                    id: this.id + '-agents',
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
                width: 225,
                layout: 'vtable',
                items: [{
                    xtype: 'xzlinktable',
                    id: this.id + '-packages',
                    title: 'agent downloads',
                    iconTemplate: 'images/{icon}.gif',
                    data: [{
                        icon: 'compress',
                        label: 'zip archive',
                        action: window.baseUrl + '/packages/pulse-agent-' + this.version + '.zip'
                    }, {
                        icon: 'compress',
                        label: 'tar archive',
                        action: window.baseUrl + '/packages/pulse-agent-' + this.version + '.tar.gz'
                    }, {
                        icon: 'pulse',
                        label: 'windows installer',
                        action: window.baseUrl + '/packages/pulse-agent-' + this.version + '.exe'
                    }],
                    listeners: {
                        afterrender: function() {
                            panel.updateRows();
                        }
                    }                    
                }]
            }]
        });
        
        Zutubi.pulse.agent.AgentsPanel.superclass.initComponent.apply(this, arguments);
    },
    
    update: function(data)
    {
        Zutubi.pulse.agent.AgentsPanel.superclass.update.apply(this, arguments);
        
        if (this.rendered)
        {
            this.updateRows();
        }
    },
    
    updateRows: function()
    {
        Ext.getCmp(this.id + '-main').getLayout().checkRows();
    }
});
