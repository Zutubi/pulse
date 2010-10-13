// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js

/**
 * A table that summarises a collection of items, one item per row.  Multiple fields
 * from each item are shown across multiple columns.
 *
 * @cfg {String} cls          Class to use for the table (defaults to 'content-table')
 * @cfg {Array}  columns      An array of {Zutubi.table.SummaryColumn} configs.
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
        this.cellTemplate = new Ext.XTemplate('<{tag} class="<tpl if="first">leftmost </tpl><tpl if="last">rightmost </tpl>">{value}</{tag}>');
        
        var columnConfigs = this.columns;
        this.columns = [];
        for (var i = 0; i < columnConfigs.length; i++)
        {
            this.columns.push(new Zutubi.table.SummaryColumn(columnConfigs[i]));
        }

        Zutubi.table.SummaryTable.superclass.initComponent.apply(this, arguments);
    },
    
    renderFixed: function() {
        var data = {};
        for (var i = 0; i < this.columns.length; i++)
        {
            var column = this.columns[i];
            data[column.name] = column.key;
        }
        
        this.tbodyEl.insertHtml('beforeEnd', this.generateRow('th', false, data, function(v) { return v; }))
    },
    
    renderData: function() {
        for (var i = 0, l = this.data.length; i < l; i++)
        {
            this.tbodyEl.insertHtml('beforeEnd', this.generateRow('td', true, this.data[i]), true);
        };
    },
    
    generateRow: function(tag, dynamic, data, renderer) {
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
            var rawValue = data[column.name];
            html += this.cellTemplate.apply({
                tag: tag,
                first: i == 0,
                last: i == columnCount - 1,
                value: renderer ? renderer(rawValue) : column.renderer(rawValue)
            });
        }
        
        html += '</tr>';
        return html;
    }
});

/**
 * A single column in a SummaryTable.
 *
 * @cfg {String} name     The name of this column, used to look up the value in the record.
 * @cfg {String} key      The key text to display in the header (defaults to the name).
 * @cfg {String} renderer Function to turn the raw string value into HTML to populate value
 *                        cells (defaults to a simple HTML encode)
 */
Zutubi.table.SummaryColumn = function(config) {
    if (typeof config == 'string')
    {
        config = {name: config};
    }

    Ext.apply(this, config, {
        renderer: Ext.util.Format.htmlEncode
    });
    
    if (!this.key)
    {
        this.key = this.name;
    }
};

Ext.reg('xzsummarytable', Zutubi.table.SummaryTable);
