// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A simple box of text decorated with a title and border style.
 *
 * @cfg {String} id    id to use for the rendered element
 * @cfg {String} title title for the box
 */
Zutubi.TextBox = Ext.extend(Ext.BoxComponent, {
    template: new Ext.XTemplate(
        '<div id="{id}">' +
            '<h3 class="content-heading">{title}</h3>' +
            '<div id="{id}-text" class="content-box"></div>' +
        '</div>'
    ),

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
        return this.data != null;
    }    
});

Ext.reg('xztextbox', Zutubi.TextBox);
