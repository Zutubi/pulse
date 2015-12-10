// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./HierarchyTree.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        BOUND = "bound",
        NODESELECT = "nodeselect";

    jQuery.expr[":"].ustartswith = jQuery.expr.createPseudo(function (arg) {
        return function (elem) {
            return jQuery(elem).text().toLowerCase().startsWith(arg);
        };
    });

    Zutubi.admin.HierarchySelector = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaHierarchySelector"
        },

        events: [
            BOUND,
            NODESELECT
        ],

        _create: function()
        {
            var that = this;

            that.lastFilterString = "";

            that.selected = "projects";

            that.outer = $('<div class="k-hierarchy-selector k-pane-content"></div>');
            that.element.append(that.outer);

            that.search = $('<input type="text" class="k-input k-textbox" placeholder="filter">');
            that.search.on("input", jQuery.proxy(that._filter, that));
            that.outer.append(that.search);
            that.clear = $('<i style="visibility: hidden" class="k-hierarchy-selector-clear fa fa-times"></i>');
            that.clear.on("click", jQuery.proxy(that._clear, that));
            that.outer.append(that.clear);

            that.treeEl = $('<div></div>');
            that.outer.append(that.treeEl);

            that.tree = that.treeEl.kendoZaHierarchyTree({scope: that.options.scope}).data("kendoZaHierarchyTree");
            that.tree.bind(BOUND, function(e) { that.trigger(BOUND); });
            that.tree.bind(NODESELECT, function(e) { that.trigger(NODESELECT, {name: e.name}); });
        },

        _filter: function()
        {
            var that = this,
                filterString = that.search.val();

            if (filterString !== that.lastFilterString)
            {
                if (that.lastFilterString)
                {
                    if (!filterString)
                    {
                        that.clear.css("visibility", "hidden");
                    }
                }
                else
                {
                    if (filterString)
                    {
                        that.clear.css("visibility", "visible");
                    }
                }

                that.lastFilterString = filterString;
                that.tree.prefixFilter(filterString);
            }

        },

        _clear: function()
        {
            this.search.val('');
            this._filter();
        },

        setScope: function(scope)
        {
            this.scope = scope;
            this._clear();
            this.tree.setScope(scope);
        },

        getRootName: function()
        {
            return this.tree.getRootName();
        },

        selectItem: function(name)
        {
            if (this.lastFilterString && name.indexOf(this.lastFilterString) !== 0)
            {
                this._clear();
            }

            this.tree.selectItem(name);
        }
    });

    ui.plugin(Zutubi.admin.HierarchySelector);
}(jQuery));
