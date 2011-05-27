// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/package.js

/**
 * A summary table that displays agents.
 *
 * @cfg {String}  id    Id to use for the table.
 * @cfg {Array}   data  An array of AgentRowModels.
 */
Zutubi.pulse.agent.AgentSummaryTable = Ext.extend(Zutubi.table.SummaryTable, {
    initComponent: function()
    {
        Ext.applyIf(this, {
            title: 'agents',
            emptyMessage: 'no agents found',
            columns: [
                Zutubi.pulse.agent.configs.name,
                Zutubi.pulse.agent.configs.location,
                Zutubi.pulse.agent.configs.status,
                Zutubi.pulse.agent.configs.executingStage
            ]
        });

        Zutubi.pulse.agent.AgentSummaryTable.superclass.initComponent.apply(this, arguments);
    }
});

Ext.reg('xzagentsummarytable', Zutubi.pulse.agent.AgentSummaryTable);
