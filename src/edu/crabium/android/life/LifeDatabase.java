package edu.crabium.android.life;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LifeDatabase {
	private Context context;
	private static LifeDatabase INSTANCE = new LifeDatabase();
	private LifeDatabase(){
	}
	
	public static LifeDatabase getInstance(){
		return INSTANCE;
	}
	
	public void setContext(Context context){
		this.context = context;
		createTables();
	}
	
	private void createTables(){
		final String PREFIX = "CREATE TABLE IF NOT EXISTS ";
		final int VERSION = 1;
		SQLiteDatabase db = openDatabase();
		db.execSQL(PREFIX + "scores" + "(distance INTEGER, ducky_left INTEGER, passport_left INTEGER, umbrella_left INTEGER, time INTEGER)");
		db.setVersion(VERSION);
		db.close();
	}
	
	private SQLiteDatabase openDatabase(){
		final String DATABASE_NAME = "/data/data/edu.crabium.android.life/life.sqlite3";
		return SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null);
	}
	
	public void addScore(int distance, int duckyCount, int passportCount, int umbrellaCount){
		SQLiteDatabase db = openDatabase();
		db.execSQL("INSERT INTO scores VALUES(\"" 
				+ distance + "\", \"" 
				+ duckyCount + 	"\", \"" 
				+ passportCount + "\", \"" 
				+ umbrellaCount + "\", \"" 
				+ System.currentTimeMillis() + "\")");
		db.close();
	}
	
	public int getRank(int distance){
		SQLiteDatabase db = openDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM scores WHERE distance > ?", new String[]{distance+""});
		cursor.moveToNext();
		int result = cursor.getInt(0);
		cursor.close();
		db.close();
		
		return result;
	}
	
	public long[][] getScores(int count){
		SQLiteDatabase db = openDatabase();
		Cursor cursor = db.rawQuery("SELECT distance, time FROM scores ORDER BY distance DESC, time ASC", new String[]{});
		long[][] scores = new long[cursor.getCount() > count ? count : cursor.getCount()][2];
		for(int i = 0; i < count && i < cursor.getCount(); i ++){
			cursor.moveToNext();
			scores[i][0] = cursor.getLong(0);
			scores[i][1] = cursor.getLong(1);
		}
		cursor.close();
		db.close();
		return scores;
	}
}
