// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js
// dependency: zutubi/KeyValue.js

/**
 * A table that shows a set of key-value pairs, one pair per row.  Keys are
 * shown as row headers, and values in adjoining cells.
 *
 * @cfg {String}  cls          Class to use for the table (defaults to 'content-table').
 * @cfg {String}  id           Id to use for the table.
 * @cfg {Object}  data         Data object used to populate the table, should be a single
 *                             Object from which the keys and values will be taken.
 * @cfg {String}  title        Title for the table heading row.
 * @cfg {String}  emptyMessage Message to show when the table has no rows to display (if not
 *                             specified, the table is hidden in this case).
 * @cfg {Integer} lengthLimit  Limit on the length of any row value.  Longer values will be
 *                             trimmed with a tooltip containing the full value. 
 * @cfg {Integer} rowLimit     Maximum number of rows to show by default.  If the table
 *                             has more rows, those beyond the limit are hidden and a special
 *                             row that expands the table on click is added.
 */
Zutubi.table.KeyValueTable = Ext.extend(Zutubi.table.ContentTable, {
    cls: 'content-table xz-keyvalue-collapsed',
    expanded: false,
    columnCount: 2,
    rowLimit: 12,
    lengthLimit: 128,
    
    rowTemplate: new Ext.XTemplate(
        '<tr class="' + Zutubi.table.CLASS_DYNAMIC + ' {cls}">' +
            '<th class="fit-width top right leftmost">{key}</th>' +
            '<td class="rightmost">{value}</td>' +
        '</tr>'
    ),

    moreTemplate: new Ext.XTemplate(
        '<tr class="xz-keyvalue-more ' + Zutubi.table.CLASS_DYNAMIC + '">' +
            '<td class="leftmost rightmost" colspan="2">... {count} more items (click to expand)</td>' +
        '</tr>'
    ),

    clippedTemplate: new Ext.XTemplate('<span title="{value}">{clippedValue}</span>'),

    getKeys: function()
    {
        var keys = [];
        if (this.data)
        {
            for (var key in this.data)
            {
                keys.push(key);
            }
        }
        
        return keys;
    },
    
    getCount: function()
    {
        return this.getKeys().length;
    },
    
    renderData: function()
    {
        var keys = this.getKeys();
        
        keys.sort();

        var previousRow = this.el.child('tr');
        var i;
        for (i = 0; i < keys.length; i++)
        {
            var key = keys[i];
            var value = this.data[key];
            var renderedValue;
            
            if (value.length > this.lengthLimit)
            {
                renderedValue = this.clippedTemplate.apply({
                    clippedValue: Ext.util.Format.htmlEncode(value.substring(0, this.lengthLimit - 3)) + '...',
                    value: Ext.util.Format.htmlEncode(value)
                });
            }
            else
            {
                renderedValue = Ext.util.Format.htmlEncode(value);
            }
            
            
            var args = {
                key: Ext.util.Format.htmlEncode(key),
                value: renderedValue,
                cls: i >= this.rowLimit ? 'xz-keyvalue-over' : ''
            };
            previousRow = this.rowTemplate.insertAfter(previousRow, args, true);
        }
        
        if (i > this.rowLimit && !this.expanded)
        {
            var moreRow = this.moreTemplate.insertAfter(previousRow, {count: i - this.rowLimit}, true);
            var table = this;
            moreRow.addClassOnOver('project-highlighted');
            moreRow.on('click', function() {
                table.el.addClass('xz-keyvalue-expanded');
                table.el.removeClass('xz-keyvalue-collapsed');
                table.expanded = true;
            });
        }
    }
});

Ext.reg('xzkeyvaluetable', Zutubi.table.KeyValueTable);
