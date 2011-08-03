// dependency: ./namespace.js
// dependency: ./Form.js

Zutubi.form.CheckForm = function(mainForm, options)
{
    Zutubi.form.CheckForm.superclass.constructor.call(this, options);
    this.mainForm = mainForm;
};

Ext.extend(Zutubi.form.CheckForm, Zutubi.form.Form, {
    isValid: function()
    {
        var mainValid, valid;
        // Call both, they have side-effects.
        mainValid = this.mainForm.isValid();
        valid = Zutubi.form.CheckForm.superclass.isValid.call(this);
        return mainValid && valid;
    },

    markInvalid: function(errors)
    {
        var i, fieldError, id, field;
        for(i = 0; i < errors.length; i++)
        {
            fieldError = errors[i];
            id = fieldError.id;

            if(id.lastIndexOf('_check') === id.length - 6)
            {
                field = this.mainForm.findField(id.substr(0, id.length - 6));
            }
            else
            {
                field = this.findField(id);
            }

            if(field)
            {
                field.markInvalid(fieldError.msg);
            }
        }
    },

    submit: function(options)
    {
        var params, mainParams, param;

        params = options.params || {};
        mainParams = this.mainForm.getValues(false);
        for(param in mainParams)
        {
           params[param + '_check'] = mainParams[param];
        }

        options.params = params;
        Zutubi.form.CheckForm.superclass.submit.call(this, options);
    }
});
