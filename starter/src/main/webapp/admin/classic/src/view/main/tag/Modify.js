Ext.define('admin.view.main.tag.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyTag',
    controller: 'tag',
    viewModel: 'tag',
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
                handler: 'modifySave'
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
        xtype : 'hiddenfield',
        name: '_id'
    },{
        fieldLabel: 'TagContext',
        name: 'tagContext',
        allowBlank: false
    },{
        fieldLabel: 'Description',
        name: 'description'
    }],
    listeners: {
        afterrender : 'loadModifyData'
    }
});