// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.admin.Panel = Observable.extend({
        init: function()
        {
            this.nextNavId = 1;

            Observable.fn.init.call(this);
        },

        beginNavigation: function()
        {
            if (!this.isDirty())
            {
                kendo.ui.progress(this.element, true);
                return this.nextNavId++;
            }
            else
            {
                return 0;
            }
        },

        endNavigation: function(id)
        {
            var latest = id + 1 === this.nextNavId;
            if (latest)
            {
                kendo.ui.progress(this.element, false);
            }

            return latest;
        },

        isDirty: function()
        {
            return false;
        }
    });
}(jQuery));
