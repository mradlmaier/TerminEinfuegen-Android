package com.mradlmaier.termineinfuegen;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Achtung: Es gibt 2 Möglichkeiten, ein Event einzufügen
        // 1. Mit Intent/GUI
        // 2. Mit ContentResolver ohne GUI
        
        
        //--------------------------------------------------------------------
        // Termin einfügen: Mittels Intent, seit API 14 (Android 4.0), mit GUI
        //--------------------------------------------------------------------
        // Hierfür sind keine uses-permissions nötig
        // Achtung: dies öffnet die Kalendar App UI, mit dem Dialog zum Speichern eines Termins, mit den jeweligen Details
        // vorausgefüllt
        // Dieser Code läuft fehlerlos auf Standard "Google Calendar" App (vor-installiert oder aus dem Google Play Store)
        
	    // ACTION_INSERT funktioniert nicht auf allen Geräten
	    // benutze stattdessen  Intent.ACTION_EDIT auf diesen Geräten (z.B. Samsung Galaxy S3)
        // Auf Geräten, auf denen mehr als eine Kalendar App installiert ist, wird der Benutzer ge-prompt-et, welche App
        // er benutzen will
        
        Log.d("MainActivity", "Termin mittels Intent/UI einfügen");
	    Intent intent = new Intent(Intent.ACTION_INSERT);
	    intent.setData(CalendarContract.Events.CONTENT_URI);
	     
	    intent.setType("vnd.android.cursor.item/event");
	    intent.putExtra(Events.TITLE, "Meeting");
	    intent.putExtra(Events.EVENT_LOCATION, "Büro in Bamberg");
	    intent.putExtra(Events.DESCRIPTION, "Projekt XYZ");
	
	    // Datum: Merke: Monate von 0 - 11! d.h. der Monat 5 ist nicht Mai, sondern Juni
	    GregorianCalendar calDate = new GregorianCalendar(2015, 0, 1);
	    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calDate.getTimeInMillis());
	    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calDate.getTimeInMillis());
	
	    // Ganztägiger Termin
	    intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
	
	    // wöchentliche Wiederholung für 5 Wochen, 10-mal, dienstags und donnerstags
	    // Wiederholungen werden nicht von allen Kalendar Apps korrekt unterstützt
	    intent.putExtra(Events.RRULE, "FREQ=WEEKLY;COUNT=10;WKST=SU;BYDAY=TU,TH");
	
	    // privat und beschäftigt
	    intent.putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
	    intent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
	     
	    startActivity(intent); 
        
	    Log.d("MainActivity", "fertig mit Calendar Events per Intent/UI.");
	    
	    //--------------------------------------------------------------
	    // Termin einfügen, ohne GUI, ab API 14, mittels ContentResolver
	    //--------------------------------------------------------------
	    // per ContentResolver, hierfür sind die uses-permission, minimum SDK = 14 erforderlich im Manifest:
	    // <uses-sdk android:minSdkVersion="14" />
	    // <uses-permission android:name="android.permission.READ_CALENDAR" />
	    // <uses-permission android:name="android.permission.WRITE_CALENDAR" />
        Log.d("MainActivity", "Termin mittels ContentResolver einfügen");
        
	    long calID = this.getCalendarId();
	    long startMillis = 0; 
	    long endMillis = 0;     
	    Calendar beginTime = Calendar.getInstance();
	    // Achtung: 2015,1,1 ist nicht der 1.Januar, sondern der 1.Februar!
	    beginTime.set(2015, 1, 1, 7, 30);
	    startMillis = beginTime.getTimeInMillis();
	    Calendar endTime = Calendar.getInstance();
	    // Achtung: 2015,1,1 ist nicht der 1.Januar, sondern der 1.Februar!
	    endTime.set(2015, 1, 1, 8, 45);
	    endMillis = endTime.getTimeInMillis();
	    
	    ContentResolver cr = getContentResolver();
	    ContentValues values = new ContentValues();
	    values.put(CalendarContract.Events.DTSTART, startMillis);
	    values.put(CalendarContract.Events.DTEND, endMillis);
	    values.put(CalendarContract.Events.TITLE, "Frühstück");
	    values.put(CalendarContract.Events.DESCRIPTION, "Mit den Kollegen");
	    values.put(CalendarContract.Events.CALENDAR_ID, calID);
	    values.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Calendars.CALENDAR_TIME_ZONE);
	    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

	    // letzte Pfadsegment ist die ID des gerade erstellten Events
	    long eventID = Long.parseLong(uri.getLastPathSegment());
	    // 
	    // ... mach was mit der eventID, z.B. Teilnehmer
	    //
	    //
	    Log.d("MainActivity", "eventID: " + String.valueOf(eventID));
	    Log.d("MainActivity", "fertig mit Calendar Events per ContentResolver.");
    }


    private long getCalendarId() {
    	// gibt die ID des ersten Kalenders zurück, der "sichtbar" ist
    	// anstelle dessen könnte man auch über den Cursor loopen und einen anderen Kalender wählen
    	// Cursor ist sowas wie eine Zeiger auf eine Zeile in einer Tabelle
    	String[] projection = 
    		      new String[]{
    		            Calendars._ID, 
    		            Calendars.NAME, 
    		            Calendars.ACCOUNT_NAME, 
    		            Calendars.ACCOUNT_TYPE};
    		Cursor calCursor = getContentResolver().query(Calendars.CONTENT_URI, 
    		                  projection, 
    		                  Calendars.VISIBLE + " = 1", 
    		                  null, 
    		                  Calendars._ID + " ASC");
    		calCursor.moveToFirst();
    		long id = calCursor.getLong(0);
			return id;
    	} 
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
