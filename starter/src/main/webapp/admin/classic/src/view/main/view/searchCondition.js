Ext.define('admin.view.main.view.SearchCondition', {
    extend: 'Ext.container.Container',
    xtype: 'searchcondition',
    controller: 'view',
    viewModel: 'view',
    defaults: {
        anchor: '100%'

    },
    margin :  '3 5 3 5',
    defaultType : 'textfield',
    layout: 'hbox',
        items: [{
            xtype: 'combobox',
            name : 'query',
            displayField: 'name',
            valueField: 'value',
            allowBlank: false,
            maxWidth : 120,
            store: Ext.create("Ext.data.Store", {
                fields: ["name", "value"],
                data: [
                    { name: "must", value: "must" },
                    { name: "must_not", value: "must_not" },
                    { name: "should", value: "should" }
                ]
            })
        },{
            xtype: 'combobox',
            name : 'property',
            minWidth : 80,
            displayField: 'name',
            valueField: 'name',
            allowBlank: false,
            //bind: {
            //    store :  '{properties}'
            //},
            queryMode: 'local'
        },
            {
                xtype: 'combobox',
                name : 'operator',
                displayField: 'name',
                valueField: 'value',
                allowBlank: false,
                maxWidth : 100,
                store: Ext.create("Ext.data.Store", {
                    fields: ["name", "value"],
                    data: [
                        { name: "range", value: "range" },
                        { name: "term", value: "term" },
                        { name: "fuzzy", value: "fuzzy"},
                        { name: "wildcard", value: "wildcard" }
                    ]
                }),
                listeners: {
                    change:"changeOperator"
                }
            },
            {
                name: 'value'
            },{
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'button',
                        text: '+',
                        style: {
                            'margin-left': '10px'
                        },
                        listeners: {
                            click: 'onAddButton'
                        }
                    },
                    {
                        xtype: 'button',
                        text: '-',
                        fieldReference: 'fieldInterval',
                        style: {
                            'margin-left': '10px'
                        },
                        listeners: {
                            click: 'onDeleteButton'
                        }
                    }
                ]
            }]
});
