// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: ./ActiveBuildsTable.js
// dependency: ./QueuedBuildsTable.js
// dependency: zutubi/pulse/project/namespace.js

/**
 * The content of the server activity tab pages.  Expects data of the form:
 *
 * {
 *     queued: [ QueuedBuildModels ],
 *     active: [ ActiveBuildModels ]
 * }
 */
Zutubi.pulse.server.ActivityPanel = Ext.extend(Zutubi.ActivePanel, {
    border: false,
    autoScroll: true,
    
    dataKeys: ['queued', 'active'],
    
    initComponent: function(container, position)
    {
        var panel = this;
        Ext.apply(this, {
            items: [{
                xtype: 'panel',
                border: false,
                id: this.id + '-inner',
                style: 'padding: 17px',
                layout: 'vtable',
                contentEl: 'center',
                tbar: {
                    id: 'build-toolbar',
                    style: 'margin: 0',
                    items: [{
                        xtype: 'label',
                        text: 'stage queue:'
                    }, ' ', {
                        xtype: 'xztbicontext',
                        id: 'stage-queue-state',
                        icon: window.baseUrl + '/images/cog.gif',
                        text: 'running'
                    }, {
                        xtype: 'xztblink',
                        id: 'stage-queue-toggle',
                        icon: window.baseUrl + '/images/config/actions/pause.gif',
                        text: 'pause',
                        style: 'margin-left: 0',
                        listeners: {
                            click: function() {
                                panel.toggleStageQueue();
                            }
                        }
                    }]
                },
                items: [{
                    id: this.id + '-queued',
                    xtype: 'xzqueuedbuildstable',
                }, {
                    id: this.id + '-active',
                    title: 'active builds',
                    emptyMessage: 'no active builds',
                    xtype: 'xzactivebuildstable'
                }]
            }]
        });

        Zutubi.pulse.server.ActivityPanel.superclass.initComponent.apply(this, arguments);
    },
    
    update: function(data)
    {
        this.updateToolbar(data);
        Zutubi.pulse.server.ActivityPanel.superclass.update.apply(this, arguments);
    },
    
    getToolbarEl: function()
    {
        return this.items.get(0).getTopToolbar().el;
    },
    
    updateToolbar: function(data)
    {
        var stateItem = Ext.getCmp('stage-queue-state');
        var toggleItem = Ext.getCmp('stage-queue-toggle');
        if (data.stageQueueRunning)
        {
            stateItem.setIcon(window.baseUrl + '/images/cog.gif');
            stateItem.setText('running');
            toggleItem.setIcon(window.baseUrl + '/images/config/actions/pause.gif');
            toggleItem.setText('pause');
        }
        else
        {
            stateItem.setIcon(window.baseUrl + '/images/stop.gif');
            stateItem.setText('paused');
            toggleItem.setIcon(window.baseUrl + '/images/config/actions/resume.gif');
            toggleItem.setText('resume');
        }
        
        if (data.stageQueueTogglePermitted != toggleItem.enabled)
        {
            if (data.stageQueueTogglePermitted)
            {
                toggleItem.enable();
            }
            else
            {
                toggleItem.disable();
            }
        }
    },
    
    toggleStageQueue: function()
    {
        this.getToolbarEl().mask();
        Ext.Ajax.request({
            url: window.baseUrl + '/ajax/toggleRecipeQueue.action',
            callback: this.handleToggleResponse.createDelegate(this)
        });
    },
    
    handleToggleResponse: function(options, success, response)
    {
        this.getToolbarEl().unmask();
        if (success)
        {
            var result = Ext.util.JSON.decode(response.responseText);
            if (result.success)
            {
                this.load();
            }
            else
            {
                showStatus(Ext.util.Format.htmlEncode(result.detail), 'failure');
            }
    
        }
        else
        {
            showStatus('Cannot contact server', 'failure');
        }
    }
});
