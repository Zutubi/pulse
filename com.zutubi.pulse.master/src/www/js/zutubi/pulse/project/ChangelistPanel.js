// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/Pager.js
// dependency: zutubi/pulse/project/BuildSummaryTable.js

/**
 * The content of the changelist page.  This page may appear in multiple
 * contexts: on the dashboard, within a project, or within a specific
 * build.  It expects data of the form:
 *
 * {
 *     change: ChangelistModel,
 *     builds: [ BuildModels ],
 *     files : [ ChangelistFileModels ],
 *     pager: PagerModel
 * }
 *
 * @cfg {String} revision Revision string for the changelist.
 * @cfg {String} pagerUrl URL to use as the basis for links in the pager.
 */
Zutubi.pulse.project.ChangelistPanel = Ext.extend(Zutubi.ActivePanel, {
    border: false,
    autoScroll: true,

    dataKeys: ['changelist', 'builds', 'files', 'pager'],
    
    initComponent: function(container, position)
    {
        var panel;

        panel = this;
        Ext.apply(this, {
            items: [{
                xtype: 'panel',
                border: false,
                id: this.id + '-inner',
                style: 'padding: 0 17px',
                layout: 'vtable',
                contentEl: 'center',
                items: [{
                    id: this.id + '-changelist',
                    xtype: 'xzpropertytable',
                    title: 'changelist ' + Ext.util.Format.htmlEncode(this.revision) + ' details',
                    rows: [
                        Zutubi.pulse.project.configs.changelist.rev,
                        'who',
                        Zutubi.pulse.project.configs.changelist.when,
                        Zutubi.pulse.project.configs.changelist.fullComment
                    ]
                }, {
                    id: this.id + '-builds',
                    xtype: 'xzbuildsummarytable',
                    title: 'all builds affected by this change',
                    selectedColumns: ['project', 'number', 'status'],
                    emptyMessage: 'no builds found',
                    customisable: false
                }, {
                    id: this.id + '-files',
                    xtype: 'xzsummarytable',
                    title: 'files changed',
                    columns: [
                        'fileName',
                        'revision',
                        'action',
                        {
                            name: 'links',
                            renderer: function(links)
                            {
                                var result;

                                result = '';
                                if (links)
                                {
                                    if (links.viewUrl)
                                    {
                                        result += ' <a class="unadorned" href="' + links.viewUrl + '" title="view"><img alt="view" src="' + window.baseUrl + '/images/page.gif"/></a>';
                                    }
                                    if (links.downloadUrl)
                                    {
                                        result += ' <a class="unadorned" href="' + links.downloadUrl + '" title="download"><img alt="download" src="' + window.baseUrl + '/images/page_put.gif"/></a>';
                                    }
                                    if (links.diffUrl)
                                    {
                                        result += ' <a class="unadorned" href="' + links.diffUrl + '" title="diff"><img alt="diff" src="' + window.baseUrl + '/images/page_diff.gif"/></a>';
                                    }
                                }
                                
                                if (result.length === 0)
                                {
                                    result = '&nbsp;';
                                }

                                return result;
                            }
                        }
                    ],
                    emptyMessage: 'no files found'
                }, {
                    id: this.id + '-pager',
                    xtype: 'xzpager',
                    itemLabel: 'file',
                    url: this.pagerUrl
                }]
            }]
        });

        Zutubi.pulse.project.ChangelistPanel.superclass.initComponent.apply(this, arguments);
    }
});
