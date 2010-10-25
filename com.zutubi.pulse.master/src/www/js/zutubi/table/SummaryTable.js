// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js
// dependency: zutubi/KeyValue.js

/**
 * A table that summarises a collection of items, one item per row.  Multiple fields
 * from each item are shown across multiple columns.
 *
 * @cfg {String} cls          Class to use for the table (defaults to 'content-table')
 * @cfg {Array}  columns      An array of {Zutubi.KeyValue} configs.
 * @cfg {String} id           Id to use for the table.
 * @cfg {Array}  data         Array of objects used to populate the table.  Should contain an entry
 *                            for each row, with fields matching the columns.
 * @cfg {String} title        Title for the table heading row.
 * @cfg {String} emptyMessage Message to show when the table has no rows to display (if not
 *                            specified, the table is hidden in this case).
 */
Zutubi.table.SummaryTable = Ext.extend(Zutubi.table.ContentTable, {
    initComponent: function() {
        this.columnCount = this.columns.length;
        this.cellTemplate = new Ext.XTemplate('<{tag} class="<tpl if="first">leftmost </tpl><tpl if="last">rightmost </tpl>{cls}">{value}</{tag}>');
        
        var columnConfigs = this.columns;
        this.columns = [];
        for (var i = 0; i < columnConfigs.length; i++)
        {
            var keyValue = new Zutubi.KeyValue(columnConfigs[i]);
            keyValue.component = this;
            this.columns.push(keyValue);
        }

        Zutubi.table.SummaryTable.superclass.initComponent.apply(this, arguments);
    },

    dataExists: function() {
        return Zutubi.table.SummaryTable.superclass.dataExists.apply(this, arguments) && this.data.length > 0;
    },
    
    renderFixed: function() {
        var data = {};
        for (var i = 0; i < this.columns.length; i++)
        {
            var column = this.columns[i];
            data[column.name] = column.key;
        }
        
        this.tbodyEl.insertHtml('beforeEnd', this.generateRow('th', false, data, false))
    },
    
    renderData: function() {
        for (var i = 0, l = this.data.length; i < l; i++)
        {
            this.tbodyEl.insertHtml('beforeEnd', this.generateRow('td', true, this.data[i], true), true);
        };
    },
    
    generateRow: function(tag, dynamic, data, useRenderer) {
        var html = '<tr';
        if (dynamic)
        {
            html += ' class="' + Zutubi.table.CLASS_DYNAMIC + '"'; 
        }
        
        html += '>';
        
        var columnCount = this.columns.length;
        for (var i = 0; i < columnCount; i++)
        {
            var column = this.columns[i];
            html += this.cellTemplate.apply({
                tag: tag,
                cls: column.cls,
                first: i == 0,
                last: i == columnCount - 1,
                value: useRenderer ? column.getRenderedValue(data) : data[column.name]
            });
        }
        
        html += '</tr>';
        return html;
    },

    getColumnNames: function() {
        var names = '';
        var columnCount = this.columns.length;
        for (var i = 0; i < columnCount; i++)
        {
            var column = this.columns[i];
            if (i > 0)
            {
                names += ',';
            }
            names += column.name;
        }

        return names;
    }
});

Ext.reg('xzsummarytable', Zutubi.table.SummaryTable);
