// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/table/package.js
// dependency: zutubi/TextBox.js
// dependency: zutubi/pulse/Graph.js

/**
 * The content of the agent statistics tab.  Expects data of the form:
 *
 * {
 *     blurb: String,
 *     recipes: RecipeStatisticsPanel,
 *     utilisation: UtilisationModel,
 *     usageGraph: GraphModel,
 * }
 */
Zutubi.pulse.agent.AgentStatisticsPanel = Ext.extend(Zutubi.ActivePanel, {
    border: false,
    autoScroll: true,
    
    dataKeys: ['blurb', 'recipes', 'utilisation', 'usageGraph'],
    
    initComponent: function(container, position)
    {
        var panel = this;
        Ext.apply(this, {
            items: [{
                id: this.id + '-inner',
                xtype: 'container',
                layout: 'htable',
                contentEl: 'center',
                items: [{
                    id: this.id + '-left',
                    xtype: 'container',
                    layout: 'vtable',
                    items: [{
                      id: this.id + '-blurb',
                      xtype: 'xztextbox'
                    }, {
                        id: this.id + '-recipes',
                        xtype: 'xzpropertytable',
                        title: 'recipes executed',
                        rows: ['totalRecipes', 'averageRecipesPerDay', 'averageBusyTimePerRecipe']
                    }, {
                        id: this.id + '-utilisation',
                        xtype: 'xzpropertytable',
                        title: 'utilisation statistics',
                        rows: ['timeDisabled', 'timeOffline', 'timeSynchronising', 'timeIdle', 'timeBusy']
                    }]
                }, {
                    id: this.id + '-usageGraph',
                    xtype: 'xzgraph',
                    style: 'margin: 24px',
                }]
            }]
        });

        Zutubi.pulse.agent.AgentStatisticsPanel.superclass.initComponent.apply(this, arguments);
    }
});
