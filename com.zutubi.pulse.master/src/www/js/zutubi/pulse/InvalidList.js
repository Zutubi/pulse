// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * An unordered list of invalid configurations.  Supports updated, and
 * self-hides when there are no invalid configurations to report. 
 *
 * @cfg {String} blurb    A blurb describing the list to the user.
 * @cfg {String} scopeUrl URL for the configuration of the scope, used to
 *                        construct URLs to each listed item.
 */
Zutubi.pulse.InvalidList = Ext.extend(Ext.BoxComponent, {
    initComponent: function()
    {
        if (this.scopeUrl[this.scopeUrl.length - 1] !== '/')
        {
            this.scopeUrl += '/';
        }
        
        Ext.applyIf(this, {
            template: new Ext.Template(
                '<div id="{id}" class="invalid-configs">' +
                    '{blurb}' +
                    '<ul id="{id}-list">' +
                    '</ul>' +
                '</div>'
            ),
                    
            itemTemplate: new Ext.Template(
                '<li>' +
                    '<a href="{url}">{item:htmlEncode}</a>' +
                '</li>'
            )
        });
        
        Zutubi.pulse.InvalidList.superclass.initComponent.apply(this, arguments);
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
        
        this.renderData();
        Zutubi.pulse.InvalidList.superclass.onRender.apply(this, arguments);        
    },
    
    renderData: function()
    {
        var i, len, item;
        this.el.select('li').remove();
        if (this.data && this.data.length > 0)
        {
            for (i = 0, len = this.data.length; i < len; i++)
            {
                item = this.data[i];
                this.itemTemplate.append(this.el, {
                    url: this.scopeUrl + encodeURIComponent(item) + '/',
                    item: item
                }, false);
            }
            
            this.el.setDisplayed(true);
        }
        else
        {
            this.el.setDisplayed(false);
        }
    },
    
    update: function(data)
    {
        this.data = data;
        if (this.rendered)
        {
            this.renderData();
        }
    }
});

Ext.reg('xzinvalidlist', Zutubi.pulse.InvalidList);
