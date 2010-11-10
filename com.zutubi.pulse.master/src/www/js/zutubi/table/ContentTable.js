// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * Base class for tables that contain dynamic content.  Applies consistent styling for the title and
 * borders.
 *
 * @cfg {String}  cls          Class to use for the table (defaults to 'content-table')
 * @cfg {String}  id           Id to use for the table.
 * @cfg {Mixed}   data         Data used to populate the table.
 * @cfg {String}  title        Title for the table heading row.
 * @cfg {String}  emptyMessage Message to show when the table has no rows to display (if not
 *                             specified, the table is hidden in this case).
 * @cfg {Boolean} customisable If true, the table will be customisable.  An icon will be shown in
 *                             the header which, when clicked, will call the customise() method.
 *                             Subclasses should override this method to allow customisation, and
 *                             call customiseComplete when done.  During customisation updates to
 *                             data are held aside, and applied on completion.
 */
Zutubi.table.ContentTable = Ext.extend(Ext.BoxComponent, {
    cls: 'content-table',
    columnCount: 1,
    customisable: false,
    customising: false,
    
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
            this.template = new Ext.XTemplate(
                '<table id="{id}" class="{cls}">' +
                    '<tr>' +
                        '<th class="heading" colspan="{columnCount}">' +
                            '<tpl if="customisable">' +
                                '<span style="float: right">' +
                                    '<a href="#" class="unadorned" onclick="Ext.getCmp(\'{id}\').customise(); return false">' +
                                        '<img ext:qtip="customise this table" alt="customise" src="{[window.baseUrl]}/images/pencil.gif"/>' +
                                    '</a>' +
                                '</span>' +
                                '<span class="clear"/>' +
                            '</tpl>' +
                            '<span id="{id}-title">{title}</span>' +
                        '</th>' +
                    '</tr>' +
                '</table>'
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

        this.tbodyEl = this.el.down('tbody');
        
        this.renderFixed();
        this.renderDynamic();
        
        Zutubi.table.ContentTable.superclass.onRender.apply(this, arguments);        
    },

    /**
     * Called on a customisable table when customising begins.  Subclasses
     * should override onCustomise to take necessary action at this point.
     */
    customise: function() {
        this.customising = true;
        this.onCustomise();
    },

    /**
     * Called to handle the start of a customisation operation.  This default
     * does nothing, and is intended to be overridden.
     */
    onCustomise: function() {
    },

    /**
     * Called on a customisable table when customising ends.  Subclasses should
     * override but onCustomiseComplete to take necessary action at this point.
     */
    customiseComplete: function() {
        this.onCustomiseComplete();
        this.customising = false;
        if (this.heldData)
        {
            this.update(this.heldData);
        }
    },

    /**
     * Called to handle the end of a customisation operation.  This default
     * does nothing, and is intended to be overridden.
     */
    onCustomiseComplete: function() {
    },

    /**
     * Updates this table with new data.
     */
    update: function(data) {
        if (this.customising)
        {
            this.heldData = data;
        }
        else
        {
            this.heldData = null;
            this.data = data;
            if (this.rendered)
            {
                this.renderDynamic();
            }
        }
    },

    /**
     * Renders the dynamic parts of the table, i.e. those that depend on the data.  All rows
     * rendered by this method should be tagged with the Zutubi.table.CLASS_DYNAMIC class.
     */
    renderDynamic: function() {
        this.clearDynamic();
        
        if (this.dataExists())
        {
            this.renderData();
            this.el.setDisplayed(true);
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
     * Indicates if there is data to fill the table.  This base implementation merely checks if any
     * data is defined.  Subclasses may override with more specific existence tests.
     */
    dataExists: function()
    {
        return this.data !== undefined && this.data !== null;
    },
    
    /**
     * Indicates how many dynamic rows are shown in this table.
     */
    getDynamicCount: function() {
        return this.tbodyEl.select('.' + Zutubi.table.CLASS_DYNAMIC).getCount();
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
    },
    
    /**
     * Updates the title of this table.
     *
     * @param title HTML fragment to use as the new title.
     */
    setTitle: function(title) {
        this.title = title;
        if (this.rendered)
        {
            Ext.get(this.id + '-title').update(title);
        }
    }
});
