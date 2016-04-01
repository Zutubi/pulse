// dependency: zutubi/pulse/namespace.js
// dependency: zutubi/pulse/project/namespace.js

window.Zutubi.pulse.agent = window.Zutubi.pulse.agent || {
    renderers: {
        AGENT_TEMPLATE: new Ext.XTemplate(
            '<a href="{link}">{name:htmlEncode}</a>&nbsp;' +
            '<tpl if="commentsTip"><img ext:qtip="{commentsTip}" src="{[window.baseUrl]}/images/comment.gif"/>&nbsp;</tpl>' +
            '<a class="unadorned" id="aactions-{id}-link" onclick="Zutubi.MenuManager.toggleMenu(this); return false">' +
                '<img src="{[window.baseUrl]}/images/default/s.gif" class="popdown floating-widget" id="aactions-{id}-button" alt="agent menu"/>' +
            '</a>'
        ),

        getAgentTabMenuItem: function(agentUrl, tab, image)
        {
            return {
                id: tab,
                title: tab,
                image: image,
                url: agentUrl + tab + '/'
            };
        },
        
        getAgentMenuItems: function(agent) {
            var encodedName, agentUrl, actionsUrl, result, i, len, action;

            encodedName = encodeURIComponent(agent.name);
            agentUrl = 'agents/' + encodedName + '/';
            actionsUrl = agentUrl + 'actions/';
            result = [
                Zutubi.pulse.agent.renderers.getAgentTabMenuItem(agentUrl, 'status', 'magnifier.gif'),
                Zutubi.pulse.agent.renderers.getAgentTabMenuItem(agentUrl, 'statistics', 'chart_bar.gif'),
                Zutubi.pulse.agent.renderers.getAgentTabMenuItem(agentUrl, 'history', 'time.gif'),
                Zutubi.pulse.agent.renderers.getAgentTabMenuItem(agentUrl, 'info', 'information.gif')
            ];

            if (agent.canViewMessages)
            {
                result.push(Zutubi.pulse.agent.renderers.getAgentTabMenuItem(agentUrl, 'messages', 'script.gif'));
            }

            for (i = 0, len = agent.actions.length; i < len; i++)
            {
                action = agent.actions[i];
                result.push({
                    id: action.action,
                    title: action.label,
                    image: 'config/actions/' + action.icon + '.gif',
                    onclick: 'agentAction(\'' + agent.id + '\', \'' + action.action + '\'); return false'
                });
            }
            
            result.push({
                id: 'configuration',
                url: 'admin/agents/' + encodedName + '/',
                image: 'pencil.gif'
            });
            
            return result;
        },
        
        agent: function(name, agent) {
            Zutubi.MenuManager.registerMenu('aactions-' + agent.id, Zutubi.pulse.agent.renderers.getAgentMenuItems.createDelegate(this, [agent]));
            return Zutubi.pulse.agent.renderers.AGENT_TEMPLATE.apply({
                name: name,
                id: agent.id,
                link: window.baseUrl + '/agents/' + encodeURIComponent(name),
                commentsTip: getCommentsTooltip(agent)
            });
        },

        STATUS_TEMPLATE: new Ext.XTemplate('<img src="{[window.baseUrl]}/images/agent/{statusType}.gif" alt="{status}"/> {status}'),

        agentStatus: function(status, agent) {
            var statusType;

            if (status === 'low disk space' ||
                status === 'synchronising' ||
                status === 'synchronised' ||
                status.indexOf('host upgrading') >= 0)
            {
                statusType = 'upgrading';
            }
            else if (status === 'offline' ||
                     status.indexOf('upgrade failed') >= 0 ||
                     status.indexOf('mismatch') >= 0 ||
                     status.indexOf('invalid') >= 0)
            {
                statusType = 'offline';
            }
            else if (status === 'disabled')
            {
                statusType = 'disabled';
            }
            else if (status === 'idle')
            {
                statusType = 'idle';
            }
            else
            {
                statusType = 'building';
            }
             
            return Zutubi.pulse.agent.renderers.STATUS_TEMPLATE.apply({status: status, statusType: statusType});
        },
        
        agentExecutingStage: function(stage, agent) {
            if (stage)
            {
               return Zutubi.pulse.project.renderers.buildOwner(stage.owner, stage) + ' :: ' +
                      Zutubi.pulse.project.renderers.buildId(stage.number, stage) + ' :: ' +
                      Zutubi.pulse.project.renderers.stageName(stage.name, stage);
            }
            else if (agent.executing)
            {
                return '<span class="understated">no permission to view stage</span>';
            }
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
