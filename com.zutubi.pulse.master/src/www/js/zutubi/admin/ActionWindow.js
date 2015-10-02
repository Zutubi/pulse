// dependency: ./namespace.js
// dependency: ./WorkflowWindow.js
// dependency: ./Form.js

(function($)
{
    var WorkflowWindow = Zutubi.admin.WorkflowWindow;

    Zutubi.admin.ActionWindow = WorkflowWindow.extend({
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            WorkflowWindow.fn.init.call(that, {
                url: "/api/action/single/" + options.action.action + "/" + options.path,
                title: options.action.label,
                continueLabel: options.action.label,
                width: 600,
                render: jQuery.proxy(that._render, that),
                success: jQuery.proxy(that._execute, that)
            });
        },

        _render: function(data, el)
        {
            var that = this,
                wrapper = $("<div></div>");

            that.action = data;

            that.form = wrapper.kendoZaForm({
                path: that.options.path,
                structure: data.form,
                values: data.formDefaults || [],
                submits: []
            }).data("kendoZaForm");

            el.append(wrapper);
        },

        _execute: function()
        {
            var that = this,
                properties = that.form.getValues();

            that.form.clearValidationErrors();

            // FIXME kendo we have no type for this, and in general can't!  Should we just use when available?
            // Or does coercion need to work differently?
            //Zutubi.admin.coerceProperties(properties, that.action.argumentType);

            that.mask(true);

            Zutubi.admin.ajax({
                type: "POST",
                url: "/api/action/single/" + that.action.action + "/" + that.options.path,
                data: {
                    kind: "composite",
                    properties: properties
                },
                success: function (data)
                {
                    that.mask(false);
                    that.close();
                    that.options.executed(data);
                },
                error: function (jqXHR)
                {
                    var details;

                    that.mask(false);
                    if (jqXHR.status === 422)
                    {
                        try
                        {
                            details = JSON.parse(jqXHR.responseText);
                            if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                            {
                                that.form.showValidationErrors(details);
                                return;
                            }
                        }
                        catch(e)
                        {
                            // Do nothing.
                        }
                    }

                    Zutubi.admin.reportError("Could not perform action: " + Zutubi.admin.ajaxError(jqXHR));
                }
            });
        }
    });
}(jQuery));

