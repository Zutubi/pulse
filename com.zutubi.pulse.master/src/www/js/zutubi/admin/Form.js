// dependency: ./namespace.js
// dependency: ./Checkbox.js
// dependency: ./TextField.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget;

    Zutubi.admin.Form = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        //events: [
        //],

        options: {
            name: "ZaForm",
            template: '<form name="#: name #" id="#: id #"><table class="form"><tbody></tbody></table></form>',
            hiddenTemplate: '<input type="hidden" id="#: id #" name="#: name #">',
            fieldTemplate: '<tr><th><label id="#: id #-label" for="#: id #">#: name #</label></th><td></td></tr>'
        },

        _create: function()
        {
            var structure = this.options.structure,
                fields = structure.fields,
                fieldOptions,
                i;

            this.fields = [];
            this.template = kendo.template(this.options.template);
            this.hiddenTemplate = kendo.template(this.options.hiddenTemplate);
            this.fieldTemplate = kendo.template(this.options.fieldTemplate);

            this.element.html(this.template(structure));
            // Can't use an id selector as the id may include jQuery special characters like ".".
            this.formElement = this.element.find("form");
            this.tableBodyElement = this.formElement.find("tbody");

            for (i = 0; i < fields.length; i++)
            {
                fieldOptions = fields[i];
                this._appendField(fieldOptions);
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
                else if (fieldOptions.type === "text")
                {
                    this.fields.push(fieldElement.kendoZaTextField({structure: fieldOptions}).data("kendoZaTextField"));
                }
            }
        },

        bindValues: function(values)
        {
            var i, field, name;

            for (i = 0; i < this.fields.length; i++)
            {
                field = this.fields[i];
                name = field.getFieldName();
                if (values.hasOwnProperty(name))
                {
                    field.bindValue(values[name])
                }
            }
        }
    });

    ui.plugin(Zutubi.admin.Form);
}(jQuery));
