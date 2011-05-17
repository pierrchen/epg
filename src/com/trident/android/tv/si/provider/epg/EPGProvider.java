package com.trident.android.tv.si.provider.epg;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
//import android.text.TextUtils;
import android.util.Log;
//import java.io.*;

import com.trident.android.tv.si.provider.epg.EPGDatabaseHelp.Table;


/**
 * ref : 
 * http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
 * http://www.devx.com/wireless/Article/41133/0/page/2
 * @author Pierr Chen
 *
 */
public class EPGProvider extends ContentProvider 
{
	
	public static final String PROVIDER_NAME = "com.trident.android.tv.si.provider.EPG";
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/events");
	
	public static final String TAG = "EPGProvider";
	
	public static final String _ID = "_id";
	public static final String SECTION_GUID = "sguid";
	public static final String TSID = "tsid";
	public static final String ONID = "onid";
	public static final String SERVICE_ID = "service_id";
    public static final String START_TIME = "start_time";
    public static final String DURATION = "duration";
    public static final String RUNNING_STATUS = "running_status";
    public static final String CA_MODE = "free_ca_mode";
	public static final String NAME = "event_name";
	public static final String SHORT_DESCRIPTION = "text";
	
	public static final String TABLE_BASIC = "tblEvent_basic";
	public static final String TABLE_EXTENDED = "tblEvent_extended";
	public static final String TABLE_GROUP = "tblEvent_group";
	
	
	
	  //---for database use---
	  //the database have alredy been created by native code
      //we just need to open so as to get an handle of it
	 private SQLiteDatabase epgDB;
	 private static final String DATABASE_NAME =  "epg_1.db";

	
	   
	   
	 
	   
	   //we don't create database. we use the one created by native code. 
	   
//	   private static final String DATABASE_CREATE =
//	         "CREATE TABLE IF NOT EXISTS " + TABLE_BASIC + 
//	         " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
//	         " sguid  INT,tsid  INT,onid  INT,service_id  INT," +
//	         " event_id  INT, start_time  INT,duration  INT,running_status  INT,free_ca_mode  INT," +
//	         " event_name  VARCHAR(256)," +
//	         " text VARCHAR(256)," +
//	         " end_time  INT);";
	   
	   public static final int EVENTS = 1;
	   public static final int EVENT_ID = 2;
	   public static final int EXTENDED_QUERY_ID = 3;
	   public static final int EXTENDED_QUERY_EGUID = 4;
	         		
	   
	   private static final UriMatcher uriMatcher;
	      static{
	         uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	         uriMatcher.addURI(PROVIDER_NAME, "events", EVENTS);
	         uriMatcher.addURI(PROVIDER_NAME, "events/#", EVENT_ID);   
	         
	         //uriMatcher.addURI(PROVIDER_NAME, "extended", EVENTS);
	         //extended/eguid/3 , query the extended information whose eguid = 3
	         //extended/id/3 , query the extended information whose _id = 3
	         uriMatcher.addURI(PROVIDER_NAME, "extended/id/#", EXTENDED_QUERY_ID);
	         uriMatcher.addURI(PROVIDER_NAME, "extended/eguid/#", EXTENDED_QUERY_EGUID);   
	      }

	   
	 
	 
   @Override
   public int delete(Uri arg0, String arg1, String[] arg2) {
      return 0;
   }

   @Override
   public String getType(Uri uri) {
	   
	   switch (uriMatcher.match(uri)){
       //---get all books---
       case EVENTS:
          return "vnd.android.cursor.dir/vnd.trident.tv.si.events ";
       //---get a particular book---
       case EVENT_ID:                
          return "vnd.android.cursor.item/vnd.trident.tv.si.events ";
       default:
          throw new IllegalArgumentException("Unsupported URI: " + uri);        
    }   

     
   }

   @Override
   public Uri insert(Uri uri, ContentValues values) {
	   
	  Log.d(TAG, "INSERT EVENTS  TO DATABASE xxxxx.................");
	  
	  //---add a new book---
      long rowID = epgDB.insert(
         TABLE_BASIC, "", values);
           
      //---if added successfully---
      if (rowID>0)
      {
         Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
         getContext().getContentResolver().notifyChange(_uri, null);    
         return _uri;                
      }        
      throw new SQLException("Failed to insert row into " + uri);

   }

   @Override
   public boolean onCreate() {
	   
	      Log.d(TAG, "EPGProvider::onCreate");
    	  Context context = getContext();
    	  EPGDatabaseHelp dbHelper = new EPGDatabaseHelp(context, DATABASE_NAME);
	      epgDB = dbHelper.getWritableDatabase();
	      return (epgDB == null)? false:true;
   }

   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
         String[] selectionArgs, String sortOrder) {
	   
	   Log.d(TAG, "QUERY the database DATABASE..................");
	   
	   SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
	   Cursor c = null;
	   
	   
	   //1.construct the sqlbuild
	   switch(uriMatcher.match(uri)) {
	   
	   case EVENTS:
	     
	      sqlBuilder.setTables(Table.BASIC);	       
	       
	      if (sortOrder==null || sortOrder=="")
	         sortOrder = _ID;
	      break;
	    
	      
	   case EXTENDED_QUERY_EGUID:
		   
		   sqlBuilder.setTables(Table.EXTENDED);	 
		   break;
	   
	   default:
		   return null;
	   }
	   
	   
	   //2. execute the query and return the cursor
	    c = sqlBuilder.query(
		         epgDB, 
		         projection, 
		         selection, 
		         selectionArgs, 
		         null, 
		         null, 
		         sortOrder);
		   
		      //---register to watch a content URI for changes---
		      c.setNotificationUri(getContext().getContentResolver(), uri);
		      return c;
	   
   }

   @Override
   public int update(Uri uri, ContentValues values, String selection,
         String[] selectionArgs) {
      return 0;
   }
}

