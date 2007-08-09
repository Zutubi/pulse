form.items.last().on('getlatest', function(field)
{
    var statusEl = Ext.get('latest.revision.status');
    if(!statusEl)
    {
        statusEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'span', id: 'latest.revision.status', style: 'margin-right: 4px'}, true);
    }

    statusEl.update('<img src="${base}/images/inprogress.gif" alt="in progress"/>');

    new Ajax.Request('${base}/ajax/getLatestRevision.action',
      {
          method:'get',
          parameters: 'projectId=${projectId?c}',

          onSuccess: function(transport, response)
          {
              var response = eval("(" + transport.responseText + ")");
              if(response.successful)
              {
                  field.setValue(response.latestRevision);
                  statusEl.update('<img src="${base}/images/accept.gif" alt="success"/>');
              }
              else
              {
                  statusEl.update('<img src="${base}/images/exclamation.gif" alt="success"/> Unable to get revision: ' + response.error);
              }
          },

          onFailure: function()
          {
              statusEl.update('<img src="${base}/images/exclamation.gif" alt="success"/> Request to get revision failed.');
          }
      });
});
