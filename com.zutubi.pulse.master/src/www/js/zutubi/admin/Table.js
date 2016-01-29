// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ACTION = "action",
        REORDER = "reorder";

    Zutubi.admin.Table = Widget.extend({
        init: function (element, options) {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaTable",
            template: '<div id="#: id #" class="k-collection-table"></div>',
            id: "za-collection-table"
        },

        events: [
            ACTION,
            REORDER
        ],

        _create: function ()
        {
            var that = this,
                templateDecorate = that._needsTemplateDecoration(),
                columns = that._formatColumns(templateDecorate),
                data = that._formatData(templateDecorate);

            that.template = kendo.template(that.options.template);
            that.element.html(that.template(that.options));

            that.grid = $("#" + that.options.id).kendoGrid({
                dataSource: new kendo.data.DataSource({
                    data: data,
                    schema: {
                        model: { id: "key" }
                    }
                }),
                columns: columns,
                scrollable: false
            }).data("kendoGrid");

            that._addDecorationsAndMenus();

            if (that.options.allowSorting)
            {
                that.grid.table.kendoSortable({
                    filter: ">tbody >tr",
                    hint: $.noop,
                    cursor: "move",
                    placeholder: function(element)
                    {
                        return element.clone().addClass("k-state-hover").css("opacity", 0.65);
                    },
                    container: "#" + that.options.id + " tbody",
                    change: function(e)
                    {
                        var grid = that.grid,
                            dataItem = grid.dataSource.getByUid(e.item.data("uid"));

                        grid.dataSource.remove(dataItem);
                        grid.dataSource.insert(e.newIndex, dataItem);
                        that._addDecorationsAndMenus();
                        that.trigger(REORDER, {item: dataItem, oldIndex: e.oldIndex, newIndex: e.newIndex});
                    }
                });
            }
        },

        _needsTemplateDecoration: function()
        {
            var options = this.options,
                items = options.items,
                i;

            if (options.templateOriginator)
            {
                for (i = 0; i < items.length; i++)
                {
                    if (items[i].templateOriginator !== options.templateOriginator)
                    {
                        return true;
                    }
                }
            }
        },

        _formatColumns: function(templateDecorated)
        {
            var originalColumns = this.options.structure.columns,
                columns = [],
                column,
                i;

            if (templateDecorated)
            {
                columns.push({
                    title: "",
                    template: '<span class="k-template-decoration"></span>',
                    width: 36
                });
            }

            for (i = 0; i < originalColumns.length; i++)
            {
                column = originalColumns[i];
                columns.push({title: column.label, field: column.name});
            }

            columns.push({
                title: "actions",
                template: '<ul class="k-collection-menu"></ul>',
                width: 100
            });

            return columns;
        },

        _formatData: function(templateDecorate)
        {
            var items = this.options.items,
                data = [],
                i;

            for (i = 0; i < items.length; i++)
            {
                data.push(this._formatItem(items[i], templateDecorate));
            }

            return data;
        },

        _formatItem: function(item, templateDecorate)
        {
            var columns = this.options.structure.columns,
                i,
                row,
                column;

            row = {
                key: item.key,
                actions: jQuery.grep(item.actions, function(action)
                {
                    return action.label !== "configure";
                })
            };

            if (templateDecorate)
            {
                row.templateOriginator = item.templateOriginator;
                row.templateOwner = item.templateOwner;
            }

            for (i = 0; i < columns.length; i++)
            {
                column = columns[i];
                row[column.name] = this._getValue(item, column.name);
            }

            return row;
        },

        _addDecorationsAndMenus: function()
        {
            var that = this,
                i = 0,
                item;

            that.grid.table.find(".k-template-decoration").each(function(index, el)
            {
                item = that.grid.dataSource.at(i);
                that._addTemplateDecoration(item, el);
                i++;
            });

            i = 0;
            that.grid.table.find(".k-collection-menu").each(function(index, el)
            {
                item = that.grid.dataSource.at(i);
                that._addActionMenu(item, el, i);
                i++;
            });
        },

        _addTemplateDecoration: function(item, el)
        {
            if (item.templateOriginator !== this.options.templateOriginator)
            {
                $(el).kendoZaComplexTemplateIcon({
                    model: item,
                    panel: this.options.panel
                });
            }
        },

        _addActionMenu: function(item, el, row)
        {
            var that = this,
                menuItems;

            if (item.actions && item.actions.length > 0)
            {
                menuItems = jQuery.map(item.actions, function(action)
                {
                    return {text: action.label};
                });

                $(el).kendoMenu({
                    dataSource: [{
                        text: "... ",
                        items: menuItems
                    }],
                    select: jQuery.proxy(that._actionSelected, that, row)
                });
            }
        },

        _actionSelected: function(row, e)
        {
            var item = this.grid.dataSource.at(row),
                actionLabel = $(e.item).text(),
                actions;

            actions = jQuery.grep(item.actions, function(action)
            {
                return action.label === actionLabel;
            });

            if (actions.length > 0)
            {
                this.trigger(ACTION, {key: item.key, action: actions[0]});
            }
        },

        _getValue: function(item, name)
        {
            if (item.formattedProperties && item.formattedProperties.hasOwnProperty(name))
            {
                return item.formattedProperties[name];
            }

            return item.properties[name];
        },

        getOrder: function()
        {
            return jQuery.map(this.grid.dataSource.data(), function(item)
            {
                return item.key;
            });
        },

        updateItem: function(key, data)
        {
            var dataSource = this.grid.dataSource,
                existingItem = dataSource.get(key),
                index;

            if (existingItem)
            {
                index = dataSource.indexOf(existingItem);
                dataSource.remove(existingItem);
                if (data)
                {
                    dataSource.insert(index, this._formatItem(data));
                }
                this._addDecorationsAndMenus();
            }
        }
    });

    ui.plugin(Zutubi.admin.Table);
}(jQuery));
