Ext.define('starter.view.main.system.group.Create', {
    extend: 'Ext.form.Panel',
    xtype: 'createGroup',
    controller: 'group',
    requires : [
        'Ext.ux.form.ItemSelector',
        'Ext.ux.ajax.JsonSimlet',
        'Ext.ux.ajax.SimManager'
    ],
    viewModel: 'group',
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
        fieldLabel: 'Group Name',
        name: 'groupName',
        allowBlank: false
    },{
        xtype: 'itemselector',
        name: 'Users',
        id: 'itemselector-Users',
        anchor: '100%',
        fieldLabel: 'Users',
        bind : {
            store : 'users'
        },
        displayField: 'Name',
        valueField: 'Id',
        msgTarget: 'side',
        fromTitle: 'Available',
        toTitle: 'Selected'
    }]
});