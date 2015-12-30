// dependency: ./namespace.js

(function($)
{
    function prompt(options, labelCount)
    {
        var message, allLabel, window;

        if (labelCount === 1)
        {
            message = "There is another label with this name, rename both labels or just this one?";
            allLabel = "rename both labels";
        }
        else
        {
            message = "There are " + labelCount + " other labels with this name, rename all labels or just this one?";
            allLabel = "rename all labels";
        }

        window = new Zutubi.core.PromptWindow({
            title: "Rename Label",
            messageHTML: message,
            width: 640,
            buttons: [{
                label: allLabel,
                value: "all",
                spriteCssClass: "fa fa-check-circle"
            }, {
                label: "rename this label only",
                value: "one",
                spriteCssClass: "fa fa-check-circle"
            }, {
                label: "cancel",
                value: "",
                spriteCssClass: "fa fa-times-circle"
            }],
            select: function(value)
            {
                var models = {};

                if (value === "all")
                {
                    Zutubi.core.ajax({
                        type: "POST",
                        maskAll: true,
                        url: "/api/action/single/rename/" + Zutubi.config.encodePath(options.path),
                        data: {
                            kind: "composite",
                            properties: options.properties
                        },
                        success: function(data)
                        {
                            if (data.success)
                            {
                                Zutubi.core.reportSuccess("all labels renamed");

                                models[options.path] = data.model;
                                options.success({
                                    models: models,
                                    updatedPaths: [options.path]
                                });
                            }
                            else
                            {
                                Zutubi.core.reportError(data.message);
                            }
                        },
                        error: function(jqXHR)
                        {
                            Zutubi.core.reportError("Could not rename label: " + Zutubi.core.ajaxError(jqXHR));
                        }
                    });
                }
                else if (value === "one")
                {
                    Zutubi.config.saveConfig(options);
                }
                else
                {
                    options.cancel();
                }
            }
        });

        window.show();
    }

    function saveLabel(options)
    {
        if (!options.properties.label)
        {
            options.invalid({label: ["label is required"]});
            return;
        }

        if (options.properties.label === options.composite.properties.label)
        {
            // Not actually edited.
            options.cancel();
            return;
        }

        kendo.ui.progress($("body"), true);
        Zutubi.core.ajax({
            type: "GET",
            maskAll: true,
            url: "/api/config/projects/*/labels/*?depth=1&predicate=" + encodeURIComponent("label=" + options.composite.properties.label),
            success: function (data)
            {
                data = jQuery.grep(data, function(element)
                {
                    return element.handle !== options.composite.handle;
                });

                if (data.length > 0)
                {
                    kendo.ui.progress($("body"), false);
                    prompt(options, data.length);
                }
                else
                {
                    // This is the only label with this name, just do a regular save without
                    // bothering the user further.  Note this will unmask when done.
                    Zutubi.config.saveConfig(options);
                }
            },
            error: function (jqXHR)
            {
                kendo.ui.progress($("body"), false);
                Zutubi.core.reportError("Could not query for existing labels: " + Zutubi.core.ajaxError(jqXHR));
                options.cancel();
            }
        });
    }

    Zutubi.config.registerSaveHook("zutubi.labelConfig", saveLabel);
}(jQuery));

