Ext.define('starter.view.main.document.SearchForm', {
    extend: 'Ext.form.Panel',
    xtype: 'searchForm',
    title: 'Search Form',
    controller: 'advancedsearch',
    viewModel: 'advancedsearch',
    collapsible: true,
    bodyPadding: 3,
    fieldDefaults: {
        labelAlign: 'left',
        labelWidth: 150,
        msgTarget: 'side'
    },

    items: [{
        xtype: 'combo',
        fieldLabel: 'Type',
        name: 'type',
        displayField: 'displayName',
        allowBlank: false,
        valueField: 'name',
        bind: {
            store :  '{types}'
            //value : '{tValue}'
        },
        listeners: {
            change:"changeType"
        }
    },{
        xtype: 'fieldset',
        title: 'Condition',
        defaults: {
            anchor: '100%'
        },
        items: [Ext.create('starter.view.main.document.SearchCondition')]},
        {
            fieldLabel: 'minimum_should_match',
            xtype: 'textfield',
            name: 'minimum_should_match',
            value : '1'
        }],
    listeners : {
        //afterrender : 'loadPropertyField'
    },
    buttons: [ {
        text: 'Search',
        handler: 'search'
    }]

});
