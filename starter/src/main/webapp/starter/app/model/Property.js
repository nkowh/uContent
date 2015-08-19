Ext.define('starter.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name',  type: 'string'},
        {name: 'type',   type: 'string'},
        {name: 'defaultValue',   type: 'string'},
        {name: 'partten',   type: 'string'},
        {name: 'promptMssage',   type: 'string'},
        {name: 'order',   type: 'int'},
        {name: 'isRequire', type: 'boolean'},
        {properties: 'isFullTextIndex', type: 'boolean'}
    ]
    //idProperty: 'name',
});