// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./Table.js

(function($)
{
    var Observable = kendo.Observable,
        ACTION = "action",
        ADD = "add",
        NAVIGATE = "navigate",
        REORDERED = "reordered";

    Zutubi.admin.CollectionPanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                collection = options.collection,
                createAllowed = jQuery.inArray("create", collection.allowedActions) !== -1,
                writeAllowed = this._isWriteAllowed(collection),
                actionsEl,
                el,
                message;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#: id #" class="k-collection-panel">' +
                    '<span class="k-template-icon-wrapper" style="display: none"></span>' +
                    '<span class="k-collection-actions">' +
                    '</span>' +
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

            actionsEl = that.view.element.find(".k-collection-actions");
            if (collection.type.ordered)
            {
                that._renderDeclaredOrder(collection);

                message = "ordered collection";
                if (writeAllowed)
                {
                    message += ", drag and drop rows to reorder";
                }

                el = $('<span class="k-collection-ordered-indicator fa fa-list-ol"></span>').appendTo(actionsEl);
                el.kendoTooltip({ content: message});
            }

            if (createAllowed)
            {
                el = $('<button><span class="k-sprite"></span> add</button>').appendTo(actionsEl);
                that.addButton = el.kendoButton({spriteCssClass: "fa fa-plus-circle"}).data("kendoButton");
                that.addButton.bind("click", jQuery.proxy(that._addClicked, that));
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

            that._renderTable(collection);

            if (collection.hiddenItems && collection.hiddenItems.length > 0)
            {
                that._renderHiddenItems(collection.hiddenItems);
            }
        },

        events: [
            ACTION,
            ADD,
            NAVIGATE,
            REORDERED
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            this.view.destroy();
            kendo.destroy(this.view.element);
        },

        updateItem: function(key, data)
        {
            this.table.updateItem(key, data);
        },

        _isWriteAllowed: function(collection)
        {
            return jQuery.inArray("write", collection.allowedActions) !== -1;
        },

        _renderDeclaredOrder: function(collection)
        {
            var el,
                writeAllowed = this._isWriteAllowed(collection),
                items,
                templateIcon;

            this.view.element.find(".k-collection-declared-order").remove();
            if (collection.declaredOrder)
            {
                el = $('<span class="k-collection-declared-order"></span>').prependTo(this.view.element.find(".k-collection-actions"));
                if (collection.orderOverriddenOwner)
                {
                    items = [{
                        text: "overrides order defined in " + kendo.htmlEncode(collection.orderOverriddenOwner),
                        action: "navigate",
                        owner: collection.orderOverriddenOwner
                    }];

                    if (writeAllowed)
                    {
                        items.push({
                            text: "revert to inherited order",
                            action: "clear"
                        });
                    }

                    templateIcon = el.kendoZaTemplateIcon({
                        spriteCssClass: "fa fa-arrow-circle-right",
                        items: items
                    }).data("kendoZaTemplateIcon");
                }
                else if (collection.orderTemplateOwner !== collection.templateOwner)
                {
                    templateIcon = el.kendoZaTemplateIcon({
                        spriteCssClass: "fa fa-arrow-circle-up",
                        items: [{
                            text: "inherits order defined in " + kendo.htmlEncode(collection.orderTemplateOwner),
                            action: "navigate",
                            owner: collection.orderTemplateOwner
                        }]
                    }).data("kendoZaTemplateIcon");
                }
                else
                {
                    templateIcon = el.kendoZaTemplateIcon({
                        spriteCssClass: "fa fa-arrow-circle-left",
                        items: [{
                            text: "explicit order defined" + (writeAllowed ? ", revert to natural order" : ""),
                            action: writeAllowed ? "clear" : ""
                        }]
                    }).data("kendoZaTemplateIcon");
                }

                templateIcon.bind("select", jQuery.proxy(this._orderAction, this));
            }
        },

        _orderAction: function(e)
        {
            var action = e.item.action;

            if (action === "clear")
            {
                this._setOrder([]);
            }
            else
            {
                this.trigger(NAVIGATE, {owner: e.item.owner});
            }
        },

        _renderTable: function(collection)
        {
            var that = this,
                el = $("#collection-table");

            if (that.table)
            {
                that.table.destroy();
                el.empty();
            }

            el = $('<div></div>').appendTo(el);
            that.table = el.kendoZaTable({
                structure: collection.table,
                items: collection.nested,
                templateOriginator: collection.templateOriginator,
                templateOwner: collection.templateOwner,
                panel: that,
                allowSorting: collection.type.ordered && this._isWriteAllowed(collection)
            }).data("kendoZaTable");

            that.table.bind(ACTION, function(e)
            {
                that.trigger(ACTION, {path: that.options.path + "/" + e.key, action: e.action});
            });

            that.table.bind("reorder", jQuery.proxy(that._reordered, that));
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

        _reordered: function()
        {
            this._setOrder(this.table.getOrder());
        },

        _setOrder: function(order)
        {
            var that = this;

            Zutubi.core.ajax({
                type: "PUT",
                maskAll: true,
                url: "/api/config/" + Zutubi.config.encodePath(that.options.path) + "?depth=2",
                data: {
                    kind: "collection",
                    nested: jQuery.map(order, function(key)
                    {
                        return {
                            kind: "composite",
                            key: key
                        };
                    })
                },
                success: function (delta)
                {
                    var collection,
                        newOrder;
                    if (delta.updatedPaths && delta.updatedPaths.length > 0)
                    {
                        collection = delta.models[delta.updatedPaths[0]];
                        that.options.collection = collection;

                        that._renderDeclaredOrder(collection);

                        newOrder = jQuery.map(collection.nested, function(item)
                        {
                            return item.key;
                        });

                        if (!Zutubi.core.arraysEqual(newOrder, that.table.getOrder()))
                        {
                            that._renderTable(collection);
                        }
                    }

                    that.trigger(REORDERED, {delta: delta});
                },
                error: function (jqXHR)
                {
                    Zutubi.core.reportError("Could not save order: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        }
    });
}(jQuery));
