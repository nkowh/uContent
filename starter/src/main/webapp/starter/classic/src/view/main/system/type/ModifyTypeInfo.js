Ext.define('starter.view.main.system.user.ModifyTypeInfo', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyTypeInfo',
    controller: 'type',
    viewModel: 'type',
    bodyPadding: 5,
    layout: 'anchor',
    items: [{
        xtype: 'fieldset',
        title: 'Type Info',
        defaultType: 'textfield',
        defaults: {
            anchor: '100%'
        },
        items: [
            {allowBlank: false, fieldLabel: 'Name', name: 'name', emptyText: '',readOnly : true},
            {allowBlank: false, fieldLabel: 'DisplayName', name: 'displayName', emptyText: ''},
            {allowBlank: true, fieldLabel: 'Description', name: 'description', emptyText: ''}
        ]}]
});