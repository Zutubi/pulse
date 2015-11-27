// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Button = ui.Button;

    Zutubi.config.Button = Button.extend({
        init: function(element, options)
        {
            var that = this;
            that.structure = options.structure;
            Button.fn.init.call(this, element, {});
        },

        options: {
            name: "ZaButton"
        }
    });

    ui.plugin(Zutubi.config.Button);
}(jQuery));
