// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A heading used to delineate sections of a page.
 *
 * @cfg {String} text Heading text.
 */
Zutubi.pulse.SectionHeading = Ext.extend(Ext.BoxComponent, {
    cls: 'hpad',
    style: 'margin-top: 7px',
    
    onRender: function(container, position) {
        if (!this.template)
        {
            this.template = new Ext.Template('<h2>:: {text} ::</h2>');
        }

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
