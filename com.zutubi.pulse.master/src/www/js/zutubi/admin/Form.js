// dependency: ./namespace.js
// dependency: ./Button.js
// dependency: ./Checkbox.js
// dependency: ./ControllingCheckbox.js
// dependency: ./DropDownList.js
// dependency: ./ItemPicker.js
// dependency: ./PasswordField.js
// dependency: ./StringList.js
// dependency: ./TextArea.js
// dependency: ./TextField.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        SUBMIT = "submit",
        DEFAULT_SUBMITS = ["apply", "reset"],
        FIELD_TYPES = {
            checkbox: "kendoZaCheckbox",
            "controlling-checkbox": "kendoZaControllingCheckbox",
            "controlling-select": "kendoZaControllingDropDownList",
            dropdown: "kendoZaDropDownList",
            itempicker: "kendoZaItemPicker",
            password: "kendoZaPasswordField",
            stringlist: "kendoZaStringList",
            text: "kendoZaTextField",
            textarea: "kendoZaTextArea"
        };

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
            formName: "form",
            template: '<form name="#: id #" id="#: id #"><table class="form"><tbody></tbody></table></form>',
            hiddenTemplate: '<input type="hidden" id="#: id #" name="#: name #">',
            fieldTemplate: '<tr><th><label id="#: id #-label" for="#: id #">#: label #</label></th><td><span id="#: id #-wrap"></span></td></tr>',
            buttonTemplate: '<button id="#: id #" type="button" value="#: value #">#: name #</button>',
            errorTemplate: '<li>#: message #</li>'
        },

        _create: function()
        {
            var structure = this.options.structure,
                fields = structure.fields,
                submits = this.options.submits || DEFAULT_SUBMITS,
                fieldOptions,
                submitCell,
                i;

            this.id = "zaf-" + this.options.formName;

            this.fields = [];
            this.submits = [];
            this.template = kendo.template(this.options.template);
            this.hiddenTemplate = kendo.template(this.options.hiddenTemplate);
            this.fieldTemplate = kendo.template(this.options.fieldTemplate);
            this.buttonTemplate = kendo.template(this.options.buttonTemplate);
            this.errorTemplate = kendo.template(this.options.errorTemplate);

            this.element.html(this.template({id: this.id}));
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
            var fieldElement, fieldType;

            fieldOptions.id = this.id + "-" + fieldOptions.name;

            if (fieldOptions.type === "hidden")
            {
                this.formElement.append(this.hiddenTemplate(fieldOptions));
            }
            else
            {
                this.tableBodyElement.append(this.fieldTemplate(fieldOptions));
                fieldElement = this.tableBodyElement.find("#" + fieldOptions.id + "-wrap");

                fieldType = FIELD_TYPES[fieldOptions.type];
                if (fieldType)
                {
                    this.fields.push(fieldElement[fieldType]({
                        structure: fieldOptions,
                        parentForm: this
                    }).data(fieldType));
                }
                else
                {
                    console.warn("Ignoring unsupported field type '" + fieldOptions.type + "'");
                }
            }
        },

        _addSubmit: function(name, parentElement)
        {
            var that = this,
                id = this.id + "-submit-" + name,
                element,
                button;

            parentElement.append(this.buttonTemplate({name: name, value: name, id: id}));
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
            if (value === "reset")
            {
                this.resetValues();
            }
            else
            {
                this.clearValidationErrors();
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
            this.clearValidationErrors();
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
        },

        getFields: function()
        {
            return this.fields;
        },

        getFieldNamed: function(name)
        {
            var i, field;

            for (i = 0; i < this.fields.length; i++)
            {
                field = this.fields[i];
                if (field.getFieldName() === name)
                {
                    return field;
                }
            }

            return null;
        },

        clearValidationErrors: function()
        {
            this.element.find(".validation-errors").remove();
        },

        showValidationErrors: function(errorDetails)
        {
            var field, fieldErrors;

            if (errorDetails.instanceErrors)
            {
                this._showInstanceErrors(errorDetails.instanceErrors);
            }

            fieldErrors = errorDetails.fieldErrors;
            if (fieldErrors)
            {
                for (field in fieldErrors)
                {
                    if (fieldErrors.hasOwnProperty(field))
                    {
                        this._showFieldErrors(field, fieldErrors[field]);
                    }
                }
            }
        },

        _showInstanceErrors: function(messages)
        {
            // FIXME kendo : what is an appropriate spot for these?
        },

        _showFieldErrors: function(fieldName, messages)
        {
            var i, field, fieldCell, errorList;

            if (messages.length)
            {
                field = this.getFieldNamed(fieldName);
                if (field)
                {
                    fieldCell = field.element.closest("td");
                    fieldCell.append('<ul class="validation-errors"></ul>');
                    errorList = fieldCell.children('.validation-errors');
                    for (i = 0; i < messages.length; i++)
                    {
                        errorList.append(this.errorTemplate({message: messages[i]}));
                    }
                }
            }
        }
    });

    ui.plugin(Zutubi.admin.Form);
}(jQuery));
