Ext.define('admin.view.main.group.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyGroup',
    controller: 'group',
    viewModel: 'group',
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
        fieldLabel: 'Group Name',
        name: 'groupName',
        allowBlank: false
    },{
        name: '_id',
        xtype: 'hiddenfield',
        allowBlank: false
    },{
        xtype: 'itemselector',
        name: 'users',
        id: 'itemselector-Users',
        anchor: '100%',
        delimiter:null,
        fieldLabel: 'users',
        store : {type : 'users',pageSize:10000},
        displayField: 'userName',
        valueField: '_id',
        height : 400,
        scrollable : true,
        msgTarget: 'side',
        fromTitle: 'Available',
        toTitle: 'Selected'
    }],
    listeners: {
        afterrender : 'loadModifyData'
    }
});