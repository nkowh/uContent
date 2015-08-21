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
        name: '_id',
        xtype: 'hiddenfield',
        allowBlank: false
    },{
        fieldLabel: 'User Id',
        name: 'userId',
        allowBlank: false
    },{
        fieldLabel: 'User Name',
        name: 'userName',
        allowBlank: false
    },{
        fieldLabel: 'Email',
        name: 'email',
        vtype: 'email',
        allowBlank: true
    },{
        fieldLabel: 'Password',
        name: 'password',
        allowBlank: false
    }],
    listeners: {
        afterrender : 'loadModifyData'
    }
});