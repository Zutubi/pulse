(function(form, field)
{
    var wizard = form.options.parentWizard;

    form.bind('action', function(e)
    {
        var rw;

        if (e.field !== field || e.action !== 'browse') return;

        rw = new Zutubi.admin.ResourceBrowserWindow({
            resourceField: field,
            defaultVersionField: form.getFieldNamed("defaultVersion"),
            versionField: form.getFieldNamed("version")
        });

        rw.show();
    });

    if (wizard)
    {
        form.bind("created", function()
        {
            var value = wizard.getValue()[""],
                type,
                step;

            if (value && value.type && value.type.symbolicName)
            {
                step = wizard.getStepWithKey("resource");
                if (step && step.parameters && step.parameters.hasOwnProperty(value.type.symbolicName))
                {
                    field.options.parentForm.getFieldNamed("addDefaultResource").bindValue(true);
                    field.bindValue(step.parameters[value.type.symbolicName]);
                }
            }
        });
    }
});
