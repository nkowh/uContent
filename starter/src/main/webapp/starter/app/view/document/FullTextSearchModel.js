Ext.define('starter.view.document.FullTextSearchModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.fulltextsearch',


    data: {
        title: '全文搜索',
        pageSize: 25

    },
    stores: {
        fulltext: {
            type: 'documents'
        }
    }

})
;
