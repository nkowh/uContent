Ext.define('starter.view.main.system.type.Modify', {
    extend: 'Ext.window.Window',
    xtype: 'modifytype',
    controller: 'type',
    viewModel: 'type',
    title:'修改类型',
    width: 1100,
    height: 600,
    layout: {
        type: 'border',
        padding: 5
    },
    items:[{
        region: 'north',
        xtype: 'modifyTypeInfo'
    },{
        region: 'center',
        xtype: 'modifyProperty'
    }],
    buttons: [{
        text: 'Close',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: 'Submit',
        handler : 'modifySave'
    }],
    listeners: {
        afterrender:'loadModifyData'
    }

});