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
                if (cancelPermitted)
                {
                    var link = ' href="#" onclick="cancelQueuedBuild(' + request.id + '); return false"';
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
    }
});

Ext.reg('xzqueuedbuildstable', Zutubi.pulse.server.QueuedBuildsTable);
