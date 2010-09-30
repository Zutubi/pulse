// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.form.FormLayout = function(config)
{
    if(!config.fieldTpl)
    {
        config.fieldTpl = new Ext.Template('<tr id="x-form-row-{id}" class="x-form-row {itemCls}">' +
                                        '<td class="x-form-label"><label for="{id}" style="{labelStyle}">{label}{labelSeparator}</td>' +
                                        '<td class="x-form-label-annotation" id="x-form-label-annotation-{id}"></td>' +
                                        '<td class="x-form-separator">{4}</td>' +
                                        '<td><div id="x-form-el-{id}" class="x-form-element" style="{elementStyle}">' +
                                        '</div><div class="{clearCls}"></div></td>' +
                                    '</tr>');
        config.fieldTpl.disableFormats = true;
        config.fieldTpl.compile();
    }

    config.labelAlign = 'right';
    Zutubi.form.FormLayout.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.form.FormLayout, Ext.layout.FormLayout, {
    renderItem: function(c, position, target)
    {
        if(c && !c.rendered && c.isFormField && c.inputType == 'hidden')
        {
            target = target.up('form');
        }

        Zutubi.form.FormLayout.superclass.renderItem.call(this, c, position, target);
    },

    setContainer: function(ct)
    {
        Zutubi.form.FormLayout.superclass.setContainer.call(this, ct);
        // Forcibly override the behaviour of the default layout (adds
        // padding to the element).
        this.elementStyle = '';
    }
});
