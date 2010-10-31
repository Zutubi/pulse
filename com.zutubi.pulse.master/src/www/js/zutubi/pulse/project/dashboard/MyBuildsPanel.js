// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/LinkTable.js
// dependency: zutubi/pulse/SectionHeading.js
// dependency: zutubi/pulse/project/BuildSummaryTable.js

/**
 * The content of the my builds page.
 */
Zutubi.pulse.project.dashboard.MyBuildsPanel = Ext.extend(Ext.Panel, {
    layout: 'border',
    border: false,
    
    initComponent: function(container, position) {
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
                id: 'my-builds-main',
                split: false,
                contentEl: this.loadingEl,
                layout: 'vtable',
                items: [{
                    xtype: 'xzbuildsummarytable',
                    id: 'my-builds-builds',
                    title: 'my builds',
                    selectedColumns: this.columns,
                    data: this.data,
                    emptyMessage: 'no personal builds found',
                }]
            }, {
                region: 'east',
                id: 'my-builds-right',
                bodyStyle: 'padding: 0 17px',
                split: true,
                collapsible: true,
                collapseMode: 'mini',
                hideCollapseTool: true,
                width: 300,
                layout: 'vtable',
                items: [{
                    xtype: 'xzlinktable',
                    id: 'my-builds-tools',
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
                    xtype: 'box',
                    autoEl: {
                        tag: 'p',
                        html: 'Personal builds allow you to test your changes before committing them to your SCM. To submit personal build requests, you must install the developer tools on your local machine. Use the links provided to download a tools package that is compatible with this server.'
                    }
                }]
            }]
        });
        
        Zutubi.pulse.project.dashboard.MyBuildsPanel.superclass.initComponent.apply(this, arguments);
    },
        
    update: function(data) {
        this.data = data;
        Ext.getCmp('my-builds-builds').update(data);    
    }
});
