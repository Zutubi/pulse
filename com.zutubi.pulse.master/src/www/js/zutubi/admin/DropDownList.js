// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        DropDownList = ui.DropDownList,
        NULL_VALUE = "/";

    Zutubi.admin.DropDownList = DropDownList.extend({
        init: function(element, options)
        {
            var that = this,
                structure = options.structure,
                kendoOptions = {
                dataSource: that._fixEmpty(structure.list, structure.listValue),
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
            if (value === null || value === "")
            {
                value = NULL_VALUE;
            }

            this.value(value);
        },

        getValue: function()
        {
            var value = this.value();
            if (value === NULL_VALUE)
            {
                value = null;
            }

            return value;
        },

        _fixEmpty: function(list, valueField)
        {
            // Kendo's DropDownList does odd things with empty values, so we use a dummy null value.
            var i, item, value, result = list;

            if (valueField && list.length > 0 && typeof list[0] === "object")
            {
                result = [];
                for (i = 0; i < list.length; i++)
                {
                    item = list[i];
                    value = item[valueField];
                    if (value === null || value ===  "")
                    {
                        item = jQuery.extend({}, item);
                        item[valueField] = NULL_VALUE;
                    }

                    result.push(item);
                }
            }

            return result;
        }
    });

    ui.plugin(Zutubi.admin.DropDownList);
}(jQuery));
