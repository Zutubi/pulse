// dependency: ./namespace.js
// dependency: ./FormPanel.js

ZUTUBI.form.CheckFormPanel = function(mainFormPanel, options)
{
    this.mainFormPanel = mainFormPanel;
    ZUTUBI.form.CheckFormPanel.superclass.constructor.call(this, options);
};

Ext.extend(ZUTUBI.form.CheckFormPanel, ZUTUBI.form.FormPanel, {
    createForm: function() {
        delete this.initialConfig.listeners;
        return new ZUTUBI.form.CheckForm(this.mainFormPanel.getForm(), this.initialConfig);
    },

    defaultSubmit: function()
    {
        var f = this.getForm();
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
