// dependency: zutubi/namespace.js
// dependency: zutubi/config/package.js

if (window.Zutubi.setup === undefined)
{
    window.Zutubi.setup = (function($)
    {
        var app = {},
            baseUrl = window.baseUrl,
            propertyRenderers = {};

        function _createNotificationWidget()
        {
            var notificationElement = $("#notification");
            return notificationElement.kendoNotification({
                autoHideAfter: 7000,
                allowHideAfter: 1000,
                button: true,
                hideOnClick: false,
                position: {
                    top: 50
                },
                stacking: "down"
            }).data("kendoNotification");
        }

        function _showInputPanel(data)
        {
            var docs = data.input.type.docs;

            if (app.panel)
            {
                app.panel.destroy();
            }

            app.panel = new Zutubi.setup.InputPanel({
                containerSelector: "#main-view",
                status: data.status,
                model: data.input
            });

            app.panel.bind("next", function(e)
            {
                Zutubi.core.ajax({
                    method: 'POST',
                    url: '/setup-api/setup/' + e.status,
                    data: {
                        kind: "composite",
                        properties: e.values
                    },
                    success: function(data)
                    {
                        Zutubi.setup.renderStatus(data[0]);
                    },
                    error: function(jqXHR)
                    {
                        var details;

                        if (jqXHR.status === 422)
                        {
                            try
                            {
                                details = JSON.parse(jqXHR.responseText);
                                if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                                {
                                    app.panel.showValidationErrors(details.validationErrors);
                                    return;
                                }
                            }
                            catch (e)
                            {
                                // Do nothing.
                            }
                        }

                        Zutubi.setup.reportError("Could not continue setup: " + Zutubi.core.ajaxError(jqXHR));
                    }
                })
            });

            if (docs && docs.verbose)
            {
                app.verboseDocs.html(docs.verbose);
            }
            else
            {
                app.verboseDocs.empty();
            }
        }

        propertyRenderers["data"] = function(data)
        {
            var div = $('<div class="temp config-file"></div>');
            div.append('<p><span class="fa fa-info-circle prop-info"></span> Using configuration file <span class="filename">' + kendo.htmlEncode(data.properties.configPath) + '</span></p>');
            if (!data.properties.configExists)
            {
                div.append('<p><span class="fa fa-exclamation-triangle prop-error"></span> Configuration file does not exist!</p>');
            }

            app.leftColumn.append(div);
        };

        return {
            app: app,

            init: function()
            {
                app.notificationWidget = _createNotificationWidget();
                app.leftColumn = $("#left-column");
                app.mainView = $("#main-view");
                app.verboseDocs = $("#verbose-docs");
            },

            start: function()
            {
                Zutubi.core.ajax({
                    method: 'GET',
                    url: '/setup-api/setup/status',
                    success: function(data)
                    {
                        Zutubi.setup.renderStatus(data[0]);
                    },
                    error: function(jqXHR)
                    {
                        Zutubi.setup.reportError("Could not load: " + Zutubi.core.ajaxError(jqXHR));
                    }
                })
            },

            renderStatus: function(data)
            {
                $(".temp").remove();
                if (data.input)
                {
                    kendo.ui.progress($("body"), false);
                    _showInputPanel(data);

                    if (data.properties && propertyRenderers.hasOwnProperty(data.status))
                    {
                        propertyRenderers[data.status](data);
                    }
                }
                else
                {
                    app.mainView.empty();
                    app.verboseDocs.empty();
                    kendo.ui.progress($("body"), true);
                }
            },

            reportSuccess: function(message)
            {
                app.notificationWidget.success(message);
            },

            reportError: function(message)
            {
                app.notificationWidget.error(message);
            },

            reportWarning: function(message)
            {
                app.notificationWidget.warning(message);
            }
        };
    }(jQuery));
}
