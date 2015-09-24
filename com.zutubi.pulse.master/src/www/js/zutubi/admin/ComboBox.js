// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        ComboBox = ui.ComboBox;

    Zutubi.admin.ComboBox = ComboBox.extend({
        init: function(element, options)
        {
            var that = this,
                structure = options.structure,
                kendoOptions;

            if (options.structure.lazy)
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
                                Zutubi.admin.ajax({
                                    url: window.baseUrl + "/api/action/options/" + options.parentForm.options.path + "/" + structure.name,
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
                    }
                }
            }
            else
            {
                kendoOptions = {
                    dataSource: structure.list,
                    dataTextField: structure.listText,
                    dataValueField: structure.listValue
                };
            }

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

    ui.plugin(Zutubi.admin.ComboBox);
}(jQuery));
