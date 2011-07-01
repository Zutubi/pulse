// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/pulse/project/namespace.js

/**
 * A table that shows log messages.
 *
 * @cfg {String}  id           Id to use for the table.
 * @cfg {Mixed}   data         Data used to populate the table.  Should be an array of log entries:
 *                             {
 *                                 when: DateModel,
 *                                 level: String,
 *                                 count: Integer,
 *                                 sourceMethod: String,
 *                                 sourceClass: String,
 *                               [ message: String, ]
 *                               [ stackPreview: String,
 *                                 stackTrace: String ]
 *                             }
 */
Zutubi.pulse.server.LogMessagesTable = Ext.extend(Ext.BoxComponent, {
    template: new Ext.XTemplate(
        '<table id="{id}" class="content-table">' +
            '<tbody></tbody>' +
        '</table>'
    ),

    emptyTemplate: new Ext.XTemplate(
        '<tr><td class="understated leftmost rightmost">no log entries found</td></tr>'
    ),

    entryTemplate: new Ext.XTemplate(
            '<tr>' +
                '<td class="leftmost {messageCls}" width="15%">{when}</td>' +
                '<td class="{messageCls}" width="10%"><img alt="{messageCls}" src="{base}/images/{image}.gif"/> {level}</td>' +
                '<td class="{messageCls}" width="10%">count :: {count}</td>' +
                '<td class="rightmost {messageCls}">{sourceClass} :: {sourceMethod}</td>' +
            '</tr>' +
            '<tpl if="message">' +
                '<tr id="{id}-message-{index}">' +
                    '<td class="leftmost rightmost" colspan="4">' +
                        '<tpl if="messagePreview">' +
                            '<pre><span title="{message:htmlEncode}">{messagePreview:htmlEncode}</span></pre>' +
                        '</tpl>' +
                        '<tpl if="!messagePreview">' +
                            '<pre>{message:htmlEncode}</pre>' +
                        '</tpl>' +
                    '</td>' +
                '</tr>' +
            '</tpl>' +
            '<tpl if="stackTrace">' +
                '<tr id="{id}-preview-{index}">' +
                    '<td class="leftmost rightmost" colspan="4">' +
                        '<pre>{stackPreview:htmlEncode}\n' +
                              '<span style="color: #555">(click to reveal full trace)</span>' +
                          '</pre>' +
                    '</td>' +
                '</tr>' +
                '<tr id="{id}-trace-{index}" style="display: none">' +
                    '<td class="leftmost rightmost" colspan="4">' +
                        '<pre>{stackTrace:htmlEncode}</pre>' +
                    '</td>' +
                '</tr>' +
            '</tpl>' +
            '<tpl if="more">' +
                '<tr><td style="border: none" colspan="4">&nbsp;</td></tr>' +
            '</tpl>'
    ),

    initComponent: function()
    {
        Zutubi.pulse.server.LogMessagesTable.superclass.initComponent.apply(this, arguments);
    },
    
    onRender: function(container, position)
    {
        if (position)
        {
            this.el = this.template.insertBefore(position, this, true);    
        }
        else
        {
            this.el = this.template.append(container, this, true);
        }

        this.tbodyEl = this.el.down('tbody');
        this.renderEntries();
        
        Zutubi.pulse.server.LogMessagesTable.superclass.onRender.apply(this, arguments);
    },

    /**
     * Updates this table with new data.
     */
    update: function(data)
    {
        this.data = data;
        if (this.rendered)
        {
            this.renderEntries();
        }
    },

    renderEntries: function()
    {
        this.clearRows();
        
        if (this.dataExists())
        {
            this.renderData();
        }
        else
        {
            this.renderEmptyMessage();
        }
    },
    
    dataExists: function()
    {
        return this.data !== undefined && this.data !== null && this.data.length > 0;
    },

    renderData: function()
    {
        for (var i = 0, len = this.data.length; i < len; i++)
        {
            var entry = this.data[i];
            var messageCls;
            var image;
            if (entry.level == 'severe')
            {
                messageCls = 'error';
                image = 'exclamation';
            }
            else if(entry.level == 'warning')
            {
                messageCls = 'warning';
                image = 'error';
            }
            else
            {
                messageCls = 'info';
                image = 'information';
            }

            this.entryTemplate.append(this.tbodyEl, {
                base: window.baseUrl,
                id: this.id,
                index: i,
                messageCls: messageCls,
                image: image,
                when: Zutubi.pulse.project.renderers.date(entry.when),
                count: entry.count,
                level: entry.level,
                sourceClass: entry.sourceClass,
                sourceMethod: entry.sourceMethod,
                messagePreview: entry.messagePreview,
                message: entry.message,
                stackPreview: entry.stackPreview,
                stackTrace: entry.stackTrace,
                more: i < len - 1
            }, false);

            if (entry.stackTrace)
            {
                var previewRow = Ext.get(this.id + '-preview-' + i);
                var traceRow = Ext.get(this.id + '-trace-' + i);
                previewRow.on('click', this.toggleTrace.createDelegate(this, [true, previewRow, traceRow]));
                previewRow.addClassOnOver('project-highlighted');
                traceRow.on('click', this.toggleTrace.createDelegate(this, [false, previewRow, traceRow]));
                traceRow.addClassOnOver('project-highlighted');
            }
        }
    },

    toggleTrace: function(reveal, previewRow, traceRow)
    {
        previewRow.setDisplayed(!reveal);
        traceRow.setDisplayed(reveal);
    },

    renderEmptyMessage: function()
    {
        this.emptyTemplate.append(this.tbodyEl, this, false);
    },

    clearRows: function()
    {
        var els = this.tbodyEl.select('tr');
        els.remove();
    }
});

Ext.reg('xzlogmessagestable', Zutubi.pulse.server.LogMessagesTable);
