// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.form.Form = function(config)
{
    config.timeout = config.timeout || 120000;
    Zutubi.form.Form.superclass.constructor.call(this, null, config);
};

Ext.extend(Zutubi.form.Form, Ext.form.BasicForm, {
    ID_SUFFIX_STATUS: '-status',
    
    clearInvalid: function()
    {
        var statusEl;
        
        Zutubi.form.Form.superclass.clearInvalid.call(this);
        statusEl = Ext.get(this.formName + this.ID_SUFFIX_STATUS);
        statusEl.update('');
    },

    handleActionErrors: function(errors)
    {
        var statusEl, listEl, i;

        statusEl = Ext.get(this.formName + this.ID_SUFFIX_STATUS);
        statusEl.update('');

        if(errors && errors.length > 0)
        {
            listEl = statusEl.createChild({tag: 'ul', cls: 'validation-error'});
            for(i = 0; i < errors.length; i++)
            {
                listEl.createChild({tag: 'li', html: Ext.util.Format.htmlEncode(errors[i])});
            }
        }
    }
});
