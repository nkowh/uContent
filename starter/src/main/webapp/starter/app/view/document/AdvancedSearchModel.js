Ext.define('starter.view.document.AdvancedSearchModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.advancedsearch',
    stores: {
        types: {type: 'types'}
        //,
        //properties: {
        //    model : Ext.create('starter.model.Property'),
        //    data : pData
        //}
    },
    //formulas: {
    //    tValue: {
    //        get: function (get) {
    //            return get(this.getStore('types').getAt(0).get('name'));
    //        },
    //
    //        set: function (tValue) {
    //            var me = this;
    //
    //            //Ext.Ajax.request({
    //            //    url: '/svc/types/'+tValue,
    //            //    callback: function (options, success, response) {
    //            //        if(!success){
    //            //            return ;
    //            //        }
    //            //        if(response.responseText!=''){
    //            //            var properties = Ext.decode(response.responseText);
    //            //            me.set('pData', date);
    //            //        }
    //            //    }
    //            //})
    //
    //        }
    //    }
    //},

    data: {
        name: 'dm'
        //pData : []

    }

});
