Ext.define('admin.view.main.tag.Create', {
    extend: 'Ext.form.Panel',
    xtype: 'createTag',
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
        fieldLabel: 'TagContext',
        name: 'tagContext',
        allowBlank: false
    },{
        fieldLabel: 'Description',
        name: 'description'
    }]
});