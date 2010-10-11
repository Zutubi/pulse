// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * Base class for tables that use applies consistent styling for the title and borders.
 *
 * @cfg {String}         cls      Class to use for the table (defaults to 'two-content')
 * @cfg {String}         id       Id to use for the table.
 * @cfg {Ext.data.Store} store    The store used to populate the table.
 * @cfg {String}         title    Title for the table heading row.
 */
Zutubi.table.ContentTable = Ext.extend(Ext.BoxComponent, {
    cls: 'two-content',
    columnCount: 1,

    onRender: function(ct, position) {
        if (!this.template)
        {
            this.template = new Ext.Template('<table id="{id}" class="{cls}"><tr><th class="two-heading" colspan="{columnCount}">{title}</th></tr></table>');
        }
        
        this.el = this.template.append(ct, this, true);
        this.renderRows();
    },
    
    renderRows: function() {
        // To be overridden by subclasses.
    }
});
