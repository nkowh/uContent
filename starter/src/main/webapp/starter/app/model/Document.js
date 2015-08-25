Ext.define('starter.model.Document', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'createdBy', type: 'string'},
        {name: 'lastupdatedBy', type: 'string'},
        {name: 'createdOn', type: 'date'},
        {name: 'lastupdatedOn', type: 'date'}
    ],
    idProperty: 'id'
});