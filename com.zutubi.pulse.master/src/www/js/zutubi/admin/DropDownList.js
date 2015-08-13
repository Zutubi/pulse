// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        DropDownList = ui.DropDownList;

    Zutubi.admin.DropDownList = DropDownList.extend({
        init: function(element, options)
        {
            var that = this,
                structure = options.structure,
                kendoOptions = {
                dataSource: structure.list,
                dataTextField: structure.listText,
                dataValueField: structure.listValue
            };

            that.structure = structure;

            DropDownList.fn.init.call(this, element, kendoOptions);
        },

        options: {
            name: "ZaDropDownList"
        },

        getFieldName: function()
        {
            return this.structure.name;
        },

        bindValue: function(value)
        {
            this.value(value);
        }
    });

    ui.plugin(Zutubi.admin.DropDownList);
}(jQuery));
