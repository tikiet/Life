package edu.crabium.android.life;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LifeSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback{

	private LifeDatabase lifeDatabase = LifeDatabase.getInstance();
	
	private SurfaceHolder holder;
	private LifeSurfaceViewThread thread;
	private Bitmap stage;
	private Bitmap stageWithBricks;
	private int heroPositionX;
	private int heroPositionY;
	private int heroSpeedX = 4;
	private int heroSpeedY = 0;
	
	private int distance;
	private int offsetMod;	
	private ThreadState threadState;
	
	private int umbrellaCount;
	private int duckyCount;
	private int passportCount;
	private int availableBrickCount;
	private int availableSpringboardCount;
	
	private boolean usingDuck;
	private boolean usingUmbrella;
	private boolean usingPassport;
	
	private int prevBackPillarEnd;
	private int prevPillarEnd;
	private int prevCloudEnd;
	private int prevGroundEnd;
	private int prevGrassEnd;
	private int prevBrickEnd;
	private int prevDarkCloudEnd;
	
	private int lastBrickOffset;
	private int lastCloudOffset;
	private int lastPillarOffset;
	private int lastPillarOffsetTmp;
	private boolean pillarDrawn = false;
	private int  lastBackPillarOffset;
	
	private PillarPosition previousPillarPosition;
	
	private int BACK_GROUND_COLOR = 0xff9bc3cf;
	private int GROUND_COLOR = 0xffced852;
	
	private int GROUND_SECTION_COLOR = 0xffb3b827;
	private int GROUND_BORDER_COLOR = 0xff8c8302;
	private int SUBTERRANEAN_COLOR = 0xffea9506;
	private int GRASS_COLOR = 0xff54540e;
	private int TRUNK_COLOR = 0xff4c4238;
	
	private int POOL_COLOR = 0xff678ee8;
	private int POOL_SECTION_COLOR = 0xff3266db;
	
	private double GROUND_HEIGHT;
	private double GROUND_SECTION_HEIGHT;
	private double SUBTERRANEAN_HEIGHT;
	private double GRASS_HEIGHT;
	private double GRASS_SPACE;
	
	private double TREE_CROWN_WIDTH;
	private double TREE_CROWN_HEIGHT;
	private double TRUNK_HEIGHT;
	private double TRUNK_WIDTH;
	private double TREE_SPACE_COUNT;
	private double TREE_SHADE_ANGLE;
	private double TREE_CROWN_SHADE_HEIGHT_OFFSET_RATIO;
	private double TREE_THRESHOLD;
	
	private double BUSH_WIDTH;
	private double BUSH_HEIGHT_MAX;
	private double BUSH_HEIGHT_MIN;
	private double BUSH_HEIGHT_OFFSET_FROM_WALL;
	private double BUSH_HEIGHT_OFFSET_FROM_BUSH;
	private double BUSH_SPACE_COUNT;
	private double BUSH_SHADE_HEIGHT_OFFSET_RATIO;
	private double BUSH_SHADE_WIDTH_OFFSET_RATIO;
	private double BUSH_THRESHOLD;
	
	private int BITMAP_WIDTH;
	private int BITMAP_HEIGHT;
	
	private int BLOCK_NUMBER_X;
	private int BLOCK_NUMBER_Y;
	
	private double CLOUD_HEIGHT_MAX;
	private double CLOUD_HEIGHT_MIN;
	private int CLOUD_WIDTH_MAX;
	private int CLOUD_WIDTH_MIN;
	private double CLOUD_SPACE_RATIO;
	private double CLOUD_SHADE_HEIGHT_OFFSET_RATIO;
	private double CLOUD_SHADE_WIDTH_OFFSET_RATIO;
	
	private int PILLAR_WIDTH;
	private double PILLAR_HEIGHT_MAX;
	private double PILLAR_HEIGHT_MIN;
	private double PILLAR_SPACE;
	
	private int BACK_PILLAR_WIDTH;
	private double BACK_PILLAR_HEIGHT_MAX;
	private double BACK_PILLAR_HEIGHT_MIN;
	private double BACK_PILLAR_SHADE_HEIGHT_OFFSET;
	private double BACK_PILLAR_SHADE_WIDTH_OFFSET;
	private double BACK_PILLAR_SPACE;
	
	private int HERO_HEIGHT;
	private int HERO_WIDTH;
	private double HERO_SHADE_HEIGHT_OFFSET;
	private double HERO_SHADE_WIDTH_OFFSET;
	
	private int RANDOM_BRICK_COUNT_MAX;
	private int RANDOM_BRICK_COUNT_MIN;
	private double RANDOM_BRICK_HEIGHT_MAX;
	private double RANDOM_BRICK_HEIGHT_MIN;
	private double RANDOM_BRICK_THRESHOLD;

	private int BRICK_HEIGHT;
	private int BRICK_WIDTH;
	private double BRICK_HEIGHT_OFFSET;
	private double BRICK_SHADE_HEIGHT_OFFSET;
	private double BRICK_SHADE_WIDTH_OFFSET;
	private double BROWN_BRICK_THRESHOLD;
	
	private double POOL_WIDTH_MIN;
	private double POOL_WIDTH_MAX;
	private double POOL_THRESHOLD;
	private int POOL_SPACE_COUNT;
		
	private int CONE_SPACE_COUNT;
	
	private int HERO_SHADE_ALPHA = 60;
	private int BACK_PILLAR_SHADE_ALPHA = 30;
	private int CLOUD_SHADE_ALPHA = 60;
	private int BRICK_SHADE_ALPHA = 60;
	private int BUSH_SHADE_ALPHA = 60;
	
	private int GAME_OVER_SCREEN_SHIFT_SPPED;
	
	private Brick[][] brickArray;
	private Ground[] groundArray;
	
	private int[] pillarColor = new int[]{
			0xff816550, 0xffa38d76, 0xff8e725a, 
			0xffc0b0a3, 0xffa69079, 0xff8c705a, 
			0xff9c846c};
	
	private int[] backPillarColor = new int[]{ 0xff342d27, 0xff221e1b, 0xff201f1d};
	private int[] bushColor = new int[]{
			0xff8d6e23,0xffaa8729,0xff997822,
			0xff7a612b,0xff4c3b1d,0xffccb871,
			0xff7c632c,0xffd4ba5b,0xffc1a446
	};
	
	private Context context;
	
	private Random rand = new Random();
	
	private enum Brick{
		NONE, YELLOW, BROWN, PASSPORT, DUCKY, UMBRELLA,
		DARK_CLOUD_X0Y0, DARK_CLOUD_X1Y0, DARK_CLOUD_X2Y0,
		DARK_CLOUD_X0Y1, DARK_CLOUD_X1Y1, DARK_CLOUD_X2Y1,
		SPRINGBOARD_CLOSED, SPRINGBOARD_OPENED_X1, SPRINGBOARD_OPENED_X2};
	
	private enum Ground{NONE, NORMAL, POOL, BUSH, TREE, CONE, 
		NORMAL_DRAWN, POOL_DRAWN, BUSH_DRAWN, TREE_DRAWN, CONE_DRAWN}
	
 	private class LifeSurfaceViewThread extends Thread{
		public LifeSurfaceViewThread(SurfaceHolder holder, Context context) {
		}

		long lastTimeIncreaseSpringboard;
		public void run(){
			while(threadState == ThreadState.RUNNING){
				Canvas canvas = new Canvas(stageWithBricks);
				RectF stageRectF = new RectF( 0, 0, stage.getWidth(), stage.getHeight());			
				canvas.drawBitmap(stage, null, stageRectF, null);
				

				synchronized(brickArray){
					drawBrickShade(canvas, 0, offsetMod);
				}
				
				drawHeroShade(canvas, heroSpeedX);
				
				synchronized(brickArray){
					drawBrick(0, offsetMod);
				}
				
			
				Rect rSrc = new Rect(0,0,BITMAP_WIDTH,BITMAP_HEIGHT);
				
				RectF rDest = new RectF(0,0,BITMAP_WIDTH,BITMAP_HEIGHT);
				
				drawHero(canvas);

				updateHero(canvas);
				
				drawGameInfo(canvas);
				
				if(usingDuck)
					drawDuckCounter(canvas);
				
				if(usingUmbrella)
					drawUmbrellaCounter(canvas);
				
				if(usingPassport)
					drawPassportCounter(canvas);

				if(threadState != ThreadState.RUNNING) break;
				canvas = holder.lockCanvas();
				canvas.drawBitmap(stageWithBricks, rSrc, rDest, null);
				holder.unlockCanvasAndPost(canvas);
				
				prevX -= heroSpeedX;
				
				offsetMod = (int) ((offsetMod + heroSpeedX) % BRICK_WIDTH);
				if(offsetMod == 0 && heroSpeedX != 0){
					synchronized(brickArray){
						for(int i = 0; i < brickArray.length - 1; i++)
							for(int j = 0; j < brickArray[i].length; j++)
								brickArray[i][j] = brickArray[i+1][j];
						for(int i = 0; i < brickArray[brickArray.length - 1].length; i ++)
							brickArray[brickArray.length - 1][i] = Brick.NONE;
					}
					
					for(int i = 0; i < groundArray.length - 1; i++)
						groundArray[i] = groundArray[i+1];
					groundArray[groundArray.length-1] = Ground.NONE;
				}
				
				/*
				 * shift stage
				 */
				rSrc = new Rect(heroSpeedX, 0, stage.getWidth(),	stage.getHeight());
				rDest = new RectF(0, 0,	stage.getWidth() - heroSpeedX, stage.getHeight());
				
				canvas = new Canvas(stage);
				canvas.drawBitmap(stage, rSrc, rDest, null);
				
				Paint backGroundPaint = new Paint();
				backGroundPaint.setColor(BACK_GROUND_COLOR);
				canvas.drawRect(
						(float) (previousPillarPosition.getX() + BACK_PILLAR_WIDTH),
						(float)0.0, 
						(float)stage.getWidth(), 
						(float)stage.getHeight(), 
						backGroundPaint);
				
				// Draw back pillar
				prevBackPillarEnd = drawBackPillarAndShade(
						canvas, 
						prevBackPillarEnd, 
						stage.getWidth(), 
						heroSpeedX);
				
				// Draw pillar
				prevPillarEnd = drawPillar(canvas, prevPillarEnd, prevBackPillarEnd, heroSpeedX);
				
				// Draw cloud
				prevCloudEnd = drawCloudAndShade(canvas, prevCloudEnd, prevBackPillarEnd, heroSpeedX);
				
				// Draw brick
				prevBrickEnd = generateRandomBrick(prevBrickEnd, brickArray, heroSpeedX);

				// Draw dark cloud
				prevDarkCloudEnd = generateDarkCloud(prevDarkCloudEnd, brickArray, heroSpeedX);
				
				// Draw ground
				generateRandomGround(groundArray, prevPillarEnd - lastPillarOffset, offsetMod);
				drawGround(canvas, prevPillarEnd - lastPillarOffset, offsetMod);

				// Draw grass
				prevGrassEnd = drawGrass(canvas, prevGrassEnd, prevPillarEnd, heroSpeedX);
				
				if(pillarDrawn){
					lastPillarOffsetTmp = 0;
					pillarDrawn = false;
				}
				
				if(System.currentTimeMillis() - lastTimeIncreaseSpringboard >= 10000){
					lastTimeIncreaseSpringboard = System.currentTimeMillis();
					availableSpringboardCount ++;
				}
				
				distance += heroSpeedX;
			}
		}
 
		public boolean onTouchEvent(MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(duckyCountRectF.contains(event.getX(), event.getY()) &&
					duckyCount > 0){
					usingDuck = true;
					duckStartTime = System.currentTimeMillis();
					duckyCount --;
					return true;
				}
				else if(passportCountRectF.contains(event.getX(), event.getY()) &&
					passportCount > 0){
					usingPassport = true;
					passportStartTime = System.currentTimeMillis();
					passportCount --;
					return true;
				}
				else if(umbrellaCountRectF.contains(event.getX(), event.getY()) &&
					umbrellaCount > 0){
					usingUmbrella = true;
					umbrellaStartTime = System.currentTimeMillis();
					umbrellaCount --;
					return true;
				}
			}
			
			if(event.getAction() == MotionEvent.ACTION_MOVE ||
					event.getAction() == MotionEvent.ACTION_DOWN){
				synchronized(brickArray){
					for(int pointId = 0; pointId < event.getPointerCount(); pointId ++){
						float x = event.getX(pointId);
						float y = event.getY(pointId);
						Log.d("Life", "[onTouchEvent] x = " + x + ", y = " + y);
						
						int blockX = (int) Math.floor((x + offsetMod)/BRICK_WIDTH);
						int blockY = (int) Math.floor((y - BRICK_HEIGHT_OFFSET)/BRICK_HEIGHT);
						if(blockY < 0 || blockY >= BLOCK_NUMBER_Y)
							continue;
						
						Log.d("Life", "[onTouchEvent] blockX = " + blockX + ", blockY = " + blockY);
						
						int prevBlockX = (int) Math.floor((prevX + offsetMod)/BRICK_WIDTH);
						int prevBlockY = (int) Math.floor((prevY - BRICK_HEIGHT_OFFSET)/BRICK_HEIGHT);
						if(blockX != prevBlockX || blockY != prevBlockY){
							prevX = (int) x;
							prevY = (int) y;
							
							if( (brickArray[blockX][blockY] == Brick.YELLOW) ||
								(brickArray[blockX][blockY] == Brick.DUCKY) ||
								(brickArray[blockX][blockY] == Brick.PASSPORT) ||
								(brickArray[blockX][blockY] == Brick.UMBRELLA)){
								
								if(brickArray[blockX][blockY] == Brick.DUCKY)
									duckyCount ++;
								else if (brickArray[blockX][blockY] == Brick.PASSPORT)
									passportCount ++;
								else if (brickArray[blockX][blockY] == Brick.UMBRELLA)
									umbrellaCount ++;
								
								brickArray[blockX][blockY] = Brick.NONE;
							}
							else if(brickArray[blockX][blockY] == Brick.NONE ){
								if(	(blockY <= brickArray[0].length - 3) &&
									(availableBrickCount > 0) &&
									(y >= (BITMAP_HEIGHT - PILLAR_HEIGHT_MAX * BITMAP_HEIGHT))){
									if(rand.nextDouble() >= BROWN_BRICK_THRESHOLD)
										brickArray[blockX][blockY] = Brick.BROWN;
									else
										brickArray[blockX][blockY] = Brick.YELLOW;
									availableBrickCount --;
									}
								else if((blockY == brickArray[0].length -1) &&
										(availableSpringboardCount > 0) &&
										(groundArray[blockX] != Ground.POOL) &&
										(groundArray[blockX] != Ground.POOL_DRAWN)){
									brickArray[blockX][blockY] = Brick.SPRINGBOARD_CLOSED;
									availableSpringboardCount --;
								}
							}
						}
						else{
							Log.d("Life", "[onTouchEvent] prevBlockX = " + prevBlockX +
									", prevBlockY = " + prevBlockY);
						}
					}
				}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				prevY = -1;
			}
			return true;
		}
	}
 	
 	int poolRemaining;
 	int poolCoolingDistance;
	int treeCoolingDistance;
	int bushCoolingDistance;
	int coneCoolingDistance;
	private void generateRandomGround(Ground[] groundArray, int end, int offset) {
		int i = 0;
		int groundArrayEnd = (int) ((end + offset) / BRICK_WIDTH);
		
		while(i <= groundArrayEnd && groundArray[i] != Ground.NONE)
			i ++;
		
		for(; i <= groundArrayEnd; i ++ ){
			if(poolRemaining > 0){
				groundArray[i] = Ground.POOL;
				poolRemaining --;
				poolCoolingDistance = POOL_SPACE_COUNT;
			}
			else if( (poolCoolingDistance <= 0 ) && (rand.nextDouble() > POOL_THRESHOLD)){
				poolRemaining = (int) 
					((POOL_WIDTH_MIN + rand.nextDouble() 
					* (POOL_WIDTH_MAX - POOL_WIDTH_MIN)) 
					/ BRICK_WIDTH);
				groundArray[i] = Ground.POOL;
				poolCoolingDistance = POOL_SPACE_COUNT;
			}
			else if( (treeCoolingDistance <= 0) && (rand.nextDouble() > TREE_THRESHOLD)){
				groundArray[i] = Ground.TREE;
				treeCoolingDistance = (int) (TREE_SPACE_COUNT);
			}
			else if( (bushCoolingDistance <= 0) && (rand.nextDouble() > BUSH_THRESHOLD)){
				groundArray[i] = Ground.BUSH;
				bushCoolingDistance = (int) (BUSH_SPACE_COUNT );
			}
			else if( (coneCoolingDistance <= 0) && (rand.nextDouble() > 0.6)){
				groundArray[i] = Ground.CONE;
				coneCoolingDistance = CONE_SPACE_COUNT;
			}
			else
				groundArray[i] = Ground.NORMAL;
			
			treeCoolingDistance --;
			bushCoolingDistance --;
			coneCoolingDistance --;
			poolCoolingDistance --;
			
			Log.d("Life","Ground " + groundArray[i]);
		}
	}
	
 	public void setThreadState(ThreadState r){
 		if(r == ThreadState.RUNNING){
 			if(getThreadState() != ThreadState.RUNNING){
 				threadState = ThreadState.RUNNING;
 				thread = new LifeSurfaceViewThread(holder, context);
 				thread.start();
 			}
 		}
 		else if( r == ThreadState.RECOVER){
 			threadState = ThreadState.STOPPED;
 			retrieveLifeData();
 			threadState = ThreadState.RUNNING;
 			thread = new LifeSurfaceViewThread(holder, context);
 			thread.start();
 		}
 		else if( r == ThreadState.STOPPED){
 			threadState = ThreadState.STOPPED;
 			saveLifeData();
 		}
 	}
 	
 	boolean isAfterPausing;
 	private void retrieveLifeData(){
 		// Check whether all of the files are present
 		File stageFile = new File("/data/data/edu.crabium.android.life/stage");
 		File arrayFile = new File("/data/data/edu.crabium.android.life/array");
 		File groundArrayFile = new File("/data/data/edu.crabium.android.life/groundArray");
 		if(!(stageFile.exists() && arrayFile.exists() && groundArrayFile.exists()))
 			return;
 		else
 			isAfterPausing = true;
 		
 		try{
 			FileInputStream stageIn = new FileInputStream("/data/data/edu.crabium.android.life/stage");
 			Bitmap oldStage = BitmapFactory.decodeStream(stageIn);
 			Canvas c = new Canvas(stage);
 			c.drawBitmap(oldStage, null, new RectF(0,0,stage.getWidth(), stage.getHeight()), null);
 			stageIn.close();
 			File file = new File("/data/data/edu.crabium.android.life/stage");
 			file.delete();
 		}
 		catch(Exception e){
 		}
 		
 		try{
 			BufferedReader arrayIn = new BufferedReader(new FileReader("/data/data/edu.crabium.android.life/array"));
 			int width = Integer.valueOf(arrayIn.readLine());
 			int height = Integer.valueOf(arrayIn.readLine());
 			brickArray = new Brick[width][height];
 			for(int i = 0; i < width; i ++){
 				for(int j = 0; j < height; j++){
 					brickArray[i][j] = Brick.valueOf(arrayIn.readLine());
 				}
 			}
 			arrayIn.close();
 			File file = new File("/data/data/edu.crabium.android.life/array");
 			file.delete();
 		}
 		catch(Exception e){
 		}
 		
 		try{
 			BufferedReader groundArrayIn = new BufferedReader(new FileReader("/data/data/edu.crabium.android.life/groundArray"));
 			int width = Integer.valueOf(groundArrayIn.readLine());
 			groundArray = new Ground[width];
 			for(int i = 0; i < width; i ++){
 				groundArray[i] = Ground.valueOf(groundArrayIn.readLine());
			}
 			groundArrayIn.close();
 			File file = new File("/data/data/edu.crabium.android.life/groundArray");
 			file.delete();
 		}
 		catch(Exception e){
 			Log.d("Life", "groundArray == null: " + (groundArray == null) + "");
 		}
 		
		SharedPreferences settings = context.getSharedPreferences("Life", 0);
		
		heroPositionX = settings.getInt("heroPositionX", heroPositionX);
		heroPositionY = settings.getInt("heroPositionY", heroPositionY);
		heroSpeedX = settings.getInt("heroSpeedX", heroSpeedX);
		heroSpeedY = settings.getInt("heroSpeedY", heroSpeedY);
		offsetMod = settings.getInt("offsetMod", offsetMod);
		distance = settings.getInt("distance", distance);
		
		prevBackPillarEnd = settings.getInt("prevBackPillarEnd", prevBackPillarEnd);
		prevPillarEnd = settings.getInt("prevPillarEnd", prevPillarEnd);
		prevCloudEnd = settings.getInt("prevCloudEnd", prevCloudEnd);
		prevGroundEnd = settings.getInt("prevGroundEnd", prevGroundEnd);
		prevGrassEnd = settings.getInt("prevGrassEnd", prevGrassEnd);
		prevBrickEnd = settings.getInt("prevBrickEnd", prevBrickEnd);
		prevDarkCloudEnd = settings.getInt("prevDarkCloudEnd", prevDarkCloudEnd);
		
		lastBrickOffset = settings.getInt("lastBrickOffset", lastBrickOffset);
		lastCloudOffset = settings.getInt("lastCloudOffset", lastCloudOffset);
		lastPillarOffset = settings.getInt("lastPillarOffset", lastPillarOffset);
		lastPillarOffsetTmp = settings.getInt("lastPillarOffsetTmp", lastPillarOffsetTmp);
		pillarDrawn = settings.getBoolean("pillarDrawn", pillarDrawn);
		lastBackPillarOffset = settings.getInt("lastBackPillarOffset", lastBackPillarOffset);
		
		poolRemaining = settings.getInt("poolRemaining", poolRemaining);
		treeCoolingDistance = settings.getInt("treeCoolingDistance", treeCoolingDistance);
		bushCoolingDistance = settings.getInt("bushCoolingDistance", bushCoolingDistance);
		
		float previousPillarPositionX  = settings.getFloat("previousPillarPositionX", 0);
		float previousPillarPositionY  = settings.getFloat("previousPillarPositionY", 0);
		
		umbrellaCount = settings.getInt("umbrellaCount", umbrellaCount);
		duckyCount = settings.getInt("duckyCount", duckyCount);
		passportCount = settings.getInt("passportCount", passportCount);
		availableBrickCount = settings.getInt("availableBrickCOunt", availableBrickCount);
		availableSpringboardCount = settings.getInt("availableSpringboardCount", availableSpringboardCount);
		
		if(previousPillarPositionX != 0 || previousPillarPositionY != 0){
 			previousPillarPosition = new PillarPosition(
 					previousPillarPositionX, 
 					previousPillarPositionY);
		}
 	}
 	
 	private void saveLifeData(){
 		try {
 			FileOutputStream stageOut = new FileOutputStream("/data/data/edu.crabium.android.life/stage");
 			stage.compress(Bitmap.CompressFormat.PNG, 90, stageOut);
 			stageOut.close();
 			
 			PrintWriter arrayOut = new PrintWriter("/data/data/edu.crabium.android.life/array");
 			arrayOut.println(brickArray.length);
 			arrayOut.println(brickArray[0].length);
 			for(int i = 0; i < brickArray.length; i ++){
 				for(int j = 0; j < brickArray[0].length; j++){
 					arrayOut.println(brickArray[i][j].toString());
 				}
 			}
 			
 			PrintWriter groundArrayOut = new PrintWriter("/data/data/edu.crabium.android.life/groundArray");
 			groundArrayOut.println(groundArray.length);
 			for(int i = 0; i < groundArray.length; i ++){
				groundArrayOut.println(groundArray[i].toString());
			}
 			
 			SharedPreferences settings = context.getSharedPreferences("Life", 0);
 			SharedPreferences.Editor editor = settings.edit();
 			
 			editor.putInt("heroPositionX",heroPositionX);
 			editor.putInt("heroPositionY",heroPositionY);
 			editor.putInt("heroSpeedX",heroSpeedX);
 			editor.putInt("heroSpeedY",heroSpeedY);
 			editor.putInt("offsetMod",offsetMod);
 			editor.putInt("distance", distance);
 			
 			editor.putInt("prevBackPillarEnd",prevBackPillarEnd);
 			editor.putInt("prevPillarEnd",prevPillarEnd);
 			editor.putInt("prevCloudEnd",prevCloudEnd);
 			editor.putInt("prevGroundEnd",prevGroundEnd);
 			editor.putInt("prevGrassEnd",prevGrassEnd);
 			editor.putInt("prevBrickEnd",prevBrickEnd);
 			editor.putInt("prevDarkCloudEnd",prevDarkCloudEnd);
 			
 			editor.putInt("lastBrickOffset",lastBrickOffset);
 			editor.putInt("lastCloudOffset",lastCloudOffset);
 			editor.putInt("lastPillarOffset",lastPillarOffset);
 			editor.putInt("lastPillarOffsetTmp",lastPillarOffsetTmp);
 			editor.putBoolean("pillarDrawn",pillarDrawn);
 			editor.putInt("lastBackPillarOffset",lastBackPillarOffset);
 			
 			editor.putFloat("previousPillarPositionX", (float) previousPillarPosition.getX());
 			editor.putFloat("previousPillarPositionY", (float) previousPillarPosition.getY());

 			editor.putBoolean("usingDuck",usingDuck);
 			editor.putBoolean("usingUmbrella",usingUmbrella);
 			editor.putBoolean("usingPassport",usingPassport);
 			
 			editor.putInt("umbrellaCount",umbrellaCount);
 			editor.putInt("duckyCount",duckyCount);
 			editor.putInt("passportCount",passportCount);
 			editor.putInt("availableBrickCount",availableBrickCount);
 			editor.putInt("availableSpringboardCount",availableSpringboardCount);
 			editor.commit();
 			
 			arrayOut.close();
 			groundArrayOut.close();
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 	}
 	}
 	
 	public ThreadState getThreadState(){
 		return threadState;
 	}
 	
 	public LifeSurfaceViewThread getThread(){
 		return thread;
 	}
 	
	public LifeSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		lifeDatabase.setContext(context);
		
		holder = getHolder();
		holder.addCallback(this);
		this.context = context;
	}

	public void setHeight(int height){
		BITMAP_HEIGHT = height;
	}
	
	public void setWidth(int width){
		BITMAP_WIDTH = width;
	}
	
	public void setBrickHeight(int height){
		BRICK_HEIGHT = height;
	}
	
	public void setBrickWidth(int width){
		BRICK_WIDTH = width;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(event.getPointerCount() == 1 
			&& (threadState == ThreadState.STOPPED || threadState == ThreadState.STOPPING) 
			&& event.getAction() == MotionEvent.ACTION_DOWN){
			if(restartDestRect.contains(event.getX(), event.getY())){
				poolCoolingDistance = BITMAP_WIDTH / BRICK_WIDTH;
				coneCoolingDistance = BITMAP_WIDTH / BRICK_WIDTH;
				poolRemaining = 0;
				
				resetVariables();
				thread = new LifeSurfaceViewThread(holder, context);
				threadState = ThreadState.RUNNING;
				thread.start();
				
				return true;
			}
			else if(cancelDestRect.contains(event.getX(), event.getY())){
				poolCoolingDistance = BITMAP_WIDTH / BRICK_WIDTH;
				coneCoolingDistance = BITMAP_WIDTH / BRICK_WIDTH;
				poolRemaining = 0;
				
				resetVariables();
				displayHome();
				
				threadState = ThreadState.READY;
				return true;
			}
		}
		else if(event.getPointerCount() == 1 && threadState == ThreadState.READY && event.getAction() == MotionEvent.ACTION_DOWN){
			onStart();
		}
		
		if(thread == null)
			return true;
		
		return thread.onTouchEvent(event);
	}
	
	private void resetVariables(){
		groundArray = null;
		brickArray = null;
		
		heroPositionX = 2*BRICK_WIDTH;
		heroPositionY = 2*BRICK_HEIGHT;
		heroSpeedX = 4;
		distance = 0;
		offsetMod = 0;
		umbrellaCount = 0;
		duckyCount = 0;
		passportCount = 0;
		usingDuck = false;
		usingUmbrella = false;
		usingPassport = false;
		
		prevBackPillarEnd = 0;
		prevPillarEnd = 0;
		prevCloudEnd = 0;
		prevGroundEnd = 0;
		prevGrassEnd = 0;
		prevBrickEnd = 0;
		prevDarkCloudEnd = 0;
		
		lastBrickOffset = 0;
		lastCloudOffset = 0;
		lastPillarOffset = 0;
		lastPillarOffsetTmp = 0;
		pillarDrawn = false;
		lastBackPillarOffset = 0;
		
		previousPillarPosition = null;
	
		initializeVariables(context);
		initializeStage();
	}
	private void onStart() {
		threadState = ThreadState.SHIFTING;
		
		for(int i = 0; i <= BITMAP_HEIGHT/2; i += GAME_OVER_SCREEN_SHIFT_SPPED){
			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(BACK_GROUND_COLOR);
			Rect srcRect = new Rect(0,0,BITMAP_WIDTH, BITMAP_HEIGHT/2 + i);
			RectF destRect = new RectF(0, BITMAP_HEIGHT/2 - i , BITMAP_WIDTH, BITMAP_HEIGHT);
			canvas.drawBitmap(stageWithBricks, srcRect, destRect, null);
			holder.unlockCanvasAndPost(canvas);
		}
		
		thread = new LifeSurfaceViewThread(holder, context);
		threadState = ThreadState.RUNNING;
		thread.start();
	}

	private void initializeStage() {
		if(brickArray == null){
			brickArray = new Brick[BLOCK_NUMBER_X][BLOCK_NUMBER_Y];
			for(int i = 0; i < brickArray.length; i++)
				for(int j = 0; j < brickArray[i].length; j++)
					brickArray[i][j] = Brick.NONE;
		}
		
		prevBrickEnd = generateRandomBrick(prevBrickEnd, brickArray, 0);

		Log.d("Life", "before prevBackPillarEnd=" + prevBackPillarEnd );
		Canvas canvas = new Canvas(stage);
		prevBackPillarEnd = drawBackPillarAndShade(
				canvas, 
				(int)(prevBackPillarEnd), 
				stage.getWidth(), 
				heroSpeedX);
		Log.d("Life", "after prevBackPillarEnd=" + prevBackPillarEnd );
		int prevPillarEndTmp = prevPillarEnd;
		
		if(prevPillarEnd == 0)
			prevPillarEnd = drawPillar(canvas, PILLAR_WIDTH / 2 , prevBackPillarEnd, heroSpeedX);
		else
			prevPillarEnd = drawPillar(canvas, prevPillarEnd, prevBackPillarEnd, heroSpeedX);

		if(groundArray == null){
			groundArray = new Ground[(int) (stage.getWidth() / BRICK_WIDTH)];
			for(int i = 0; i < groundArray.length; i++)
				groundArray[i] = Ground.NONE;

			generateRandomGround(groundArray, prevPillarEnd - lastPillarOffset, 0);
			Log.d("Life", "1:" + Arrays.toString(groundArray));
		}
		
		prevCloudEnd = drawCloudAndShade(canvas, prevCloudEnd, prevBackPillarEnd, heroSpeedX);

		Log.d("Life", "prevPillarEnd - lastPillarOffset:"+ ( prevPillarEnd - lastPillarOffset));
		drawGround(canvas, prevPillarEnd - lastPillarOffset, 0/*heroSpeedX*/ );
		
		prevGrassEnd = drawGrass(canvas, prevGrassEnd, prevPillarEndTmp, heroSpeedX);
		
		prevDarkCloudEnd = prevCloudEnd;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		poolCoolingDistance = BITMAP_WIDTH / BRICK_WIDTH;
		coneCoolingDistance = BITMAP_WIDTH / BRICK_WIDTH;
		poolRemaining = 0;
		
		initializeVariables(context);
		initializeStage();
		
		if(isAfterPausing){
			thread = new LifeSurfaceViewThread(holder, context);
			threadState = ThreadState.RUNNING;
			thread.start();
		}
		else{
			displayHome();
			threadState = ThreadState.READY;
		}
	}
	
	private void displayHome() {
		Canvas canvas = new Canvas(stageWithBricks);
		RectF stageRectF = new RectF( 0, 0, stage.getWidth(), stage.getHeight());			
		canvas.drawBitmap(stage, null, stageRectF, null);
		
		synchronized(brickArray){
			drawBrickShade(canvas, 0, offsetMod);
		}
		
		drawHeroShade(canvas, heroSpeedX);
		
		synchronized(brickArray){
			drawBrick(0, offsetMod);
		}
		
		drawHero(canvas);
		
		canvas = holder.lockCanvas();
		canvas.drawColor(BACK_GROUND_COLOR);
		Rect srcRect = new Rect(0,0,BITMAP_WIDTH, BITMAP_HEIGHT/2);
		RectF destRect = new RectF(0, BITMAP_HEIGHT/2, BITMAP_WIDTH, BITMAP_HEIGHT);
		
		canvas.drawBitmap(stageWithBricks, srcRect, destRect, null);
		
		Paint textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(BITMAP_WIDTH / 10);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(Color.WHITE);
		canvas.drawText("Uniform Motion", BITMAP_WIDTH/2, BITMAP_HEIGHT/3, textPaint);
		
		textPaint.setTextSize(BITMAP_WIDTH / 6);
		canvas.drawText("Life", BITMAP_WIDTH/2, BITMAP_HEIGHT*2/3, textPaint);
		holder.unlockCanvasAndPost(canvas);
		
	}

	private int generateRandomBrick(int start, Brick[][] array, int offset) {
		int result = start;
		lastBrickOffset += offset;
		boolean randomBrickOn = false;
		int randomBricksRemain = 0;
		
		start = (int) (Math.floor(start - lastBrickOffset) / BRICK_WIDTH);
		for(int i = start; i < array.length - RANDOM_BRICK_COUNT_MAX; i++){
			if(randomBrickOn){
				lastBrickOffset = 0;
				
				randomBricksRemain = RANDOM_BRICK_COUNT_MIN + 
						rand.nextInt(RANDOM_BRICK_COUNT_MAX - RANDOM_BRICK_COUNT_MIN);
				
				int randomBrickHeight = BITMAP_HEIGHT - (int) (RANDOM_BRICK_HEIGHT_MIN*BITMAP_HEIGHT +
						rand.nextInt((int)(RANDOM_BRICK_HEIGHT_MAX * BITMAP_HEIGHT - 
								RANDOM_BRICK_HEIGHT_MIN* BITMAP_HEIGHT))) ;
				
				int randomBrickBlockHeight = (int) (randomBrickHeight / BRICK_HEIGHT);

				String brickPattern = Integer.toBinaryString((1 << rand.nextInt(randomBricksRemain)) + (1 << (randomBricksRemain))) + "0";
				brickPattern = brickPattern.substring(1);
				Log.d("Life", "brickPattern:" + brickPattern);
				
				for(int j = 0; j < randomBricksRemain; j++){
					if( (i+j) < 0 || (i+j) >= array.length)
						continue;
					Log.d("Life", "i="+i+",j="+j+",length="+array.length);
					array[i + j][randomBrickBlockHeight] = 
							Integer.valueOf(brickPattern.substring(j,j+1)) == 1 ? Brick.BROWN : Brick.YELLOW;
					
					if( array[i + j][randomBrickBlockHeight-1] == Brick.NONE &&
						array[i + j][randomBrickBlockHeight] == Brick.BROWN	&& 
					    rand.nextDouble() > 0.5){
						
						double tmp = rand.nextDouble();
						if(tmp > 0.6)
							array[i + j][randomBrickBlockHeight-1] = Brick.PASSPORT;
						else if(tmp > 0.3)
							array[i + j][randomBrickBlockHeight-1] = Brick.DUCKY;
						else
							array[i + j][randomBrickBlockHeight-1] = Brick.UMBRELLA;
					}
				}
				
				result = (int) ((i + randomBricksRemain + 1) * BRICK_WIDTH);
				randomBrickOn = false;
				i+= randomBricksRemain / 2;
				
			}else{
				if(rand.nextDouble() >= RANDOM_BRICK_THRESHOLD){
					randomBrickOn = true;
				}
			}
		}
		
		return result;
	}
	double duckStartTime;
	private void drawDuckCounter(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		if(currentTime - duckStartTime > 5000){
			usingDuck = false;
		}
		else{
			double usedTimeRatio = (currentTime - duckStartTime) / 5000 * 360;
			Paint duckPaint = new Paint();
			duckPaint.setAntiAlias(true);
			
			duckPaint.setStyle(Style.STROKE);
			duckPaint.setColor(Color.BLACK);
			duckPaint.setStrokeWidth(3);
			canvas.drawCircle(9*BRICK_WIDTH, BITMAP_HEIGHT/10  , BRICK_WIDTH + 2, duckPaint);

			duckPaint.setStyle(Style.FILL_AND_STROKE);
			duckPaint.setColor(Color.YELLOW);
			canvas.drawCircle(9*BRICK_WIDTH, BITMAP_HEIGHT/10  , BRICK_WIDTH, duckPaint);
			duckPaint.setColor(Color.GRAY);
			canvas.drawArc(
					new RectF(8*BRICK_WIDTH, BITMAP_HEIGHT/10 - BRICK_WIDTH, 10*BRICK_WIDTH, BITMAP_HEIGHT/10 + BRICK_WIDTH), 
					(float)0, 
					(float)usedTimeRatio, 
					true, 
					duckPaint);
		}
		
	}
	
	double umbrellaStartTime;
	private void drawUmbrellaCounter(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		if(currentTime - umbrellaStartTime > 5000){
			usingUmbrella = false;
		}
		else{
			double usedTimeRatio = (currentTime - umbrellaStartTime) / 5000 * 360;
			Paint umbrellaPaint = new Paint();
			umbrellaPaint.setAntiAlias(true);
			
			umbrellaPaint.setStyle(Style.STROKE);
			umbrellaPaint.setColor(Color.BLACK);
			umbrellaPaint.setStrokeWidth(3);
			canvas.drawCircle(11*BRICK_WIDTH + BRICK_WIDTH/3, BITMAP_HEIGHT/10, BRICK_WIDTH + 2, umbrellaPaint);

			umbrellaPaint.setStyle(Style.FILL_AND_STROKE);
			umbrellaPaint.setColor(POOL_SECTION_COLOR);
			canvas.drawCircle(11*BRICK_WIDTH + BRICK_WIDTH/3, BITMAP_HEIGHT/10, BRICK_WIDTH, umbrellaPaint);
			umbrellaPaint.setColor(Color.GRAY);
			canvas.drawArc(
					new RectF(10*BRICK_WIDTH + BRICK_WIDTH/3, BITMAP_HEIGHT/10 - BRICK_WIDTH, 12*BRICK_WIDTH + BRICK_WIDTH/3, BITMAP_HEIGHT/10 + BRICK_WIDTH), 
					(float)0, 
					(float)usedTimeRatio, 
					true, 
					umbrellaPaint);
		}
		
	}

	
	double passportStartTime;
	private void drawPassportCounter(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		if(currentTime - passportStartTime > 5000){
			usingPassport = false;
		}
		else{
			double usedTimeRatio = (currentTime - passportStartTime) / 5000 * 360;
			Paint passportPaint = new Paint();
			passportPaint.setAntiAlias(true);
			
			passportPaint.setStyle(Style.STROKE);
			passportPaint.setColor(Color.BLACK);
			passportPaint.setStrokeWidth(3);
			canvas.drawCircle(13*BRICK_WIDTH + 2 * BRICK_WIDTH/3, BITMAP_HEIGHT/10, BRICK_WIDTH + 2, passportPaint);

			passportPaint.setStyle(Style.FILL_AND_STROKE);
			passportPaint.setColor(0xff85472b);
			canvas.drawCircle(13*BRICK_WIDTH + 2 * BRICK_WIDTH/3, BITMAP_HEIGHT/10, BRICK_WIDTH, passportPaint);
			passportPaint.setColor(Color.GRAY);
			canvas.drawArc(
					new RectF(12*BRICK_WIDTH + 2 * BRICK_WIDTH/3, BITMAP_HEIGHT/10 - BRICK_WIDTH, 14*BRICK_WIDTH + 2 * BRICK_WIDTH/3, BITMAP_HEIGHT/10 + BRICK_WIDTH), 
					(float)0, 
					(float)usedTimeRatio, 
					true, 
					passportPaint);
		}
		
	}
	
	RectF duckyCountRectF;
	RectF umbrellaCountRectF;
	RectF passportCountRectF;
	RectF availableBrickCountRectF;
	RectF availableSpringboardCountRectF;
	private void drawGameInfo(Canvas canvas) {
		int textSize = 0;
		
		// Display distance
		Paint distancePaint = new Paint();
		distancePaint.setAntiAlias(true);
		distancePaint.setColor(Color.WHITE);
		distancePaint.setTextSize(BITMAP_WIDTH/20);
		distancePaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(String.format("%05dM", distance/4), BITMAP_HEIGHT/10 - BITMAP_WIDTH/20, BITMAP_HEIGHT/10, distancePaint);
		
		// Decide text size
		Paint tmpPaint = new Paint();
		tmpPaint.setTextAlign(Align.LEFT);
		tmpPaint.setTypeface(Typeface.MONOSPACE);
		for(int size = 1;; size++){
			tmpPaint.setTextSize(size);
			Rect tmpRect = new Rect();
			tmpPaint.getTextBounds("00", 0, 2, tmpRect);
			
			if((tmpRect.width() > duckyCountRectF.width()*0.6) ||
			   (tmpRect.height() > duckyCountRectF.height())){
				textSize = size - 1;
				Log.d("Life", "set text size to " + textSize);
				break;
			}
		}
		
		// Display ducky info
		Drawable duckyDrawable = context.getResources().getDrawable(R.drawable.ducky);
		Bitmap duckyBitmap = ((BitmapDrawable)duckyDrawable).getBitmap();
		RectF duckyRectF = new RectF(
				duckyCountRectF.left + duckyCountRectF.width()*(float)0.7, 
				duckyCountRectF.top + duckyCountRectF.height()/2, 
				duckyCountRectF.right, duckyCountRectF.bottom);
		canvas.drawBitmap(duckyBitmap, null, duckyRectF, null);
				
		Paint duckyPaint = new Paint();
		duckyPaint.setAntiAlias(true);
		duckyPaint.setColor(Color.WHITE);
		duckyPaint.setTextSize(textSize);
		duckyPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(String.format("%02d", duckyCount),
				duckyCountRectF.left, duckyCountRectF.bottom ,
				duckyPaint);
		
		// Display umbrella info
		Drawable umbrellaDrawable = context.getResources().getDrawable(R.drawable.umbrella);
		Bitmap umbrellaBitmap = ((BitmapDrawable)umbrellaDrawable).getBitmap();
		RectF umbrellaRectF = new RectF(
				umbrellaCountRectF.left + umbrellaCountRectF.width()*(float)0.7, 
				umbrellaCountRectF.top + umbrellaCountRectF.height()/2, 
				umbrellaCountRectF.right, umbrellaCountRectF.bottom);
		canvas.drawBitmap(umbrellaBitmap, null, umbrellaRectF, null);
		
		Paint umbrellaPaint = new Paint();
		umbrellaPaint.setAntiAlias(true);
		umbrellaPaint.setColor(Color.WHITE);
		umbrellaPaint.setTextSize(textSize);
		umbrellaPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(String.format("%02d", umbrellaCount),
				umbrellaCountRectF.left, umbrellaCountRectF.bottom,
				umbrellaPaint);
		
		// Display passport info
		Drawable passportDrawable = context.getResources().getDrawable(R.drawable.passport);
		Bitmap passportBitmap = ((BitmapDrawable)passportDrawable).getBitmap();
		RectF passportRectF = new RectF(
				passportCountRectF.left + passportCountRectF.width() *(float)0.7, 
				passportCountRectF.top + passportCountRectF.height()/2, 
				passportCountRectF.right, passportCountRectF.bottom);
		canvas.drawBitmap(passportBitmap, null, passportRectF, null);
		
		Paint passportPaint = new Paint();
		passportPaint.setAntiAlias(true);
		passportPaint.setColor(Color.WHITE);
		passportPaint.setTextSize(textSize);
		passportPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(String.format("%02d", passportCount),
				passportCountRectF.left, passportCountRectF.bottom,
				passportPaint);
		
		// Display available brick info
		Drawable brickDrawable = context.getResources().getDrawable(R.drawable.tile_yellow);
		Bitmap brickBitmap = ((BitmapDrawable)brickDrawable).getBitmap();
		RectF brickRectF = new RectF(
				availableBrickCountRectF.left, availableBrickCountRectF.top,
				availableBrickCountRectF.left + availableBrickCountRectF.width() / 2, availableBrickCountRectF.bottom);
		canvas.drawBitmap(brickBitmap, null, brickRectF, null);
		
		Paint brickPaint = new Paint();
		brickPaint.setAntiAlias(true);
		brickPaint.setColor(Color.WHITE);
		brickPaint.setTextSize(textSize);
		brickPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(String.format("%02d", availableBrickCount),
				availableBrickCountRectF.left + availableBrickCountRectF.width()/2, 
				availableBrickCountRectF.bottom,
				brickPaint);
		
		// Display available springboard info
		Drawable springboardDrawable = context.getResources().getDrawable(R.drawable.springboard_closed);
		Bitmap springboardBitmap = ((BitmapDrawable)springboardDrawable).getBitmap();
		RectF springboardRectF = new RectF(
				availableSpringboardCountRectF.left, availableSpringboardCountRectF.top,
				availableSpringboardCountRectF.left + availableSpringboardCountRectF.width() / 2, availableSpringboardCountRectF.bottom);
		canvas.drawBitmap(springboardBitmap, null, springboardRectF, null);
		
		Paint springboardPaint = new Paint();
		springboardPaint.setAntiAlias(true);
		springboardPaint.setColor(Color.WHITE);
		springboardPaint.setTextSize(textSize);
		springboardPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(String.format("%02d", availableSpringboardCount),
				availableSpringboardCountRectF.left + availableSpringboardCountRectF.width() / 2 , 
				availableSpringboardCountRectF.bottom,
				springboardPaint);
	}

	int lastX;
	int lastY;
	boolean isJumping;
	private void updateHero(Canvas canvas){
		int x = (int) ((heroPositionX ) / BRICK_WIDTH);
		int y = (int) ((BITMAP_HEIGHT - heroPositionY - BRICK_HEIGHT_OFFSET) / BRICK_HEIGHT );
		
		if(brickArray[x+1][y] == Brick.DUCKY){
			duckyCount ++;
			brickArray[x+1][y] = Brick.NONE;
		}else if(brickArray[x+1][y] == Brick.PASSPORT){
			passportCount ++;
			brickArray[x+1][y] = Brick.NONE;
		}else if(brickArray[x+1][y] == Brick.UMBRELLA){
			umbrellaCount ++;
			brickArray[x+1][y] = Brick.NONE;
		} 
		
		if(isJumping)
			if(heroSpeedY > 4){
				heroPositionY += heroSpeedY;
				heroSpeedY -= 4;
				return;
			}
			else if(heroPositionY % BRICK_HEIGHT == 0)
				isJumping = false;
			else{
				int left = (int) (BRICK_HEIGHT -  (heroPositionY - Math.floor(heroPositionY/BRICK_HEIGHT)*BRICK_HEIGHT));
				
				heroPositionY += left;
				return;
			}
		
		
		if(offsetMod == 0 || offsetMod == BRICK_WIDTH/2)
			if(heroPositionY >= BITMAP_HEIGHT * PILLAR_HEIGHT_MIN)
				availableBrickCount ++;
		
		if(offsetMod == 0){
			lastX = lastY = 0;
		}
		
		if(y < brickArray[0].length -1 && brickArray[x][y+1] == Brick.SPRINGBOARD_CLOSED){
			heroPositionY += BRICK_HEIGHT;
			heroSpeedY = 40;
			isJumping = true;
			brickArray[x][y] = Brick.SPRINGBOARD_OPENED_X1;
			brickArray[x][y+1] = Brick.SPRINGBOARD_OPENED_X2;
		}
		
		// Down
		if( ( y < ( Math.floor((BITMAP_HEIGHT - BRICK_HEIGHT_OFFSET)/ BRICK_HEIGHT - 1 )) ) &&
				((brickArray[x][y+1] != Brick.YELLOW) &&
				( brickArray[x][y+1] != Brick.BROWN))){
			if( (x != lastX) || (y + 1 != lastY)){
				heroPositionY -= BRICK_HEIGHT;
				lastX = x;
				lastY = y;
			}
			
			heroSpeedX = 4;
			
			if(brickArray[x][y+1] == Brick.DUCKY){
				duckyCount ++;
				brickArray[x][y+1] = Brick.NONE;
			}else if(brickArray[x][y+1] == Brick.PASSPORT){
				passportCount ++;
				brickArray[x][y+1] = Brick.NONE;
			}else if(brickArray[x][y+1] == Brick.UMBRELLA){
				umbrellaCount ++;
				brickArray[x][y+1] = Brick.NONE;
			} 
			
		}
		// Up
		else if ( (y > 1) && (brickArray[x+1][y] != Brick.NONE) && 
				( ( brickArray[x+1][y-1] != Brick.YELLOW) &&
				( brickArray[x+1][y-1] != Brick.BROWN) )){
			
			heroPositionY += BRICK_HEIGHT;
			lastX = x;
			lastY = y;
			heroSpeedX = 4;
			
			if(brickArray[x+1][y-1] == Brick.DUCKY){
				duckyCount ++;
				brickArray[x+1][y-1]  = Brick.NONE;
			}else if(brickArray[x+1][y-1]  == Brick.PASSPORT){
				passportCount ++;
				brickArray[x+1][y-1]  = Brick.NONE;
			}else if(brickArray[x+1][y-1]  == Brick.UMBRELLA){
				umbrellaCount ++;
				brickArray[x+1][y-1]  = Brick.NONE;
			} 
		}
		else if ( (y == brickArray[0].length -1) && !usingDuck && ( groundArray[x+1] == Ground.POOL_DRAWN ))
			onGameOver();
		else if ( (y == brickArray[0].length -1 )&& !usingPassport && ( groundArray[x+1] == Ground.CONE_DRAWN))
			onGameOver();
		else if( !usingUmbrella && (inCloud(x, y-3) || inCloud(x, y-1) || inCloud(x, y-2)))
			onGameOver();
		
		Paint rectPaint = new Paint();
		rectPaint.setColor(Color.WHITE);
		canvas.drawRect(
				(float)(x*BRICK_WIDTH - offsetMod), 
				(float)(y*BRICK_HEIGHT+BRICK_HEIGHT_OFFSET), 
				(float)((x+1)*BRICK_WIDTH - offsetMod), 
				(float)((y+1)*BRICK_HEIGHT+BRICK_HEIGHT_OFFSET), 
				rectPaint);
	}
	
	private boolean inCloud(int x, int y){
		if(x < 0 || x >= brickArray.length || y < 0 || y >= brickArray[0].length)
			return false;
			
		if( (brickArray[x][y] == Brick.DARK_CLOUD_X0Y0) ||
			(brickArray[x][y] == Brick.DARK_CLOUD_X1Y0) ||
			(brickArray[x][y] == Brick.DARK_CLOUD_X2Y0) ||
			(brickArray[x][y] == Brick.DARK_CLOUD_X0Y1) ||
			(brickArray[x][y] == Brick.DARK_CLOUD_X1Y1) ||
			(brickArray[x][y] == Brick.DARK_CLOUD_X2Y1))
			return true;
		else
			return false;
	}
	
	private int prevX = 0;
	private int prevY = 0;
	
	RectF restartDestRect;
	RectF cancelDestRect;
	
	private void onGameOver(){
		heroSpeedX  = 0;
		threadState = ThreadState.STOPPING;
		
		int gameOverScreenShift = BITMAP_HEIGHT;
		
		Paint distancePaint = new Paint();
		distancePaint.setAntiAlias(true);
		distancePaint.setColor(Color.WHITE);
		distancePaint.setTextSize(BITMAP_WIDTH / 8);
		
		Paint rankPaint = new Paint();
		rankPaint.setAntiAlias(true);
		rankPaint.setColor(Color.WHITE);
		rankPaint.setTextSize(BITMAP_WIDTH / 16);
		
		Paint toolPaint = new Paint();
		toolPaint.setAntiAlias(true);
		toolPaint.setColor(Color.WHITE);
		toolPaint.setTextSize(BITMAP_WIDTH / 16);
		
		Drawable duckyDrawable = context.getResources().getDrawable(R.drawable.ducky);
		Drawable passportDrawable = context.getResources().getDrawable(R.drawable.passport);
		Drawable umbrellaDrawable = context.getResources().getDrawable(R.drawable.umbrella);
		Drawable brownDrawable = context.getResources().getDrawable(R.drawable.tile_brown);
		Drawable yellowDrawable = context.getResources().getDrawable(R.drawable.tile_yellow);
		Drawable cancelDrawable = context.getResources().getDrawable(R.drawable.cancel);
		Drawable restartDrawable = context.getResources().getDrawable(R.drawable.restart);
		
		Bitmap duckyBitmap = ((BitmapDrawable)duckyDrawable).getBitmap();
		Bitmap passportBitmap = ((BitmapDrawable)passportDrawable).getBitmap();
		Bitmap umbrellaBitmap = ((BitmapDrawable)umbrellaDrawable).getBitmap();
		Bitmap brownBitmap = ((BitmapDrawable)brownDrawable).getBitmap();
		Bitmap yellowBitmap = ((BitmapDrawable)yellowDrawable).getBitmap();
		Bitmap restartBitmap = ((BitmapDrawable)restartDrawable).getBitmap();
		Bitmap cancelBitmap = ((BitmapDrawable)cancelDrawable).getBitmap();
		
		RectF duckyDestRect = new RectF(
			(float)(BITMAP_WIDTH / 6),
			(float)(BITMAP_HEIGHT / 2),
			(float)(BITMAP_WIDTH / 6 + 1.5*BRICK_WIDTH),
			(float)(BITMAP_HEIGHT / 2  + 1.5*BRICK_HEIGHT)
			);
		
		RectF passportDestRect = new RectF(
			(float)(BITMAP_WIDTH / 6),
			(float)(BITMAP_HEIGHT / 2 + 2 * BRICK_HEIGHT),
			(float)(BITMAP_WIDTH / 6 + 1.5*BRICK_WIDTH),
			(float)(BITMAP_HEIGHT / 2  + 3.5*BRICK_HEIGHT)
			);
		
		RectF umbrellaDestRect = new RectF(
			(float)(BITMAP_WIDTH / 6),
			(float)(BITMAP_HEIGHT / 2 + 4 * BRICK_HEIGHT),
			(float)(BITMAP_WIDTH / 6 + 1.5*BRICK_WIDTH),
			(float)(BITMAP_HEIGHT / 2  + 5.5*BRICK_HEIGHT)
			);
		

		lifeDatabase.addScore(distance, duckyCount, passportCount, umbrellaCount);
		int rank =  lifeDatabase.getRank(distance) + 1;
		
		for(int i = 0; i < gameOverScreenShift && threadState == ThreadState.STOPPING; i+= GAME_OVER_SCREEN_SHIFT_SPPED){
			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(BACK_GROUND_COLOR);
			Rect srcRect = new Rect(0, 0, BITMAP_WIDTH , BITMAP_HEIGHT - i);
			RectF destRect = new RectF(0, i, BITMAP_WIDTH, BITMAP_HEIGHT);
			canvas.drawBitmap(stageWithBricks, srcRect, destRect, null);
			
			canvas.drawText(
				String.format("%05d", distance/4), 
				BITMAP_WIDTH/6, 
				BITMAP_HEIGHT/6 + BITMAP_WIDTH/12, 
				distancePaint);
			
			canvas.drawText(String.format("Rank: %2d", rank), BITMAP_WIDTH/6, BITMAP_HEIGHT/6 + BITMAP_WIDTH/6, rankPaint);
			canvas.drawBitmap(duckyBitmap, null, duckyDestRect, null);
			canvas.drawBitmap(passportBitmap, null, passportDestRect, null);
			canvas.drawBitmap(umbrellaBitmap, null, umbrellaDestRect, null);
			
			canvas.drawText(
				String.format("%02d", duckyCount), 
				(float)(BITMAP_WIDTH / 6 + 1.5*BRICK_WIDTH + 0.5*BRICK_WIDTH),
				(float)(BITMAP_HEIGHT / 2 + BITMAP_WIDTH / 16), 
				toolPaint
			);
			
			canvas.drawText(
				String.format("%02d", passportCount),
				(float)(BITMAP_WIDTH / 6 + 1.5*BRICK_WIDTH + 0.5*BRICK_WIDTH),
				(float)(BITMAP_HEIGHT / 2 + 2 * BRICK_HEIGHT + BITMAP_WIDTH / 16),
				toolPaint
			);
			
			canvas.drawText(
				String.format("%02d", umbrellaCount),
				(float)(BITMAP_WIDTH / 6 + 1.5*BRICK_WIDTH + 0.5*BRICK_WIDTH),
				(float)(BITMAP_HEIGHT / 2 + 4 * BRICK_HEIGHT + BITMAP_WIDTH / 16),
				toolPaint
			);
			
			canvas.drawBitmap(yellowBitmap, null, restartDestRect, null);
			canvas.drawBitmap(restartBitmap, null, restartDestRect, null);
			canvas.drawBitmap(brownBitmap, null, cancelDestRect, null);
			canvas.drawBitmap(cancelBitmap, null, cancelDestRect, null);
			
			holder.unlockCanvasAndPost(canvas);
		}
		
		if(threadState == ThreadState.STOPPING)
			threadState = ThreadState.STOPPED;
	}
	
	int lastDarkCloudOffset;
	private int generateDarkCloud(int start, Brick[][] array, int offset) {
		int result = start;
		lastDarkCloudOffset += offset;
		
		if(offsetMod != 0)
			return result;
		
		double DARK_CLOUD_THRESHOLD = 0.96;
		if(rand.nextDouble() < DARK_CLOUD_THRESHOLD)
			return result;
		
		double DARK_CLOUD_HEIGHT_MIN = 0.7;
		double DARK_CLOUD_HEIGHT_MAX = 1.0;
		start = Math.max(start - lastDarkCloudOffset, BITMAP_WIDTH);
		start = (int) (Math.floor(start) / BRICK_WIDTH);
		if(start < array.length - 3){
			lastDarkCloudOffset = 0;
			
			int darkCloudkHeight = BITMAP_HEIGHT - (int) (DARK_CLOUD_HEIGHT_MIN * BITMAP_HEIGHT +
					rand.nextInt((int)(DARK_CLOUD_HEIGHT_MAX * BITMAP_HEIGHT - 
							DARK_CLOUD_HEIGHT_MIN* BITMAP_HEIGHT))) ;
			
			int darkCloudBlockHeight = (int) (darkCloudkHeight / BRICK_HEIGHT);
			array[start + 0][darkCloudBlockHeight + 0] = Brick.DARK_CLOUD_X0Y0;
			array[start + 1][darkCloudBlockHeight + 0] = Brick.DARK_CLOUD_X1Y0;
			array[start + 2][darkCloudBlockHeight + 0] = Brick.DARK_CLOUD_X2Y0;
			array[start + 0][darkCloudBlockHeight + 1] = Brick.DARK_CLOUD_X0Y1;
			array[start + 1][darkCloudBlockHeight + 1] = Brick.DARK_CLOUD_X1Y1;
			array[start + 2][darkCloudBlockHeight + 1] = Brick.DARK_CLOUD_X2Y1;
			
			result = start*BRICK_WIDTH + (3 + 1)*BRICK_WIDTH;
			
		}else
			return result;
		
		return result;
	}

	private int drawCloudAndShade(Canvas canvas, int start, int end, int offset) {
		lastCloudOffset += offset;
		int result = start;
		/*
		 * Draw clouds
		 */
		Drawable cloud1Tile = context.getResources().getDrawable(R.drawable.cloud1);
		Drawable cloud1ShadeTile = context.getResources().getDrawable(R.drawable.cloud1_shade);

		Bitmap cloud1Bitmap = ((BitmapDrawable)cloud1Tile).getBitmap();
		Bitmap cloud1ShadeBitmap = ((BitmapDrawable)cloud1ShadeTile).getBitmap();
		
		start = (int) (start - lastCloudOffset 
				+ CLOUD_WIDTH_MAX * CLOUD_SPACE_RATIO
				+ CLOUD_WIDTH_MIN * CLOUD_SPACE_RATIO * rand.nextDouble());
		
		for(int i = start; i< end; i += CLOUD_WIDTH_MAX * CLOUD_SPACE_RATIO){
			if(i + CLOUD_WIDTH_MIN*1.5 <= end){
				lastCloudOffset = 0;
				int cloudWidth = CLOUD_WIDTH_MIN + rand.nextInt(CLOUD_WIDTH_MAX - CLOUD_WIDTH_MIN);
				int cloudHeight = (int) (BITMAP_HEIGHT * 
						(1-(CLOUD_HEIGHT_MIN + rand.nextDouble()*(CLOUD_HEIGHT_MAX - CLOUD_HEIGHT_MIN))));
				
				int x1 = i;
				int y1 = cloudHeight;
				int x2 = x1 + cloudWidth;
				int y2 = y1 + cloudWidth;
				
				RectF cloudRectF = new RectF(x1, y1, x2, y2);
				RectF shadeRectF = new RectF(
						(float)(x1 - cloudWidth * CLOUD_SHADE_WIDTH_OFFSET_RATIO), 
						(float)(y1 + cloudWidth * CLOUD_SHADE_HEIGHT_OFFSET_RATIO),
						(float)(x2 - cloudWidth * CLOUD_SHADE_WIDTH_OFFSET_RATIO), 
						(float)(y2 + cloudWidth * CLOUD_SHADE_HEIGHT_OFFSET_RATIO)
						);
				
				Paint shadePaint = new Paint();
				shadePaint.setAlpha(CLOUD_SHADE_ALPHA);
				canvas.drawBitmap(cloud1ShadeBitmap, null, shadeRectF, shadePaint);
				canvas.drawBitmap(cloud1Bitmap, null, cloudRectF, null);
				
				result = (int) (x2 - cloudWidth * CLOUD_SHADE_WIDTH_OFFSET_RATIO);
				i += CLOUD_WIDTH_MIN * CLOUD_SPACE_RATIO * rand.nextDouble();
			}
		}
		
		return result;
	}

	private int drawPillar(Canvas canvas, int start, int end, int offset) {
		Log.d("Life", "[drawPillar] start=" + start + ", end=" + end +", offset=" + offset);
		lastPillarOffset += offset;
		lastPillarOffsetTmp += offset;
		int result = start;
		/*
		 * Draw pillars
		 */
		Paint pillarPaint = new Paint();
		pillarPaint.setAntiAlias(true);
		
		start = (int) (start + PILLAR_SPACE - lastPillarOffset);
		end = end - lastBackPillarOffset;
		for(int i = start; i < end; i += PILLAR_WIDTH){
			int pillarHeight = (int) ((BITMAP_HEIGHT* 0.5 * (PILLAR_HEIGHT_MIN + PILLAR_HEIGHT_MAX)) + 
					Math.floor(rand.nextGaussian()*10) / 10.0 * 0.3 *
					(BITMAP_HEIGHT * PILLAR_HEIGHT_MAX - BITMAP_HEIGHT * PILLAR_HEIGHT_MIN));
			pillarPaint.setColor(pillarColor[rand.nextInt(pillarColor.length)]);
			
			if( i + PILLAR_WIDTH <= end){
				pillarDrawn = true;
				lastPillarOffset = 0;
				canvas.drawCircle(
						i + PILLAR_WIDTH / 2, 
						BITMAP_HEIGHT - pillarHeight, 
						PILLAR_WIDTH / 2, 
						pillarPaint);
				canvas.drawRect(
						i, 
						BITMAP_HEIGHT - pillarHeight, 
						i+PILLAR_WIDTH, 
						BITMAP_HEIGHT, 
						pillarPaint);
				
				result = i+PILLAR_WIDTH;
			}
			
			i += PILLAR_SPACE;
		}
		
		return result;
	}

	/**
	 * 
	 * @param canvas
	 * @param start
	 * @param end
	 * @param offset Offset from last drawing position
	 * @return Ending position
	 */

	private int drawBackPillarAndShade(Canvas canvas, int start, int end, int offset) {
		lastBackPillarOffset += offset;
		
		int result = start;
		boolean drawn = false;
		
		/*
		 * Draw back pillars' shade
		 */
		Paint backPillarShadePaint = new Paint();
		backPillarShadePaint.setAntiAlias(true);
		backPillarShadePaint.setColor(Color.BLACK);
		backPillarShadePaint.setAlpha(BACK_PILLAR_SHADE_ALPHA);
		
		if(previousPillarPosition != null)
			start = (int) (previousPillarPosition.getX() + BACK_PILLAR_WIDTH - lastBackPillarOffset 
					+ BACK_PILLAR_SPACE);
		else
			start = (int) (start - lastBackPillarOffset 
				    + BACK_PILLAR_SPACE);
		
		List<PillarPosition> backPillarPositionList = new ArrayList<PillarPosition>();
		for(int i = start ; i < end; i += BACK_PILLAR_WIDTH){
			int backPillarHeight = (int)(BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MIN) + 
					rand.nextInt(
					(int)(BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MAX 
							- BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MIN));
			
			if( i + BACK_PILLAR_WIDTH <= end){
				backPillarPositionList.add(new PillarPosition(i, backPillarHeight));
				drawn = true;
				
				double x1 = i - BACK_PILLAR_SHADE_WIDTH_OFFSET;
				double y1 = BITMAP_HEIGHT - (backPillarHeight - BACK_PILLAR_SHADE_HEIGHT_OFFSET + BACK_PILLAR_WIDTH / 2);
				RectF rectF = new RectF(
						(float) (x1),
						(float) (y1), 
						(float)(x1 + BACK_PILLAR_WIDTH),
						(float)BITMAP_HEIGHT
						);
			
				canvas.drawRoundRect(rectF, BACK_PILLAR_WIDTH / 2, BACK_PILLAR_WIDTH / 2, backPillarShadePaint);
			}
			
			i += BACK_PILLAR_SPACE;
		}
		
		if(drawn == false){
			return result;
		}
		/*
		 * Draw back pillars
		 */
		Paint backPillarPaint = new Paint();
		backPillarPaint.setAlpha(255);
		backPillarPaint.setAntiAlias(true);
		
		if(previousPillarPosition != null){
			backPillarPaint.setColor(backPillarColor[rand.nextInt(backPillarColor.length)]);
			canvas.drawCircle(
					(float)(previousPillarPosition.getX()+BACK_PILLAR_WIDTH/2 - lastBackPillarOffset), 
					(float)(BITMAP_HEIGHT - previousPillarPosition.getY()), 
					BACK_PILLAR_WIDTH / 2, 
					backPillarPaint);
			canvas.drawRect(
					(float)(previousPillarPosition.getX() - lastBackPillarOffset ), 
					(float)(BITMAP_HEIGHT - previousPillarPosition.getY()), 
					(float) (previousPillarPosition.getX()+BACK_PILLAR_WIDTH - lastBackPillarOffset ), 
					BITMAP_HEIGHT, 
					backPillarPaint);
		}
		
		for(int i = 0; i < backPillarPositionList.size() - 1; i ++){
			PillarPosition pillarPosition = backPillarPositionList.get(i);
			
			backPillarPaint.setColor(backPillarColor[rand.nextInt(backPillarColor.length)]);
			
			canvas.drawCircle(
					(float)(pillarPosition.getX()+BACK_PILLAR_WIDTH/2), 
					(float)(BITMAP_HEIGHT - pillarPosition.getY()), 
					BACK_PILLAR_WIDTH/2, 
					backPillarPaint);
			canvas.drawRect(
					(float)(pillarPosition.getX()), 
					(float)(BITMAP_HEIGHT - pillarPosition.getY()), 
					(float) (pillarPosition.getX() + BACK_PILLAR_WIDTH), 
					BITMAP_HEIGHT, 
					backPillarPaint);
			
			result = (int) (pillarPosition.getX() + BACK_PILLAR_WIDTH);
		}
		previousPillarPosition = backPillarPositionList.get(backPillarPositionList.size() -1);
		lastBackPillarOffset = 0;
		return result;
	}
	
	private void initializeVariables(Context context){
		heroPositionX = Integer.valueOf(context.getResources().
				getString(R.string.hero_position_x));
		
		heroPositionY = Integer.valueOf(context.getResources().
				getString(R.string.hero_position_y));
		
		BROWN_BRICK_THRESHOLD = Double.valueOf(
			context.getResources().
					getString(R.string.brown_brick_threshold));
		
		PILLAR_WIDTH = Integer.valueOf(
			context.getResources().
					getString(R.string.pillar_width));
		
		PILLAR_HEIGHT_MAX = Double.valueOf(
			context.getResources().
					getString(R.string.pillar_height_max_ratio));
		
		PILLAR_HEIGHT_MIN = Double.valueOf(
			context.getResources().
					getString(R.string.pillar_height_min_ratio));
		
		PILLAR_SPACE = Integer.valueOf(
			context.getResources().
					getString(R.string.pillar_space));
		
		BACK_PILLAR_WIDTH = Integer.valueOf(
			context.getResources().
					getString(R.string.back_pillar_width));
		
		BACK_PILLAR_SPACE = Integer.valueOf(
			context.getResources().
					getString(R.string.back_pillar_space));
		
		BACK_PILLAR_HEIGHT_MAX = Double.valueOf(
			context.getResources().
					getString(R.string.back_pillar_height_max));
		BACK_PILLAR_HEIGHT_MIN = Double.valueOf(
			context.getResources().
					getString(R.string.back_pillar_height_min));
		
		BACK_PILLAR_SHADE_WIDTH_OFFSET = 
			BACK_PILLAR_WIDTH * Double.valueOf(context.getResources().
			getString(R.string.back_pillar_shade_width_ratio));
		
		// yes, use BACK_PILLAR_WIDTH, not BACK_PILLAR_HEIGHT
		// although we're calculating height offset
		BACK_PILLAR_SHADE_HEIGHT_OFFSET = 
			BACK_PILLAR_WIDTH * Double.valueOf(context.getResources().
			getString(R.string.back_pillar_shade_height_ratio));
		
		HERO_HEIGHT = 
			Integer.valueOf(context.getResources().
					getString(R.string.hero_height));
		HERO_WIDTH = 
			Integer.valueOf(context.getResources().
					getString(R.string.hero_width));
		
		HERO_SHADE_HEIGHT_OFFSET = 
			HERO_HEIGHT * Double.valueOf(context.getResources().
			getString(R.string.hero_shade_height_offset_ratio));
		
		HERO_SHADE_WIDTH_OFFSET = 
			HERO_WIDTH * Double.valueOf(context.getResources().
			getString(R.string.hero_shade_width_offset_ratio));
		
		CLOUD_HEIGHT_MAX = Double.valueOf(
			context.getResources().
					getString(R.string.cloud_height_max));
		CLOUD_HEIGHT_MIN = Double.valueOf(
			context.getResources().
					getString(R.string.cloud_height_min));
		CLOUD_WIDTH_MAX = Integer.valueOf(
			context.getResources().
					getString(R.string.cloud_width_max));
		CLOUD_WIDTH_MIN = Integer.valueOf(
			context.getResources().
					getString(R.string.cloud_width_min));
		CLOUD_SPACE_RATIO = Double.valueOf(
			context.getResources().
					getString(R.string.cloud_space_ratio));
		
		CLOUD_SHADE_HEIGHT_OFFSET_RATIO = Double.valueOf(
			context.getResources().
			getString(R.string.cloud_shade_height_offset_ratio));
		
		CLOUD_SHADE_WIDTH_OFFSET_RATIO = Double.valueOf(
			context.getResources().
			getString(R.string.cloud_shade_width_offset_ratio));
		
		BUSH_SHADE_HEIGHT_OFFSET_RATIO = Double.valueOf(
			context.getResources().
			getString(R.string.bush_shade_height_offset_ratio));
		
		BUSH_SHADE_WIDTH_OFFSET_RATIO = Double.valueOf(
			context.getResources().
			getString(R.string.bush_shade_width_offset_ratio));
		
		RANDOM_BRICK_HEIGHT_MAX = Double.valueOf(
			context.getResources().
					getString(R.string.random_brick_height_max));
		RANDOM_BRICK_HEIGHT_MIN = Double.valueOf(
			context.getResources().
					getString(R.string.random_brick_height_min));
		RANDOM_BRICK_COUNT_MAX = Integer.valueOf(
			context.getResources().
					getString(R.string.random_brick_count_max));
		RANDOM_BRICK_COUNT_MIN = Integer.valueOf(
			context.getResources().
					getString(R.string.random_brick_count_min));
		
		RANDOM_BRICK_THRESHOLD = Double.valueOf(
			context.getResources().
					getString(R.string.random_brick_threshold));
		
		heroPositionX *= BRICK_WIDTH;
		heroPositionY *= BRICK_HEIGHT;
		
		GROUND_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.ground_height_ratio));
		
		GROUND_SECTION_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.ground_section_ratio));
		
		SUBTERRANEAN_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.subterranean_ratio));
		
		GRASS_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.grass_ratio));
		
		GRASS_SPACE = Double.valueOf(
				context.getResources().
				getString(R.string.grass_space));
		
		TREE_CROWN_WIDTH = BITMAP_WIDTH * Double.valueOf(
				context.getResources().
				getString(R.string.tree_crown_width_ratio));
		
		TREE_CROWN_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.tree_crown_height_ratio));
		
		TRUNK_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.trunk_height_ratio));
		
		TRUNK_WIDTH= BITMAP_WIDTH * Double.valueOf(
				context.getResources().
				getString(R.string.trunk_width_ratio));
		
		TREE_SPACE_COUNT = Double.valueOf(
				context.getResources().
				getString(R.string.tree_space_count));
		
		TREE_SHADE_ANGLE = Double.valueOf(
				context.getResources().
				getString(R.string.tree_shade_angle));

		TREE_CROWN_SHADE_HEIGHT_OFFSET_RATIO = Double.valueOf(
				context.getResources().
				getString(R.string.tree_crown_shade_height_offset_ratio));
		
		TREE_THRESHOLD = Double.valueOf(
				context.getResources().
				getString(R.string.tree_threshold));
		
		BUSH_WIDTH = BITMAP_WIDTH * Double.valueOf(
				context.getResources().
				getString(R.string.bush_width_ratio));
		
		BUSH_HEIGHT_MAX = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.bush_height_max_ratio));
		
		BUSH_HEIGHT_MIN = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.bush_height_min_ratio));
		
		BUSH_HEIGHT_OFFSET_FROM_WALL = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.bush_height_offset_from_wall_ratio));
		
		BUSH_HEIGHT_OFFSET_FROM_BUSH = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.bush_height_offset_from_bush_ratio));
		
		BUSH_SPACE_COUNT = Double.valueOf(
				context.getResources().
				getString(R.string.bush_space_count));
		
		BUSH_THRESHOLD = Double.valueOf(
				context.getResources().
				getString(R.string.bush_threshold));
		
		BRICK_SHADE_HEIGHT_OFFSET = BRICK_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.brick_shade_height_offset_ratio));
		
		BRICK_SHADE_WIDTH_OFFSET = BRICK_WIDTH * Double.valueOf(
				context.getResources().
				getString(R.string.brick_shade_width_offset_ratio));
				
		POOL_WIDTH_MAX = BITMAP_WIDTH * Double.valueOf(
				context.getResources().
				getString(R.string.pool_width_max_ratio));
		
		POOL_WIDTH_MIN = BITMAP_WIDTH * Double.valueOf(
				context.getResources().
				getString(R.string.pool_width_min_ratio));
		
		POOL_THRESHOLD = Double.valueOf(
				context.getResources().
				getString(R.string.pool_threshold));
		
		POOL_SPACE_COUNT = Integer.valueOf(
				context.getResources().
				getString(R.string.pool_space_count));
		
		CONE_SPACE_COUNT = Integer.valueOf(
				context.getResources().
				getString(R.string.cone_space_count));
		
		GAME_OVER_SCREEN_SHIFT_SPPED = Integer.valueOf(
				context.getResources().
				getString(R.string.game_over_screen_shift_speed));
		
		double stageWidth =
				BITMAP_WIDTH +
				RANDOM_BRICK_COUNT_MAX * BRICK_WIDTH +
				BACK_PILLAR_WIDTH * 2 + 
				PILLAR_WIDTH + BUSH_WIDTH * 3 + 
				TREE_CROWN_WIDTH + 
				GROUND_HEIGHT / Math.tan(Math.toRadians(TREE_SHADE_ANGLE));
		
		stage = Bitmap.createBitmap(
				(int) stageWidth, 
				BITMAP_HEIGHT, 
				Bitmap.Config.ARGB_8888);
		
		Paint backGroundPaint = new Paint();
		Canvas canvas = new Canvas(stage);
		backGroundPaint.setColor(BACK_GROUND_COLOR);
		canvas.drawRect(0, 0, stage.getWidth(), stage.getHeight(), backGroundPaint);
		
		if(stageWithBricks == null)
			stageWithBricks = Bitmap.createBitmap(
				stage.getWidth(),
				stage.getHeight(),
				Bitmap.Config.ARGB_8888);
		
		
		double availableHeight = BITMAP_HEIGHT - (SUBTERRANEAN_HEIGHT + GROUND_SECTION_HEIGHT);
		double bricksHeight = Math.floor(availableHeight / BRICK_HEIGHT)*BRICK_HEIGHT;
		
		BRICK_HEIGHT_OFFSET = availableHeight - bricksHeight;
		
		BLOCK_NUMBER_X = (int) ( stage.getWidth() / BRICK_WIDTH);
		BLOCK_NUMBER_Y = (int) ( ( stage.getHeight() - BRICK_HEIGHT_OFFSET ) / BRICK_HEIGHT );
		
		restartDestRect = new RectF(
				(float)(BITMAP_WIDTH / 3 *2 + BITMAP_WIDTH / 3 / 3),
				(float)(BITMAP_HEIGHT / 3 + (BITMAP_HEIGHT/3 - 2*BRICK_HEIGHT) / 2),
				(float)(BITMAP_WIDTH / 3 *2 + BITMAP_WIDTH / 3 / 3 + 2 * BRICK_WIDTH),
				(float)(BITMAP_HEIGHT / 3 + (BITMAP_HEIGHT/3 - 2*BRICK_HEIGHT) / 2 + 2 * BRICK_WIDTH)
				);
		
		cancelDestRect = new RectF(
					(float)(BITMAP_WIDTH / 3 *2 + BITMAP_WIDTH / 3 / 3),
					(float)(BITMAP_HEIGHT / 3 * 2 + (BITMAP_HEIGHT/3 - 2*BRICK_HEIGHT) / 2),
					(float)(BITMAP_WIDTH / 3 * 2 + BITMAP_WIDTH / 3 / 3 + 2 * BRICK_WIDTH),
					(float)(BITMAP_HEIGHT / 3 * 2+ (BITMAP_HEIGHT/3 - 2*BRICK_HEIGHT) / 2 + 2 * BRICK_WIDTH)
					);
			
		duckyCountRectF = new RectF(
				(float)(BITMAP_HEIGHT/10 - BITMAP_WIDTH/20), 
				(float)(BITMAP_HEIGHT / 10 + BITMAP_HEIGHT / 40),
				(float)(BITMAP_HEIGHT/10 - BITMAP_WIDTH/20 + 1.6 * BRICK_WIDTH),
				(float)(BITMAP_HEIGHT / 10  + BITMAP_HEIGHT / 40 + 0.8 * BRICK_HEIGHT));
		
		umbrellaCountRectF = new RectF(
				(float)(BITMAP_HEIGHT/10 - BITMAP_WIDTH/20 + 2 * BRICK_WIDTH), 
				(float)(BITMAP_HEIGHT / 10 + BITMAP_HEIGHT / 40),
				(float)(BITMAP_HEIGHT/10 - BITMAP_WIDTH/20 + 1.6 * BRICK_WIDTH + 2 * BRICK_WIDTH),
				(float)(BITMAP_HEIGHT / 10  + BITMAP_HEIGHT / 40 + 0.8 * BRICK_HEIGHT));
				
		passportCountRectF = new RectF(
				(float)(BITMAP_HEIGHT/10 - BITMAP_WIDTH/20 + 4 * BRICK_WIDTH), 
				(float)(BITMAP_HEIGHT / 10 + BITMAP_HEIGHT / 40),
				(float)(BITMAP_HEIGHT/10 - BITMAP_WIDTH/20 + 1.6 * BRICK_WIDTH + 4 * BRICK_WIDTH),
				(float)(BITMAP_HEIGHT / 10  + BITMAP_HEIGHT / 40 + 0.8 * BRICK_HEIGHT));
		
		availableBrickCountRectF = new RectF(
				(float)(BITMAP_WIDTH * 6 / 8 ), 
				(float)(BITMAP_HEIGHT / 30),
				(float)(BITMAP_WIDTH * 6 / 8 + 1.6 * BRICK_WIDTH ),
				(float)(BITMAP_HEIGHT / 30 + 0.8 * BRICK_HEIGHT));
				
		availableSpringboardCountRectF = new RectF(
				(float)(BITMAP_WIDTH * 6 / 8), 
				(float)(BITMAP_HEIGHT / 30 + 0.8 * BRICK_HEIGHT + BITMAP_HEIGHT / 30),
				(float)(BITMAP_WIDTH * 6 / 8 + 1.6 * BRICK_WIDTH ),
				(float)(BITMAP_HEIGHT / 30 + 0.8 * BRICK_HEIGHT + BITMAP_HEIGHT / 30 + 0.8 * BRICK_HEIGHT));
		availableBrickCount = BITMAP_WIDTH / BRICK_WIDTH;
		availableSpringboardCount = 5;
		threadState = ThreadState.STOPPED;
		
		retrieveLifeData();
	}

	private void drawBush(Canvas canvas, int i){
		i = (int) (i + BRICK_WIDTH - 2 * BUSH_WIDTH);
		
		Paint bushShadePaint = new Paint();
		bushShadePaint.setAlpha(BUSH_SHADE_ALPHA);
		Drawable bushDrawable = context.getResources().getDrawable(R.drawable.bush);
		Bitmap bushBitmap = ((BitmapDrawable)bushDrawable).getBitmap();
		
		/*
		 * Draw left bush's shade
		 */
		
		double lBushHeight = BUSH_HEIGHT_MIN + (BUSH_HEIGHT_MAX - BUSH_HEIGHT_MIN) * rand.nextDouble();
		double lBushX1 = i;
		double lBushY1 = (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT)
				- lBushHeight + BUSH_HEIGHT_OFFSET_FROM_WALL;
		double lBushX2 = lBushX1 + BUSH_WIDTH;
		double lBushY2 = lBushY1 + lBushHeight;
		
		RectF lBushShadeRectF = new RectF(
				(float)(lBushX1 - BUSH_WIDTH * BUSH_SHADE_HEIGHT_OFFSET_RATIO),
				(float)(lBushY1 + lBushHeight * BUSH_SHADE_WIDTH_OFFSET_RATIO),
				(float)(lBushX2),
				(float)(lBushY2)
				);
		
		canvas.drawBitmap(bushBitmap, null, lBushShadeRectF, bushShadePaint);
		
		/*
		 * Draw right bush's shade
		 */
		
		double rBushHeight = BUSH_HEIGHT_MIN + (BUSH_HEIGHT_MAX - BUSH_HEIGHT_MIN) * rand.nextDouble();
		double rBushX1 = i + BUSH_WIDTH;
		double rBushY1 = (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT)
				- rBushHeight + BUSH_HEIGHT_OFFSET_FROM_WALL;
		double rBushX2 = rBushX1 + BUSH_WIDTH;
		double rBushY2 = rBushY1 + rBushHeight;
		
		RectF rBushShadeRectF = new RectF(
				(float)(rBushX1 - BUSH_WIDTH * BUSH_SHADE_WIDTH_OFFSET_RATIO),
				(float)(rBushY1 + rBushHeight * BUSH_SHADE_WIDTH_OFFSET_RATIO),
				(float)(rBushX2),
				(float)(rBushY2)
				);
		canvas.drawBitmap(bushBitmap, null, rBushShadeRectF, bushShadePaint);
		
		/*
		 * Draw middle bush's shade
		 */
		
		double mBushHeight = BUSH_HEIGHT_MIN + (BUSH_HEIGHT_MAX - BUSH_HEIGHT_MIN) * rand.nextDouble();
		double mBushX1 = i + BUSH_WIDTH / 2;
		double mBushY1 = (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT)
				- mBushHeight + BUSH_HEIGHT_OFFSET_FROM_WALL + BUSH_HEIGHT_OFFSET_FROM_BUSH;
		double mBushX2 = mBushX1 + BUSH_WIDTH;
		double mBushY2 = mBushY1 + mBushHeight;
		
		RectF mBushShadeRectF = new RectF(
				(float)(mBushX1 - BUSH_WIDTH * BUSH_SHADE_WIDTH_OFFSET_RATIO),
				(float)(mBushY1 + mBushHeight * BUSH_SHADE_WIDTH_OFFSET_RATIO),
				(float)(mBushX2),
				(float)(mBushY2)
				);
		canvas.drawBitmap(bushBitmap, null, mBushShadeRectF, bushShadePaint);
		
		/*
		 * Draw left bush
		 */
		RectF lBushRectF = new RectF(
				(float)(lBushX1),
				(float)(lBushY1),
				(float)(lBushX2),
				(float)(lBushY2)
				);
		
		Paint lBushPaint = new Paint();
		lBushPaint.setColorFilter(
				new PorterDuffColorFilter(
						bushColor[rand.nextInt(bushColor.length)], 
						PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bushBitmap, null, lBushRectF, lBushPaint);
		
		/*
		 * Draw right bush
		 */
		RectF rBushRectF = new RectF(
				(float)(rBushX1),
				(float)(rBushY1),
				(float)(rBushX2),
				(float)(rBushY2)
				);
		Paint rBushPaint = new Paint();
		rBushPaint.setColorFilter(
				new PorterDuffColorFilter(
						bushColor[rand.nextInt(bushColor.length)], 
						PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bushBitmap, null, rBushRectF, rBushPaint);
		
		/*
		 * Draw middle bush
		 */
		RectF mBushRectF = new RectF(
				(float)(mBushX1),
				(float)(mBushY1),
				(float)(mBushX2),
				(float)(mBushY2)
				);
		Paint mBushPaint = new Paint();
		mBushPaint.setColorFilter(
				new PorterDuffColorFilter(
						bushColor[rand.nextInt(bushColor.length)], 
						PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bushBitmap, null, mBushRectF, mBushPaint);
}
	
	private void drawTree(Canvas canvas, int i){
		
		float rootHeightMax = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT);
		float rootHeightMin = (float) (rootHeightMax + GROUND_HEIGHT - GRASS_HEIGHT);
		float rootHeight = (float) (rootHeightMax + rand.nextDouble() * (rootHeightMin - rootHeightMax));
		
		/*
		 * Draw trunk
		 */
		Paint trunkPaint = new Paint();
		trunkPaint.setColor(TRUNK_COLOR);
		double trunkX1 = i + TREE_CROWN_WIDTH / 2 - TRUNK_WIDTH / 2;
		double trunkY1 = rootHeight - TRUNK_HEIGHT;
		double trunkX2 = i + TREE_CROWN_WIDTH / 2 + TRUNK_WIDTH / 2;
		double trunkY2 = rootHeight;
		RectF trunkRectF = new RectF(
				(float)(trunkX1),
				(float)(trunkY1),
				(float)(trunkX2),
				(float)(trunkY2));
		canvas.drawRect(trunkRectF, trunkPaint);
		
		/*
		 * Draw trunk shade
		 */
		Paint trunkShadePaint = new Paint();
		trunkShadePaint.setColor(Color.BLACK);
		trunkShadePaint.setStrokeWidth((float) TRUNK_WIDTH);
		trunkShadePaint.setAlpha(60);
		trunkShadePaint.setAntiAlias(true);
		double trunkShadeX1 = trunkX1 - ((rootHeight - rootHeightMax) / Math.tan(Math.toRadians(TREE_SHADE_ANGLE)));
		double trunkShadeY1 = rootHeightMax;
		double trunkShadeX2 = i + TREE_CROWN_WIDTH / 2;
		double trunkShadeY2 = rootHeight;
		canvas.drawLine(
				(float)(trunkShadeX1), 
				(float)(trunkShadeY1), 
				(float)(trunkShadeX2), 
				(float)(trunkShadeY2), 
				trunkShadePaint);
		
		double trunkShadeOnWallX1 = trunkShadeX1;
		double trunkShadeOnWallY1 = trunkShadeY1 - TREE_CROWN_HEIGHT * TREE_CROWN_SHADE_HEIGHT_OFFSET_RATIO;
		double trunkShadeOnWallX2 = trunkShadeX1;
		double trunkShadeOnWallY2 = rootHeightMax;
		canvas.drawLine(
				(float)(trunkShadeOnWallX1), 
				(float)(trunkShadeOnWallY1), 
				(float)(trunkShadeOnWallX2), 
				(float)(trunkShadeOnWallY2), 
				trunkShadePaint);
		/*
		 * Draw crown shade
		 */
		double treeCrownX1 = i;
		double treeCrownY1 = trunkY1 - TREE_CROWN_HEIGHT;
		double treeCrownX2 = i + TREE_CROWN_WIDTH;
		double treeCrownY2 = trunkY1;
		
		double treeCrownShadeX1 = i - ((rootHeight - rootHeightMax) / Math.tan(Math.toRadians(TREE_SHADE_ANGLE)));
		double treeCrownShadeY1 = trunkShadeOnWallY1 - TREE_CROWN_HEIGHT;
		double treeCrownShadeX2 = treeCrownShadeX1 + TREE_CROWN_WIDTH;
		double treeCrownShadeY2 = trunkShadeOnWallY1;
		Drawable treeCrownShadeTile = context.getResources().getDrawable(R.drawable.tree_crown_shade);
		Bitmap treeCrownShadeBitmap = ((BitmapDrawable)treeCrownShadeTile).getBitmap();
		RectF treeCrownShadeRectF = new RectF((float)(treeCrownShadeX1),(float)(treeCrownShadeY1),
				(float)(treeCrownShadeX2),(float)(treeCrownShadeY2));
		Paint treeCrownShadePaint = new Paint();
		treeCrownShadePaint.setAlpha(60);
		treeCrownShadePaint.setAntiAlias(true);
		canvas.drawBitmap(treeCrownShadeBitmap, null, treeCrownShadeRectF, treeCrownShadePaint);
		
		/*
		 * Draw crown
		 */
		Drawable treeCrownTile = context.getResources().getDrawable(R.drawable.tree_crown);
		Bitmap treeCrownBitmap = ((BitmapDrawable)treeCrownTile).getBitmap();
		RectF treeCrownRectF = new RectF((float)(treeCrownX1),(float)(treeCrownY1),
				(float)(treeCrownX2),(float)(treeCrownY2));
		Paint treeCrownPaint = new Paint();
		treeCrownPaint.setAntiAlias(true);
		canvas.drawBitmap(treeCrownBitmap, null, treeCrownRectF, treeCrownPaint);
	}
	
	
	private int drawGrass(Canvas canvas, int start, int end, int offset){
		start = start - lastPillarOffsetTmp;
		for(int i = start; i < end; i ++){
			// Max, from the lower left corner
			float grassHeightMax = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT);
			float grassHeightMin = (float) (grassHeightMax + GROUND_HEIGHT - GRASS_HEIGHT);
			double grassRootOffset = rand.nextGaussian();
			
			float grassHeight = (float) (grassHeightMax + rand.nextDouble()*(grassHeightMin - grassHeightMax));
			Paint grassPaint = new Paint();
			grassPaint.setColor(GRASS_COLOR);
			canvas.drawLine(
					(float)i, 
					(float)grassHeight, 
					(float)(i + grassRootOffset), 
					(float)(grassHeight + GRASS_HEIGHT), 
					grassPaint);
			
			i += GRASS_SPACE + rand.nextDouble()*GRASS_SPACE;
		}
		
		return end;
	}
	
	private void drawGround(Canvas canvas, int groundEnd, int offset){
		int groundArrayEnd = (int) ((groundEnd + offset) / BRICK_WIDTH);
		
		Paint subterraneanPaint = new Paint();
		Paint groundSectionPaint = new Paint();
		Paint groundPaint = new Paint();
		Paint groundBorderPaint = new Paint();
		
		int i = 0;
		while(i < groundArray.length && (groundArray[i] == Ground.BUSH_DRAWN 
									|| groundArray[i] == Ground.NORMAL_DRAWN 
									|| groundArray[i] == Ground.CONE_DRAWN
									|| groundArray[i] == Ground.POOL_DRAWN
									|| groundArray[i] == Ground.TREE_DRAWN))
			i++;
		
		for(; i < groundArrayEnd; i++){
			if(groundArray[i] == Ground.NORMAL 
			|| groundArray[i] == Ground.TREE
			|| groundArray[i] == Ground.BUSH
			|| groundArray[i] == Ground.CONE
					){
				subterraneanPaint.setColor(SUBTERRANEAN_COLOR);
				groundSectionPaint.setColor(GROUND_SECTION_COLOR);
				groundBorderPaint.setColor(GROUND_BORDER_COLOR);
				groundPaint.setColor(GROUND_COLOR);
			}
			else if(groundArray[i] == Ground.POOL){
				subterraneanPaint.setColor(POOL_SECTION_COLOR);
				groundSectionPaint.setColor(POOL_SECTION_COLOR);
				groundBorderPaint.setColor(POOL_SECTION_COLOR);
				groundPaint.setColor(POOL_COLOR);
			}
			
			float start = (float) (i * BRICK_WIDTH - offset);
			float end = (float) (start + BRICK_WIDTH);
			

			/*
			 * Draw subterranean layer
			 */
			float subterraneanX1 = start;
			float subterraneanY1 = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT);
			float subterraneanX2 = end;
			float subterraneanY2 = stage.getHeight();
			RectF subterraneanRectF = new RectF(
					subterraneanX1,
					subterraneanY1,
					subterraneanX2,
					subterraneanY2
					);
			canvas.drawRect(subterraneanRectF, subterraneanPaint);
			
			/*
			 * Draw ground section
			 */
			float groundSectionX1 = start;
			float groundSectionY1 = (int) (subterraneanY1 - GROUND_SECTION_HEIGHT);
			float groundSectionX2 = end;
			float groundSectionY2 = subterraneanY1;
			RectF groundSectionRectF = new RectF(
					groundSectionX1,
					groundSectionY1,
					groundSectionX2,
					groundSectionY2
					);
			canvas.drawRect(groundSectionRectF, groundSectionPaint);
			
			/*
			 * Draw ground
			 */
			float groundX1 = start;
			float groundY1 = (int) (groundSectionY1 - GROUND_HEIGHT);
			float groundX2 = end;
			float groundY2 = groundSectionY1;
			RectF groundRectF = new RectF(
					groundX1,
					groundY1,
					groundX2,
					groundY2
					);
			canvas.drawRect(groundRectF, groundPaint);
			
			/*
			 * Draw the border of ground section and subterranean layer
			 */
			float groundBorderX1 = start;
			float groundBorderY1 = subterraneanY1;
			float groundBorderX2 = end;
			float groundBorderY2 = subterraneanY1;
			canvas.drawLine(groundBorderX1, groundBorderY1, groundBorderX2, groundBorderY2, groundBorderPaint);
			
			if(groundArray[i] == Ground.TREE)
				drawTree(canvas,(int) start);
			else if(groundArray[i] == Ground.BUSH)
				drawBush(canvas, (int) start);
			else if(groundArray[i] == Ground.CONE)
				drawCone(canvas, (int)start);
			
			if (groundArray[i]== Ground.TREE) groundArray[i] = Ground.TREE_DRAWN; 
			if (groundArray[i]== Ground.POOL) groundArray[i] = Ground.POOL_DRAWN; 
			if (groundArray[i]== Ground.BUSH) groundArray[i] = Ground.BUSH_DRAWN; 
			if (groundArray[i]== Ground.CONE) groundArray[i] = Ground.CONE_DRAWN; 
			if (groundArray[i]== Ground.NORMAL) groundArray[i] = Ground.NORMAL_DRAWN; 
		}
	}
	
	private void drawCone(Canvas canvas, int start) {
		Drawable coneTile = context.getResources().getDrawable(R.drawable.cone);
		Bitmap coneBitmap = ((BitmapDrawable)coneTile).getBitmap();
		
		int CONE_WIDTH = (int) (BRICK_WIDTH );
		int CONE_HEIGHT = (int) (BRICK_HEIGHT);
		int x1 = start - (CONE_WIDTH - BRICK_WIDTH) / 2;
		int y1 = (int) (BITMAP_HEIGHT - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - CONE_HEIGHT);
		int x2 = x1 + CONE_WIDTH;
		int y2 = y1 + CONE_HEIGHT;
		canvas.drawBitmap(coneBitmap, null, new RectF(x1,y1,x2,y2), null);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private void drawBrickShade(Canvas canvas, int start, int offset) {
		/*
		 * Draw bricks' shade
		 */
		Drawable shadeTile = context.getResources().getDrawable(R.drawable.shade);
		Bitmap shadeBitmap = ((BitmapDrawable)shadeTile).getBitmap();
		Paint brickShadePaint = new Paint();
		brickShadePaint.setAlpha(BRICK_SHADE_ALPHA);
		
		for(int i = (int)Math.floor(start / BRICK_WIDTH); 
				i < (int)Math.ceil((start + BITMAP_WIDTH)/ BRICK_WIDTH + 2);
				i++){
			for(int j = 0; j < brickArray[i].length; j++){
				if( (brickArray[i][j] == Brick.BROWN)||(brickArray[i][j] == Brick.YELLOW) ){
					int x1 = (int) (i * BRICK_WIDTH - BRICK_SHADE_WIDTH_OFFSET - offset);
					int y1 = (int) (j * BRICK_HEIGHT + BRICK_SHADE_HEIGHT_OFFSET + BRICK_HEIGHT_OFFSET);
					int x2 = (int) (x1 + BRICK_WIDTH);
					int y2 = (int) (y1 + BRICK_HEIGHT);
					
					if(y1 > BITMAP_HEIGHT) y1 = BITMAP_HEIGHT;
					if(y2 > BITMAP_HEIGHT) y2 = BITMAP_HEIGHT;
					
					RectF rDest = new RectF(x1,y1,x2,y2);
					
					canvas.drawBitmap(shadeBitmap, null, rDest, brickShadePaint);
				}
			}
		}
	}

	private void drawHeroShade(Canvas canvas, int startx) {
		/*
		 * Draw hero's shade
		 */
		Drawable heroShadeTile = context.getResources().getDrawable(R.drawable.hero_shade);
		Bitmap heroShadeBitmap = ((BitmapDrawable)heroShadeTile).getBitmap();
		Paint heroShadePaint = new Paint();
		heroShadePaint.setAlpha(HERO_SHADE_ALPHA);
		double sx1 = startx + heroPositionX - (HERO_WIDTH - BRICK_WIDTH)/2 - HERO_SHADE_WIDTH_OFFSET;
		double sy1 = 
				(BITMAP_HEIGHT - heroPositionY) + 
				BRICK_HEIGHT - HERO_HEIGHT + 
				HERO_SHADE_HEIGHT_OFFSET + 
				BRICK_HEIGHT_OFFSET;
		double sx2 = sx1 + HERO_WIDTH;
		double sy2 = sy1 + HERO_HEIGHT;
		RectF hsDest = new RectF((float)sx1, (float)sy1, (float)sx2, (float)sy2);
		canvas.drawBitmap(heroShadeBitmap, null, hsDest, heroShadePaint);
	}

	private void drawBrick(int start, int offset) {
		/*
		 * Draw bricks
		 */
		Drawable yellowTile = context.getResources().getDrawable(R.drawable.tile_yellow);
		Drawable brownTile = context.getResources().getDrawable(R.drawable.tile_brown);
		Drawable darkCloudTile = context.getResources().getDrawable(R.drawable.dark_cloud);
		Drawable lightningTile = context.getResources().getDrawable(R.drawable.lightning);
		Drawable rainTile = context.getResources().getDrawable(R.drawable.rain);
		Drawable passportTile = context.getResources().getDrawable(R.drawable.passport);
		Drawable duckyTile = context.getResources().getDrawable(R.drawable.ducky);
		Drawable umbrellaTile = context.getResources().getDrawable(R.drawable.umbrella);
		Drawable springboardClosedTile = context.getResources().getDrawable(R.drawable.springboard_closed);
		Drawable springboardOpened1Tile = context.getResources().getDrawable(R.drawable.springboard_opened_1);
		Drawable springboardOpened2Tile = context.getResources().getDrawable(R.drawable.springboard_opened_2);
		
		
		Bitmap yellowBitmap = ((BitmapDrawable)yellowTile).getBitmap();
		Bitmap brownBitmap = ((BitmapDrawable)brownTile).getBitmap();
		Bitmap darkCloudBitmap = ((BitmapDrawable)darkCloudTile).getBitmap();
		Bitmap lightningBitmap = ((BitmapDrawable)lightningTile).getBitmap();
		Bitmap rainBitmap = ((BitmapDrawable)rainTile).getBitmap();
		Bitmap passportBitmap = ((BitmapDrawable)passportTile).getBitmap();
		Bitmap duckyBitmap = ((BitmapDrawable)duckyTile).getBitmap();
		Bitmap umbrellaBitmap = ((BitmapDrawable)umbrellaTile).getBitmap();
		Bitmap springboardClosedBitmap = ((BitmapDrawable)springboardClosedTile).getBitmap();
		Bitmap springboardOpened1Bitmap = ((BitmapDrawable)springboardOpened1Tile).getBitmap();
		Bitmap springboardOpened2Bitmap = ((BitmapDrawable)springboardOpened2Tile).getBitmap();
		
		
		Canvas canvas = new Canvas(stageWithBricks);
		
		for(int i = (int)Math.floor(start / BRICK_WIDTH); 
				i < (int)Math.ceil((start + BITMAP_WIDTH)/ BRICK_WIDTH + 2);
				i++){
			for(int j = 0; j < brickArray[i].length; j++){
				if( (brickArray[i][j] == Brick.BROWN) ||
					(brickArray[i][j] == Brick.YELLOW) ||
					(brickArray[i][j] == Brick.SPRINGBOARD_CLOSED) ||
					(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X1) ||
					(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X2)){
					
					RectF rDest = new RectF(
							(float)(i*BRICK_WIDTH - offset), 
							(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
							(float)((i+1)*BRICK_WIDTH - offset), 
							(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  
							);
					
					if(brickArray[i][j] == Brick.BROWN )
						canvas.drawBitmap(brownBitmap, null, rDest,	null);
					else if(brickArray[i][j] == Brick.YELLOW)
						canvas.drawBitmap(yellowBitmap, null, rDest, null);
					else if(brickArray[i][j] == Brick.SPRINGBOARD_CLOSED)
						canvas.drawBitmap(springboardClosedBitmap, null, rDest, null);
					else if(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X1)
						canvas.drawBitmap(springboardOpened1Bitmap, null, rDest, null);
					else if(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X2)
						canvas.drawBitmap(springboardOpened2Bitmap, null, rDest, null);
					
				}
				else if( (brickArray[i][j] == Brick.DARK_CLOUD_X0Y0) ||
						 (brickArray[i][j] == Brick.DARK_CLOUD_X1Y0) || 
						 (brickArray[i][j] == Brick.DARK_CLOUD_X2Y0) || 
						 (brickArray[i][j] == Brick.DARK_CLOUD_X0Y1) ||
						 (brickArray[i][j] == Brick.DARK_CLOUD_X1Y1) ||
						 (brickArray[i][j] == Brick.DARK_CLOUD_X2Y1)){
					
					int srcX1 = 0;
					int srcY1 = 0;
					boolean drawShade = false;
					if(brickArray[i][j] == Brick.DARK_CLOUD_X0Y0){
						srcX1 = (0)*darkCloudBitmap.getWidth()/3;
						srcY1 = (0)*darkCloudBitmap.getHeight()/2;
						drawShade = false;
					}
					else if(brickArray[i][j] == Brick.DARK_CLOUD_X1Y0){
						srcX1 = (1)*darkCloudBitmap.getWidth()/3;
						srcY1 = (0)*darkCloudBitmap.getHeight()/2;
						drawShade = false;
					}
					else if(brickArray[i][j] == Brick.DARK_CLOUD_X2Y0){
						srcX1 = (2)*darkCloudBitmap.getWidth()/3;
						srcY1 = (0)*darkCloudBitmap.getHeight()/2;
						drawShade = false;
					}
					else if(brickArray[i][j] == Brick.DARK_CLOUD_X0Y1){
						srcX1 = (0)*darkCloudBitmap.getWidth()/3;
						srcY1 = (1)*darkCloudBitmap.getHeight()/2;
						drawShade = false;
					}
					else if(brickArray[i][j] == Brick.DARK_CLOUD_X1Y1){
						srcX1 = (1)*darkCloudBitmap.getWidth()/3;
						srcY1 = (1)*darkCloudBitmap.getHeight()/2;
						drawShade = false;
					}
					else if(brickArray[i][j] == Brick.DARK_CLOUD_X2Y1){
						srcX1 = (2)*darkCloudBitmap.getWidth()/3;
						srcY1 = (1)*darkCloudBitmap.getHeight()/2;
						drawShade = true;
					}
					
					
					int srcX2 = srcX1 + darkCloudBitmap.getWidth()/3;
					int srcY2 = srcY1 + darkCloudBitmap.getHeight()/2;
					
					Rect rSrc = new Rect( srcX1, srcY1, srcX2, srcY2);

					RectF rDest = new RectF(
							(float)(i*BRICK_WIDTH - offset), 
							(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
							(float)((i+1)*BRICK_WIDTH - offset), 
							(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  
							);
					canvas.drawBitmap(
							darkCloudBitmap, 
							rSrc,
							rDest, 
							null);
					
					if(drawShade){
						Paint darkCloudShadePaint = new Paint();
						darkCloudShadePaint.setAlpha(180);
						RectF shadeDest = new RectF(
								(float)((i-2)*BRICK_WIDTH - offset), 
								(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
								(float)((i+1)*BRICK_WIDTH - offset), 
								(float)((j+4)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  
								);
						//canvas.drawBitmap(darkCloudShadeBitmap, null, shadeDest, darkCloudShadePaint );
						
						Paint rainPaint = new Paint();
						rainPaint.setAlpha(200);
						canvas.drawBitmap(rainBitmap, null, shadeDest, rainPaint );
						
						if(lightningTimeRemaining > 0){
							Paint lightningPaint = new Paint();
							lightningPaint.setAntiAlias(true);
							canvas.drawBitmap(lightningBitmap, null, new RectF(
									(float)((i-1)*BRICK_WIDTH - offset), 
									(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
									(float)((i+0)*BRICK_WIDTH - offset), 
									(float)((j+2)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  ), lightningPaint);
							lightningTimeRemaining = 1000 - (System.currentTimeMillis() - lastFlashTime);
						}
						else{
							if(System.currentTimeMillis() - lastFlashTime > 2000){
								lightningTimeRemaining = 1000;
								lastFlashTime = System.currentTimeMillis();
							}
						}
					}
				}
				else if(brickArray[i][j] == Brick.PASSPORT){
					RectF rDest = new RectF(
							(float)(i*BRICK_WIDTH - offset), 
							(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
							(float)((i+1)*BRICK_WIDTH - offset), 
							(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  
							);
						canvas.drawBitmap(passportBitmap, null,	rDest, null);
				}else if(brickArray[i][j] == Brick.DUCKY){
					RectF rDest = new RectF(
							(float)(i*BRICK_WIDTH - offset), 
							(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
							(float)((i+1)*BRICK_WIDTH - offset), 
							(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  
							);
						canvas.drawBitmap(duckyBitmap, null,	rDest, null);
				}else if(brickArray[i][j] == Brick.UMBRELLA){
					RectF rDest = new RectF(
							(float)(i*BRICK_WIDTH - offset), 
							(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
							(float)((i+1)*BRICK_WIDTH - offset), 
							(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  
							);
						canvas.drawBitmap(umbrellaBitmap, null,	rDest, null);
				}
			}
		}
	}

	long lightningTimeRemaining;
	long lastFlashTime;
	private void drawHero(Canvas canvas) {
		/*
		 * Draw hero
		 */
		Drawable heroTile = context.getResources().getDrawable(R.drawable.hero);
		Bitmap heroBitmap = ((BitmapDrawable)heroTile).getBitmap();
		int x1 = (int) (heroPositionX - (HERO_WIDTH - BRICK_WIDTH) / 2);
		int y1 = (int) (BITMAP_HEIGHT + BRICK_HEIGHT - HERO_HEIGHT - (heroPositionY + SUBTERRANEAN_HEIGHT + GROUND_SECTION_HEIGHT ));
		
		//int y1 = (int) ((BITMAP_HEIGHT -heroPositionY) + BRICK_HEIGHT - HERO_HEIGHT + BRICK_HEIGHT_OFFSET);
		int x2 = x1 + HERO_WIDTH;
		int y2 = y1 + HERO_HEIGHT;
		RectF hDest = new RectF(x1, y1, x2, y2);
		canvas.drawBitmap(heroBitmap, null, hDest, null);
	}

}
