// dependency: ./namespace.js
// dependency: zutubi/core/package.js
// dependency: zutubi/config/package.js

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

            Zutubi.core.ajax({
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
                        that.end("Load error: " + Zutubi.core.ajaxError(jqXHR));
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
        navigate: function(url, targets, callback)
        {
            if (currentNavigation)
            {
                currentNavigation.cancel();
            }

            currentNavigation = new Navigation(url, targets, callback);
            currentNavigation.begin();
        },

        checkConfig: function(path , type, form, checkForm)
        {
            var properties = form.getValues(),
                checkProperties = checkForm.getValues();

            form.clearMessages();

            Zutubi.config.coerceProperties(properties, type.simpleProperties);
            Zutubi.config.coerceProperties(checkProperties, type.checkType.simpleProperties);

            Zutubi.core.ajax({
                type: "POST",
                maskAll: true,
                url: "/api/action/check/" + Zutubi.config.encodePath(path),
                data: {
                    main: {kind: "composite", properties: properties, type: {symbolicName: type.symbolicName}},
                    check: {kind: "composite", properties: checkProperties}
                },
                success: function (data)
                {
                    var message = data.success ? "configuration ok" : (data.message || "check failed");
                    checkForm.showStatus(data.success, message);
                },
                error: function (jqXHR)
                {
                    var details;

                    if (jqXHR.status === 422)
                    {
                        try
                        {
                            details = JSON.parse(jqXHR.responseText);
                            if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                            {
                                if (details.key === "main")
                                {
                                    form.showValidationErrors(details.validationErrors);
                                }
                                else
                                {
                                    checkForm.showValidationErrors(details.validationErrors);
                                }
                                return;
                            }
                        }
                        catch(e)
                        {
                            // Do nothing.
                        }
                    }

                    Zutubi.admin.reportError("Could not check configuration: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        }
    });
}(jQuery));
