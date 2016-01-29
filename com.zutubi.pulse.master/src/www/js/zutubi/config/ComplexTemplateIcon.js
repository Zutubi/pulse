// dependency: ./namespace.js
// dependency: ./TemplateIcon.js

(function($)
{
    var ui = kendo.ui,
        TemplateIcon = Zutubi.config.TemplateIcon;

    Zutubi.config.ComplexTemplateIcon = TemplateIcon.extend({
        init: function(element, options)
        {
            var iconOptions,
                model = options.model,
                panel = options.panel;

            $(element).show();
            if (model.templateOwner === model.templateOriginator)
            {
                iconOptions = {
                    spriteCssClass: "fa fa-arrow-circle-left",
                    items: [{
                        text: "first defined at this level of the hierarchy"
                    }]
                };
            }
            else
            {
                iconOptions = {
                    spriteCssClass: "fa fa-arrow-circle-up",
                    items: [{
                        text: "inherited from " + kendo.htmlEncode(model.templateOriginator),
                        action: "navigate",
                        owner: model.templateOriginator
                    }],
                    select: function(e)
                    {
                        panel.trigger("navigate", {owner: e.item.owner});
                    }
                };
            }

            TemplateIcon.fn.init.call(this, element, iconOptions);
        },

        options: {
            name: "ZaComplexTemplateIcon"
        }
    });

    ui.plugin(Zutubi.config.ComplexTemplateIcon);
}(jQuery));
