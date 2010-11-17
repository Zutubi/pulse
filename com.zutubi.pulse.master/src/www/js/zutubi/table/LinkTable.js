// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js

/**
 * A table that shows a collection of links, one per row, with an icon and link text.  Links may
 * trigger client-side (i.e. JavaScript) actions, or may navigate to other pages.
 *
 * For a simple link table, the data is an array of models containing an icon, label and action.
 * The models may optionally include an argument property, which is passed to the handler function:
 *
 * [{
 *      icon: 'lightning',
 *      label: 'trigger',
 *      action: 'triggerBuild',
 *      argument: false
 *  }, {
 *      icon: 'lightning',
 *      label: 'rebuild',
 *      action: 'triggerBuild',
 *      argument: true
 *  }, {
 *      icon: 'brush',
 *      label: 'clean up',
 *      action: 'cleanUp',
 *  }, ...
 * ]
 *
 * Links may optionally be divided into named categories.  In this case the data is an array of
 * objects that contain a name and an array of links in a property named by the linksProperty
 * config.  For example, if linksProperty was set to 'actions', two categories named 'c1' and 'c2'
 * may be represented like:
 *
 * [{
 *     name: 'c1',
 *     actions: [{icon: 'foo', label: 'foo it', action: 'dofoo'}, ...]
 *  }, {
 *     name: 'c2',
 *     actions: [{icon: 'bar', label: 'bar me', action: 'dobar'}, ...],
 * }]
 *
 * @cfg {String} cls            Class to use for the table (defaults to 'content-table')
 * @cfg {Object} handlers       Mapping of link actions to either URLs (strings) or callback
 *                              functions.  If not specified, the actions themselves will be used as
 *                              URLs.
 * @cfg {String} iconTemplate   Template used to turn icon names into img src attributes.  Need not
 *                              include the base url.  Defaults to a template that uses config
 *                              action icons.
 * @cfg {String} id             Id to use for the table.
 * @cfg {String} title          Title for the table heading row.
 * @cfg {String} idProperty     Which property of the links to use as the id suffix for the link
 *                              elements.  Defaults to 'icon'.
 * @cfg {String} categorised    If true, expect data split into named categories.  Each category is
 *                              separated by a row with the category prefix and name.
 * @cfg {String} categoryPrefix Optional prefix to prepend to category names.
 * @cfg {String} linksProperty  Name of the property that holds the links in a category model
 *                              (defaults to 'links').
 * @cfg {String} emptyMessage   Message to show when the table has no rows to display (if not
 *                              specified, the table is hidden in this case).
 */
Zutubi.table.LinkTable = Ext.extend(Zutubi.table.ContentTable, {
    initComponent: function()
    {
        this.columnCount = 1;
        
        if (this.iconTemplate)
        {
            this.iconTemplate = new Ext.XTemplate(this.iconTemplate);
        }
        else
        {
            this.iconTemplate = new Ext.XTemplate('images/config/actions/{icon}.gif');
        }
        
        Ext.applyIf(this, {
            idProperty: 'icon',
            categorised: false,
            categoryPrefix: '',
            linksProperty: 'links',
            
            categoryRowTemplate: new Ext.XTemplate(
                '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '">' +
                    '<th class="leftmost rightmost">' +
                        '{prefix}{name:htmlEncode}' +
                    '</th>' +
                '</tr>'
            ),
        
            rowTemplate: new Ext.XTemplate(
                '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '">' +
                    '<td class="leftmost rightmost">' +
                        '{indent}' +
                        '<img alt="{label}" src="{iconSrc}"/> ' +
                        '<tpl if="client">' +
                            '<a href="#" id="{linkId}" onclick="{onclick}">{label}</a>' +
                        '</tpl>' +
                        '<tpl if="!client">' +
                            '<a href="{url}" id="{linkId}">{label}</a>' +
                        '</tpl>' +
                    '</td>' + 
                '</tr>'
            )
        });
                
        Zutubi.table.LinkTable.superclass.initComponent.apply(this, arguments);
    },

    dataExists: function()
    {
        if (this.data && this.data.length)
        {
            if (this.categorised)
            {
                for (var i = 0, l = this.data.length; i < l; i++)
                {
                    var categoryLinks = this.data[i][this.linksProperty];
                    if (categoryLinks && categoryLinks.length > 0)
                    {
                        return true;
                    }
                }
                
                return false;
            }
            
            return true;
        }
        
        return false;
    },

    renderData: function()
    {
        if (this.categorised)
        {
            for (var i = 0, l = this.data.length; i < l; i++)
            {
                var category = this.data[i];
                var links = category[this.linksProperty];
                if (links && links.length > 0)
                {
                    this.categoryRowTemplate.append(this.tbodyEl, {
                        prefix: this.categoryPrefix,
                        name: category.name
                    });
                    
                    this.renderLinks(links, category.name);
                }
            }
        }
        else
        {
            this.renderLinks(this.data);
        }
    },
    
    renderLinks: function(links, categoryName)
    {
        for (var i = 0, l = links.length; i < l; i++)
        {
            var args = Ext.apply({}, links[i]);
            var idPrefix = this.id + '-';
            if (categoryName)
            {
                idPrefix += toHtmlName(categoryName) + '-';
            }
            
            args.linkId = idPrefix + toHtmlName(links[i][this.idProperty]);
            args.iconSrc = window.baseUrl + '/' + this.iconTemplate.apply(args);
            args.indent = this.categorised ? '&nbsp;&nbsp;' : '';
            
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
                args.onclick = 'Ext.getCmp(\'' + this.id + '\').doAction(\'' + action;
                if (args.argument)
                {
                    args.onclick += '\', \'' + args.argument;
                }
                args.onclick += '\'); return false';
                args.client = true;
            }
            
            this.rowTemplate.append(this.tbodyEl, args);
        };
    },
    
    doAction: function(action, arg)
    {
        this.handlers[action](arg);
    }
});

Ext.reg('xzlinktable', Zutubi.table.LinkTable);
