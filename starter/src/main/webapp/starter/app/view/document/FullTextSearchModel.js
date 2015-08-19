Ext.define('starter.view.document.FullTextSearchModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.fulltextsearch',


    data: {
        title: '全文搜索',
        size: 5

    },
    formulas: {
        pageSize: {
            get: function (get) {
                return get('size');
            },

            set: function (size) {
                this.set('size', size);
                this.getStore('documents').setPageSize(size);
                this.getStore('documents').load();
            }
        }
    },
    stores: {
        documents: {type: 'documents', pageSize: '{size}'}
    }

});
