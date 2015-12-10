// dependency: zutubi/namespace.js
// dependency: zutubi/config/package.js

if (window.Zutubi.setup === undefined)
{
    window.Zutubi.setup = (function($)
    {
        var app = {},
            propertyRenderers = {},
            CONTACT_SUPPORT = 'If you believe this to be a bug, please contact <a href="mailto:support@zutubi.com">' +
                'support@zutubi.com</a>.  To help us diagnose the issue please include the above details and attach ' +
                'your server logs (from $PULSE_HOME/logs).',
            SUCCESS_MESSAGES = {
                migrate: 'Migration complete, please click continue to resume server startup.',
                restore: 'The restore succeeded, please click continue to resume server startup.',
                upgrade: 'All upgrade tasks are complete, please click continue to resume server startup.'
            },
            FAILURE_MESSAGES = {
                migrate: 'The migration has failed, please see above for details.  ' + CONTACT_SUPPORT,
                restore: 'The restore has failed, please see above for details.  ' + CONTACT_SUPPORT,
                upgrade: 'One or more upgrade tasks failed, please see above for details.  ' + CONTACT_SUPPORT
            };

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

            if (app.rightPanel)
            {
                app.rightPanel.destroy();
                app.rightPanel = null;
            }

            $(".temp").remove();
            app.mainView.empty();
            app.rightColumn.empty();
            app.rightColumn.css("flex-basis", "0");
        }

        function _showRightColumn()
        {
            app.rightColumn.css("flex-basis", "340px");
        }

        function _showDocs(html)
        {
            var verboseDocs = $('<div id="verbose-docs"></div>');
            verboseDocs.html(html);
            app.rightColumn.append(verboseDocs);
            _showRightColumn();
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
                        Zutubi.core.reportError("Could not update status: " + Zutubi.core.ajaxError(jqXHR));
                    }
                });

            }, 5000);
        }

        function _showWaitingMessage(message, errorMessages)
        {
            var errorList, i;

            if (!message)
            {
                message = "unknown";
            }

            app.mainView.html('<div id="waiting-message">' +
                '<h1>Please Wait</h1>' +
                '<p><span class="fa fa-2x fa-spinner fa-spin"></span></p>' +
                '<p><b>Status</b>: ' + kendo.htmlEncode(message) + '</p>' +
                '<p>(This view will refresh automatically.)</p>' +
            '</div>');

            if (errorMessages && errorMessages.length > 0)
            {
                app.mainView.append('<div id="waiting-errors"><p><b>Note:</b> some errors have been detected while ' +
                    'waiting. These errors may prevent the server from starting.  Please see below, or check the ' +
                    'server logs (<code>$PULSE_HOME/logs</code>) for full details.</p><ul></ul></div>');

                errorList = app.mainView.find("ul");

                for (i = 0; i < errorMessages.length; i++)
                {
                    errorList.append('<li>' + kendo.htmlEncode(errorMessages[i]) + '</li>');
                }
            }
        }

        function _postForm(action, values)
        {
            kendo.ui.progress(app.mainView, true);
            Zutubi.core.ajax({
                method: 'POST',
                url: '/setup-api/setup/' + action,
                data: {
                    kind: "composite",
                    properties: values
                },
                success: function(data)
                {
                    kendo.ui.progress(app.mainView, false);
                    Zutubi.setup.renderStatus(data[0]);
                },
                error: function(jqXHR)
                {
                    var details = Zutubi.config.getValidationErrors(jqXHR);

                    kendo.ui.progress(app.mainView, false);
                    if (details)
                    {
                        app.panel.showValidationErrors(details.validationErrors);
                    }
                    else
                    {
                        Zutubi.core.reportError("Could not continue setup: " + Zutubi.core.ajaxError(jqXHR));
                    }
                }
            })
        }

        function _showInputPanel(data)
        {
            app.panel = new Zutubi.setup.InputPanel({
                containerSelector: "#main-view",
                status: data.status,
                model: data.input
            });

            app.panel.bind("submit", function(e)
            {
                _postForm(e.status, e.values);
            });
        }

        function _showMigratePanel(data)
        {
            var existingDb = $('<div><h2>Existing Database Properties</h2><div id="db-table-wrapper"></div></div>');

            app.rightColumn.append(existingDb);
            app.rightPanel = $("#db-table-wrapper").kendoZaPropertyTable({
                id: "current-db-properties",
                data: [{
                    key: 'Database Type',
                    value: data.properties.databaseType
                }, {
                    key: 'Host',
                    value: data.properties.host
                }, {
                    key: 'Port',
                    value: data.properties.port
                }, {
                    key: 'Database',
                    value: data.properties.database
                }, {
                    key: 'User',
                    value: data.properties.user
                }]
            }).data("kendoZaPropertyTable");
            _showRightColumn();

            app.panel = new Zutubi.setup.InputPanel({
                containerSelector: "#main-view",
                status: data.status,
                model: data.input,
                submits: ["migrate database", "abort migrate (normal startup)"]
            });

            app.panel.bind("submit", function(e)
            {
                if (e.submit === "migrate database")
                {
                    _postForm("migrate", e.values);
                }
                else
                {
                    Zutubi.setup.postAndUpdate("migrateAbort", "Aborting migration...");
                }
            });
        }

        function _showRestorePanel(data)
        {
            app.panel = new Zutubi.setup.RestorePanel({
                containerSelector: "#main-view",
                properties: data.properties
            });
        }

        function _showUpgradePanel(data)
        {
            app.panel = new Zutubi.setup.UpgradePanel({
                containerSelector: "#main-view",
                properties: data.properties
            });
        }

        function _showProgressPanel(data)
        {
            var list, taskList;

            if (!app.panel || app.panel.options.type !== data.status)
            {
                if (app.panel)
                {
                    app.panel.destroy();
                }

                if (app.rightPanel)
                {
                    app.rightPanel.destroy();
                }

                app.panel = new Zutubi.setup.ProgressPanel({
                    containerSelector: "#main-view",
                    type: data.status,
                    successVerbose: SUCCESS_MESSAGES[data.status],
                    failureVerbose: FAILURE_MESSAGES[data.status]
                });

                app.panel.bind("continue", jQuery.proxy(Zutubi.setup.postAndUpdate, app, data.status + "Continue", "Continuing startup..."));

                _showRightColumn();
                taskList = $('<div></div>');
                app.rightColumn.append(taskList);
                app.rightPanel = taskList.kendoZaTaskList({id: "task-list" }).data("kendoZaTaskList");
            }

            app.panel.setProgress(data.progress);
            app.rightPanel.setData(data.progress.tasks);

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
                Zutubi.core.registerFeedbackHandler(app.notificationWidget);

                app.leftColumn = $("#left-column");
                app.rightColumn = $("#right-column");
                app.mainView = $("#main-view");
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
                        Zutubi.core.reportError("Could not load: " + Zutubi.core.ajaxError(jqXHR));
                    }
                });
            },

            renderStatus: function(data)
            {
                _clear();

                if (data.progress && data.progress.status !== "pending")
                {
                    _showProgressPanel(data);
                }
                else if (data.status === "migrate")
                {
                    _showMigratePanel(data);
                }
                else if (data.status === "restore")
                {
                    _showRestorePanel(data);
                }
                else if (data.status === "upgrade")
                {
                    _showUpgradePanel(data);
                }
                else if (data.input)
                {
                    _showInputPanel(data);

                    if (data.properties && propertyRenderers.hasOwnProperty(data.status))
                    {
                        propertyRenderers[data.status](data);
                    }
                }
                else if (data.status === "waiting")
                {
                    _showWaitingMessage(data.statusMessage, data.errorMessages);
                    _pollStatus();
                }
                else
                {
                    _showWaitingMessage("Deploying main web interface...", data.errorMessages);
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
                        Zutubi.core.reportError("Could not load: " + Zutubi.core.ajaxError(jqXHR));
                    }
                });
            }
        };
    }(jQuery));
}
