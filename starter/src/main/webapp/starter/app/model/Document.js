Ext.define('starter.model.Document', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'createdBy', type: 'string'},
        {name: 'lastUpdatedBy', type: 'string'},
        {name: 'createdOn', type: 'date'},
        {name: 'createdOn', type: 'date'},
        {name: 'lastUpdatedOn', type: 'date'},
        {name: 'score', type: 'float'},
        {name: 'highlight', type: 'auto'}
    ],
    idProperty: 'id'
});