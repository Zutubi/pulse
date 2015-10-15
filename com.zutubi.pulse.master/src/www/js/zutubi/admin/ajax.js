// dependency: ./namespace.js
// dependency: ./LoginWindow.js

(function($)
{
    var currentNavigation = null,

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

    jQuery.extend(Zutubi.admin, {
        ajax: function(options)
        {
            var o = jQuery.extend({
                dataType: "json",
                headers: {
                    Accept: "application/json; charset=utf-8",
                    "Content-Type": "application/json; charset=utf-8"
                }
            }, options, {
                url: window.baseUrl + options.url,
                data: JSON.stringify(options.data),
                success: function()
                {
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

                    if (jqXHR.status === 401)
                    {
                        Zutubi.admin.app.loginWindow = new Zutubi.admin.LoginWindow({
                            success: function()
                            {
                                jQuery.ajax(o);
                            },
                            cancel: function()
                            {
                                Zutubi.admin.reportError("Action cancelled: authentication required.");
                            }
                        });

                        Zutubi.admin.app.loginWindow.show();
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

            jQuery.ajax(o);
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
