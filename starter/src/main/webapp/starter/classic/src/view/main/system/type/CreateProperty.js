Ext.define('starter.view.main.system.type.CreateProperty', {
    extend: 'Ext.grid.Panel',
    xtype: 'createProperty',
    controller: 'type',
    viewModel: 'type',
    bind: {
        title: '{propertyTitle}',
        store : '{properties}'
    },
    columns: [{
        header: 'Name',
        dataIndex: 'name',
        width: 150,
        editor: {
            allowBlank: false
        }
    }, {
        header: 'Type',
        dataIndex: 'type',
        width: 100,
        editor: {
            allowBlank: false,
            xtype: 'combo',
            displayField: 'name',
            valueField: 'value',
            store: Ext.create("Ext.data.Store", {
                fields: ["name", "value"],
                data: [
                    { name: "string", value: "string" },
                    { name: "integer", value: "integer" },
                    { name: "float", value: "float" },
                    { name: "boolean", value: "boolean" },
                    { name: "date", value: "date"}
                ]
            })
        }
    },  {
        header: 'DefaultValue',
        dataIndex: 'defaultValue',
        width: 130,
        editor: {
            allowBlank: true
        }
    }, {
        header: 'Pattern',
        dataIndex: 'pattern',
        width: 200,
        editor: {
            allowBlank: true
        }
    }, {
        header: 'PromptMessage',
        dataIndex: 'promptMessage',
        width: 220,
        editor: {
            allowBlank: true
        }
    }, {
        xtype: 'checkcolumn',
        header: 'Required',
        dataIndex: 'required',
        width: 80

    }, {
        xtype: 'checkcolumn',
        header: 'Index',
        dataIndex: 'isFullIndex',
        width: 80,
        editor: {
            xtype: 'checkbox',
            inputValue : 'analyzed',
            uncheckedValue: 'not_analyzed'
        }
    },  {
        header: 'Order',
        dataIndex: 'order',
        width: 80
    }],
    selModel: 'cellmodel',
    plugins: {
        ptype: 'cellediting',
        clicksToEdit: 1,
        listeners:{
            "beforeedit" : 'validateProperty'
            //"edit" : 'editProperty'
        }
    },
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button', text: 'Add', handler: 'addProperty' },
            { xtype: 'button', text: 'Delete',handler: 'deleteProperty'}
        ]
    }]
});