// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        ComboBox = ui.ComboBox,
        NULL_VALUE = "/";

    Zutubi.admin.ComboBox = ComboBox.extend({
        init: function(element, options)
        {
            var that = this,
                structure = options.structure,
                kendoOptions = {
                    dataSource: structure.list,
                    dataTextField: structure.listText,
                    dataValueField: structure.listValue
                };

            that.parentForm = options.parentForm;
            that.structure = structure;

            ComboBox.fn.init.call(this, element, kendoOptions);
        },

        options: {
            name: "ZaComboBox"
        },

        getFieldName: function()
        {
            return this.structure.name;
        },

        bindValue: function(value)
        {
            this.value(value);
        },

        getValue: function()
        {
            return this.value();
        }
    });

    ui.plugin(Zutubi.admin.ComboBox);
}(jQuery));
