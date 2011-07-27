// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/ContentTable.js
// dependency: zutubi/KeyValue.js
// dependency: zutubi/pulse/project/namespace.js

/**
 * A table that shows a set of properties related to one "entity".  The property names
 * are listed in a left column, and the values in the right one.
 *
 * @cfg {String} id           Id to use for the table.
 * @cfg {Object} data         Data object used to populate the table.
 * @cfg {String} title        Title for the table heading row.
 * @cfg {String} emptyMessage Message to show when the table has no rows to display (if not
 *                            specified, the table is hidden in this case).
 */
Zutubi.pulse.server.ActiveBuildsTable = Ext.extend(Zutubi.table.ContentTable, {
    columnCount: 4,
    
    template: new Ext.XTemplate(
        '<table id="{id}" class="{cls}">' +
            '<tr>' +
                '<th class="heading" colspan="{columnCount}">' +
                    '<span style="float: right">' +
                        '<a href="#" class="unadorned" onclick="Ext.getCmp(\'{id}\').expandAll(); return false">' +
                            '<img ext:qtip="expand all builds" alt="expand all" src="{[window.baseUrl]}/images/expand.gif"/>' +
                        '</a> ' +
                        '<a href="#" class="unadorned" onclick="Ext.getCmp(\'{id}\').collapseAll(); return false">' +
                            '<img ext:qtip="collapse all builds" alt="collapse all" src="{[window.baseUrl]}/images/collapse.gif"/>' +
                        '</a>' +
                    '</span>' +
                    '<span class="clear"/>' +
                    '<span id="{id}-title">{title}</span>' +
                '</th>' +
            '</tr>' +
        '</table>'
    ),

    initComponent: function()
    {
        this.builds = [];
        Zutubi.pulse.server.ActiveBuildsTable.superclass.initComponent.apply(this, arguments);
    },
    
    dataExists: function()
    {
        return Zutubi.table.SummaryTable.superclass.dataExists.apply(this, arguments) && this.data.length > 0;
    },

    renderData: function()
    {
       var builds = [];
       var i, len;
       for (i = 0, len = this.data.length; i < len; i++)
       {
           var item = this.data[i];
           if (item.hidden)
           {
               this.tbodyEl.createChild('<td class="leftmost rightmost understated" colspan="4">you do not have permission to view this build</td>');
           }
           else
           {
               var previousBuild = this.findBuild(item.id); 
               var build = new Zutubi.pulse.server.ActiveBuild(item);
               build.appendRows(this.tbodyEl, previousBuild ? previousBuild.collapsed : true);
               builds.push(build);
           }
       }
       
       this.builds = builds;
    },
    
    findBuild: function(id)
    {
        var i, len;
        for (i = 0, len = this.builds.length; i < len; i++)
        {
            var build = this.builds[i];
            if (build.data.id == id)
            {
                return build;
            }
        }
        
        return null;
    },
    
    expandAll: function()
    {
        var i, len;
        for (i = 0, len = this.builds.length; i < len; i++)
        {
            this.builds[i].expand();
        }
    },
    
    collapseAll: function()
    {
        var i, len;
        for (i = 0, len = this.builds.length; i < len; i++)
        {
            this.builds[i].collapse();
        }
    }
});

Zutubi.pulse.server.ActiveBuild = function(data)
{
    this.data = data;
    this.childRows = [];
};

Ext.apply(Zutubi.pulse.server.ActiveBuild.prototype, {
    collapsed: true,
    
    buildTemplate: new Ext.XTemplate(
        '<tr class="' + Zutubi.table.CLASS_DYNAMIC + ' project-expandable {cls}">' +
            '<td class="leftmost"><img src="{base}/images/default/s.gif" alt="-" class="group-name"/> {name}</td>' +
            '<td class="nowrap">{status}</td>' +
            '<td>{details}</td>' +
            '<td class="rightmost">{actions}</td>'+
        '</tr>'
    ),

    stageTemplate: new Ext.XTemplate(
        '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '" style="display: {display}">' +
            '<td class="leftmost">{indent} {name}</td>' +
            '<td class="nowrap">{indent} {status}</td>' +
            '<td>{indent} {details}</td>' +
            '<td class="rightmost">{indent} {actions}</td>' +
        '</tr>'
    ),
    
    appendRows: function(tbodyEl, collapsed)
    {
        this.collapsed = collapsed;
        
        var renderers = Zutubi.pulse.project.renderers;
        var data = this.data;
        var actions;
        var i;
        if (data.cancelPermitted)
        {
            var link = ' href="#" onclick="cancelBuild(' + data.id + ', false); return false"';
            actions = '<a class="unadorned" id="cancel-' + data.id + '-image-button"' + link + '><img alt="cancel" src="' + window.baseUrl + '/images/cancel.gif"/></a> ' +
                      '<a id="cancel-' + data.id + '-button"' + link + '>cancel</a>';
        }
        else
        {
            actions = '&nbsp;';
        }
            
        var args = {
            base: window.baseUrl,
            cls: collapsed ? 'project-collapsed' : '',
            name: renderers.buildOwner(data.owner, data) + ' :: ' +  renderers.buildId(data.number, data),
            status: renderers.resultStatus(data.status, data),
            details: 'revision: ' + renderers.buildRevision(data.revision, data) + ' <span class="understated">//</span> reason: ' + Ext.util.Format.htmlEncode(data.reason),
            actions: actions
        };
       
        this.el = this.buildTemplate.append(tbodyEl, args, true);
        this.el.addClassOnOver('project-highlighted');
        this.el.on('click', this.onClick, this);
       
        // Reverse the stages so that the stage queue is visible in order (pending
        // stages form the queue, with the head at the bottom of the page).
        for (i = data.stages.length - 1; i >= 0; i--)
        {
            var stage = data.stages[i];
            args = {
                base: window.baseUrl,
                display: collapsed ? 'none' : '',
                indent: '<img src="' + window.baseUrl + '/images/default/s.gif" style="width: 18px; height: 12px" alt="-" class="group-name"/>',
                name: renderers.stageName(stage.name, stage),
                status: renderers.resultStatus(stage.status, stage),
                details: 'recipe: ' + renderers.stageRecipe(stage.recipe) + (stage.agent ? ' <span class="understated">//</span> agent: ' + renderers.stageAgent(stage.agent) : ''),
                actions: renderers.stageLogs(null, stage)
            };
           
            this.childRows.push(this.stageTemplate.append(tbodyEl, args, true));
        }
    },
    
    onClick: function(event, el)
    {
        if (el.id.indexOf('-button') > 0)
        {
            // Click is on a menu/cancel button.
            return;
        }
        
        if (this.collapsed)
        {
            this.expand();
        }
        else
        {
            this.collapse();
        }
    },
    
    expand: function()
    {
        if (this.collapsed)
        {
            this.setChildDisplays('');
            this.el.removeClass('project-collapsed');
            this.collapsed = false;
        }
    },
    
    collapse: function()
    {
        if (!this.collapsed)
        {
            this.setChildDisplays('none');
            this.el.addClass('project-collapsed');
            this.collapsed = true;
        }
    },
    
    setChildDisplays: function(value)
    {
        var i, len;
        for (i = 0, len = this.childRows.length; i < len; i++)
        {
            this.childRows[i].setStyle('display', value);
        }
    }
});

Ext.reg('xzactivebuildstable', Zutubi.pulse.server.ActiveBuildsTable);
