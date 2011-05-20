// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A graph displayed using an image map.
 *
 * @cfg {String} text Heading text.
 */
Zutubi.pulse.Graph = Ext.extend(Ext.BoxComponent, {
    initComponent: function()
    {
        Ext.applyIf(this, {
            template: new Ext.XTemplate(
                '{imageMap}' +
                '<img style="border:none" src="{[window.baseUrl]}/chart?filename={location}" ' +
                'border="1" height="{height}" width="{width}" usemap="{imageMapName}"/>'
            )
        });
        
        Zutubi.pulse.Graph.superclass.initComponent.apply(this, arguments);
    },
    
    onRender: function(container, position)
    {
        this.el = container.createChild({tag: 'div', id: this.id});
        this.renderGraph();
        Zutubi.pulse.Graph.superclass.onRender.apply(this, arguments);
    },
    
    renderGraph: function()
    {
        if (this.data)
        {
            this.template.overwrite(this.el, this.data);
            this.el.setDisplayed(true);            
        }
        else
        {
            this.el.setDisplayed(false);
        }    
    },
    
    update: function(data)
    {
        this.data = data;
        if (this.rendered)
        {
            this.renderGraph();
        }
    }
});

Ext.reg('xzgraph', Zutubi.pulse.Graph);
