// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A customised table layout that acts like a vertical box (i.e. single column).  Unlike
 * Ext.layout.VBoxLayout, the table will not shrink its contents if they are too big to fit in the
 * container.
 */
Zutubi.layout.VerticalTableLayout = Ext.extend(Ext.layout.TableLayout, {
    defaultTableAttrs: {
        style: {
            'width': '100%',
            'border-collapse': 'separate',
            'border-spacing': '0 15px'
        }    
    },
    
    onLayout: function(container, target) {
        this.columns = 1;
        if (!this.tableAttrs)
        {
            this.tableAttrs = this.defaultTableAttrs;
        }
        else
        {
            Ext.apply(this.tableAttrs, this.defaultTableAttrs);
        }
        
        Zutubi.layout.VerticalTableLayout.superclass.onLayout.apply(this, arguments);        
    }
});

Ext.Container.LAYOUTS['vtable'] = Zutubi.layout.VerticalTableLayout;