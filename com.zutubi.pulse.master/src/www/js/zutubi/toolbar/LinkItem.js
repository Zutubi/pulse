// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.toolbar.LinkItem = Ext.extend(Ext.Toolbar.Item, {
    /**
     * @cfg {String} icon  URL of the image to show beside the link.
     * @cfg {String} text  The text to be shown in the link.
     * @cfg {String} url   The URL to link to.
     */

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
                    src: this.icon
                }]
            });
        }
        
        if (this.text)
        {
            this.autoEl.children.push({
                tag: 'a',
                href: this.url || '#',
                html: this.text || ''
            });
        }
        Zutubi.toolbar.LinkItem.superclass.onRender.call(this, ct, position);
        this.mon(this.el, {scope: this, click: this.onClick});
    },

    handler: function(e)
    {
        if (this.url)
        {
            window.location.href = this.url;
        }
    },

    onClick: function(e)
    {
        if (e && !this.url)
        {
            e.preventDefault();
        }

        this.fireEvent('click', this, e);
    }
});
Ext.reg('xztblink', Zutubi.toolbar.LinkItem);
