// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget;

    Zutubi.admin.TextField = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaTextField",
            template: '<input class="k-input k-textbox" type="text" id="#: id #" name="#: name #">'
        },

        _create: function()
        {
            var options = this.options, width = "100%";

            this.template = kendo.template(options.template);
            this.element.html(this.template(options.structure));
            this.inputElement = this.element.find("input");

            if (options.structure.size)
            {
                width = options.structure.size + "px";
            }
            this.inputElement.css("width", width);

            if (options.structure.readOnly)
            {
                this.inputElement.attr("readonly", "");
            }

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
            this.inputElement.prop("value", value);
        },

        getValue: function()
        {
            return this.inputElement.prop("value");
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
        },

        isEnabled: function()
        {
            return !this.inputElement.prop("disabled");
        }
    });

    ui.plugin(Zutubi.admin.TextField);
}(jQuery));
