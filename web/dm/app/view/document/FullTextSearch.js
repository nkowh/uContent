Ext.define('dm.view.document.FullTextSearch', {
   extend: 'dm.grid.DocumentGrid',
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            tbar: [
                {
                    xtype: 'textfield',
                    name: 'text',
                    allowBlank: false
                },
                {
                    xtype: 'button',
                    text: 'search',
                    scope: me,
                    handler: this.fulltextSearch
                }
            ],
            listeners: {
                beforerender: function () {
                    dm.model.system.User.load(Ext.util.Cookies.get('username'), {
                        callback: function (user, operation, success) {
                            if (!success)return;
                            me.user = user;
                        }
                    });
                }
            }
        });

        me.callParent();


    },


    fulltextSearch: function () {
        var me = this;

        var keywords = me.down('textfield[name=text]').getValue();
        var q = {
            size: 50,
            "query": {
                "bool": {
                    "must": [
                        {match: {'_all': keywords}}
                    ]
                }
            }
        };

        me.search(q);
    }


})
;