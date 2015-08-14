// dependency: ./namespace.js
// dependency: ./Checkbox.js

(function($)
{
    var ui = kendo.ui,
        Checkbox = Zutubi.admin.Checkbox;

    Zutubi.admin.ControllingCheckbox = Checkbox.extend({
        init: function(element, options)
        {
            var that = this;

            Checkbox.fn.init.call(this, element, options);

            that._create();

            that.inputElement.change(function()
            {
                that._updateDependents();
            });
        },

        options: {
            name: "ZaControllingCheckbox"
        },

        bindValue: function(value)
        {
            Checkbox.fn.bindValue.call(this, value);
            this._updateDependents();
        },

        _updateDependents: function()
        {
            var checked = this.getValue();
            this._updateFields(this.options.structure.checkedFields, checked);
            this._updateFields(this.options.structure.uncheckedFields, !checked);
        },

        _updateFields: function(fieldNames, enable)
        {
            var i,
                field,
                form = this.options.parentForm;

            if (fieldNames)
            {
                for (i = 0; i < fieldNames.length; i++)
                {
                    field = form.getFieldNamed(fieldNames[i]);
                    if (field)
                    {
                        field.enable(enable);
                    }
                }
            }
        }
    });

    ui.plugin(Zutubi.admin.ControllingCheckbox);
}(jQuery));
