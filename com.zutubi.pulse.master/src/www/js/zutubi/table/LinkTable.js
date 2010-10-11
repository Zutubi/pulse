// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js

/**
 * A table that shows a collection of links, one per row, with an icon and link text.
 * Links may trigger client-side (i.e. JavaScript) actions, or may navigate to other
 * pages.
 *
 * @cfg {String}         cls          Class to use for the table (defaults to 'two-content')
 * @cfg {String}         handlers     Mapping of link actions to either URLs (strings) or callback
 *                                    functions.  If not specified, the actions themselves will be
 *                                    used as URLs.
 * @cfg {String}         iconTemplate Template used to turn icon names into img src attributes.
 *                                    Need not include the base url.  Defaults to a template that
 *                                    uses config action icons.
 * @cfg {String}         id           Id to use for the table.
 * @cfg {Ext.data.Store} store        The store used to populate the table.  Records in the store
 *                                    should contain the properties icon, label and action.
 * @cfg {String}         title        Title for the table heading row.
 */
Zutubi.table.LinkTable = Ext.extend(Zutubi.table.ContentTable, {
    initComponent: function() {
        this.columnCount = 1;
        if (this.iconTemplate)
        {
            this.iconTemplate = new Ext.XTemplate(this.iconTemplate);
        }
        else
        {
            this.iconTemplate = new Ext.XTemplate('images/config/actions/{icon}.gif');
        }
        
        this.rowTemplate = new Ext.XTemplate('<tr>' +
                '<td class="content leftmost rightmost">' +
                    '<img alt="{label}" src="{iconSrc}"/> ' +
                    '<tpl if="client">' +
                        '<a href="#" onclick="{onclick}">{label}</a>' +
                    '</tpl>' +
                    '<tpl if="!client">' +
                        '<a href="{url}">{label}</a>' +
                    '</tpl>' +
                '</td>' + 
            '</tr>'
        );
        
        Zutubi.table.LinkTable.superclass.initComponent.apply(this, arguments);
    },

    renderRows: function() {
        var previousRow = this.el.child('tr');
        this.store.each(function(r) {
            var args = Ext.apply({}, r.data);
            args['iconSrc'] = window.baseUrl + '/' + this.iconTemplate.apply(args);
            
            var action = args['action'];
            var handler;
            if (this.handlers)
            {
                handler = this.handlers[action];
            }
            else
            {
                handler = action;
            }
            
            if (typeof handler == 'string')
            {
                args['url'] = handler;
                args['client'] = false;
            }
            else
            {
                args['onclick'] = 'Ext.getCmp(\'' + this.id + '\').doAction(\'' + action + '\'); return false'; 
                args['client'] = true;
            }
            
            previousRow = this.rowTemplate.insertAfter(previousRow, args, true);
        }, this);
    },
    
    doAction: function(action) {
        this.handlers[action]();
    }
});
