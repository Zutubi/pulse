// dependency: ./namespace.js
// dependency: ./ConfigTree.js

(function($) {
    var Observable = kendo.Observable,
        PATHSELECT = 'pathselect';

    Zutubi.admin.ConfigPanel = Observable.extend({
        init: function (containerSelector)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View('<div id="outer-split" style="height: 100%; width: 100%">' +
                                           '<div id="left-pane">' +
                                               '<div id="config-tree" class="pane-content">' +
                                               '</div>' +
                                           '</div>' +
                                           '<div id="center-pane">' +
                                               '<div class="pane-content">' +
                                                   '<p>Main pane.</p>' +
                                               '</div>' +
                                           '</div>' +
                                           '<div id="right-pane">' +
                                               '<div class="pane-content">' +
                                                   '<p>Help pane.</p>' +
                                               '</div>' +
                                           '</div>' +
                                       '</div>', {wrap: false});

            that.view.render($(containerSelector));

            $("#outer-split").kendoSplitter({
                panes: [
                    { collapsible: true, size: "250px" },
                    { collapsible: false },
                    { collapsible: true, size: "250px" }
                ]
            });

            that.configTree = $("#config-tree").kendoZaConfigTree().data("kendoZaConfigTree");
            that.configTree.bind("pathselect", function(e) {
                that.configTree.selectConfig(subpath(e.path, 2));
                that.trigger(PATHSELECT, {path: e.path});
            });
        },

        events: [
            PATHSELECT
        ],

        setPath: function(path)
        {
            this.configTree.setRootPath(subpath(path, 0, 2));
            this.configTree.selectConfig(subpath(path, 2));
        }
    });
}(jQuery));
