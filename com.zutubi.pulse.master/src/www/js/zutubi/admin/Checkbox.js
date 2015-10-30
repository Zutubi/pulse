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
            // FIXME kendo this is hackish
            if (value === "true")
            {
                value = true;
            }
            else if (value === "false")
            {
                value = false;
            }
            this.inputElement.prop("checked", value);
        },

        getValue: function()
        {
            return this.inputElement.prop("checked");
        },

        enable: function(enable)
        {
            this.inputElement.prop("disabled", !enable);
            if (enable)
            {
                this.inputElement.removeClass("k-state-disabled");
            }
            else
            {
                this.inputElement.addClass("k-state-disabled");
            }
        }
    });

    ui.plugin(Zutubi.admin.Checkbox);
}(jQuery));
