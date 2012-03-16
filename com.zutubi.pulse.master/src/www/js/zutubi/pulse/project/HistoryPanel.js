// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/Pager.js
// dependency: zutubi/pulse/project/BuildSummaryTable.js

/**
 * The content of the history pages.  Expects data of the form:
 *
 * {
 *     builds: [ BuildModels ],
 *     pager: PagingModel
 * }
 *
 * @cfg {Array}  columns     Columns to display in the builds table.
 * @cfg {String} pagerUrl    URL to use as the basis for links in the pager.
 * @cfg {String} stateFilter State filter value.
 */
Zutubi.pulse.project.HistoryPanel = Ext.extend(Zutubi.ActivePanel, {
    border: false,
    autoScroll: true,
    projectId: '0',
    agentId: '0',
    
    dataKeys: ['builds', 'pager'],
    
    initComponent: function(container, position)
    {
        var panel;

        panel = this;
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
                            ['healthy', '[any healthy]'],
                            ['error', 'error'],
                            ['failure', 'failure'],
                            ['terminated', 'terminated'],
                            ['warnings', 'warnings'],
                            ['success', 'success']
                        ],
                        value: this.stateFilter,
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
                    }, ' ', ' ', ' ', ' ', {
                        xtype: 'label',
                        text: 'builds per page:'
                    }, ' ', {
                        xtype: 'textfield',
                        id: 'builds-per-page',
                        width: 30,
                        value: this.buildsPerPage,
                        listeners: {
                            specialkey: function(c, e) {
                                if (e.getKey() === e.RETURN)
                                {
                                    panel.updateBuildsPerPage();
                                }
                            }
                        }
                    }, {
                        xtype: 'xztblink',
                        icon: window.baseUrl + '/images/arrow_refresh.gif',
                        text: 'update',
                        listeners: {
                            click: function() {
                                panel.updateBuildsPerPage();
                            }
                        }
                    }, '->', {
                        xtype: 'xztblink',
                        icon: window.baseUrl + '/images/feed-icon-16x16.gif',
                        url: window.baseUrl + '/rss.action' + (this.projectId === '0' ? '' : '?projectId=' + this.projectId),
                        hidden: this.agentId !== '0'
                    }]
                },
                items: [{
                    id: this.id + '-builds',
                    xtype: 'xzbuildsummarytable',
                    title: 'build history',
                    selectedColumns: this.columns,
                    emptyMessage: 'no builds found',
                    customisable: !this.anonymous
                }, {
                    id: this.id + '-pager',
                    xtype: 'xzpager',
                    itemLabel: 'build',
                    url: this.pagerUrl, 
                    extraParams: this.stateFilter === '' ? '' : 'stateFilter/' + this.stateFilter + '/',
                    labels: {
                        first: 'latest',
                        previous: 'newer',
                        next: 'older',
                        last: 'oldest'
                    }
                }]
            }]
        });

        Zutubi.pulse.project.HistoryPanel.superclass.initComponent.apply(this, arguments);
    },
    
    setFilter: function(filter)
    {
        var location;

        location = this.pagerUrl + this.data.pager.currentPage + '/';
        if (filter)
        {
            location += 'stateFilter/' + filter + '/';
        }
        
        window.location.href = location;
    },
    
    updateBuildsPerPage: function()
    {
        var panel, toolbar;

        panel = this;
        toolbar = Ext.get('build-toolbar');
        toolbar.mask();
        runAjaxRequest({
            url: window.baseUrl + '/ajax/customiseHistoryBuilds.action',
            params: {'buildsPerPage': Ext.get('builds-per-page').getValue() },
            success: function() {
                panel.load(function() {
                    toolbar.unmask();
                });
            },
            failure: function() {
                toolbar.unmask();
            }
        });
    }
});
