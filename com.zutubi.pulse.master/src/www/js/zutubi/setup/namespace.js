// dependency: zutubi/namespace.js
// dependency: zutubi/config/package.js

if (window.Zutubi.setup === undefined)
{
    window.Zutubi.setup = (function($)
    {
        var app = {},
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

        function _clear()
        {
            if (app.panel)
            {
                app.panel.destroy();
                app.panel = null;
            }

            if (app.taskList)
            {
                app.taskList.destroy();
                app.taskList = null;
            }

            $(".temp").remove();
            app.mainView.empty();
            app.verboseDocs.hide();
            app.rightColumn.css("flex-basis", "0");
        }

        function _showRightColumn()
        {
            app.rightColumn.css("flex-basis", "340px");
        }

        function _showDocs(html)
        {
            _showRightColumn();
            app.verboseDocs.html(html);
            app.verboseDocs.show();
        }

        function _pollStatus()
        {
            setTimeout(function()
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
                        Zutubi.setup.reportError("Could not update status: " + Zutubi.core.ajaxError(jqXHR));
                    }
                });

            }, 5000);
        }

        function _showWaitingMessage(message)
        {
            if (!message)
            {
                message = "unknown";
            }

            app.mainView.html('<div id="waiting-message">' +
                '<h1>Starting Pulse</h1>' +
                '<p><span class="fa fa-2x fa-spinner fa-spin"></span></p>' +
                '<p>Please wait while the server initializes.</p>' +
                '<p><b>Status</b>: ' + kendo.htmlEncode(message) + '</p>' +
                '<p>(This view will refresh automatically.)</p>' +
            '</div>');
        }

        function _showInputPanel(data)
        {
            app.panel = new Zutubi.setup.InputPanel({
                containerSelector: "#main-view",
                status: data.status,
                model: data.input
            });

            app.panel.bind("next", function(e)
            {
                kendo.ui.progress(app.mainView, true);
                Zutubi.core.ajax({
                    method: 'POST',
                    url: '/setup-api/setup/' + e.status,
                    data: {
                        kind: "composite",
                        properties: e.values
                    },
                    success: function(data)
                    {
                        kendo.ui.progress(app.mainView, false);
                        Zutubi.setup.renderStatus(data[0]);
                    },
                    error: function(jqXHR)
                    {
                        var details;

                        kendo.ui.progress(app.mainView, false);
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
        }

        function _showRestorePanel(data)
        {
            app.panel = new Zutubi.setup.RestorePanel({
                containerSelector: "#main-view",
                properties: data.properties
            });
        }

        function _showProgressPanel(data)
        {
            var list;

            if (!app.panel || app.panel.options.type !== data.status)
            {
                if (app.panel)
                {
                    app.panel.destroy();
                }

                if (app.taskList)
                {
                    app.taskList.destroy();
                }

                app.panel = new Zutubi.setup.ProgressPanel({
                    containerSelector: "#main-view",
                    type: data.status
                });

                app.panel.bind("continue", jQuery.proxy(Zutubi.setup.postAndUpdate, app, data.status + "Continue", "Continuing startup..."));

                _showRightColumn();
                app.taskList = app.taskListWrapper.kendoZaTaskList({id: "task-list" }).data("kendoZaTaskList");
            }

            app.panel.setProgress(data.progress);
            app.taskList.setData(data.progress.tasks);

            _pollStatus();
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
                app.rightColumn = $("#right-column");
                app.mainView = $("#main-view");
                app.verboseDocs = $("#verbose-docs");
                app.taskListWrapper = $("#task-list-wrapper");
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
                });
            },

            renderStatus: function(data)
            {
                _clear();

                if (data.input)
                {
                    _showInputPanel(data);

                    if (data.properties && propertyRenderers.hasOwnProperty(data.status))
                    {
                        propertyRenderers[data.status](data);
                    }
                }
                else if (data.progress && data.progress.status !== "pending")
                {
                    _showProgressPanel(data);
                }
                else if(data.status === "restore")
                {
                    _showRestorePanel(data);
                }
                else if(data.status === "waiting")
                {
                    _showWaitingMessage(data.statusMessage);
                    _pollStatus();
                }
                else
                {
                    _showWaitingMessage("Deploying main web interface...");
                    setTimeout(function()
                    {
                        window.location.reload(false);
                    }, 5000);
                }

                if (app.panel && app.panel.htmlDocs)
                {
                    _showDocs(app.panel.htmlDocs);
                }
            },

            postAndUpdate: function(action, message)
            {
                _clear();
                _showWaitingMessage(message);

                Zutubi.core.ajax({
                    method: 'POST',
                    url: '/setup-api/setup/' + action,
                    success: function(data)
                    {
                        Zutubi.setup.renderStatus(data[0]);
                    },
                    error: function(jqXHR)
                    {
                        Zutubi.setup.reportError("Could not load: " + Zutubi.core.ajaxError(jqXHR));
                    }
                });
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
