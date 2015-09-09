Ext.define('starter.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name',  type: 'string'},
        {name: 'type',   type: 'string'},
        {name: 'defaultValue',   type: 'string'},
        {name: 'pattern',   type: 'string'},
        {name: 'promptMessage',   type: 'string'},
        {name: 'order',   type: 'int'},
        {name: 'required', type: 'boolean'},
        {properties: 'index', type: 'string'}
    ]
    //idProperty: 'name',
});