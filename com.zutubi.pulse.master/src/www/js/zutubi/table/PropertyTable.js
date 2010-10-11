// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js

/**
 * A table that shows a set of properties related to one "entity".  The property names
 * are listed in a left column, and the values in the right one.  The rows are populated
 * from a single record in a store.
 *
 * @cfg {String}         cls      Class to use for the table (defaults to 'two-content')
 * @cfg {String}         id       Id to use for the table.
 * @cfg {Array}          rows     An array of {Zutubi.table.PropertyRow} configs.
 * @cfg {Ext.data.Store} store    The store used to populate the table.  Should contain
 *                                a single record with keys that match the rows of this
 *                                table.
 * @cfg {String}         title    Title for the table heading row.
 */
Zutubi.table.PropertyTable = Ext.extend(Zutubi.table.ContentTable, {
    initComponent: function() {
        this.columnCount = 2;
        this.rowTemplate = new Ext.Template('<tr><th class="content fit-width right leftmost">{key}</th><td id="{id}" class="content rightmost">{value}</td></tr>');
        
        var rowConfigs = this.rows;
        this.rows = [];
        for (var i = 0; i < rowConfigs.length; i++)
        {
            this.rows.push(new Zutubi.table.PropertyRow(rowConfigs[i]));
        }

        Zutubi.table.PropertyTable.superclass.initComponent.apply(this, arguments);
    },
     
    renderRows: function() {
        var record = this.store.getAt(0);
        var rows = this.rows;
        var previousRow = this.el.child('tr');
        for (var i = 0; i < this.rows.length; i++)
        {
            var row = this.rows[i];
            var args = row.getTemplateArgs(record);
            args['id'] = this.id + '-' + row.name;
            previousRow = this.rowTemplate.insertAfter(previousRow, args, true);
        }
    }
});

/**
 * A single row in a PropertyTable.
 *
 * @cfg {String} name     The name of this row, used to look up the value in the record.
 * @cfg {String} key      The key text to display in the left cell (defaults to the name).
 * @cfg {String} renderer Function to turn the raw string value into HTML to populate the
 *                        right cell (defaults to a simple HTML encode)
 */
Zutubi.table.PropertyRow = function(config) {
    Ext.apply(this, config, {
        renderer: Ext.util.Format.htmlEncode
    });
    
    if (!this.key)
    {
        this.key = this.name;
    }
};

Zutubi.table.PropertyRow.prototype = {
    getTemplateArgs: function(record) {
        return {key: this.key, value: this.renderer(record.get(this.name))};
    }
};
