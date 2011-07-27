// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A customised table layout that acts like a vertical box (i.e. single column).  Unlike
 * Ext.layout.VBoxLayout, the table will not shrink its contents if they are too big to fit in the
 * container.
 */
Zutubi.layout.VerticalTableLayout = Ext.extend(Ext.layout.TableLayout, {
    width: '100%',
    spacing: 17,
    
    onLayout: function(container, target)
    {
        this.columns = 1;

        if (!this.tableAttrs)
        {
            this.tableAttrs = {style: {}};
        }

        Ext.apply(this.tableAttrs.style, {
            'width': this.width,
            'border-collapse': 'separate',
            'border-spacing': '0 ' + this.spacing + 'px'
        });
        
        Zutubi.layout.VerticalTableLayout.superclass.onLayout.apply(this, arguments);        
    },
    
    /**
     * Checks for rows that include no displayed child DOM nodes and hides any
     * that are found (otherwise spacing would still exist for rows with no
     * content).
     */
    checkRows: function()
    {
        if (this.table)
        {
            var rows = this.table.tBodies[0].childNodes;
            var i, j, l, m;
            for (i = 0, l = rows.length; i < l; i++)
            {
                var display = false;
                var row = rows[i];
                var cells = row.childNodes;
                for (j = 0, m = cells.length; j < m; j++)
                {
                    var cell = cells[j];
                    if (this.hasDisplayedChild(cell))
                    {
                        display = true;
                        break;
                    }
                }
                
                row.style.display = display ? '' : 'none';
            }
        }
    },
    
    hasDisplayedChild: function(node)
    {
        var children = node.childNodes;
        var i, l;
        for (i = 0, l = children.length; i < l; i++)
        {
            var d = children[i].style.display;
            if (d != 'none')
            {
                return true;
            }
        }
        
        return false;
    }
});

Ext.Container.LAYOUTS['vtable'] = Zutubi.layout.VerticalTableLayout;