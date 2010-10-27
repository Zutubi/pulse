// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/Pager.js
// dependency: zutubi/table/SummaryTable.js

/**
 * The content of the project history page.  Expects data of the form:
 * {
 *     url: '/browse/projects/some-project/history/',
 *     stateFilter: '[any broken]',
 *     builds: [ BuildModels ],
 *     pager: PagingModel
 * }
 */
Zutubi.pulse.project.browse.ProjectHistoryPanel = Ext.extend(Ext.Panel, {
    border: false,
    autoScroll: true,
    
    dataKeys: ['builds', 'pager'],
    
    initComponent: function(container, position) {
        var panel = this;
        Ext.apply(this, {
            items: [{
                xtype: 'panel',
                border: false,
                id: 'history-inner',
                style: 'padding: 17px',
                layout: 'vtable',
                contentEl: 'center',
                tbar: {
                    id: 'build-toolbar',
                    style: 'margin: 0',
                    items: [{
                        xtype: 'label',
                        text: 'state filter:'
                    }, ' ', {
                        xtype: 'combo',
                        id: 'state-filter',
                        editable: false,
                        forceSelection: true,
                        triggerAction: 'all',
                        store: ['[any]', '[any broken]', 'error', 'failure', 'terminated', 'success'],
                        value: this.data.stateFilter,
                        listeners: {
                            select: function(combo) {
                                window.location = panel.data.url + panel.data.pager.currentPage + '/stateFilter/' + encodeURIComponent(combo.getValue()) + '/';
                            }
                        }
                    }, '->', {
                        xtype: 'xztblink',
                        icon: window.baseUrl + '/images/feed-icon-16x16.gif',
                        url: window.baseUrl + '/rss.action?projectId=' + this.projectId
                    }]
                },
                items: [{
                    id: 'history-builds',
                    xtype: 'xzsummarytable',
                    title: 'build history',
                    columns: [
                        Zutubi.pulse.project.configs.build.id,
                        Zutubi.pulse.project.configs.build.rev,
                        Zutubi.pulse.project.configs.build.status,
                        'reason',
                        Zutubi.pulse.project.configs.build.tests,
                        Zutubi.pulse.project.configs.build.when,
                        Zutubi.pulse.project.configs.build.elapsed
                    ],
                    data: this.data.builds,
                    emptyMessage: 'no builds found'
                }, {
                    id: 'history-pager',
                    xtype: 'xzpager',
                    itemLabel: 'build',
                    url: this.data.url, 
                    extraParams: '/stateFilter/' + encodeURIComponent(this.data.stateFilter) + '/', 
                    data: this.data.pager
                }]
            }]
        });

        Zutubi.pulse.project.browse.ProjectHistoryPanel.superclass.initComponent.apply(this, arguments);
    },
        
    update: function(data) {
        this.data = data;
        for (var i = 0, l = this.dataKeys.length; i < l; i++)
        {
            var key = this.dataKeys[i];
            Ext.getCmp('history-' + key).update(data[key]);    
        }
    }
});
