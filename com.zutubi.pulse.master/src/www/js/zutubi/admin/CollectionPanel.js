// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./Table.js

(function($)
{
    var Observable = kendo.Observable,
        ACTION = "action",
        ADD = "add",
        REORDERED = "reordered";

    Zutubi.admin.CollectionPanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                collection = options.collection,
                createAllowed = jQuery.inArray("create", collection.allowedActions) !== -1,
                writeAllowed = (jQuery.inArray("write", collection.allowedActions) !== -1),
                el;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#: id #" class="k-collection-panel">' +
                    '<span class="k-template-icon-wrapper" style="display: none"></span>' +
                    '<button id="#: id #-add" class="k-collection-add"><span class="k-sprite"></span> add</button>' +
                    '<h1>#: label #</h1>' +
                    '<div style="display:none" class="k-state-wrapper">' +
                    '</div>' +
                    '<div id="#: id #-table"></div>' +
                    '<div id="#: id #-hidden-wrapper" class="k-collection-hidden" style="display: none">' +
                        '<h1>hidden items</h1>' +
                        '<div id="#: id #-hidden"></div>' +
                    '</div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: "collection",
                        label: collection.label
                    }
                });

            that.view.render(options.container);

            if (collection.templateOwner)
            {
                that.templateIcon = that.view.element.find(".k-template-icon-wrapper").kendoZaComplexTemplateIcon({
                    model: collection,
                    panel: that
                }).data("kendoZaComplexTemplateIcon");
            }

            that.addElement = $("#collection-add");
            if (createAllowed)
            {
                that.addButton = that.addElement.kendoButton({spriteCssClass: "fa fa-plus-circle"}).data("kendoButton");
                that.addButton.bind("click", jQuery.proxy(that._addClicked, that));
            }
            else
            {
                that.addElement.hide();
            }

            if (collection.state && collection.state.fields)
            {
                el = that.view.element.find(".k-state-wrapper");
                el.kendoZaPropertyTable({
                    id: "composite-state",
                    title: collection.state.label,
                    data: collection.state.fields
                }).data("kendoZaPropertyTable");
                el.show();
            }

            that.table = $("#collection-table").kendoZaTable({
                structure: collection.table,
                items: collection.nested,
                templateOriginator: collection.templateOriginator,
                templateOwner: collection.templateOwner,
                panel: that,
                allowSorting: collection.type.ordered && writeAllowed
            }).data("kendoZaTable");

            that.table.bind(ACTION, function(e)
            {
                that.trigger(ACTION, {path: that.options.path + "/" + e.key, action: e.action});
            });

            that.table.bind("reorder", jQuery.proxy(that._reordered, that));

            if (collection.hiddenItems && collection.hiddenItems.length > 0)
            {
                that._renderHiddenItems(collection.hiddenItems);
            }
        },

        events: [
            ACTION,
            ADD,
            REORDERED
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            this.view.destroy();
        },

        updateItem: function(key, data)
        {
            this.table.updateItem(key, data);
        },

        _addClicked: function(e)
        {
            e.preventDefault();
            this.trigger(ADD);
        },

        _renderHiddenItems: function(items)
        {
            var that = this;

            that.hiddenTable = $("#collection-hidden").kendoZaTable({
                id: "hidden-grid",
                structure: {
                    columns: [{
                        name: 'key',
                        label: 'name'
                    }, {
                        name: 'templateOwner',
                        label: 'hidden owner'
                    }
                ]},
                items: jQuery.map(items, function(item)
                {
                    return {
                        properties: item,
                        actions: [{
                            action: "restore",
                            label: "restore"
                        }]
                    };
                }),
                allowSorting: false
            }).data("kendoZaTable");

            this.hiddenTable.bind(ACTION, function(e)
            {
                that.trigger(ACTION, {path: that.options.path + "/" + e.key, action: e.action});
            });

            $("#collection-hidden-wrapper").show();
        },

        _reordered: function(order)
        {
            var that = this;

            Zutubi.core.ajax({
                type: "PUT",
                maskAll: true,
                url: "/api/config/" + Zutubi.config.encodePath(that.options.path) + "?depth=-1",
                data: {
                    kind: "collection",
                    nested: jQuery.map(that.table.getOrder(), function(key)
                    {
                        return {
                            kind: "composite",
                            key: key
                        };
                    })
                },
                success: function (data)
                {
                    that.trigger(REORDERED, {delta: data});
                },
                error: function (jqXHR)
                {
                    Zutubi.core.reportError("Could not save order: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        }
    });
}(jQuery));
