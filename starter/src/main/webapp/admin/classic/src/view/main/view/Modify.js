Ext.define('admin.view.main.view.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyView',
    controller: 'view',
    viewModel: 'view',
    frame: true,
    bodyPadding: 3,
    scrollable : 'y',
    fieldDefaults: {
        labelAlign: 'left',
        labelWidth: 150,
        msgTarget: 'side'
    },

    items: [{
        name: '_id',
        xtype: 'hiddenfield'
    },{
        fieldLabel: 'ViewName',
        xtype: 'textfield',
        name: 'viewName',
        allowBlank: false
    },{
        xtype: 'tagfield',
        fieldLabel: 'Type',
        name: 'type',
        displayField: 'displayName',
        allowBlank: false,
        valueField: 'name',
        bind: {
            store: '{types}'
        },
        filterPickList: true,
        publishes: 'value',
        listeners: {
            change: "changeType"
        }
    }, {
        xtype: 'fieldset',
        title: 'Condition',
        defaults: {
            anchor: '100%'
        }
        //items: [{
        //    xtype : 'searchcondition'
        //}]
    },
        {
            fieldLabel: 'minimum_should_match',
            xtype: 'textfield',
            name: 'minimum_should_match',
            value: '1'
        },
        {
            fieldLabel: 'permission',
            xtype: 'tagfield',
            name : 'permissionObj',
            displayField: 'name',
            valueField: 'id',
            forceSelection: true
        }],
    listeners: {
        afterrender : 'loadModifyData'
    },
    buttons: [{
        text: 'Close',
        handler: function() {
            this.up('window').close();
        }
    },{
        text: 'Save',
        handler: 'save'
    }]
});