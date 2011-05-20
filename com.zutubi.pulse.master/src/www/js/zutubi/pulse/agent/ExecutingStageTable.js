// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/package.js
// dependency: zutubi/pulse/project/namespace.js

/**
 * A property table that displays a build stage executing on an agent.
 *
 * @cfg {String}  id              Id to use for the table.
 * @cfg {Array}   data            An ExecutingStageModel.
 * @cfg {String}  title           Title for the table heading row.
 * @cfg {String}  emptyMessage    Message to show when the table has no rows to display (if not
 *                                specified, the table is hidden in this case).
 */
Zutubi.pulse.agent.ExecutingStageTable = Ext.extend(Zutubi.table.PropertyTable, {
    initComponent: function()
    {
        Ext.applyIf(this, {
            rows: [
                Zutubi.pulse.project.configs.build.numberLeft,
                Zutubi.pulse.project.configs.build.project,
                Zutubi.pulse.project.configs.build.owner,
                Zutubi.pulse.project.configs.stage.name,
                Zutubi.pulse.project.configs.stage.recipe,
                Zutubi.pulse.project.configs.result.when,
                Zutubi.pulse.project.configs.result.elapsed,
                Zutubi.pulse.project.configs.stage.logs
            ]
        });

        Zutubi.pulse.agent.ExecutingStageTable.superclass.initComponent.apply(this, arguments);
    }
});

Ext.reg('xzexecutingstagetable', Zutubi.pulse.agent.ExecutingStageTable);
