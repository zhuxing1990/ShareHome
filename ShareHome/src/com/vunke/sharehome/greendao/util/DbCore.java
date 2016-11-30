package com.vunke.sharehome.greendao.util;

import android.content.Context;

import com.vunke.sharehome.greendao.dao.DaoMaster;
import com.vunke.sharehome.greendao.dao.DaoSession;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * 
 * Date:2015-09-01
 * Time: 10:47
 */
public class DbCore {
    private static final String DEFAULT_DB_NAME = "default.db";
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    private static Context mContext;
    private static String DB_NAME;

    public static void init(Context context) {
        init(context, DEFAULT_DB_NAME);
    }

    public static void init(Context context, String dbName) {
        if (context == null) {
            throw new IllegalArgumentException("context can't be null");
        }
        mContext = context.getApplicationContext();
        DB_NAME = dbName;
    }

    public static DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        
        return daoMaster;
    }

    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster();
            }
            //daoSession.getContactDao().insertInTx(entities)
            daoSession = daoMaster.newSession();
        }
        
        return daoSession;
    }

    public static void enableQueryBuilderLog(){

        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }
}
