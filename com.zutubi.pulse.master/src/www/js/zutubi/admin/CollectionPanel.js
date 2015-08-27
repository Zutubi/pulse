// dependency: ./namespace.js
// dependency: ./Table.js

(function($)
{
    var Observable = kendo.Observable,
        NS = ".kendoZaCollectionPanel",
        ADD = "add",
        REORDER = "reorder";

    Zutubi.admin.CollectionPanel = Observable.extend({
        init: function (config)
        {
            var that = this,
                collection = config.collection,
                createAllowed = jQuery.inArray("create", collection.allowedActions) !== -1,
                writeAllowed = (jQuery.inArray("write", collection.allowedActions) !== -1);

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

            that.view.render($(config.containerSelector));

            that.addElement = $("#collection-add");
            that.addButton = that.addElement.kendoButton({spriteCssClass: "fa fa-plus-circle"}).data("kendoButton");
            if (createAllowed)
            {
                that.addElement.on("click" + NS, jQuery.proxy(that._addClicked, that));
            }

            that.table = $("#collection-table").kendoZaTable({
                structure: collection.table,
                items: collection.nested,
                allowSorting: collection.type.ordered && writeAllowed
            }).data("kendoZaTable");

            that.table.bind(REORDER, function(e)
            {
                that.trigger(REORDER, e);
            });
        },

        events: [
            ADD,
            REORDER
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            this.addElement.off(NS);
            this.view.destroy();
        },

        _addClicked: function(e)
        {
            e.preventDefault();
            this.trigger(ADD);
        }
    });
}(jQuery));
