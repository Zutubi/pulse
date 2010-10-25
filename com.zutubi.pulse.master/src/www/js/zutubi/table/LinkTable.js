// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js

/**
 * A table that shows a collection of links, one per row, with an icon and link text.
 * Links may trigger client-side (i.e. JavaScript) actions, or may navigate to other
 * pages.
 *
 * @cfg {String} cls          Class to use for the table (defaults to 'content-table')
 * @cfg {Object} handlers     Mapping of link actions to either URLs (strings) or callback
 *                            functions.  If not specified, the actions themselves will be used as
 *                            URLs.
 * @cfg {String} iconTemplate Template used to turn icon names into img src attributes.  Need not
 *                            include the base url.  Defaults to a template that uses config action
 *                            icons.
 * @cfg {String} id           Id to use for the table.
 * @cfg {Array}  data         An array of instances to populate the table.  Each instance should
 *                            contain the properties icon, label and action.
 * @cfg {String} title        Title for the table heading row.
 * @cfg {String} emptyMessage Message to show when the table has no rows to display (if not
 *                            specified, the table is hidden in this case).
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
        
        this.rowTemplate = new Ext.XTemplate(
            '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '">' +
                '<td class="leftmost rightmost">' +
                    '<img alt="{label}" src="{iconSrc}"/> ' +
                    '<tpl if="client">' +
                        '<a href="#" id="{linkId}" onclick="{onclick}">{label}</a>' +
                    '</tpl>' +
                    '<tpl if="!client">' +
                        '<a href="{url}" id="{linkId}">{label}</a>' +
                    '</tpl>' +
                '</td>' + 
            '</tr>'
        );
        
        Zutubi.table.LinkTable.superclass.initComponent.apply(this, arguments);
    },

    dataExists: function() {
        return Zutubi.table.LinkTable.superclass.dataExists.apply(this, arguments) && this.data.length > 0;
    },

    renderData: function() {
        var previousRow = this.el.child('tr');
        for (var i = 0, l = this.data.length; i < l; i++)
        {
            var args = Ext.apply({}, this.data[i]);
            args.linkId = this.id + '-' + args.action;
            args.iconSrc = window.baseUrl + '/' + this.iconTemplate.apply(args);
            
            var action = args.action;
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
                args.url = handler;
                args.client = false;
            }
            else
            {
                args.onclick = 'Ext.getCmp(\'' + this.id + '\').doAction(\'' + action + '\'); return false';
                args.client = true;
            }
            
            previousRow = this.rowTemplate.insertAfter(previousRow, args, true);
        };
    },
    
    doAction: function(action) {
        this.handlers[action]();
    }
});

Ext.reg('xzlinktable', Zutubi.table.LinkTable);
