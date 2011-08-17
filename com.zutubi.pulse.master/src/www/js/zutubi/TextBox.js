// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A simple box of text.  May optionally have a title, in which case a border
 * is also added.
 *
 * @cfg {String} id    id to use for the rendered element
 * @cfg {String} title title for the box (optional)
 */
Zutubi.TextBox = Ext.extend(Ext.BoxComponent, {
    initComponent: function()
    {
        Ext.applyIf(this, {
            title: '',
            template: new Ext.XTemplate(
                '<div id="{id}">' +
                    '<tpl if="title">' +
                        '<h3 class="content-heading">{title}</h3>' +
                    '</tpl>' +
                    '<div id="{id}-text" <tpl if="title">class="content-box"</tpl>></div>' +
                '</div>'
            )
        });
        
        Zutubi.TextBox.superclass.initComponent.apply(this, arguments);
    },

    onRender: function(container, position)
    {
        if (position)
        {
            this.el = this.template.insertBefore(position, this, true);
        }
        else
        {
            this.el = this.template.append(container, this, true);
        }

        this.textEl = Ext.get(this.id + '-text');
        
        Zutubi.TextBox.superclass.onRender.apply(this, arguments);
        
        this.renderText();
    },

    update: function(data)
    {
        this.data = data;
        if (this.rendered)
        {
            this.renderText();
        }
    },
    
    renderText: function()
    {
        if (this.data)
        {
            this.textEl.update(Ext.util.Format.htmlEncode(this.data));
            this.el.setDisplayed(true);
        }
        else
        {
            this.el.setDisplayed(false);
        }
    },
    
    dataExists: function()
    {
        return !!this.data;
    }    
});

Ext.reg('xztextbox', Zutubi.TextBox);
