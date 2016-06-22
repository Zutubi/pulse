// dependency: ./namespace.js
// dependency: zutubi/config/package.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView;

    Zutubi.admin.Tree = TreeView.extend({
        FILTER_REVEAL: 2,
        FILTER_VISIBLE: 1,
        FILTER_HIDE: 0,

        options: {
            name: "ZaTree"
        },
        
        getRoot: function()
        {
            return this.wrapper.find(".k-item:first");
        },

        eachItem: function(fn)
        {
            this._depthFirstEach(this.dataSource, fn);
        },

        _depthFirstEach: function(dataSource, fn)
        {
            var i, data, item;

            if (dataSource)
            {
                data = dataSource.data();
                for (i = 0; i < data.length; i++)
                {
                    item = data[i];
                    fn(item);
                    if (item.hasChildren)
                    {
                        this._depthFirstEach(data[i].children, fn);
                    }
                }
            }
        },

        setFilter: function(fn)
        {
            if (fn)
            {
                this._filterSubtree(this.root.children("li").first(), this.dataSource.at(0), fn);
            }
            else
            {
                this.root.find("li").css("display", "");
            }
        },

        _filterSubtree: function(root, dataItem, fn)
        {
            var visibility = fn(root),
                nested,
                childVisibility,
                childrenVisibility = this.FILTER_HIDE,
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

                    childVisibility = this._filterSubtree(item, child, fn);
                    if (childVisibility > childrenVisibility)
                    {
                        childrenVisibility = childVisibility;
                    }
                }

                if (childrenVisibility === this.FILTER_REVEAL && !dataItem.expanded)
                {
                    this._toggle(root, dataItem, true);
                }

                if (childrenVisibility > visibility)
                {
                    visibility = childrenVisibility;
                }
            }

            root.css("display", visibility === this.FILTER_HIDE ? "none" : "");
            return visibility;
        }
    });

    ui.plugin(Zutubi.admin.Tree);
}(jQuery));
