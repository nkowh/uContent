Ext.define('dm.view.document.IndexDocumentController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.indexdocument',

    loadEntityTypes: function (cmp, eOpts) {
        var me = this;
        Ext.Ajax.request({
            url: '/dm/$metadata',
            callback: function (options, success, response) {
                var $metadata = $(response.responseText);
                me.namespace = $metadata.find('Schema').attr('Namespace');
                me.entityTypes = $metadata.find('Schema EntityType');
                var matches = [];
                _.each($metadata.find('Schema EntityContainer EntitySet'), function (entitySet) {
                    var fullname = $(entitySet).attr('EntityType');
                    var entityType = me.isDocument(fullname);
                    if (entityType)matches.push({name: $(entitySet).attr('Name'), entityType: entityType});
                });

                cmp.down('combo').bindStore(
                    Ext.create('Ext.data.Store', {
                        fields: ['name', 'entityType'],
                        data: matches
                    })
                );
            }
        })
    },

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
        var form = combo.up('form');
        var fieldset = form.down('fieldset[itemId=documenttype]');
        fieldset.removeAll(true);
        var type = newValue;
        do {
            var baseType = $(type).attr('BaseType');
            _.each($(type).find('Property'), function (property) {
                var $property = $(property);
                fieldset.add({
                    fieldLabel: $property.attr('Name'),
                    name: $property.attr('Name')
                });
            });
            type = me.findType(baseType);
        } while (type !== null);

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
