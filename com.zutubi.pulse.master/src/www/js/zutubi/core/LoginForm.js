// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable,
        SUBMIT = "submit";

    Zutubi.core.LoginForm = Observable.extend({
        // options:
        //   - container: jQuery object for container to render form in
        //   - username: if specified, prefilled text for the username box
        //   - rememberMe: if specified and true, pre-checks the remember me box
        //   - action: if specified, the URL to POST the form to on submit (if not specified a
        //             submit event is raised instead, and default POSTing is suppressed)
        init: function (options)
        {
            var that = this;

            that.options = options;

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div id="login-wrapper">' +
                    '<div id="login-error"></div>' +
                    '<form id="login-form" method="post">' +
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
                , { wrap: false });

            that.element = that.view.render(options.container);

            that.loginForm = $("#login-form");
            if (options.username)
            {
                $('input[name="username"]').val(options.username);
            }
            if (options.rememberMe)
            {
                $('input[name="rememberMe"]').prop("checked", true);
            }

            if (options.action)
            {
                that.loginForm[0].action = options.action;
            }
            else
            {
                that.loginForm.on("submit", function (e)
                {
                    e.preventDefault();
                    that._raiseSubmit();
                });
            }
        },

        _raiseSubmit: function()
        {
            var data = {};
            $("#login-form").serializeArray().map(function(x) { data[x.name] = x.value; });
            this.trigger(SUBMIT, {data: data});
        },

        focus: function()
        {
            var input = $('input[name="username"]');
            if (input.val())
            {
                input.select();
            }
            else
            {
                input.focus();
            }
        }
    });
}(jQuery));
