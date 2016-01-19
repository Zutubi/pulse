// dependency: ./namespace.js
// dependency: ./LoginWindow.js

(function($)
{
    var HEADER_CSRF = "X-CSRF-TOKEN",
        DEFAULT_HEADERS = {
            Accept: "application/json; charset=utf-8",
            "Content-Type": "application/json; charset=utf-8"
        },
        headers = jQuery.extend({}, DEFAULT_HEADERS);

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

    function loginThenRetryAjax(ajaxOptions)
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
                    jQuery.ajax(ajaxOptions);
                }
                else
                {
                    Zutubi.core.loginWindow = new Zutubi.core.LoginWindow({
                        csrfToken: token,
                        success: function(data, status, jqXHR)
                        {
                            stashCsrfToken(jqXHR);
                            jQuery.ajax(ajaxOptions);
                        },
                        cancel: function()
                        {
                            // FIXME kendo
                        }
                    });

                    Zutubi.core.loginWindow.show();
                }
            },
            error: function(jqXHR)
            {
                ajaxOptions.error.apply(this, arguments);
            }
        });
    }

    jQuery.extend(Zutubi.core, {
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

                    if (!options.suppressLogin && loginRequired(jqXHR))
                    {
                        loginThenRetryAjax(resolvedOptions);
                    }
                    else if (options.error)
                    {
                        options.error.apply(this, arguments);
                    }
                    else
                    {
                        Zutubi.core.reportError("Error: " + Zutubi.core.ajaxError(jqXHR));
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

        csrfToken: function()
        {
            return headers[HEADER_CSRF];
        }
    });
}(jQuery));
