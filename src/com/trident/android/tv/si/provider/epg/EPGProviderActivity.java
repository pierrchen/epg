package com.trident.android.tv.si.provider.epg;

//import android.R.*;

//import android.content.ContentValues;
//import com.trident.android.tv.si.provider.epg.EPGDatabaseHelp.BasicColumns;
import java.util.Calendar;

import com.trident.android.tv.si.provider.epg.EPGDatabaseHelp.ContentTypeColumns;

import android.content.Intent;
import android.net.Uri;
import android.database.*;
import android.widget.*;
import android.util.Log;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;

//import android.app.Activity;
import android.os.Bundle;
import android.widget.AdapterView.*;
import android.view.*; //import android.widget.*;
//import android.R.layout.*;
//import android.content.Intent;
import android.view.View.OnClickListener;

/**
 * 
 * This Activity is just a demonstration of how to use EPGProvider to write a
 * EPG Program
 * 
 * @author Pierr
 * 
 *         Feature list:
 * 
 *         1. Display the Basic Event list 2. When press will show detail
 *         information 3. Be able to filter/search against the event name ,event
 *         short descriptor, event extended descriptors 4. Be able to search by
 *         event type 5. Be able to filter by event start/end time
 * 
 *         Others:
 * 
 *         1. db_populator will keep populating the database locating at
 *         /data/system/epg-1.db, which is the database the contentProvider open
 *         and read.
 * 
 * 
 */

public class EPGProviderActivity extends ListActivity {

	private static final String TAG = "EPGProviderActivity";
	
	 private TextView mStarTimeTextView;
	 private TextView mEndTimeTextView;
	 //private Button mPickDate;
	 private int mStartYear;
	 private int mMonth;
	 private int mDay;
	 
	 
	 private int mEndYear;
	 private int mEndMonth;
	 private int mEndDay;

	 static final int DATE_DIALOG_ID = 0;
	 static final int END_DATE_DIALOG_ID = 1;
	 
	 private DatePickerDialog.OnDateSetListener mDateSetListener =
         new DatePickerDialog.OnDateSetListener() {

             public void onDateSet(DatePicker view, int year, 
                                   int monthOfYear, int dayOfMonth) {
                 mStartYear = year;
                 mMonth = monthOfYear;
                 mDay = dayOfMonth;
                 updateDisplay();
             }
         };
         
