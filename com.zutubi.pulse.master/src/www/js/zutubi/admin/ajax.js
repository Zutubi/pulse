// dependency: ./namespace.js
// dependency: ./LoginWindow.js

(function($)
{
    var HEADER_CSRF = "X-CSRF-TOKEN",
        DEFAULT_HEADERS = {
            Accept: "application/json; charset=utf-8",
            "Content-Type": "application/json; charset=utf-8"
        },
        currentNavigation = null,
        headers = jQuery.extend({}, DEFAULT_HEADERS),

    Navigation = function(url, targets, callback)
    {
        this.url = url;
        this.targets = targets;
        this.callback = callback;
        this.cancelled = false;
    };

    Navigation.prototype = {
        begin: function()
        {
            var that = this,
                i;

            for (i = 0; i < this.targets.length; i++)
            {
                that.targets[i].beginNavigation();
            }

            Zutubi.admin.ajax({
                type: "GET",
                url: that.url,
                success: function (data)
                {
                    if (!that.cancelled)
                    {
                        that.end(that.callback(data));
                    }
                },
                error: function (jqXHR)
                {
                    if (!that.cancelled)
                    {
                        that.end("Load error: " + Zutubi.admin.ajaxError(jqXHR));
                    }
                }
            });
        },

        end: function(error)
        {
            var i;

            for (i = 0; i < this.targets.length; i++)
            {
                this.targets[i].endNavigation(error);
            }
        },

        cancel: function()
        {
            this.end();
            this.cancelled = true;
        }
    };

    function stashCsrfToken(jqXHR)
    {
        var token = jqXHR.getResponseHeader(HEADER_CSRF);
        if (token)
        {
            headers[HEADER_CSRF] = token;
        }

        return token;
    }

    function loginRequired(jqXHR)
    {
        return jqXHR.status === 401 || (jqXHR.status === 403 && jqXHR.statusText && jqXHR.statusText.indexOf("CSRF") >= 0);
    }

    jQuery.extend(Zutubi.admin, {
        ajax: function(options)
        {
            var resolvedOptions;

            resolvedOptions = jQuery.extend({
                dataType: "json",
                headers: headers
            }, options, {
                url: window.baseUrl + options.url,
                data: JSON.stringify(options.data),
                success: function(data, status, jqXHR)
                {
                    stashCsrfToken(jqXHR);

                    if (options.maskAll)
                    {
                        kendo.ui.progress($("body"), false);
                    }

                    options.success.apply(this, arguments);
                },
                error: function(jqXHR, textStatus)
                {
                    if (options.maskAll)
                    {
                        kendo.ui.progress($("body"), false);
                    }

                    if (loginRequired(jqXHR))
                    {
                        // The initial GET may automatically log us in via remember-me.  If not, it also serves to
                        // ensure we have a live session and matching CSRF token.
                        jQuery.ajax({
                            method: "GET",
                            url: window.baseUrl + "/api/auth/session",
                            dataType: "json",
                            headers: DEFAULT_HEADERS,
                            success: function(data, status, jqXHR)
                            {
                                var token = stashCsrfToken(jqXHR);

                                if (data.username)
                                {
                                    jQuery.ajax(resolvedOptions);
                                }
                                else
                                {
                                    Zutubi.admin.app.loginWindow = new Zutubi.admin.LoginWindow({
                                        csrfToken: token,
                                        success: function(data, status, jqXHR)
                                        {
                                            stashCsrfToken(jqXHR);
                                            jQuery.ajax(resolvedOptions);
                                        },
                                        cancel: function()
                                        {
                                            Zutubi.admin.reportError("Action cancelled: authentication required.");
                                        }
                                    });

                                    Zutubi.admin.app.loginWindow.show();
                                }
                            },
                            error: function(jqXHR)
                            {
                                options.error.apply(this, arguments);
                            }
                        });
                    }
                    else
                    {
                        options.error.apply(this, arguments);
                    }
                }
            });

            if (options.maskAll)
            {
                kendo.ui.progress($("body"), true);
            }

            jQuery.ajax(resolvedOptions);
        },

        ajaxError: function(jqXHR)
        {
            var message = "",
                details;

            if (jqXHR.statusText)
            {
                message = jqXHR.statusText + " ";
            }

            message += "(" + jqXHR.status + ")";

            try
            {
                details = JSON.parse(jqXHR.responseText);
                if (details.message)
                {
                    message += ": " + details.message;
                }
            }
            catch (e)
            {
                // Do nothing.
            }

            return message;
        },

        navigate: function(url, targets, callback)
        {
            if (currentNavigation)
            {
                currentNavigation.cancel();
            }

            currentNavigation = new Navigation(url, targets, callback);
            currentNavigation.begin();
        }
    });
}(jQuery));
