// dependency: ./namespace.js
// dependency: ./LoginWindow.js

jQuery.extend(Zutubi.admin, {
    ajax: function (options)
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
            error: function (jqXHR, textStatus)
            {
                if (jqXHR.status === 401)
                {
                    Zutubi.admin.app.loginWindow = new Zutubi.admin.LoginWindow({
                        success: function ()
                        {
                            jQuery.ajax(o);
                        },
                        cancel: function ()
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
        catch(e)
        {
            // Do nothing.
        }

        return message;
    }
});
