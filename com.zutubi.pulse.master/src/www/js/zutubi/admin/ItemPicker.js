// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        MultiSelect = ui.MultiSelect;

    Zutubi.admin.ItemPicker = MultiSelect.extend({
        init: function(element, options)
        {
            var that = this,
                tagTemplate,
                structure = options.structure,
                kendoOptions;

            that.structure = structure;

            kendoOptions = {
                dataSource: that._removeEmpty(structure.list),
                dataTextField: structure.listText,
                dataValueField: structure.listValue
            };

            MultiSelect.fn.init.call(this, element, kendoOptions);

            if (typeof structure.allowReordering === "undefined" || structure.allowReordering)
            {
                // This is hacking into the guts of the Kendo multiselect widget to add our own
                // content to tags: specifically drag handles.
                tagTemplate = kendo.template("#:" + kendo.expr(kendoOptions.dataTextField, "data") + "#", { useWithBlock: false });

                that.tagTemplate = function(data)
                {
                    return '<li class="k-button" unselectable="on"><span unselectable="on">' +
                        '<span class="k-handle">||</span> <span class="k-tag-content" unselectable="on">' +
                        tagTemplate(data) +
                        '</span></span><span unselectable="on" class="k-select"><span unselectable="on" class="k-icon k-i-close">delete</span></span></li>';
                };

                that.tagList.kendoSortable({
                    ignore: ".k-tag-content",
                    hint: function (element)
                    {
                        return element.clone().addClass("k-hint");
                    },
                    placeholder: function (element)
                    {
                        return element.clone().addClass("k-placeholder");
                    },
                    change: function()
                    {
                        var multiSelectItems = that.dataSource.data(),
                            sortedValues = [];
                        jQuery.each(this.items(), function(index, sortableItemEl)
                        {
                            var matchingDataItems = jQuery.grep(multiSelectItems, function (dataItem)
                            {
                                return dataItem.text === $(sortableItemEl).find('.k-tag-content').html();
                            });

                            if (matchingDataItems.length)
                            {
                                sortedValues.push(matchingDataItems[0].value);
                            }
                        });

                        that.value(sortedValues);
                        that.trigger("change");
                    }
                });
            }
        },

        options: {
            name: "ZaItemPicker"
        },

        // Overriding a private method (beware on upgrade!) so we can stop the list from opening
        // when a drag handle is clicked.
        _wrapperMousedown: function(e)
        {
            var that = this,
                notInput = e.target.nodeName.toLowerCase() !== "input";

            if (notInput)
            {
                e.preventDefault();
            }

            if (e.target.className.indexOf("k-delete") === -1 && e.target.className.indexOf("k-handle") === -1)
            {
                if (that.input[0] !== kendo._activeElement() && notInput)
                {
                    that.input.focus();
                }

                if (that.options.minLength === 0)
                {
                    that.open();
                }
            }
        },

        _removeEmpty: function(list)
        {
            // The list may have an empty option, but that is not needed and just confuses a blank
            // widget, so remove it.
            var valueKey = this.structure.listValue;
            list = jQuery.grep(list, function(element)
            {
                var t = valueKey ? element[valueKey] : element;
                return t !== null && t !== "";
            });

            return list;
        },

        getFieldName: function()
        {
            return this.structure.name;
        },

        bindValue: function(value)
        {
            // The kendo widget does not handle unexpected values gracefully, so we help cleanse.
            value = jQuery.grep(value, function(element)
            {
                return element !== null;
            });
            this.value(value);
        },

        getValue: function()
        {
            return this.value();
        }
    });

    ui.plugin(Zutubi.admin.ItemPicker);
}(jQuery));
