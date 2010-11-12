// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A box showing a list of comments.  Expects data of the form:
 *
 * [{
 *     id: 5678,
 *     message: 'This is the comment body',
 *     author: 'jblogs',
 *     date: DateModel,
 *     canDelete: true,
 * }, ... ]
 *
 * @cfg {String} id      Id to use for this component.
 * @cfg {String} buildId Id of the build these comments are on.
 */
Zutubi.pulse.project.CommentList = Ext.extend(Ext.BoxComponent, {
    template: new Ext.XTemplate(
        '<ul id="{id}" class="comments">' +
        '</ul>'
    ),

    commentTemplate: new Ext.XTemplate(
        '<li>' +
            '<div class="comment-body">' +
                '{message:plainToHtml}' +
            '</div>' +
            '<div class="comment-author">' +
                'by {author:htmlEncode}, {relativeDate} ({absoluteDate})' +
                '<tpl if="canDelete">' +
                    ' [<a id="delete-comment-{#}" href="#" onclick="deleteComment({buildId}, {id}); return false;">delete</a>]' +
                '</tpl>' +
            '</div>' +
        '</li>'
    ),
    
    onRender: function(container, position)
    {
        if (position)
        {
            this.el = this.template.insertBefore(position, this, true);    
        }
        else
        {
            this.el = this.template.append(container, this, true);
        }
        
        this.renderComments();
        
        Zutubi.pulse.project.CommentList.superclass.onRender.apply(this, arguments);
    },

    update: function(data)
    {
        this.data = data;
        
        if (this.rendered)
        {
            this.el.select('li').remove();
            this.renderComments();
        }
    },
    
    renderComments: function()
    {
        if (this.data && this.data.length > 0)
        {
            for (var i = 0, l = this.data.length; i < l; i++)
            {
                var comment = this.data[i];
                this.commentTemplate.append(this.el, {
                    id: comment.id,
                    buildId: this.buildId,
                    message: comment.message,
                    author: comment.author,
                    relativeDate: comment.date.relative,
                    absoluteDate: comment.date.absolute,
                    canDelete: comment.canDelete
                });
            }
            
            this.el.setDisplayed(true);
        }
        else
        {
            this.el.setDisplayed(false);
        }
    }
});

Ext.reg('xzcommentlist', Zutubi.pulse.project.CommentList);