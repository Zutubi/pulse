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
                    window.loginWindow = new Zutubi.admin.LoginWindow({
                        success: function ()
                        {
                            jQuery.ajax(o);
                        },
                        cancel: function ()
                        {
                            zaReportError("Action cancelled: authentication required.");
                        }
                    });

                    window.loginWindow.show();
                }
                else
                {
                    options.error.apply(this, arguments);
                }
            }
        });

        jQuery.ajax(o);
    }
});
