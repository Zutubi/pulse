// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        NODESELECT = "nodeselect";

    Zutubi.admin.HierarchyTree = TreeView.extend({
        init: function(element, options)
        {
            TreeView.fn.init.call(this, element, options);

            this.bound = false;

            if (options && options.scope)
            {
                this.setScope(options.scope);
            }
        },

        events: [
            // FIXME kendo: to subscribe to our own select event we need to have this here, is there a better way?
            "dataBound",
            "select",
            NODESELECT
        ],

        options: {
            name: "ZaHierarchyTree",
            dataTextField: "name",
            loadOnDemand: false,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node.
                if (!e.node)
                {
                    this.bound = true;
                    this._updateSelected();
                }
            },
            select: function(e)
            {
                var that = this;
                that.trigger(NODESELECT, {name: that.dataItem(e.node).name});
            }
        },

        setScope: function(scope)
        {
            var dataSource;

            if (this.scope && this.scope === scope)
            {
                return;
            }

            this.scope = scope;
            this.bound = false;

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: {
                        url: window.baseUrl + "/api/template/" + scope,
                        dataType: "json",
                        headers: {
                            Accept: "application/json; charset=utf-8",
                            "Content-Type": "application/json; charset=utf-8"
                        }
                    }
                },
                schema: {
                    model: {
                        children: "nested"
                    }
                }
            });

            dataSource.bind('error', function(e)
            {
                zaReportError('Could not load hierarchy tree: ' + zaAjaxError(e.xhr));
            });

            this.setDataSource(dataSource);
        },

        selectItem: function(name)
        {
            this.item = name;
            if (this.bound)
            {
                this.updateSelected();
            }
        },

        _prefixFilterSubtree: function(root, dataItem, text)
        {
            var visible = root.find(".k-in").first().text().toLowerCase().indexOf(text) === 0,
                nested,
                childVisible = false,
                children,
                items,
                child,
                item,
                i;

            nested = root.children("ul");
            if (nested.length > 0)
            {
                children = dataItem.children.data();
                items = nested.children("li");

                for (i = 0; i < items.length; i++)
                {
                    item = items.eq(i);
                    child = children[i];

                    childVisible = this._prefixFilterSubtree(item, child, text) || childVisible;
                }

                if (childVisible && !dataItem.expanded)
                {
                    this._toggle(root, dataItem, true);
                }

                visible = visible || childVisible;
            }

            root.css("display", visible ? "" : "none");
            return visible;
        },

        prefixFilter: function(s)
        {
            if (s)
            {
                this._prefixFilterSubtree(this.root.children("li").first(), this.dataSource.at(0), s.toLowerCase());
            }
            else
            {
                this.root.find("li").css("display", "");
            }
        },

        _updateSelected: function()
        {
            var that = this,
                root = that.wrapper.find(".k-item:first"),
                node;

            if (that.item)
            {
                node = that.findByText(that.item);
            }

            if (!node)
            {
                node = root;
            }

            if (node === root)
            {
                that.expand(root);
            }
            else
            {
                that.expandTo(that.dataItem(node));
            }

            that.select(node);
        }
    });

    ui.plugin(Zutubi.admin.HierarchyTree);
}(jQuery));
