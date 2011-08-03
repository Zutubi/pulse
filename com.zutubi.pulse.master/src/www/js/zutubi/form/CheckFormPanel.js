// dependency: ./namespace.js
// dependency: ./FormPanel.js

Zutubi.form.CheckFormPanel = function(mainFormPanel, options)
{
    this.mainFormPanel = mainFormPanel;
    Zutubi.form.CheckFormPanel.superclass.constructor.call(this, options);
};

Ext.extend(Zutubi.form.CheckFormPanel, Zutubi.form.FormPanel, {
    createForm: function() {
        delete this.initialConfig.listeners;
        return new Zutubi.form.CheckForm(this.mainFormPanel.getForm(), this.initialConfig);
    },

    defaultSubmit: function()
    {
        var f;

        f = this.getForm();
        if (f.isValid())
        {
            f.clearInvalid();
            window.formSubmitting = true;
            f.submit({
                clientValidation: false
            });
        }
    }
});
