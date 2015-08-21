Ext.define('starter.view.main.system.user.Create', {
    extend: 'Ext.form.Panel',
    xtype: 'createUser',
    controller: 'user',
    viewModel: 'user',
    initComponent: function(){
        var me = this;
        Ext.apply(this, {
            buttons: [{
                text: 'Close',
                handler: function() {
                    this.up('window').close();
                }
            }, {
                text: 'Submit',
                formBind: true, //only enabled once the form is valid
                disabled: true,
                handler: 'createSave'
            }]

        });
        this.callParent();
    },
    bodyPadding: 5,
    width: 480,
    layout: 'anchor',
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [{
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
    }]
});