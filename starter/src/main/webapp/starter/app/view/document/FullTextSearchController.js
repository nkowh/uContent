Ext.define('starter.view.document.FullTextSearchController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.fulltextsearch',

    search: function () {
        var me = this;
        var keytext = me.getView().down('textfield[name=keytext]').getValue();
        Ext.Ajax.request({
            url: '/svc',
            method: 'GET',
            params: {query: Ext.JSON.encode({match: {"_fullText": keytext}}), highlight: "_fullText"},
            callback: function (options, success, response) {
                var result = Ext.JSON.decode(response.responseText);
                console.log(result);
                var fulltextStore = me.getView().getStore();
                var data = Ext.Array.map(result.documents, function (doc) {
                    return {
                        createdBy: doc.createdBy,
                        createdOn: doc.createdOn,
                        lastUpdatedBy: doc.lastUpdatedBy,
                        lastUpdatedOn: doc.lastUpdatedOn,
                        name: doc.name,
                        score: doc._score,
                        highlight: doc._highlight._fullText
                    }
                });
                fulltextStore.loadRawData(data);
            }
        });
    }
    //http://localhost:8080/svc/type9527?query={"fuzzy":{"_fullText":"Sheet1"}}&highlight=_fullText

});
