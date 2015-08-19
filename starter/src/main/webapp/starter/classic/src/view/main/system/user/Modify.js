Ext.define('starter.view.main.system.user.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyUser',
    controller: 'user',
    viewModel: 'user',
    buttons: [{
        text: 'Close',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: 'Submit',
        formBind: true, //only enabled once the form is valid
        disabled: true,
        handler: 'modifySave'
    }],
    bodyPadding: 5,
    width: 480,
    layout: 'anchor',
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [{
        fieldLabel: 'Name',
        name: 'Name',
        allowBlank: false
    },{
        name: 'Id',
        xtype: 'hiddenfield',
        allowBlank: false
    },{
        fieldLabel: 'Password',
        name: 'Password',
        allowBlank: false
    }],
    listeners: {
        afterrender : 'loadModifyData'
    }
});