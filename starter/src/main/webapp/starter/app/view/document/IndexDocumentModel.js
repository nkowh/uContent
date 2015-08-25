Ext.define('starter.view.document.IndexDocumentModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.indexdocument',

    data: {
        title: '导入文档'
    },
    stores: {
        types: {type: 'types'},
        documents : {type : 'documents'}
    }

});
