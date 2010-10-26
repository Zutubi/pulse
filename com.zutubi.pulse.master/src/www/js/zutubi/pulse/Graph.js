// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A graph displayed using an image map.
 *
 * @cfg {String} text Heading text.
 */
Zutubi.pulse.Graph = Ext.extend(Ext.BoxComponent, {
    onRender: function(container, position) {
        if (!this.template)
        {
            this.template = new Ext.XTemplate(
                '{imageMap}' +
                '<img style="border:none" src="{[window.baseUrl]}/chart?filename={location}" ' +
                'border="1" height="{height}" width="{width}" usemap="{imageMapName}"/>'
            );
        }

        if (position)
        {
            this.el = this.template.insertBefore(position, this, true);    
        }
        else
        {
            this.el = this.template.append(container, this, true);
        }
        
        Zutubi.pulse.Graph.superclass.onRender.apply(this, arguments);
    }
});

Ext.reg('xzgraph', Zutubi.pulse.Graph);
