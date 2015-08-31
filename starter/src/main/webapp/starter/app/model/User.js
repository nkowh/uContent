Ext.define('starter.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id', type: 'string'},
        {name: 'userId', type: 'string'},
        {name: 'userName', type: 'string'},
        {name: 'email', type: 'string'},
        {name: 'password', type: 'string'},
        {name: 'createdBy',  type: 'string'},
        {name: 'createdOn',   type: 'date'},
        {name: 'lastupdatedBy', type: 'string'},
        {name: 'lastupdatedOn', type: 'date'}
    ],
    idProperty: '_id'
});