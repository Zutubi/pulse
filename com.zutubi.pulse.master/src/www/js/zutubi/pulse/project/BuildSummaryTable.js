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
    initComponent: function()
    {
        Ext.applyIf(this, {
            customisable: true,
            columns: [
                Zutubi.pulse.project.configs.result.status,
                Zutubi.pulse.project.configs.result.errors,
                Zutubi.pulse.project.configs.result.warnings,
                Zutubi.pulse.project.configs.result.when,
                Zutubi.pulse.project.configs.result.completed,
                Zutubi.pulse.project.configs.result.elapsed,
                Zutubi.pulse.project.configs.build.number,
                Zutubi.pulse.project.configs.build.project,
                Zutubi.pulse.project.configs.build.owner,
                Zutubi.pulse.project.configs.build.reason,
                Zutubi.pulse.project.configs.build.revision,
                Zutubi.pulse.project.configs.build.tests,
                Zutubi.pulse.project.configs.build.maturity,
                Zutubi.pulse.project.configs.build.version,
                Zutubi.pulse.project.configs.build.pinned
            ],
            saveUrl: window.baseUrl + '/ajax/customiseBuildColumns.action',
            saveParams: {
                tableId: this.id
            }
        });

        Zutubi.pulse.project.BuildSummaryTable.superclass.initComponent.apply(this, arguments);
    }
});

Ext.reg('xzbuildsummarytable', Zutubi.pulse.project.BuildSummaryTable);
