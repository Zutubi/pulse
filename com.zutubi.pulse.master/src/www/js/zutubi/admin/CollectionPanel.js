// dependency: ./namespace.js
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
                writeAllowed = (jQuery.inArray("write", collection.allowedActions) !== -1);

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#: id #" class="k-collection-panel">' +
                    '<button id="#: id #-add" class="k-collection-add"><span class="k-sprite"></span> add</button>' +
                    '<h1>#: label #</h1>' +
                    '<div id="#: id #-table"></div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: "collection",
                        label: collection.label
                    }
                });

            that.view.render($(options.containerSelector));

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

            that.table = $("#collection-table").kendoZaTable({
                structure: collection.table,
                items: collection.nested,
                allowSorting: collection.type.ordered && writeAllowed
            }).data("kendoZaTable");

            that.table.bind(ACTION, function(e)
            {
                that.trigger(ACTION, e);
            });

            that.table.bind("reorder", jQuery.proxy(that._reordered, that));
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

        _addClicked: function(e)
        {
            e.preventDefault();
            this.trigger(ADD);
        },

        _reordered: function(order)
        {
            var that = this;

            Zutubi.admin.ajax({
                type: "PUT",
                url: "/api/config/" + that.options.path + "?depth=-1",
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
                    console.log('set order succcess');
                    console.dir(data);
                    that.trigger(REORDERED, {delta: data});
                },
                error: function (jqXHR)
                {
                    Zutubi.admin.reportError("Could not save order: " + Zutubi.admin.ajaxError(jqXHR));
                }
            });
        }
    });
}(jQuery));
