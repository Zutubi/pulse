// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A heading used to delineate sections of a page.
 *
 * @cfg {String} text Heading text.
 * @cfg {String} cls  CSS class used to style this element (defaults to xz-sectionheading).
 */
Zutubi.pulse.SectionHeading = Ext.extend(Ext.BoxComponent, {
    initComponent: function()
    {
        Ext.applyIf(this, {
            cls: 'xz-sectionheading',
            template: new Ext.Template('<h2 class="{cls}">:: {text} ::</h2>')        
        });
        
        Zutubi.pulse.SectionHeading.superclass.initComponent.apply(this, arguments);
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
        
        Zutubi.pulse.SectionHeading.superclass.onRender.apply(this, arguments);        
    }
});

Ext.reg('xzsectionheading', Zutubi.pulse.SectionHeading);