         private DatePickerDialog.OnDateSetListener mEndDateSetListener =
             new DatePickerDialog.OnDateSetListener() {

                 public void onDateSet(DatePicker view, int year, 
                                       int monthOfYear, int dayOfMonth) {
                     mEndYear = year;
                     mEndMonth = monthOfYear;
                     mEndDay = dayOfMonth;
                     updateDisplay();
                 }
             };

         
        @Override
         protected Dialog onCreateDialog(int id) {
             switch (id) {
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this,
                             mDateSetListener,
                             mStartYear, mMonth, mDay);
             case END_DATE_DIALOG_ID:
                 return new DatePickerDialog(this,
                             mEndDateSetListener,
                             mEndYear, mEndMonth, mEndDay);
             }
             return null;
         }
         
         
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.d(TAG, "onCreate");

		
		 // capture our View elements
        mStarTimeTextView = (TextView) findViewById(R.id.startDateDisplay);
        mEndTimeTextView = (TextView) findViewById(R.id.endDateDisplay);
        //mPickDate = (Button) findViewById(R.id.pickDateButton);

        // add a click listener to the button
        mStarTimeTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        
        // add a click listener to the button
        mEndTimeTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(END_DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mStartYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        
        
        mEndYear = c.get(Calendar.YEAR);
        mEndMonth = c.get(Calendar.MONTH);
        mEndDay = c.get(Calendar.DAY_OF_MONTH) + 1;

        // display the current date (this method is below)
        updateDisplay();

        
        
		
		
		
		// This is activity could also started by an intent from the search
		// dialog..
		Intent intent = getIntent();

		String query = null;

		int query_type = 0;

		if (intent == null) {
			// no search, display all the events.
			Log.d(TAG,"started without search query ,will display all the events..");

		} else {

			// intent from Search bar
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				query = intent.getStringExtra(SearchManager.QUERY);
				Log.d(TAG, "Will search .." + query);

			} else {

				// intent from movie/news button
				Bundle bd = getIntent().getExtras();

				String type;
				if (bd != null) {

					type = bd.getString("TYPE");

					if (type.equals("movie") || type.equals("news")) {
						query_type = 1;

						Log.d(TAG, "try to get events belong to type " + type);

					}

				}

			}
		}

		ListAdapter adapter;
		if (query_type == 1) {

			adapter = doMySearchByType(query);
		} else {
			adapter = doMySearch(query);
		}
		// adapter.setFilterQueryProvider (new CountryFilterProvider ());
		// adapter.setViewBinder (new FlagViewBinder ());

		//
		// http://stackoverflow.com/questions/4571401/trying-to-filter-a-listview-with-runqueryonbackgroundthread-but-nothing-happens

		setListAdapter(adapter);

		ListView lv = getListView(); // == findViewById(android.R.id.list)

		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent myIntent = new Intent(EPGProviderActivity.this,
						EventDetail.class);

				// The detailActivity will use this to query the detail
				// information.
				//need to get the TextView within this LinalLayoutView
				//String event_name = ((TextView)((ViewGroup)view).getChildAt(1)).getText();
				//The view object is actually a LinealLayout GroupView ,contaning 
				//several TextViews..
				
				//TODO:This is highly coupled with the View...
				//get the view by name???? instead of use magic index number 
				//TextView nameView =  (TextView)((ViewGroup)view).getChildAt(2);
				TextView nameView =  (TextView)view.findViewById(R.id.eventName);
				
			    myIntent.putExtra("EVENT_NAME", nameView.getText());
				
				startActivity(myIntent);
				finish();

			}
		});

		// search button
		Button searchButton = (Button) findViewById(R.id.searchbutton);

		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// start an search
				onSearchRequested();
				// finish() should not be called

			}
		});

		// movie button
		Button movieButton = (Button) findViewById(R.id.bt_movie);

		movieButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Log.d(TAG, "movie button has been pressed...........");
				// start himself...
				Intent myIntent = new Intent(EPGProviderActivity.this,
						EPGProviderActivity.class);

				// The detailActivity will use this to query the detail
				// information.

				myIntent.putExtra("TYPE", "movie");
				startActivity(myIntent);
				finish();
			}
		});

	}

	ListAdapter doMySearch(String constraint) {

		Cursor c = null;
		
		//get all the events .. 
		//TODO: we should only get limited events that is enough for display in current
		//screen.
		if (constraint == null || constraint == "") {

			c = managedQuery(EPGProvider.CONTENT_URI_EVENTS, 
					new String[] {Events.SERVICE_ID, Events.NAME , Events.LEVEL1 , Events.START_TIME}, //selections
					null, 
					null,
					null);

		} else {

			// USE LIKE
			// c = managedQuery(EPGProvider.CONTENT_URI_EVENTS, null,
			// "event_name LIKE " + "\"%" + constraint + "%\"", null, null);

			// use FTS
			
			c = managedQuery(EPGProvider.CONTENT_URI_EVENTS_SEARCH, 
					new String[] {Events.SERVICE_ID, Events.NAME}, //selections
					null,                            //always be NULL 
					new String[] { constraint },    //the keywords
					null);

		}

		// Used to map notes entries from the database to views
		// show only the event name
		
		return getAdaptor(c);
	}
	
	SimpleCursorAdapter getAdaptor(Cursor c)
	{
		
		//The Cursor should include all the entries specified in "from"
		//TODO: add check??
		
		SimpleCursorAdapter adapter = null;
		
		//if the c did not contains "LEVEL1"
		//When using FTS, the returned Cursor won't contain level column
		
		if(c.getColumnIndex(ContentTypeColumns.LEVEL1) != -1)
		{
		
			adapter = new EventCursorAdaptor(this,
				R.layout.list_item, c,
				new String[] { Events.SERVICE_ID, Events.NAME , Events.LEVEL1 ,Events.START_TIME}, 
				new int[] { R.id.serviceID, R.id.eventName ,R.id.eventType ,R.id.startTime});
		
		} else {
			
			adapter = new EventCursorAdaptor(this,
					R.layout.list_item, c,
					new String[] { Events.SERVICE_ID, Events.NAME }, 
					new int[] { R.id.serviceID, R.id.eventName });
			
		}
		
		return adapter;
		
	}
	

	ListAdapter doMySearchByType(String query) {
		Cursor c = null;
		// TODO: use FTS instead of LIKE
		c = managedQuery(EPGProvider.CONTENT_URI_EVENTS_MOVIE, null, null,
				null, null);


		return getAdaptor(c);

	}
	
	 // updates the date in the TextView
    private void updateDisplay() {
        mStarTimeTextView.setText(
            new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mMonth + 1).append("-")
                    .append(mDay).append("-")
                    .append(mStartYear).append(" "));
        
        mEndTimeTextView.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mEndMonth + 1).append("-")
                        .append(mEndDay).append("-")
                        .append(mEndYear).append(" "));
    }
    

	void populate_the_database() {
		// add 2 EPG events after app starte
		// ---add an event
		// ContentValues values = new ContentValues();
		// values.put(EPGProvider.NAME, "hello");
		// values.put(EPGProvider.SHORT_DESCRIPTION, "hello android");
		// Uri uri = getContentResolver().insert(
		// EPGProvider.CONTENT_URI, values);
		//	               
		// //---add another event---
		// values.clear();
		// values.put(EPGProvider.NAME, "hello2");
		// values.put(EPGProvider.SHORT_DESCRIPTION, "hello android2");
		// uri = getContentResolver().insert(
		// EPGProvider.CONTENT_URI, values);
	}
}