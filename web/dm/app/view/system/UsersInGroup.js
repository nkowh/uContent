Ext.define('dm.view.system.UsersInGroup', {
    extend: 'Ext.form.Panel',

    items: [{
        xtype: 'tagfield',
        anchor: '100%',
        fieldLabel: 'tagfield',
        store: Ext.create('dm.store.system.Users'),
        displayField: '_id',
        valueField: '_id',
        msgTarget: 'side',
        listeners: {
            beforerender: function (tagfield, eOpts) {
                var me = tagfield.up('form');
                tagfield.setValue(me.group.get('users'));
            }
        }
    }],

    buttons: [{
        text: '确定',
        disabled: true,
        formBind: true,
        handler: function () {
            var me = this.up('form');
            var values = Ext.Array.map(me.down('tagfield').getValueRecords(), function (item) {
                return item.get('_id');
            });
            me.group.set('users', values);
            me.up('window').close();
        }
    }, {
        text: '关闭',
        handler: function () {
            var me = this;
            me.up('window').close();
        }
    }]

});


