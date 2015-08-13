// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget;

    Zutubi.admin.Checkbox = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaCheckbox",
            template: '<input class="k-input" type="checkbox" id="#: id #" name="#: name #" value="true">'
        },

        _create: function()
        {
            var options = this.options;

            this.template = kendo.template(options.template);
            this.element.html(this.template(options.structure));
            this.inputElement = this.element.find("input");

            if (typeof options.value !== "undefined")
            {
                this.bindValue(options.value);
            }
        },

        getFieldName: function()
        {
            return this.options.structure.name;
        },

        bindValue: function(value)
        {
            this.inputElement.prop("checked", value);
        }
    });

    ui.plugin(Zutubi.admin.Checkbox);
}(jQuery));
