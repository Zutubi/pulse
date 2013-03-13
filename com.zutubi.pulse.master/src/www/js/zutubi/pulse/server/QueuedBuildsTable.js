// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/SummaryTable.js

/**
 * A simple customisation of the summary table that allows hidden queue items
 * to be rendered specially. 
 *
 * @cfg {String} id           Id to use for the table.
 * @cfg {Object} data         Data object used to populate the table.
 * @cfg {String} title        Title for the table heading row.
 * @cfg {String} emptyMessage Message to show when the table has no rows to display (if not
 *                            specified, the table is hidden in this case).
 */
Zutubi.pulse.server.QueuedBuildsTable = Ext.extend(Zutubi.table.SummaryTable, {
    title: 'queued builds',
    emptyMessage: 'no queued builds',
    customisable: false,

    template: new Ext.XTemplate(
        '<table id="{id}" class="{cls}">' +
            '<tr>' +
                '<th class="heading" colspan="{columnCount}">' +
                    '<span style="float: right">' +
                        '<span id="{id}-cancel-all" style="display: none">' +
                            '<a href="#" class="unadorned" onclick="Ext.getCmp(\'{id}\').cancelAll(); return false">' +
                                '<img ext:qtip="cancel all queued builds" alt="cancel all" src="{[window.baseUrl]}/images/cancel.gif"/>' +
                            '</a>' +
                        '</span>' +
                    '</span>' +
                    '<span class="clear"/>' +
                    '<span id="{id}-title">{title}</span>' +
                '</th>' +
            '</tr>' +
        '</table>'
    ),

    columns: [{
            name: 'owner',
            renderer: function(owner, request)
            {
                if (request.personal)
                {
                    return '<img alt="personal" src="' + window.baseUrl + '/images/user.gif"/> ' +
                           Ext.util.Format.htmlEncode(owner) + ' build ' + request.personalNumber;
                }
                else
                {
                    return Zutubi.pulse.project.renderers.project(owner);
                }
            }
        },
        Zutubi.pulse.project.configs.build.revision, {
            name: 'prettyQueueTime',
            key: 'queued',
            renderer: Ext.util.Format.htmlEncode
        },
        Zutubi.pulse.project.configs.build.reason, {
            name: 'cancelPermitted',
            key: 'actions',
            renderer: function(cancelPermitted, request)
            {
                var link;
                
                if (cancelPermitted)
                {
                    link = ' href="#" onclick="cancelQueuedBuild(' + request.id + '); return false"';
                    return '<a class="unadorned"' + link + '><img alt="cancel" src="' + window.baseUrl + '/images/cancel.gif"/></a> ' +
                           '<a id="cancel-' + request.id + '-button" ' + link + '>cancel</a>';
                }
                else
                {
                    return '&nbsp';
                }
            }
        }
    ],
        
    generateRow: function(data)
    {
        if (data.hidden)
        {
            return '<tr><td class="leftmost rightmost understated" colspan="5">you do not have permission to view this entry</td></tr>';
        }
        else
        {
            return Zutubi.pulse.server.QueuedBuildsTable.superclass.generateRow.apply(this, arguments);
        }
    },

    renderDynamic: function()
    {
        Zutubi.table.SummaryTable.superclass.renderDynamic.apply(this, arguments);
        this.updateCancelAll();
    },

    setCancelAllPermitted: function(permitted)
    {
        if (permitted !== this.cancelAllPermitted)
        {
            this.cancelAllPermitted = permitted;
            this.updateCancelAll();
        }
    },

    updateCancelAll: function()
    {
        var el= Ext.get(this.id + '-cancel-all');
        if (el)
        {
            el.setStyle('display', this.cancelAllPermitted ? 'inline' : 'none');
        }
    },

    cancelAll: function()
    {
        cancelQueuedBuild(-1);
    }
});

Ext.reg('xzqueuedbuildstable', Zutubi.pulse.server.QueuedBuildsTable);
