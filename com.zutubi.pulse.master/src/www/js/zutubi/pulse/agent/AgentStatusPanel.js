// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ExecutingStageTable.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/table/package.js
// dependency: zutubi/pulse/project/namespace.js

/**
 * The content of the agent status tab.  Expects data of the form:
 *
 * {
 *     status: object,
 *     executingStage: ExecutingStageModel,
 *     synchronisationMessages: [ SynchronisationMessageModel ]
 * }
 */
Zutubi.pulse.agent.AgentStatusPanel = Ext.extend(Zutubi.ActivePanel, {
    border: false,
    autoScroll: true,
    messageIndex: 0,
    
    STATUS_TEMPLATE: new Ext.XTemplate(
        '<img alt="{status}" src="{baseUrl}/images/synch/{squashedStatus}.gif"/> {status} ' +
        '<tpl if="statusMessage">' +
            '<a class="unadorned" id="synch-{id}-link" onclick="Zutubi.FloatManager.showHideFloat(\'synch\', \'synch-{id}\'); return false">' +
                '<img src="{baseUrl}/images/default/s.gif" class="popdown floating-widget" id="synch-{id}-button" alt="full status"/>' +
            '</a>' +
            '<div id="synch-{id}" style="display: none">' +
                '<table class="content" style="margin: 0">' +
                    '<tr>' +
                        '<th class="heading">' +
                            '<span class="action">' +
                                '<a href="#" onclick="Zutubi.FloatManager.showHideFloat(\'synch\', \'synch-{id}\'); return false;"><img alt="close" src="{baseUrl}/images/delete.gif"/>close</a>' +
                            '</span>' +
                            'status message' +
                        '</th>' +
                    '</tr>' +
                    '<tr>' +
                        '<td><pre id="sync-{id}">{statusMessage:htmlEncode}</pre></td>' +
                    '</tr>' +
                '</table>' +
            '</div>' +
        '</tpl>'
    ),
    
    dataKeys: ['info', 'status', 'executingStage', 'synchronisationMessages'],
    
    initComponent: function(container, position)
    {
        var panel = this;
        Ext.apply(this, {
            items: [{
                id: this.id + '-inner',
                xtype: 'container',
                layout: 'htable',
                contentEl: 'center',
                items: [{
                    id: this.id + '-left',
                    xtype: 'container',
                    layout: 'vtable',
                    items: [{
                        id: this.id + '-info',
                        xtype: 'xzpropertytable',
                        title: 'agent details',
                        rows: [{
                            name: 'name',
                            formatter: Ext.util.Format.htmlEncode
                        }, {
                            name: 'location',
                            formatter: Ext.util.Format.htmlEncode                        
                        }]
                    }, {
                        id: this.id + '-status',
                        xtype: 'xzkeyvaluetable',
                        title: 'agent status',
                        lengthLimit: 196
                    }, {
                        id: this.id + '-executingStage',
                        xtype: 'xzexecutingstagetable',
                        title: 'executing build stage'
                    }]
                }, {
                    id: this.id + '-synchronisationMessages',
                    title: 'recent synchronisation messages',
                    emptyMessage: 'no synchronisation messages found',
                    xtype: 'xzsummarytable',
                    style: 'margin-top: 17px',
                    columns: [{
                        name: 'type',
                        cls: 'fit-width',
                        renderer: Ext.util.Format.htmlEncode
                    }, {
                        name: 'description',
                        renderer: Ext.util.Format.htmlEncode
                    }, {
                        name: 'status',
                        cls: 'fit-width',
                        renderer: function(status, message) {
                            return panel.STATUS_TEMPLATE.apply({
                                baseUrl: window.baseUrl,
                                id: panel.messageIndex++,
                                status: status,
                                squashedStatus: status.replace(/\s+/, ''),
                                statusMessage: message.statusMessage
                            })
                        }
                    }]
                }]
            }]
        });

        Zutubi.pulse.agent.AgentStatusPanel.superclass.initComponent.apply(this, arguments);
    },
    
    update: function(data)
    {
        this.messageIndex = 0;
        Zutubi.pulse.agent.AgentStatusPanel.superclass.update.apply(this, arguments);
    }
});
