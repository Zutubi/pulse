// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ns = ".kendoZaPropertyTable",
        CLICK = "click" + ns;

    Zutubi.core.PropertyTable = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaPropertyTable",
            title: '',
            template: '<table id="#= id #" class="k-property-table"><tbody></tbody></table>',
            titleTemplate: '<tr><td class="k-title" colspan="2">#: title #</td></tr>',
            rowTemplate: '<tr><th>#: label #</th><td></td>'
        },

        _create: function()
        {
            var that = this,
                data = that.options.data,
                i,
                item,
                row;

            that.template = kendo.template(that.options.template);
            that.rowTemplate = kendo.template(that.options.rowTemplate);

            that.element.html(that.template({id: that.options.id}));
            that.tbody = that.element.find("tbody");

            if (that.options.title)
            {
                that.titleTemplate = kendo.template(that.options.titleTemplate);
                that.tbody.append(that.titleTemplate(that.options));
            }

            for (i = 0; i < data.length; i++)
            {
                item = data[i];
                row = $(that.rowTemplate({
                    label: item.label || item.key
                }));
                row.find("td").html(that._renderValue(item.value));
                that.tbody.append(row);
            }

            that.tbody.on(CLICK, jQuery.proxy(that._clicked, that));
        },

        destroy: function()
        {
            this.tbody.off(ns);
            Widget.fn.destroy.call(this);
        },

        _clicked: function(e)
        {
            var item = $(e.target).closest(".k-collapsed,.k-expanded");
            if (item)
            {
                if (item.hasClass("k-collapsed"))
                {
                    item.removeClass("k-collapsed");
                    item.addClass("k-expanded");
                }
                else
                {
                    item.removeClass("k-expanded");
                    item.addClass("k-collapsed");
                }
            }
        },

        _renderValue: function(value)
        {
            var list, i, wrapper;

            if (typeof value === "string")
            {
                return kendo.htmlEncode(value);
            }
            else if (Array.isArray(value))
            {
                if (value.length === 0)
                {
                    return "-";
                }
                else if (typeof value[0] === "string")
                {
                    // List of strings.
                    list = $('<ul class="k-simple-value-list"></ul>');
                    for (i = 0; i < value.length; i++)
                    {
                        list.append('<li>' + kendo.htmlEncode(value[i]) + '</li>');
                    }
                }
                else
                {
                    // List of nested key-value pairs, only one level of nesting is supported
                    // (we don't recursively render, rather output an unordered list).
                    list = $('<ul class="k-property-list"></ul>');
                    for (i = 0; i < value.length; i++)
                    {
                        list.append('<li class="k-collapsed">' + this._renderNested(value[i].label || value[i].key, value[i].value) + '</li>');
                    }
                }

                wrapper = $('<div class="k-list-limiter"></div>');
                wrapper.append(list);
                return wrapper[0].outerHTML;
            }
        },

        _renderNested: function(key, value)
        {
            var result, inner, i;

            result = $('<li>' + kendo.htmlEncode(key) + '<ul></ul></li>');
            inner = result.find("ul");
            for (i = 0; i < value.length; i++)
            {
                inner.append('<li>' + kendo.htmlEncode(value[i]) + '</li>');
            }

            return result.html();
        }
    });

    ui.plugin(Zutubi.core.PropertyTable);
}(jQuery));
