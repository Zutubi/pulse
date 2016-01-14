// dependency: ./namespace.js
// dependency: ./DropDownList.js

(function($)
{
    var ui = kendo.ui,
        DropDownList = Zutubi.config.DropDownList;

    Zutubi.config.ControllingDropDownList = DropDownList.extend({
        init: function(element, options)
        {
            var that = this;

            DropDownList.fn.init.call(this, element, options);

            that.bind("change", function()
            {
                that._updateDependents();
            });
        },

        options: {
            name: "ZaControllingDropDownList"
        },

        bindValue: function(value)
        {
            DropDownList.fn.bindValue.call(this, value);
            this._updateDependents();
        },

        _updateDependents: function()
        {
            var value,
                structure,
                enable,
                fieldNames,
                form = this.parentForm,
                fields,
                i,
                field;

            if (!form.options.readOnly)
            {
                value = this.getValue();
                structure = this.structure;
                enable = jQuery.inArray(value, structure.enableSet) !== -1;
                fieldNames = structure.dependentFields;

                if (fieldNames && fieldNames.length > 0)
                {
                    for (i = 0; i < fieldNames.length; i++)
                    {
                        field = form.getFieldNamed(fieldNames[i]);
                        if (field)
                        {
                            form.enableField(field, enable);
                        }
                    }
                }
                else
                {
                    fields = form.getFields();
                    for (i = 0; i < fields.length; i++)
                    {
                        field = fields[i];
                        if (field !== this)
                        {
                            form.enableField(field, enable);
                        }
                    }
                }
            }
        }
    });

    ui.plugin(Zutubi.config.ControllingDropDownList);
}(jQuery));
