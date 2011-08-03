// dependency: ./namespace.js
// dependency: ./LogMessagesTable.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/Pager.js

/**
 * The content of the server and agent messages tabs.  Expects data of the form:
 *
 * {
 *     entries: { LogEntryModel, ... },
 *     pager: PagingModel
 * }
 */
Zutubi.pulse.server.MessagesPanel = Ext.extend(Zutubi.ActivePanel, {
    border: false,
    autoScroll: true,
    
    dataKeys: ['entries', 'pager'],
    
    initComponent: function(container, position)
    {
        var panel;

        panel = this;
        Ext.apply(this, {
            items: [{
                layout: 'vtable',
                xtype: 'container',
                contentEl: 'center',
                style: 'padding: 0 17px',
                items: [{
                    id: this.id + '-entries',
                    xtype: 'xzlogmessagestable'
                }, {
                    id: this.id + '-pager',
                    xtype: 'xzpager',
                    itemLabel: 'message',
                    url: this.pagerUrl,
                    labels: {
                        first: 'latest',
                        previous: 'newer',
                        next: 'older',
                        last: 'oldest'
                    }
                }]
            }]
        });

        Zutubi.pulse.server.MessagesPanel.superclass.initComponent.apply(this, arguments);
    }
});
