// dependency: ./namespace.js
// dependency: ext/package.js

ZUTUBI.form.Form = function(config)
{
    ZUTUBI.form.Form.superclass.constructor.call(this, null, config);
};

Ext.extend(ZUTUBI.form.Form, Ext.form.BasicForm, {
    ID_SUFFIX_STATUS: '-status',
    
    clearInvalid: function()
    {
        ZUTUBI.form.Form.superclass.clearInvalid.call(this);
        var statusEl = Ext.get(this.formName + this.ID_SUFFIX_STATUS);
        statusEl.update('');
    },

    handleActionErrors: function(errors)
    {
        var statusEl = Ext.get(this.formName + this.ID_SUFFIX_STATUS);
        statusEl.update('');

        if(errors && errors.length > 0)
        {
            var listEl = statusEl.createChild({tag: 'ul', cls: 'validation-error'});
            for(var i = 0; i < errors.length; i++)
            {
                listEl.createChild({tag: 'li', html: Ext.util.Format.htmlEncode(errors[i])});
            }
        }
    }
});
