(function(form, field)
{
    var depName = field.structure.parameters.dependentOn,
        depField;

    if (depName)
    {
        depField = form.getFieldNamed(depName);
        if (depField)
        {
            depField.bind('change', function(e)
            {
                var value = depField.getValue(),
                    widget,
                    formOptions;

                if (value)
                {
                    widget = field.element.closest(".k-widget");
                    kendo.ui.progress(widget, true);
                    formOptions = depField.parentForm.options;
                    Zutubi.core.ajax({
                        method: "POST",
                        url: "/api/action/options/" + Zutubi.config.encodePath(formOptions.parentPath),
                        data: {
                            symbolicName: formOptions.symbolicName,
                            baseName: formOptions.baseName,
                            propertyName: field.getFieldName(),
                            scopePath: value
                        },
                        success: function(data)
                        {
                            kendo.ui.progress(widget, false);
                            field.setData(data);
                        },
                        error: function(jqXHR)
                        {
                            kendo.ui.progress(widget, false);
                            Zutubi.core.reportError("Unable to update dependent options: " + Zutubi.core.ajaxError(jqXHR));
                        }
                    });
                }
                else
                {
                    field.setData([]);
                }
            });
        }
    }
});
