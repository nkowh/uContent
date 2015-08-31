Ext.define('starter.view.main.system.type.Create', {
    extend: 'Ext.window.Window',
    xtype: 'createtype',
    controller: 'type',
    viewModel: 'type',
    title:'新建类型',
    width: 1100,
    header: {
        titlePosition: 2,
        titleAlign: 'center'
    },
    height: 600,
    layout: {
        type: 'border',
        padding: 5
    },
    items:[{
        region: 'north',
        xtype: 'createTypeInfo'
    },{
        region: 'center',
        xtype: 'createProperty'
    }],
    buttons: [{
        text: 'Close',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: 'Submit',
        handler : 'createSave'
    }]

});