    </div>
    </div>

    <script language="JavaScript" type="text/javascript">
        var tabpanelc_${parameters.id} = new TabContent( "${parameters.id}", ${parameters.remote} );
        dojo.event.topic.subscribe( "${parameters.subscribeTopicName}", tabpanelc_${parameters.id}, "updateVisibility" );
    </script>