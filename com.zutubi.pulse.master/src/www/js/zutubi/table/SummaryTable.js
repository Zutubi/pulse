// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js

/**
 * A table that summarises a collection of items, one item per row.  Multiple fields
 * from each item are shown across multiple columns.
 *
 * @cfg {String}         cls      Class to use for the table (defaults to 'two-content')
 * @cfg {Array}          columns  An array of {Zutubi.table.SummaryColumn} configs.
 * @cfg {String}         id       Id to use for the table.
 * @cfg {Ext.data.Store} store    The store used to populate the table.  Should contain
 *                                a record for each row, with fields matching the columns.
 * @cfg {String}         title    Title for the table heading row.
 */
Zutubi.table.SummaryTable = Ext.extend(Zutubi.table.ContentTable, {
    initComponent: function() {
        this.columnCount = this.columns.length;
        this.cellTemplate = new Ext.XTemplate('<{tag} class="content <tpl if="first">leftmost </tpl><tpl if="last">rightmost </tpl>">{value}</{tag}>');
        
        var columnConfigs = this.columns;
        this.columns = [];
        for (var i = 0; i < columnConfigs.length; i++)
        {
            this.columns.push(new Zutubi.table.SummaryColumn(columnConfigs[i]));
        }

        Zutubi.table.SummaryTable.superclass.initComponent.apply(this, arguments);
    },
     
    renderRows: function() {
        var previousRow = this.el.child('tr');
        previousRow = previousRow.insertHtml('afterEnd', this.generateHeader(), true);
        this.store.each(function(r) {
            previousRow = previousRow.insertHtml('afterEnd', this.generateRow('td', r.data), true);
        }, this);
    },
    
    generateHeader: function() {
        var data = {};
        for (var i = 0; i < this.columns.length; i++)
        {
            var column = this.columns[i];
            data[column.name] = column.key;
        }

        return this.generateRow('th', data, function(v) { return v; })
    },
    
    generateRow: function(tag, data, renderer) {
        var html = '<tr>';
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
    Ext.apply(this, config, {
        renderer: Ext.util.Format.htmlEncode
    });
    
    if (!this.key)
    {
        this.key = this.name;
    }
};
