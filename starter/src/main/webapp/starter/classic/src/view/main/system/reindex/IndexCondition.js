Ext.define('starter.view.main.system.reindex.IndexCondition', {
    extend: 'Ext.form.Panel',
    xtype: 'indexCondition',
    controller: 'reIndex',
    viewModel: 'reIndex',
    bodyPadding: 5,
    width: 480,
    layout: 'anchor',
    defaults: {
        anchor: '100%'
    },
    // The fields
    url: '/svc/_reindex',
    defaultType: 'textfield',
    items: [{
        fieldLabel: 'Index',
        //allowBlank: false,
        name: 'target'
        },{
        xtype: 'datefield',
        fieldLabel: 'Start Time',
        format : 'Y-m-d',
        name: 'from'
    },{
        xtype: 'datefield',
        fieldLabel: 'End Time',
        format : 'Y-m-d',
        name: 'to'
    }],
    buttons: [{
        text: 'Rebuild Index',
        handler: 'reIndex'
    }]

});
