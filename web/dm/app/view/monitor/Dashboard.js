Ext.define('dm.view.monitor.Dashboard', {
    extend: 'Ext.dashboard.Dashboard',
    requires: ['dm.view.monitor.CatGrid'],

    columnWidths: [
        1
    ],

    parts: {
        "aliases": {"viewTemplate": {"title": "aliases", "items": [{"xtype": "cat", "tag": "aliases"}]}},
        "allocation": {"viewTemplate": {"title": "allocation", "items": [{"xtype": "cat", "tag": "allocation"}]}},
        "count": {"viewTemplate": {"title": "count", "items": [{"xtype": "cat", "tag": "count"}]}},
        "fielddata": {"viewTemplate": {"title": "fielddata", "items": [{"xtype": "cat", "tag": "fielddata"}]}},
        "health": {"viewTemplate": {"title": "health", "items": [{"xtype": "cat", "tag": "health"}]}},
        "indices": {"viewTemplate": {"title": "indices", "items": [{"xtype": "cat", "tag": "indices"}]}},
        "master": {"viewTemplate": {"title": "master", "items": [{"xtype": "cat", "tag": "master"}]}},
        "nodes": {"viewTemplate": {"title": "nodes", "items": [{"xtype": "cat", "tag": "nodes"}]}},
        "pending_tasks": {
            "viewTemplate": {
                "title": "pending_tasks",
                "items": [{"xtype": "cat", "tag": "pending_tasks"}]
            }
        },
        "plugins": {"viewTemplate": {"title": "plugins", "items": [{"xtype": "cat", "tag": "plugins"}]}},
        "recovery": {"viewTemplate": {"title": "recovery", "items": [{"xtype": "cat", "tag": "recovery"}]}},
        "segments": {"viewTemplate": {"title": "segments", "items": [{"xtype": "cat", "tag": "segments"}]}},
        "shards": {"viewTemplate": {"title": "shards", "items": [{"xtype": "cat", "tag": "shards"}]}},
        "thread_pool": {"viewTemplate": {"title": "thread_pool", "items": [{"xtype": "cat", "tag": "thread_pool"}]}}
    },


    defaultContent: [
        {"type": "aliases", collapsed: true,closable:false},
        {
            "type": "allocation", collapsed: true
        },
        {"type": "count", collapsed: true}, {
            "type": "fielddata", collapsed: true
        }, {"type": "health", collapsed: true}, {
            "type": "indices", collapsed: true
        }, {"type": "master", collapsed: true}, {
            "type": "nodes", collapsed: true
        }, {"type": "pending_tasks", collapsed: true}, {
            "type": "plugins", collapsed: true
        }, {"type": "recovery", collapsed: true}, {
            "type": "segments", collapsed: true
        }, {"type": "shards", collapsed: true},
        {"type": "thread_pool", collapsed: true}],


    initComponent: function () {
        var me = this;
        me.callParent();

    }


});