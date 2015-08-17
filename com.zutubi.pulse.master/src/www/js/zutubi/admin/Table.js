// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        TABLE_ID = "za-collection-table";

    Zutubi.admin.Table = Widget.extend({
        init: function (element, options) {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaTable",
            template: '<div id="#: id #" style="margin: 20px"></div>'
        },

        _create: function () {
            var columns = this._formatColumns(),
                data = this._formatData();

            this.template = kendo.template(this.options.template);
            this.element.html(this.template({id: TABLE_ID}));

            $("#" + TABLE_ID).kendoGrid({
                dataSource: data,
                columns: columns
            });
        },

        _formatColumns: function()
        {
            var originalColumns = this.options.structure.columns,
                columns = [],
                column,
                i;

            for (i = 0; i < originalColumns.length; i++)
            {
                column = originalColumns[i];
                columns.push({title: column.label, field: column.name});
            }

            return columns;
        },

        _formatData: function()
        {
            var columns = this.options.structure.columns,
                items = this.options.items,
                data = [],
                item,
                column,
                row,
                i, j;

            for (i = 0; i < items.length; i++)
            {
                item = items[i];
                row = {};
                for (j = 0; j < columns.length; j++)
                {
                    column = columns[j];
                    row[column.name] = this._getValue(item, column.name);
                }

                data.push(row);
            }

            return data;
        },

        _getValue: function(item, name)
        {
            if (item.formattedProperties && item.formattedProperties.hasOwnProperty(name))
            {
                return item.formattedProperties[name];
            }

            return item.properties[name];
        }
    });

    ui.plugin(Zutubi.admin.Table);
}(jQuery));
