// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./WorkflowWindow.js

(function($)
{
    var WorkflowWindow = Zutubi.admin.WorkflowWindow,
        CLONE = "clone",
        CLONE_KEY = "cloneKey",
        CLONE_KEY_PREFIX = "cloneKey_",
        DEFAULT_ACTIONS = [CLONE, "pullUp", "pushDown"];

    Zutubi.admin.ActionWindow = WorkflowWindow.extend({
        init: function (options)
        {
            var that = this,
                actionPart;

            that.options = jQuery.extend({}, that.options, options);

            if (DEFAULT_ACTIONS.indexOf(options.action.action) >= 0)
            {
                actionPart = options.action.action;
            }
            else
            {
                actionPart = "single/" + options.action.action;
                if (options.action.variant)
                {
                    actionPart += ":" + options.action.variant;
                }
            }

            that.url = "/api/action/" + Zutubi.config.encodePath(actionPart + "/" + options.path);

            WorkflowWindow.fn.init.call(that, {
                url: that.url,
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

            // It's important to add this element to the DOM before rendering a form in it, as it
            // needs a width for downstream calculations to work.
            el.append(wrapper);

            that.action = data;

            that.form = wrapper.kendoZaForm({
                parentPath: Zutubi.config.parentPath(that.options.path),
                baseName: Zutubi.config.baseName(that.options.path),
                structure: data.form,
                values: data.formDefaults || [],
                submits: []
            }).data("kendoZaForm");

            that.form.bind("enterPressed", jQuery.proxy(that.complete, that));
        },

        _translateProperties: function()
        {
            var properties,
                fields,
                field,
                name,
                i;

            // Some actions need to transform from the form to a more direct representation.
            // FIXME kendo this is perhaps where we also need to coerce? Could generic actions
            // have a type to allow this?
            if (this.action.action === CLONE)
            {
                properties = {};
                properties[Zutubi.config.baseName(this.options.path)] = this.form.getFieldNamed(CLONE_KEY).getValue();
                fields = this.form.getFields();
                for (i = 0; i < fields.length; i++)
                {
                    field = fields[i];
                    name = field.getFieldName();
                    if (name.indexOf(CLONE_KEY_PREFIX) === 0 && field.isEnabled())
                    {
                        properties[name.substring(CLONE_KEY_PREFIX.length)] = field.getValue();
                    }
                }
            }
            else
            {
                properties = this.form.getValues();
            }

            return properties;
        },

        _translateErrors: function(errors)
        {
            var baseName,
                field,
                translated;

            if (this.action.action === CLONE)
            {
                if (errors)
                {
                    baseName = Zutubi.config.baseName(this.options.path);
                    translated = {};
                    for (field in errors)
                    {
                        if (errors.hasOwnProperty(field))
                        {
                            if (field === baseName)
                            {
                                translated[CLONE_KEY] = errors[field];
                            }
                            else
                            {
                                translated[CLONE_KEY_PREFIX + field] = errors[field];
                            }
                        }
                    }

                    errors = translated;
                }
            }

            return errors;
        },

        _execute: function()
        {
            var that = this,
                properties = that._translateProperties();

            that.form.clearMessages();

            that.mask(true);

            Zutubi.core.ajax({
                type: "POST",
                url: that.url,
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
                    var details = Zutubi.config.getValidationErrors(jqXHR);

                    that.mask(false);
                    if (details)
                    {
                        that.form.showValidationErrors(that._translateErrors(details.validationErrors));
                    }
                    else
                    {
                        Zutubi.core.reportError("Could not perform action: " + Zutubi.core.ajaxError(jqXHR));
                    }
                }
            });
        }
    });
}(jQuery));
