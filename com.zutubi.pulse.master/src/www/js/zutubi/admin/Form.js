// dependency: ./namespace.js
// dependency: ./Button.js
// dependency: ./Checkbox.js
// dependency: ./DropDownList.js
// dependency: ./TextField.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        SUBMIT = "submit";

    Zutubi.admin.Form = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        events: [
            SUBMIT
        ],

        options: {
            name: "ZaForm",
            template: '<form name="#: name #" id="#: id #"><table class="form"><tbody></tbody></table></form>',
            hiddenTemplate: '<input type="hidden" id="#: id #" name="#: name #">',
            fieldTemplate: '<tr><th><label id="#: id #-label" for="#: id #">#: label #</label></th><td></td></tr>',
            buttonTemplate: '<button id="#: id #" type="button" value="#: value #">#: name #</button>'
        },

        _create: function()
        {
            var structure = this.options.structure,
                fields = structure.fields,
                submits = structure.actions,
                fieldOptions,
                submitCell,
                i;

            this.fields = [];
            this.submits = [];
            this.template = kendo.template(this.options.template);
            this.hiddenTemplate = kendo.template(this.options.hiddenTemplate);
            this.fieldTemplate = kendo.template(this.options.fieldTemplate);
            this.buttonTemplate = kendo.template(this.options.buttonTemplate);

            this.element.html(this.template(structure));
            this.formElement = this.element.find("form");
            this.tableBodyElement = this.formElement.find("tbody");

            for (i = 0; i < fields.length; i++)
            {
                fieldOptions = fields[i];
                this._appendField(fieldOptions);
            }

            this.tableBodyElement.append('<tr><td class="submit" colspan="2"></td></tr>');
            submitCell = this.tableBodyElement.find(".submit");
            for (i = 0; i < submits.length; i++)
            {
                this._addSubmit(submits[i], submitCell);
            }

            if (this.options.values)
            {
                this.bindValues(this.options.values);
            }
        },

        _appendField: function(fieldOptions)
        {
            var fieldElement;

            fieldOptions.id = "zaf-" + fieldOptions.name;

            if (fieldOptions.type === "hidden")
            {
                this.formElement.append(this.hiddenTemplate(fieldOptions));
            }
            else
            {
                this.tableBodyElement.append(this.fieldTemplate(fieldOptions));
                fieldElement = this.tableBodyElement.children().last().find("td");

                if (fieldOptions.type === "checkbox")
                {
                    this.fields.push(fieldElement.kendoZaCheckbox({structure: fieldOptions}).data("kendoZaCheckbox"));
                }
                if (fieldOptions.type === "dropdown")
                {
                    this.fields.push(fieldElement.kendoZaDropDownList({structure: fieldOptions}).data("kendoZaDropDownList"));
                }
                else if (fieldOptions.type === "text")
                {
                    this.fields.push(fieldElement.kendoZaTextField({structure: fieldOptions}).data("kendoZaTextField"));
                }
            }
        },

        _addSubmit: function(name, parentElement)
        {
            var that = this,
                id = "zas-" + name,
                displayName = name,
                element,
                button;

            if (name === "cancel")
            {
                // FIXME kendo in the case of collapsed collection parent, this should not be done.
                // (that is the displayMode false case).
                displayName = "reset";
            }

            parentElement.append(this.buttonTemplate({name: displayName, value: name, id: id}));
            element = parentElement.find("button").last();
            button = element.kendoZaButton({structure: {value: name}}).data("kendoZaButton");
            button.bind("click", function(e)
            {
                that._buttonClicked(e.sender.structure.value);
            });

            that.submits.push(button);
        },

        _buttonClicked: function(value)
        {
            if (value === "cancel")
            {
                this.resetValues();
            }
            else
            {
                this.trigger(SUBMIT, {value: value});
            }
        },

        bindValues: function(values)
        {
            var i, field, name;

            if (typeof this.originalValues === "undefined")
            {
                this.originalValues = values;
            }

            for (i = 0; i < this.fields.length; i++)
            {
                field = this.fields[i];
                name = field.getFieldName();
                if (values.hasOwnProperty(name))
                {
                    field.bindValue(values[name]);
                }
            }
        },

        resetValues: function()
        {
            if (this.originalValues)
            {
                this.bindValues(this.originalValues);
            }
        },

        getValues: function()
        {
            var values = {}, i, field;

            for (i = 0; i < this.fields.length; i++)
            {
                field = this.fields[i];
                values[field.getFieldName()] = field.getValue();
            }

            return values;
        }
    });

    ui.plugin(Zutubi.admin.Form);
}(jQuery));
