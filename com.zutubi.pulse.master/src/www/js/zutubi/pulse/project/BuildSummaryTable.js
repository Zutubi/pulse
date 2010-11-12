// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/package.js

/**
 * A summary table that displays build results.
 *
 * @cfg {Array}   selectedColumns An array of names of columns that should be displayed.
 * @cfg {String}  id              Id to use for the table.
 * @cfg {Array}   data            An array of BuildModels.
 * @cfg {String}  title           Title for the table heading row.
 * @cfg {String}  emptyMessage    Message to show when the table has no rows to display (if not
 *                                specified, the table is hidden in this case).
 */
Zutubi.pulse.project.BuildSummaryTable = Ext.extend(Zutubi.table.SummaryTable, {
    initComponent: function() {
        var columns = [];
        for (var key in Zutubi.pulse.project.configs.result)
        {
            columns.push(Zutubi.pulse.project.configs.result[key]);
        }
        
        for (var key in Zutubi.pulse.project.configs.build)
        {
            columns.push(Zutubi.pulse.project.configs.build[key]);
        }
        
        Ext.applyIf(this, {
            customisable: true,
            columns: columns,
            saveUrl: window.baseUrl + '/ajax/customiseBuildColumns.action',
            saveParams: {
                tableId: this.id
            }
        });

        Zutubi.pulse.project.BuildSummaryTable.superclass.initComponent.apply(this, arguments);
    }
});

Ext.reg('xzbuildsummarytable', Zutubi.pulse.project.BuildSummaryTable);
