// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.toolbar.LinkItem = Ext.extend(Ext.Toolbar.Item, {
    /**
     * @cfg {String} icon  URL of the image to show beside the link.
     * @cfg {String} text  The text to be shown in the link.
     * @cfg {String} url   The URL to link to.
     */
    enabled: true,
    
    initComponent: function()
    {
        Zutubi.toolbar.LinkItem.superclass.initComponent.call(this);

        this.addEvents(
            /**
             * @event click
             * Fires when this button is clicked
             * @param {LinkItem} this
             * @param {EventObject} e The click event
             */
            'click'
        );
    },

    // private
    onRender: function(ct, position)
    {
        this.id = this.id || Ext.id();
        
        this.autoEl = {
            tag: 'span',
            cls: 'xz-tblink',
            children: []
        };

        if (this.icon)
        {
            this.autoEl.children.push({
                tag: 'a',
                cls: 'unadorned',
                href: this.url || '#',
                children: [{
                    tag: 'img',
                    id: this.id + '-icon',
                    src: this.icon
                }]
            });
        }
        
        if (this.text)
        {
            this.autoEl.children.push({
                tag: 'a',
                id: this.id + '-text',
                href: this.url || '#',
                html: this.text || ''
            });
        }
        Zutubi.toolbar.LinkItem.superclass.onRender.call(this, ct, position);
        this.mon(this.el, {scope: this, click: this.onClick});
        if (!this.enabled)
        {
            this.disable();
        }
    },

    handler: function(e)
    {
        if (this.enabled && this.url)
        {
            window.location.href = this.url;
        }
    },

    onClick: function(e)
    {
        if (e && (!this.enabled || !this.url))
        {
            e.preventDefault();
        }

        if (this.enabled)
        {
            this.fireEvent('click', this, e);
        }
    },
    
    enable: function()
    {
        this.enabled = true;
        if (this.el)
        {
            this.el.show();
        }
    },
    
    disable: function()
    {
        this.enabled = false;
        if (this.el)
        {
            this.el.hide();
        }
    },

    setIcon: function(icon)
    {
        this.icon = icon;
        var iconEl = Ext.get(this.id + '-icon');
        if (iconEl)
        {
            iconEl.set({src: icon});
        }
    },

    setText: function(text)
    {
        this.text = text;
        var textEl = Ext.get(this.id + '-text');
        if (textEl)
        {
            textEl.update(text);
        }
    }
});
Ext.reg('xztblink', Zutubi.toolbar.LinkItem);
