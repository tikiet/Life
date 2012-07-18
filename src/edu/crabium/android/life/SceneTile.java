package edu.crabium.android.life;

import java.util.Comparator;

import android.graphics.Bitmap;

public class SceneTile {
	//private int magicNumber;
	private long timestamp;
	private int row;
	private Bitmap bitmap;
	
	public SceneTile(int row, long timestamp, Bitmap bitmap){
		//this.magicNumber = magicNumber;
		this.row = row;
		this.timestamp = timestamp;
		this.bitmap = bitmap;
	}
	
	public long getTimestamp(){
		return timestamp;
	}
	
//	public int getMagicNumber(){
//		return magicNumber;
//	}
	
	public int getRow(){
		return row;
	}
	
	public Bitmap getBitmap(){
		return bitmap;
	}
	
	public static class comparator implements Comparator<SceneTile>{
		@Override
		public int compare(SceneTile lhs, SceneTile rhs) {
			if (lhs.timestamp < rhs.timestamp)
				return -1;
			else if(lhs.timestamp > rhs.timestamp)
				return +1;
			else
				return 0;
		}
	}
}
