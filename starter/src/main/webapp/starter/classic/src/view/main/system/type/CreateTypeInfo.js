Ext.define('starter.view.main.system.user.CreateTypeInfo', {
    extend: 'Ext.form.Panel',
    xtype: 'createTypeInfo',
    controller: 'type',
    viewModel: 'type',
    bodyPadding: 5,

    items: [{
        xtype: 'fieldset',
        title: 'Type Info',
        defaultType: 'textfield',
        defaults: {
            anchor: '100%'
        },
        items: [
            {allowBlank: false, fieldLabel: 'Name', name: 'name', emptyText: ''},
            {allowBlank: false, fieldLabel: 'DisplayName', name: 'displayName', emptyText: ''},
            {allowBlank: true, fieldLabel: 'Description', name: 'description', emptyText: ''}
        ]}]
});