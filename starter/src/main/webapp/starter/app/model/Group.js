Ext.define('starter.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id', type: 'string'},
        {name: 'groupName', type: 'string'},
        {name: 'createdBy',  type: 'string'},
        {name: 'createdOn',   type: 'date'},
        {name: 'lastupdatedBy', type: 'string'},
        {name: 'lastupdatedOn', type: 'date'},
        {name: 'users', type: 'auto'}
    ],
    idProperty: '_id'
});