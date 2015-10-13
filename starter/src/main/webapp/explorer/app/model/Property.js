Ext.define('explorer.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name',  type: 'string'},
        {name: 'type',   type: 'string'},
        {name: 'defaultValue',   type: 'string'},
        {name: 'pattern',   type: 'string'},
        {name: 'promptMessage',   type: 'string'},
        {name: 'order',   type: 'int'},
        {name: 'required', type: 'boolean'},
        {name: 'indexAnalyzer', type: 'string'},
        {name: 'searchAnalyzer', type: 'string'},
        {properties: 'index', type: 'string'}
    ]
});