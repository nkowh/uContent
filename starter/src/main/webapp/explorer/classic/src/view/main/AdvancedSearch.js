Ext.define('explorer.view.main.AdvancedSearch', {
    extend: 'Ext.form.Panel',
    xtype: 'advancedsearch',

    controller: 'advsearch',
    bodyPadding: 5,
    fieldDefaults: {
        labelAlign: 'left',
        labelWidth: 150,
        msgTarget: 'side'
    },
    scrollable : true,
    items: [{
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
        },
        items: [{
            xtype : 'searchcondition'
        }]
    },
        {
            fieldLabel: 'minimum_should_match',
            xtype: 'textfield',
            name: 'minimum_should_match',
            value: '1'
        }],
    buttons: [{
        text: 'Search',
        handler: 'search'
    }]
});
