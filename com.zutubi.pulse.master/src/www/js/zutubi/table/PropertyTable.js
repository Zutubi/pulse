// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js

/**
 * A table that shows a set of properties related to one "entity".  The property names
 * are listed in a left column, and the values in the right one.
 *
 * @cfg {String} cls          Class to use for the table (defaults to 'content-table').
 * @cfg {String} id           Id to use for the table.
 * @cfg {Array}  rows         An array of {Zutubi.table.PropertyRow} configs.
 * @cfg {Object} data         Data object used to populate the table, should contain keys that match
 *                            the rows of this table.
 * @cfg {String} title        Title for the table heading row.
 * @cfg {String} emptyMessage Message to show when the table has no rows to display (if not
 *                            specified, the table is hidden in this case).
 */
Zutubi.table.PropertyTable = Ext.extend(Zutubi.table.ContentTable, {
    initComponent: function() {
        this.columnCount = 2;
        this.rowTemplate = new Ext.Template('<tr class="' + Zutubi.table.CLASS_DYNAMIC + '"><th class="fit-width right leftmost">{key}</th><td id="{id}" class="rightmost">{value}</td></tr>');
        
        var rowConfigs = this.rows;
        this.rows = [];
        for (var i = 0; i < rowConfigs.length; i++)
        {
            this.rows.push(new Zutubi.table.PropertyRow(rowConfigs[i]));
        }

        Zutubi.table.PropertyTable.superclass.initComponent.apply(this, arguments);
    },
     
    renderData: function() {
        var rows = this.rows;
        var previousRow = this.el.child('tr');
        for (var i = 0, l = this.rows.length; i < l; i++)
        {
            var row = this.rows[i];
            var args = row.getTemplateArgs(this.data);
            args['id'] = this.id + '-' + row.name;
            previousRow = this.rowTemplate.insertAfter(previousRow, args, true);
        }
    }
});

/**
 * A single row in a PropertyTable.
 *
 * @cfg {String} name     The name of this row, used to look up the value in the data object.
 * @cfg {String} key      The key text to display in the left cell (defaults to the name).
 * @cfg {String} renderer Function to turn the raw string value into HTML to populate the
 *                        right cell (defaults to a simple HTML encode)
 */
Zutubi.table.PropertyRow = function(config) {
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

Zutubi.table.PropertyRow.prototype = {
    getTemplateArgs: function(data) {
        return {key: this.key, value: this.renderer(data[this.name])};
    }
};

Ext.reg('xzpropertytable', Zutubi.table.PropertyTable);

