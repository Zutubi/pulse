// dependency: ./namespace.js

(function($) {
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        PATHSELECT = "pathselect";

    Zutubi.admin.ConfigTree = TreeView.extend({
        init: function(element, options) {
            options = $.extend(true, {
                dataTextField: "label",
                dataSource: {
                    transport: {
                        read: {
                            url: window.baseUrl + "/api/config/" + "projects/lgit?depth=-1&filter=nested",
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
                },
                select: function(e) {
                    var that = this;
                    that.trigger(PATHSELECT, {path: that.nodePath(e.node)});
                }
            }, options);

            TreeView.fn.init.call(this, element, options);
        },

        events: [
            // FIXME kendo: to subscribe to oour own select event we need to have this here, is there a better way?
            "select",
            PATHSELECT
        ],

        options: {
            name: "ZaConfigTree"
        },

        nodePath: function(node) {
            var nodeData = this.dataItem(node),
                path = nodeData.key,
                parent = this.parent(node);

            if (parent.length)
            {
                path = this.nodePath(parent) + '/' + path;
            }

            return path;
        }
    });

    ui.plugin(Zutubi.admin.ConfigTree);
})(jQuery);
