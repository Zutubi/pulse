// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A customised table layout that acts like a horizontal box (i.e. single row).  Unlike
 * Ext.layout.HBoxLayout, the table will not shrink its contents if they are too big to fit in the
 * container.
 */
Zutubi.layout.HorizontalTableLayout = Ext.extend(Ext.layout.TableLayout, {
    width: '100%',
    spacing: 17,
    
    onLayout: function(container, target)
    {
        this.columns = container.items.length;
        
        if (!this.tableAttrs)
        {
            this.tableAttrs = {style: {}};
        }

        Ext.apply(this.tableAttrs.style, {
            'width': this.width,
            'border-collapse': 'separate',
            'border-spacing': '' + this.spacing + 'px 0'
        });
        
        Zutubi.layout.HorizontalTableLayout.superclass.onLayout.apply(this, arguments);        
    }
});

Ext.Container.LAYOUTS['htable'] = Zutubi.layout.HorizontalTableLayout;
