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
        // Call both, they have side-effects.
        var mainValid = this.mainForm.isValid();
        var valid = Zutubi.form.CheckForm.superclass.isValid.call(this);
        return mainValid && valid;
    },

    markInvalid: function(errors)
    {
        for(var i = 0; i < errors.length; i++)
        {
            var fieldError = errors[i];
            var id = fieldError.id;
            var field;

            if(id.lastIndexOf('_check') == id.length - 6)
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
        var params = options.params || {};
        var mainParams = this.mainForm.getValues(false);

        for(var param in mainParams)
        {
           params[param + '_check'] = mainParams[param];
        }

        options.params = params;
        Zutubi.form.CheckForm.superclass.submit.call(this, options);
    }
});
