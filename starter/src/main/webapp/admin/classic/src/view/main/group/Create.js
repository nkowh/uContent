Ext.define('admin.view.main.group.Create', {
    extend: 'Ext.form.Panel',
    xtype: 'createGroup',
    controller: 'group',
    viewModel: 'group',
    requires : [
        'Ext.ux.form.ItemSelector',
        'Ext.ux.ajax.JsonSimlet',
        'Ext.ux.ajax.SimManager'
    ],
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
                formBind: true,
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
        fieldLabel: 'Group Id',
        name: 'groupId',
        allowBlank: false
    },{
        fieldLabel: 'Group Name',
        name: 'groupName',
        allowBlank: false
    },{
        xtype: 'itemselector',
        name: 'users',
        id: 'itemselector-Users',
        anchor: '100%',
        fieldLabel: 'Users',
        delimiter:null,
        store : {type : 'users',pageSize:10000},
        displayField: 'userName',
        valueField: '_id',
        msgTarget: 'side',
        height : 400,
        scrollable : true,
        fromTitle: 'Available',
        toTitle: 'Selected'
    }]
});