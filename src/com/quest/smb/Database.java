package com.quest.smb;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database  extends SQLiteOpenHelper{
    private static String DATABASE_NAME="QUEST_SMB";
    private static String DATABASE_PATH="/data/data/com.quest.smb/databases/";
	public Database(Context cxt){
	  super(cxt,DATABASE_NAME,null,1);
	  DATABASE_NAME=cxt.getResources().getString(R.string.database_name);
	  DATABASE_PATH=cxt.getResources().getString(R.string.database_path);

	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//create the tables we will need currently
		// ADDRESSES, USERNAMES, PASSWORDS, DOMAINS
		db.execSQL("create table ADDRESS (DATE_ENTERED LONG primary key,AD_NAME TEXT)");
		db.execSQL("create table CREDENTIALS (DATE_ENTERED LONG primary key,USER_NAME TEXT,PASS_WORD TEXT,DOMAIN TEXT)" );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
   public boolean ifValueExists(String value, String column, String table){
	  Cursor cs=getReadableDatabase().rawQuery("SELECT "+column+" FROM "+table+" WHERE "+column+"='"+value+"'",null);
	  String exists=null;
	  try{
			while(cs.moveToNext()){
			   exists=cs.getString(cs.getColumnIndex(column));
			}
		  cs.close();
		  if(exists!=null && exists.length()>0 ){
			  return true;
		  }
		 return false;
    }
    catch(Exception e){
      e.printStackTrace();
   	  return false;
    }
   }
	

}
