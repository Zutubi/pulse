// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.toolbar.ToolbarLayout = Ext.extend(Ext.layout.ToolbarLayout, {
    addComponentToMenu : function(m, c)
    {
        if (c instanceof Zutubi.toolbar.LinkItem)
        {
            m.add(this.createMenuConfig(c, true));
        }
        else
        {
            Zutubi.toolbar.ToolbarLayout.superclass.addComponentToMenu.call(this, m, c);
        }
    }
});
Ext.Container.LAYOUTS.xztoolbar = Zutubi.toolbar.ToolbarLayout;
