// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/TextBox.js
// dependency: zutubi/table/LinkTable.js
// dependency: zutubi/pulse/SectionHeading.js
// dependency: zutubi/pulse/project/BuildSummaryTable.js

/**
 * The content of the my builds page.
 *
 * @cfg {Array} columns Array of names for columns to show in the table. 
 */
Zutubi.pulse.project.dashboard.MyBuildsPanel = Ext.extend(Zutubi.ActivePanel, {
    layout: 'border',
    border: false,
    dataKeys: ['builds'],
    
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
                    xtype: 'xzbuildsummarytable',
                    id: this.id + '-builds',
                    title: 'my builds',
                    selectedColumns: this.columns,
                    emptyMessage: 'no personal builds found'
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
                    id: this.id + '-tools',
                    title: 'tools downloads',
                    iconTemplate: 'images/{icon}.gif',
                    data: [{
                        icon: 'compress',
                        label: 'zip archive',
                        action: window.baseUrl + '/packages/pulse-dev-' + this.version + '.zip'
                    }, {
                        icon: 'compress',
                        label: 'tar archive',
                        action: window.baseUrl + '/packages/pulse-dev-' + this.version + '.tar.gz'
                    }, {
                        icon: 'pulse',
                        label: 'windows installer',
                        action: window.baseUrl + '/packages/pulse-dev-' + this.version + '.exe'
                    }]
                }, {
                    xtype: 'xztextbox',
                    title: 'personal builds',
                    data: 'Personal builds allow you to test your changes before committing them to your SCM. To submit personal build requests, you must install the developer tools on your local machine. Use the links provided to download a tools package that is compatible with this server.'
                }]
            }]
        });
        
        Zutubi.pulse.project.dashboard.MyBuildsPanel.superclass.initComponent.apply(this, arguments);
    }
});
