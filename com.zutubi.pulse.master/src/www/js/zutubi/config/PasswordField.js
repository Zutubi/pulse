// dependency: ./namespace.js
// dependency: ./TextField.js

(function($)
{
    var ui = kendo.ui,
        TextField = Zutubi.config.TextField;

    Zutubi.config.PasswordField = TextField.extend({
        init: function(element, options)
        {
            TextField.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaPasswordField",
            template: '<input class="k-input k-textbox" type="password" id="#: id #" name="#: name #">'
        }
    });

    ui.plugin(Zutubi.config.PasswordField);
}(jQuery));
