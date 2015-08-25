Ext.define('starter.view.document.IndexDocumentController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.indexdocument',

    isDocument: function (fullname) {
        var me = this;
        var match = _.find(me.entityTypes, function (entityType) {
            var $entityType = $(entityType);
            var name = $entityType.attr('Name');
            var baseType = $entityType.attr('BaseType');
            if (me.namespace + '.' + name !== fullname)return false;
            if (baseType === 'com.nikoyo.uContent.dm.Document')
                return true;
            else if (fullname === 'com.nikoyo.uContent.dm.Document')
                return true;
            else if (baseType != null)
                return me.isDocument($entityType.attr('BaseType'), me.namespace, me.entityTypes);
            return false;
        });
        return match;
    },


    changeType: function (combo, newValue, oldValue, eOpts) {
        var me = this;
        if(newValue&&newValue!=oldValue){
            var form = this.getView();
            var fieldset = form.down('fieldset[itemId="documenttype"]');
            fieldset.removeAll(true);
            var type = newValue;
            Ext.Ajax.request({
                url: '/svc/types/'+type,
                callback: function (options, success, response) {
                    if(!success){
                        return ;
                    }
                    if(response.responseText!=''){
                        var properties = Ext.decode(response.responseText);
                        Ext.Array.each(properties, function(property, index, countriesItSelf) {
                            fieldset.add({
                                fieldLabel: property.name,
                                name: property.name
                            });
                        });

                    }
                }
            });
        }

    },

    findType: function (fullname) {
        var me = this;
        if (fullname == null)return null;
        return _.find(me.entityTypes, function (entityType) {
            var $entityType = $(entityType);
            var name = $entityType.attr('Name');
            return me.namespace + '.' + name === fullname;
        });
    }
});
