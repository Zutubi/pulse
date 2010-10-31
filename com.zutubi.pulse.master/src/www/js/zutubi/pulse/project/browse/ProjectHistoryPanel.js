// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/Pager.js
// dependency: zutubi/pulse/project/BuildSummaryTable.js

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
                        store: [
                            ['', '[any]'],
                            ['broken', '[any broken]'],
                            ['error', 'error'],
                            ['failure', 'failure'],
                            ['terminated', 'terminated'],
                            ['success', 'success']
                        ],
                        value: this.data.stateFilter,
                        listeners: {
                            select: function(combo) {
                                panel.setFilter(combo.getValue());
                            }
                        }
                    }, ' ', {
                        xtype: 'xztblink',
                        id: 'state-filter-clear',
                        text: 'clear',
                        icon: window.baseUrl + '/images/config/actions/clean.gif',
                        listeners: {
                            click: function() {
                                panel.setFilter();
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
                    xtype: 'xzbuildsummarytable',
                    title: 'build history',
                    selectedColumns: this.columns,
                    data: this.data.builds,
                    emptyMessage: 'no builds found'
                }, {
                    id: 'history-pager',
                    xtype: 'xzpager',
                    itemLabel: 'build',
                    url: this.data.url, 
                    extraParams: this.data.stateFilter == '' ? '' : 'stateFilter/' + this.data.stateFilter + '/',
                    labels: {
                        first: 'latest',
                        previous: 'newer',
                        next: 'older',
                        last: 'oldest'
                    },
                    data: this.data.pager
                }]
            }]
        });

        Zutubi.pulse.project.browse.ProjectHistoryPanel.superclass.initComponent.apply(this, arguments);
    },
    
    setFilter: function(filter)
    {
        var location = this.data.url + this.data.pager.currentPage + '/';
        if (filter)
        {
            location += 'stateFilter/' + filter + '/';
        }
        
        window.location.href = location;
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
