// dependency: ./namespace.js
// dependency: ./Checkbox.js

(function($)
{
    var ui = kendo.ui,
        Checkbox = Zutubi.config.Checkbox;

    Zutubi.config.ControllingCheckbox = Checkbox.extend({
        init: function(element, options)
        {
            var that = this;

            Checkbox.fn.init.call(this, element, options);

            that.options.parentForm.one("created", jQuery.proxy(that._updateDependents, that));

            that.inputElement.change(function()
            {
                that._updateDependents();
            });
        },

        options: {
            name: "ZaControllingCheckbox"
        },

        enable: function(enable)
        {
            Checkbox.fn.enable.call(this, enable);
            this._updateDependents();
        },

        bindValue: function(value)
        {
            Checkbox.fn.bindValue.call(this, value);
            this._updateDependents();
        },

        _updateDependents: function()
        {
            var checked,
                fields,
                field,
                i;

            if (!this.options.parentForm.options.readOnly)
            {
                checked = !this.inputElement.prop("disabled") && this.getValue();

                if (this._hasFields(this.options.structure.checkedFields) || this._hasFields(this.options.structure.uncheckedFields))
                {
                    this._updateFields(this.options.structure.checkedFields, checked);
                    this._updateFields(this.options.structure.uncheckedFields, !checked);
                }
                else
                {
                    fields = this.options.parentForm.getFields();
                    for (i = 0; i < fields.length; i++)
                    {
                        field = fields[i];
                        if (field !== this && field.enable)
                        {
                            field.enable(checked);
                        }
                    }
                }
            }
        },

        _hasFields: function(fieldNames)
        {
            return fieldNames && fieldNames.length > 0;
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

    ui.plugin(Zutubi.config.ControllingCheckbox);
}(jQuery));
