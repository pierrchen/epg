package com.trident.android.tv.si.provider.epg;

//import android.R.*;

//import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.database.*;
import android.widget.*;
import android.util.Log;
import android.app.ListActivity;
import android.app.SearchManager;


//import android.app.Activity;
import android.os.Bundle;
import android.widget.AdapterView.*;
import android.view.*;
//import android.widget.*;
//import android.R.layout.*;
//import android.content.Intent;

/**
 * 
 * This Activity is just a demonstration of how to use EPGProvider to write a EPG Program
 * @author Pierr
 * 
 * Feature list: 
 * 
 * 1. Display the Basic Event list - only event name , might add more filed later in the first page
 * 2. When press will show detail information
 * 3. Be able to filter/search against the event name
 * 4. Be able to search by event type
 * 
 * Others:
 * 
 * 1. db_populator will keep populating the database locating at /data/system/epg-1.db, which is 
 * the database the contentProvider open and read.
 * 
 *
 */

public class EPGProviderActivity extends ListActivity {
	
	private static final String TAG = "EPGProviderActivity";
	
    
	/** Called when the activity is first created. */
	
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 //     setContentView(R.layout.main);
       
        Log.d(TAG, "onCreate");

        //This is activity could also started by an intent from the search dialog..
        Intent intent = getIntent();
        
        String query = null;
        
        if(intent == null) 
        {
        	//no search, display all the events.
        	 Log.d(TAG, "started without search query ,will display all the events..");
        } else {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
           query = intent.getStringExtra(SearchManager.QUERY);
           Log.d(TAG, "Will search .." + query);
         
        }
        }
        
        ListAdapter adapter = doMySearch(query);

   
      
  
       
//       adapter.setFilterQueryProvider (new CountryFilterProvider ());
//       adapter.setViewBinder (new FlagViewBinder ());

//
       //http://stackoverflow.com/questions/4571401/trying-to-filter-a-listview-with-runqueryonbackgroundthread-but-nothing-happens

       
       setListAdapter(adapter);

       
       ListView lv = getListView();
       lv.setTextFilterEnabled(true);

       lv.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
             int position, long id) {
 
           
           Intent myIntent = new Intent(EPGProviderActivity.this, EventDetail.class);
           
           //The detailActivity will use this to query the detail information.
           
           myIntent.putExtra("EVENT_NAME", ((TextView) view).getText());
           startActivity(myIntent);
           finish();
           
         }
       });
        
    }
	
	
	ListAdapter doMySearch(String constraint)
	{
	    
		Cursor c = null;
		if(constraint == null || constraint=="")
		{
		
		 c = managedQuery(EPGProvider.CONTENT_URI_EVENTS , null, null, null, null);
	        
		} else {
			
			//TODO: use FTS instead of LIKE
			c = managedQuery(EPGProvider.CONTENT_URI_EVENTS , 
					null, 
					"event_name LIKE " + "%" + constraint + "%", 
					null, 
					null);
			
		}
         
	       // Used to map notes entries from the database to views
	       // show only the event name
	       SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, 
	    		   android.R.layout.simple_list_item_1, 
	    		   c,
	               new String[] { "event_name" }, 
	               new int[] { android.R.id.text1 });
	       
	       return adapter;
	}
	
	
	void populate_the_database()
	{
		  //add 2 EPG events after app starte
	       //---add an event
//	        ContentValues values = new ContentValues();
//	        values.put(EPGProvider.NAME, "hello");
//	        values.put(EPGProvider.SHORT_DESCRIPTION, "hello android");        
//	        Uri uri = getContentResolver().insert(
//	           EPGProvider.CONTENT_URI, values);
//	               
//	        //---add another event---
//	        values.clear();
//	        values.put(EPGProvider.NAME, "hello2");
//	        values.put(EPGProvider.SHORT_DESCRIPTION, "hello android2");        
//	        uri = getContentResolver().insert(
//	           EPGProvider.CONTENT_URI, values);
	}
}