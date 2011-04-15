// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A toolbar item that shows an image alongside some static text.
 * 
 * @cfg {String} icon  URL of the image to show beside the text.
 * @cfg {String} text  The text to be shown.
 */ 
Zutubi.toolbar.IconTextItem = Ext.extend(Ext.Toolbar.Item, {
    onRender: function(ct, position)
    {
        this.autoEl = {
            tag: 'span',
            cls: 'xz-tbicontext',
            children: []
        };

        if (this.icon)
        {
            this.autoEl.children.push({
                tag: 'img',
                src: this.icon
            });
        }
        
        if (this.text)
        {
            this.autoEl.children.push({
                tag: 'span',
                html: this.text
            });
        }
        
        Zutubi.toolbar.IconTextItem.superclass.onRender.call(this, ct, position);
    },

    setIcon: function(icon)
    {
        this.icon = icon;
        if (this.el)
        {
            this.el.select('img').set({src: icon});
        }
    },

    setText: function(text)
    {
        this.text = text;
        if (this.el)
        {
            this.el.select('span').update(text);
        }
    }
});
Ext.reg('xztbicontext', Zutubi.toolbar.IconTextItem);
