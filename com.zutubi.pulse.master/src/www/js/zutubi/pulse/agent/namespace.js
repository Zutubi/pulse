// dependency: zutubi/pulse/namespace.js
// dependency: zutubi/pulse/project/namespace.js

window.Zutubi.pulse.agent = window.Zutubi.pulse.agent || {
    renderers: {
        AGENT_TEMPLATE: new Ext.XTemplate(
            '<a href="{link}">{name:htmlEncode}</a>&nbsp;' +
            '<a class="unadorned" id="aactions-{id}-link" onclick="Zutubi.MenuManager.toggleMenu(this); return false">' +
                '<img src="{[window.baseUrl]}/images/default/s.gif" class="popdown floating-widget" id="aactions-{id}-button" alt="agent menu"/>' +
            '</a>'
        ),

        getAgentMenuItems: function(agent) {
            var result = [];
            var actionsUrl = 'agents/' + encodeURIComponent(agent.name) + '/actions/';
            for (var i = 0, len = agent.actions.length; i < len; i++)
            {
                var action = agent.actions[i];
                result.push({
                    id: action.action,
                    title: action.label,
                    image: 'config/actions/' + action.icon + '.gif',
                    url: actionsUrl + action.action + '/'
                });
            }
            
            return result;
        },
        
        agent: function(name, agent) {
            Zutubi.MenuManager.registerMenu('aactions-' + agent.id, Zutubi.pulse.agent.renderers.getAgentMenuItems.createDelegate(this, [agent]));
            return Zutubi.pulse.agent.renderers.AGENT_TEMPLATE.apply({
                name: name,
                id: agent.id,
                link: window.baseUrl + '/agents/' + encodeURIComponent(name)
            });
        },

        agentStatus: function(status, agent) {
            return status;
        },
        
        agentExecutingStage: function(stage, agent) {
            if (stage)
            {
               return Zutubi.pulse.project.renderers.buildOwner(stage.owner, stage) + ' :: ' +
                      Zutubi.pulse.project.renderers.buildId(stage.number, stage) + ' :: ' +
                      Zutubi.pulse.project.renderers.stageName(stage.name, stage);
            }
            else
            {
                return 'none';
            }
        }
    }
};

Ext.apply(Zutubi.pulse.agent, {
    /**
     * A collection of KeyValue configurations that can be used to build tables for displaying
     * agents.
     */
    configs: {
        name: {
            name: 'name',
            renderer: Zutubi.pulse.agent.renderers.agent
        },
        
        location: {
            name: 'location',
            renderer: Ext.util.Format.htmlEncode
        },
        
        status: {
            name: 'status',
            renderer: Zutubi.pulse.agent.renderers.agentStatus
        },
        
        executingStage: {
            name: 'executingStage',
            renderer: Zutubi.pulse.agent.renderers.agentExecutingStage
        }
    }
});
