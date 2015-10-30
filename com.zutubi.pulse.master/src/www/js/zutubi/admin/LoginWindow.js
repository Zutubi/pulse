// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.admin.LoginWindow = Observable.extend({
        // options: {
        //    success: function (no args) called after a successful auth
        //    cancel: function (no args) called after user bails
        // }
        init: function (options)
        {
            var that = this;

            that.options = options;

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div id="login" style="display: none">' +
                    '<div id="login-error"></div>' +
                    '<form id="login-form">' +
                        '<table class="k-form">' +
                            '<tr>' +
                                '<th><label for="username">login:</label></th>' +
                                '<td><input class="k-textbox" type="text" name="username" required/></td>' +
                            '</tr>' +
                            '<tr>' +
                                '<th><label for="password">password:</label></th>' +
                                '<td><input class="k-textbox" type="password" name="password"/></td>' +
                            '</tr>' +
                            '<tr>' +
                                '<th><label for="rememberMe">remember me:</label></th>' +
                                '<td><input type="checkbox" name="rememberMe" value="true"/></td>' +
                            '</tr>' +
                            '<tr>' +
                                '<th>&nbsp;</th>' +
                                '<td><input class="k-button" type="submit" name="login" value="login"/></td>' +
                            '</tr>' +
                        '</table>' +
                    '</form>' +
                '</div>'
            , {wrap: false});

            that.element = that.view.render("body");

            $("#login-form").on("submit", function(e)
            {
                e.preventDefault();
                that._loginUser();
            });
        },

        show: function()
        {
            var that = this;

            that.completed = false;

            $("#login-form").kendoValidator();
            that.loginWindow = $("#login").kendoWindow({
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

        _loginUser: function()
        {
            var that = this,
                form = $("#login-form"),
                data = {};

            if (that.errorWidget)
            {
                jQuery.each(that.errorWidget.getNotifications(), function(index, el)
                {
                    $(el).remove();
                });

                that.errorWidget.destroy();
            }

            form.serializeArray().map(function(x) { data[x.name] = x.value; });

            jQuery.ajax({
                type: "POST",
                url:  window.baseUrl + "/api/auth/session",
                dataType: 'json',
                headers: {
                    Accept: "application/json; charset=utf-8",
                    "Content-Type": "application/json; charset=utf-8"
                },
                data: JSON.stringify(data),
                success: function ()
                {
                    that.completed = true;
                    that.loginWindow.close();
                    that.options.success();
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
