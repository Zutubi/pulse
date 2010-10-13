// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A customised table layout that acts like a horizontal box (i.e. single row).  Unlike
 * Ext.layout.HBoxLayout, the table will not shrink its contents if they are too big to fit in the
 * container.
 */
Zutubi.layout.HorizontalTableLayout = Ext.extend(Ext.layout.TableLayout, {
    defaultTableAttrs: {
        style: {
            'width': '100%',
            'border-collapse': 'separate',
            'border-spacing': '15px 0'
        }
    },
    
    onLayout: function(container, target) {
        this.columns = container.items.length;
        if (!this.tableAttrs)
        {
            this.tableAttrs = this.defaultTableAttrs;
        }
        else
        {
            Ext.apply(this.tableAttrs, this.defaultTableAttrs);
        }
        
        Zutubi.layout.HorizontalTableLayout.superclass.onLayout.apply(this, arguments);        
    }
});

Ext.Container.LAYOUTS['htable'] = Zutubi.layout.HorizontalTableLayout;
