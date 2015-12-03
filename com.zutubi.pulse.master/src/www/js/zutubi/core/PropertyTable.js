// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget;

    Zutubi.core.PropertyTable = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaPropertyTable",
            template: '<table id="#= id #" class="k-property-table"><tbody></tbody></table>',
            rowTemplate: '<tr><th>#: key #</th><td>#: value #</td>'
        },

        _create: function()
        {
            var that = this,
                data = that.options.data,
                i;

            that.template = kendo.template(that.options.template);
            that.rowTemplate = kendo.template(that.options.rowTemplate);

            that.element.html(that.template({id: that.options.id}));
            that.tbody = that.element.find("tbody");

            for (i = 0; i < data.length; i++)
            {
                that.tbody.append(that.rowTemplate(data[i]));
            }
        }
    });

    ui.plugin(Zutubi.core.PropertyTable);
}(jQuery));
