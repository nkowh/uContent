Ext.define('admin.view.main.type.CreateProperty', {
    extend: 'Ext.form.Panel',
    xtype: 'createProperty',
    controller: 'type',
    width : 500,
    viewModel: 'type',
    layout: 'anchor',
    bodyPadding: 5,
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [{
        fieldLabel: 'Name',
        name: 'name',
        allowBlank: false
    },{
        fieldLabel: 'Type',
        name: 'type',
        xtype : 'combo',
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
        }),
        allowBlank: false
    },{
        fieldLabel: 'DefaultValue',
        itemId : 'defaultValue',
        name: 'defaultValue'
    },{
        fieldLabel: 'Pattern',
        name: 'pattern'
    },{
        fieldLabel: 'PromptMessage',
        name: 'promptMessage'
    },{
        fieldLabel: 'Required',
        name: 'required',
        uncheckedValue : false,
        xtype : 'checkboxfield',
        inputValue: true
    },{
        fieldLabel: 'Index',
        name: 'index',
        allowBlank: false,
        xtype: 'combo',
        displayField: 'name',
        valueField: 'value',
        store: Ext.create("Ext.data.Store", {
            fields: ["name", "value"],
            data: [
                { name: "analyzed", value: "analyzed" },
                { name: "not_analyzed", value: "not_analyzed" }
            ]
        })
    },{
        fieldLabel: 'IndexAnalyzer',
        name: 'indexAnalyzer',
        xtype: 'combo',
        displayField: 'name',
        valueField: 'value',
        store: Ext.create("Ext.data.Store", {
            fields: ["name", "value"],
            data: [
                { name: "ansj_index", value: "ansj_index" }
            ]
        })
    },{
        fieldLabel: 'SearchAnalyzer',
        name: 'searchAnalyzer',
        xtype: 'combo',
        displayField: 'name',
        valueField: 'value',
        store: Ext.create("Ext.data.Store", {
            fields: ["name", "value"],
            data: [
                { name: "ansj_query", value: "ansj_query" }
            ]
        })
    },{
        fieldLabel: 'Order',
        name: 'order',
        allowBlank: false
    }],

    // Reset and Submit buttons
    buttons: [{
        text: 'Close',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: 'Save',
        formBind: true, //only enabled once the form is valid
        disabled: true,
        handler: 'saveProperty'
    }],
    listeners: {
        afterrender:'loadProperty'
    }
});