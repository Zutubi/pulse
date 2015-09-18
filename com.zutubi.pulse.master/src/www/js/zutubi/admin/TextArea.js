// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        CHARS_PER_ROW = 60,
        MAX_ROWS = 10;

    Zutubi.admin.TextArea = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaTextArea",
            template: '<textarea class="k-input k-textbox" id="#: id #" name="#: name #" autocomplete="off">'
        },

        _create: function()
        {
            var options = this.options,
                width = "100%";

            this.template = kendo.template(options.template);
            this.element.html(this.template(options.structure));
            this.inputElement = this.element.find("textarea");

            if (options.structure.cols)
            {
                width = options.structure.cols + "ch";
            }

            this.inputElement.css("width", width);

            if (options.structure.rows)
            {
                this.inputElement.prop("rows", options.structure.rows);
            }

            if (typeof options.value !== "undefined")
            {
                this.bindValue(options.value);
            }
        },

        autoSize: function()
        {
            var width = CHARS_PER_ROW + "ch",
                value = this.getValue(),
                rows;

            if (value)
            {
                rows = Math.ceil(value.length / CHARS_PER_ROW);
                rows = Math.max(0, Math.min(rows, MAX_ROWS));

                this.inputElement.css("width", width);
                this.inputElement.prop("rows", rows);
            }
        },

        getFieldName: function()
        {
            return this.options.structure.name;
        },

        bindValue: function(value)
        {
            this.inputElement.prop("value", value);
            if (this.options.structure.autoSize)
            {
                this.autoSize();
            }
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
        }
    });

    ui.plugin(Zutubi.admin.TextArea);
}(jQuery));
