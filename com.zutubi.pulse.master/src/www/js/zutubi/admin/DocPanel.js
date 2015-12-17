// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable,
        SELECT = 'select';

    Zutubi.admin.DocPanel = Observable.extend({
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div class="k-doc-panel">' +
                    '<div class="k-doc-intro">#: introduction #</div>' +
                    '<table class="k-doc-fields" style="display:false">' +
                        '<tbody>' +
                            '<tr><th class="k-doc-fields-title"></th></tr>' +
                        '</tbody>' +
                    '</table>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: that.options
                });

            that.view.render($(options.containerSelector));
            that.introduction = that.view.element.find(".k-doc-intro");
            that.fieldsTable = that.view.element.find(".k-doc-fields");
            that.fieldsTableTitle = that.fieldsTable.find(".k-doc-fields-title");
        },

        events: [
        ],

        options: {
            introduction: 'This panel shows context-sensitive documentation.'
        },

        destroy: function()
        {
            this.view.destroy();
        },

        setDocs: function(docs)
        {
            // Note that documentation may contain HTML, so is included verbatim (no encoding).
            if (!docs)
            {
                this.introduction.html('No documentation available.');
                this.fieldsTable.hide();
            }
            else
            {
                this.introduction.html(docs.verbose || docs.brief || "");
            }
        }
    });
}(jQuery));
