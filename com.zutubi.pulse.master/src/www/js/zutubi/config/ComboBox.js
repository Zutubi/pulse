// dependency: ./namespace.js
// zutubi/core/package.js

(function($)
{
    var ui = kendo.ui,
        ComboBox = ui.ComboBox;

    Zutubi.config.ComboBox = ComboBox.extend({
        init: function(element, options)
        {
            var that = this,
                structure = options.structure,
                formOptions = options.parentForm.options,
                kendoOptions;

            if (structure.lazy)
            {
                kendoOptions = {
                    autoBind: false,
                    dataBound: function()
                    {
                        // Rebind the value so it is selected in the list.
                        that.bindValue(this.input.val());
                    },
                    dataSource: {
                        transport: {
                            read: function(o)
                            {
                                Zutubi.core.ajax({
                                    method: "POST",
                                    url: window.baseUrl + "/api/action/options/" + Zutubi.config.encodePath(formOptions.parentPath),
                                    data: {
                                        symbolicName: formOptions.symbolicName,
                                        baseName: formOptions.baseName,
                                        propertyName: structure.name
                                    },
                                    success: function(result)
                                    {
                                        o.success(result);
                                    },
                                    error: function(result)
                                    {
                                        o.error(result);
                                    }
                                });
                            }
                        }
                    },
                    structure: structure
                };
            }
            else
            {
                kendoOptions = {
                    dataSource: structure.list,
                    dataTextField: structure.listText,
                    dataValueField: structure.listValue,
                    structure: structure
                };
            }

            that.parentForm = options.parentForm;

            ComboBox.fn.init.call(this, element, kendoOptions);
        },

        options: {
            name: "ZaComboBox"
        },

        getFieldName: function()
        {
            return this.options.structure.name;
        },

        bindValue: function(value)
        {
            if (!this.listView.isBound())
            {
                // A shortcut allowed because lazy loading only supports strings (i.e. text === value).
                this.input.val(value);
            }
            else
            {
                this.value(value);
            }
        },

        getValue: function()
        {
            if (!this.listView.isBound())
            {
                // A shortcut allowed because lazy loading only supports strings (i.e. text === value).
                return this.input.val();
            }
            else
            {
                return this.value();
            }
        }
    });

    ui.plugin(Zutubi.config.ComboBox);
}(jQuery));
