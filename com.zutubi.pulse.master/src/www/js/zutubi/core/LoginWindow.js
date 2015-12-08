// dependency: ./namespace.js
// dependency: ./LoginForm.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.core.LoginWindow = Observable.extend({
        // options: {
        //    success: function (no args) called after a successful auth
        //    cancel: function (no args) called after user bails
        // }
        init: function (options)
        {
            var that = this;

            that.options = options;

            Observable.fn.init.call(that);

            that.container = $('<div id="login-container" style="display:none"></div>');
            $("body").append(that.container);

            that.form = new Zutubi.core.LoginForm({
                containerSelector: "#login-container"
            });

            that.form.bind("submit", function(e)
            {
                that._loginUser(e.data);
            });
        },

        show: function()
        {
            var that = this;

            that.completed = false;

            that.loginWindow = that.container.kendoWindow({
                width: 400,
                modal: true,
                title: "Login",
                close: function()
                {
                    if (!that.completed)
                    {
                        that.options.cancel();
                    }
                },
                deactivate: function()
                {
                    that.loginWindow.destroy();
                }
            }).data("kendoWindow");

            that.loginWindow.center();
            that.loginWindow.open();
        },

        _loginUser: function(data)
        {
            var that = this;

            if (that.errorWidget)
            {
                jQuery.each(that.errorWidget.getNotifications(), function(index, el)
                {
                    $(el).remove();
                });

                that.errorWidget.destroy();
            }

            jQuery.ajax({
                type: "POST",
                url:  window.baseUrl + "/api/auth/session",
                dataType: 'json',
                headers: {
                    Accept: "application/json; charset=utf-8",
                    "Content-Type": "application/json; charset=utf-8",
                    "X-CSRF-TOKEN": that.options.csrfToken
                },
                data: JSON.stringify(data),
                success: function ()
                {
                    that.completed = true;
                    that.loginWindow.close();
                    that.options.success.apply(that, arguments);
                },
                error: function (jqXHR, textStatus)
                {
                    var data, message;
                    if (jqXHR.status === 401)
                    {
                        message = "invalid username or password";
                    }
                    else
                    {
                        data = JSON.parse(jqXHR.responseText);
                        message = data.message || textStatus;
                    }

                    that.errorWidget = $("#notification").kendoNotification({
                        appendTo: "#login-error",
                        stacking: 'down'
                    }).data("kendoNotification");
                    that.errorWidget.error(message);
                }
            });
        }
    });
}(jQuery));
