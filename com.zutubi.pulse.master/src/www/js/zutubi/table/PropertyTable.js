// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js
// dependency: zutubi/KeyValue.js

/**
 * A table that shows a set of properties related to one "entity".  The property names
 * are listed in a left column, and the values in the right one.
 *
 * @cfg {String} cls          Class to use for the table (defaults to 'content-table').
 * @cfg {String} id           Id to use for the table.
 * @cfg {Array}  rows         An array of {Zutubi.KeyValue} configs.
 * @cfg {Object} data         Data object used to populate the table, should contain keys that match
 *                            the rows of this table.
 * @cfg {String} title        Title for the table heading row.
 * @cfg {String} emptyMessage Message to show when the table has no rows to display (if not
 *                            specified, the table is hidden in this case).
 */
Zutubi.table.PropertyTable = Ext.extend(Zutubi.table.ContentTable, {
    columnCount: 2,
    rowTemplate: new Ext.XTemplate(
        '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '">' +
            '<th class="fit-width top right leftmost">{key}</th>' +
            '<td id="{id}" class="rightmost {cls}">{value}</td>' +
        '</tr>'
    ),

    initComponent: function()
    {
        var rowConfigs = this.rows;
        this.rows = [];
        for (var i = 0; i < rowConfigs.length; i++)
        {
            var keyValue = new Zutubi.KeyValue(rowConfigs[i]);
            keyValue.component = this;
            this.rows.push(keyValue);
        }

        Zutubi.table.PropertyTable.superclass.initComponent.apply(this, arguments);
    },
     
    renderData: function()
    {
        var rows = this.rows;
        var previousRow = this.el.child('tr');
        for (var i = 0, l = rows.length; i < l; i++)
        {
            var row = rows[i];
            var args = {
                id: this.id + '-' + row.name,
                cls: row.cls || '',
                key: row.key,
                value: row.getRenderedValue(this.data)
            };
            previousRow = this.rowTemplate.insertAfter(previousRow, args, true);
        }
    }
});

Ext.reg('xzpropertytable', Zutubi.table.PropertyTable);
