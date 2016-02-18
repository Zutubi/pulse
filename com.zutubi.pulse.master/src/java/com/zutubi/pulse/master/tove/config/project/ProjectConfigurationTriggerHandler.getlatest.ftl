(function(form, field)
{
    form.bind('action', function(e)
    {
        var widget;

        if (e.field !== field || e.action !== 'latest') return;

        widget = field.element.closest(".k-widget");
        kendo.ui.progress(widget, true);
        jQuery.ajax({
            method: "GET",
            url: window.baseUrl + "/ajax/getLatestRevision.action",
            data: {
                projectId: field.options.structure.parameters.projectId
            },
            success: function(data, status, jqXHR)
            {
                kendo.ui.progress(widget, false);
                if (data.successful)
                {
                    field.bindValue(data.latestRevision);
                }
                else
                {
                    Zutubi.core.reportError("Could not fetch latest revision: " + data.error);
                }
            },
            error: function(jqXHR)
            {
                kendo.ui.progress(widget, false);
                Zutubi.core.reportError("Error requesting master to fetch revision: " + Zutubi.core.ajaxError(jqXHR));
            }
        });
    });
});
