// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * Base class for tables that contain dynamic content.  Applies consistent styling for the title and
 * borders.
 *
 * @cfg {String} cls          Class to use for the table (defaults to 'content-table')
 * @cfg {String} id           Id to use for the table.
 * @cfg {Mixed}  data         Data used to populate the table.
 * @cfg {String} title        Title for the table heading row.
 * @cfg {String} emptyMessage Message to show when the table has no rows to display (if not
 *                            specified, the table is hidden in this case).
 */
Zutubi.table.ContentTable = Ext.extend(Ext.BoxComponent, {
    cls: 'content-table',
    columnCount: 1,
    emptyTemplate: new Ext.XTemplate(
        '<tr>' +
            '<td colspan="{columnCount}" class="understated leftmost rightmost ' + Zutubi.table.CLASS_DYNAMIC + '">' +
                '{emptyMessage:htmlEncode}' +
            '</td>' +
        '</tr>'
    ),

    onRender: function(container, position) {
        if (!this.template)
        {
            this.template = new Ext.Template('<table id="{id}" class="{cls}"><tr><th class="heading" colspan="{columnCount}">{title}</th></tr></table>');
        }

        if (position)
        {
            this.el = this.template.insertBefore(position, this, true);    
        }
        else
        {
            this.el = this.template.append(container, this, true);
        }

        this.tbodyEl = this.el.down('tbody');
        
        this.renderFixed();
        this.renderDynamic();
        
        Zutubi.table.ContentTable.superclass.onRender.apply(this, arguments);        
    },

    /**
     * Updates this table with new data.
     */
    update: function(data) {
        this.data = data;
        this.renderDynamic();
    },

    /**
     * Renders the dynamic parts of the table, i.e. those that depend on the data.  All rows
     * rendered by this method should be tagged with the Zutubi.table.CLASS_DYNAMIC class.
     */
    renderDynamic: function() {
        this.clearDynamic();
        
        if (this.data)
        {
            this.renderData();
        }
        else
        {
            if (this.emptyMessage)
            {
                this.renderEmptyMessage();
            }
            else
            {
                this.el.setDisplayed(false);
            }
        }
    },
    
    /**
     * Clears any rows added by the renderDynamic function.  This includes data rows and empty
     * message rows.  Rows are identified using the Zutubi.table.CLASS_DYNAMIC class.
     */
     clearDynamic: function() {
         var els = this.tbodyEl.select('.' + Zutubi.table.CLASS_DYNAMIC);
         els.remove();
     },
     
    /**
     * Renders the fixed parts of the table, i.e. those that are independent of the data.  For
     * example, fixed headers may be displayed.
     *
     * This default implementation does nothing.  It may be overridden as required.
     */
    renderFixed: function() {
    },

    /**
     * Renders rows to show the data.
     *
     * This default implementation does nothing.  It may be overridden as required.
     */
    renderData: function() {
    },
    
    /**
     * Renders a row with a message indicating the data for this table is empty.
     */
    renderEmptyMessage: function() {
        this.emptyTemplate.append(this.tbodyEl, this, false);
    }
});
