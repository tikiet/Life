package edu.crabium.android.life;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LifeSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback{

	/** A database instance that to save record and get rank */
	private LifeDatabase lifeDatabase = LifeDatabase.getInstance();
	
	private SurfaceHolder holder;
	
	/** The main thread, draw all the scenes and respond to touch events */
	private LifeSurfaceViewThread thread;
	
	/** The bitmap that all the things(except bricks) are drawn */
	private Bitmap stage;
	
	/** 
	 * The bitmap that all the things are drawn,
	 * it will be drawn to the canvas which is got after the lockCanvas() method 
	 */
	private Bitmap stageWithBricks;
	
	/** Hero's x-position, in pixels */
	private int heroPositionX;
	
	/** Hero's y-position, in pixels */
	private int heroPositionY;
	
	/** 
	 * Hero's horizontal moving speed, 
	 * which is also the background's moving speed
	 */
	private int heroSpeedX = 7;
	
	/**
	 *  Hero's vertical moving speed, 
	 *  it will be set after been bounced by springboard
	 */
	private int heroSpeedY = 0;
	
	/** Hero's default moving speed */
	private int INITIAL_SPEED = 7;
	
	/** The distance that have been traveled, in pixels */
	private int traveledPixels;
	
	/* Screen offset, used to draw the hero by a proper shift
	 * and indicate how far has the screen been shifted.
	 * 
	 * The 'Mod' means modular by BRICK_WIDTH, if offsetMod
	 * equals zero, it means the screen have been shifted by
	 * BRICK_WIDTH pixels, thus some action should be performed,
	 * such as shift the brick's array
	 */
	private int offsetMod;	
	
	/**
	 * A ThreadState instance, used to indicate thread status.
	 * It can be set by surfaceViewThread to tell other methods, 
	 * or be set by other methods to tell surfaceViewThread to
	 * stop or pause, etc.
	*/
	private ThreadState threadState;
	
	/* 
	 * Counter variables for available resources
	 */
	private int umbrellaCount;
	private int duckyCount;
	private int passportCount;
	private int availableBrickCount;
	private int availableSpringboardCount;
	
	/*
	 * Boolean variables used to indicate whether
	 * gadgets are being used.
	 */
	private boolean usingDuck;
	private boolean usingUmbrella;
	private boolean usingPassport;
	
	/*
	 * Variables used to mark the ending positions
	 * of previous drawing actions. Latter drawing
	 * actions will start from those endings.
	 */
	private int prevBackPillarEnd;
	private int prevPillarEnd;
	private int prevCloudEnd;
	private int prevGroundEnd;
	private int prevGrassEnd;
	private int prevBrickEnd;
	private int prevDarkCloudEnd;
	
	/*
	 * Variables used to indicate screen offset
	 * between two drawing actions.
	 * 
	 * Hey, the world is always moving, right?
	 */
	private int lastBrickOffset;
	private int lastCloudOffset;
	private int lastPillarOffset;
	private int lastBackPillarOffset;
	private int lastPillarOffsetTmp;
	
	/**
	 * Used to indicate whether new pillars have been
	 * drawn in the new screen updating process
	 * 
	 * It's used together with lastPillarOffsetTmp for
	 * other drawing methods to set a proper offset at
	 * the start position.
	 */
	private boolean pillarDrawn = false;
	
	/**
	 * A PillarPosition instance, used to store the position
	 * of the last pillar from previous drawing action.
	 * 
	 * In every pillar drawing action, the last pillar's
	 * position should be save and it shouldn't be drawn
	 * immediately, or it will be covered by latter pillar's
	 * shade.
	 * 
	 * Thus this instance is used to save its position and draw
	 * it in the next time
	 */
	private PillarPosition previousPillarPosition;
	
	/*
	 * Color constants
	 */
	private int BACK_GROUND_COLOR = 0xff9bc3cf;
	private int GROUND_COLOR = 0xffced852;
	
	private int GROUND_SECTION_COLOR = 0xffb3b827;
	private int GROUND_BORDER_COLOR = 0xff8c8302;
	private int SUBTERRANEAN_COLOR = 0xffea9506;
	private int GRASS_COLOR = 0xff54540e;
	private int TRUNK_COLOR = 0xff4c4238;
	
	private int POOL_COLOR = 0xff678ee8;
	private int POOL_SECTION_COLOR = 0xff3266db;
	
	private int YELLOW_BRICK_COLOR = 0xfffbe146;
	private int BROWN_BRICK_COLOR = 0xff503e34;
	
	/*
	 * Constants for background
	 */
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
	
	private double CLOUD_HEIGHT_MAX;
	private double CLOUD_HEIGHT_MIN;
	private int CLOUD_WIDTH_MAX;
	private int CLOUD_WIDTH_MIN;
	private double CLOUD_SPACE_RATIO;
	private double CLOUD_SHADE_HEIGHT_OFFSET_RATIO;
	private double CLOUD_SHADE_WIDTH_OFFSET_RATIO;
	
	private double DARK_CLOUD_THRESHOLD;
	private double DARK_CLOUD_HEIGHT_MIN;
	private double DARK_CLOUD_HEIGHT_MAX;
	private int INITIAL_DARK_CLOUD_COOLING_DISTANCE;
	
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
	
	/* 
	 * Constants for hero
	 */
	private int HERO_HEIGHT;
	private int HERO_WIDTH;
	private double HERO_SHADE_HEIGHT_OFFSET;
	private double HERO_SHADE_WIDTH_OFFSET;
	
	/*
	 * Constants for random bricks
	 */
	private int RANDOM_BRICK_COUNT_MAX;
	private int RANDOM_BRICK_COUNT_MIN;
	private double RANDOM_BRICK_HEIGHT_MAX;
	private double RANDOM_BRICK_HEIGHT_MIN;
	private double RANDOM_BRICK_THRESHOLD;

	/*
	 * Constants for bricks
	 * 
	 * Brick height and width are determined
	 * by scrren height, width and ratios from
	 * resource file
	 */
	private int BRICK_HEIGHT;
	private int BRICK_WIDTH;
	private double BRICK_HEIGHT_OFFSET;
	private double BRICK_SHADE_HEIGHT_OFFSET;
	private double BRICK_SHADE_WIDTH_OFFSET;
	private double BROWN_BRICK_THRESHOLD;
	
	/* 
	 * Constants for pool
	 */
	private double POOL_WIDTH_MIN;
	private double POOL_WIDTH_MAX;
	private double POOL_THRESHOLD;
	private int POOL_SPACE_COUNT;
	
	/*
	 * Constants for cone
	 */
	private int CONE_SPACE_COUNT;
	private double CONE_THRESHOLD;
	
	/*
	 * Constants for gadgets
	 */
	private double GADGET_THRESHOLD;
	private double DUCKY_THRESHOLD;
	private double PASSPORT_THRESHOLD;
	private double UMBRELLA_THRESHOLD;
	private double DUCKY_EFFECTIVE_DURATION_MILLIS;
	private double UMBRELLA_EFFECTIVE_DURATION_MILLIS;
	private double PASSPORT_EFFECTIVE_DURATION_MILLIS;
	
	/*
	 * Constants for lightning
	 */
	private int LIGHTNING_DURATION_MILLIS;
	private int LIGHTNING_INTERVAL_MILLIS;
	
	/** Screen width, determine in LifeActivity */
	private int SCREEN_WIDTH;
	
	/** Screen height, determine in LifeActivity */
	private int SCREEN_HEIGHT;
	
	/** Maximum number of bricks that can be drawn horizontally */
	private int BRICK_NUMBER_X;
	
	/** Maximum number of bricks that can be drawn vertically */
	private int BRICK_NUMBER_Y;
	
	/*
	 * Alpha constants
	 */
	private int HERO_SHADE_ALPHA = 60;
	private int BACK_PILLAR_SHADE_ALPHA = 30;
	private int CLOUD_SHADE_ALPHA = 60;
	private int BRICK_SHADE_ALPHA = 60;
	private int BUSH_SHADE_ALPHA = 60;
	private int RAIN_ALPHA = 200;
	/** Screen shift speed (vertically), used in game over animation */ 
	private int GAME_OVER_SCREEN_SHIFT_SPPED;
	
	/** The maximum times to upadte screen every second */
	private int MAXIMUM_REFRESH_RATE = 200;
	
	/** The interval between two springboard incrase, in milliseconds*/
	private int SPRINGBOARD_INCREASE_INTERVAL_MILLIS;
	
	/** The array to store status for every brick in the screen */
	private Brick[][] brickArray;
	private enum Brick{
		NONE, YELLOW, BROWN, PASSPORT, DUCKY, UMBRELLA,
		DARK_CLOUD_X0Y0, DARK_CLOUD_X1Y0, DARK_CLOUD_X2Y0,
		DARK_CLOUD_X0Y1, DARK_CLOUD_X1Y1, DARK_CLOUD_X2Y1,
		SPRINGBOARD_CLOSED, SPRINGBOARD_OPENED_X1, SPRINGBOARD_OPENED_X2};
	
	/** The array to store status for every ground components */
	private Ground[] groundArray;
	private enum Ground{NONE, NORMAL, POOL, BUSH, TREE, CONE, 
		NORMAL_DRAWN, POOL_DRAWN, BUSH_DRAWN, TREE_DRAWN, CONE_DRAWN}
	
	/*
	 * This array is used to support variable speed, 
	 * it stores offsets that every update needs.
	 * 
	 * For example, if BRICK_WIDTH is 32 and screen moving speed is 3, 
	 * then brickArray should make a shift to left after 11 movings,
	 * because after 11 movings, the screen has shifted 33 pixels to
	 * left and the first columns in brickArray is now invisible.
	 * 
	 * brickArray shifting is performed every time offsetMod equals 0.
	 * But it's hard to properly shift brickArray if only offsetMod and
	 * a constant speed variable is used, think about the previous case,
	 * brickArray should shift after 11 movings but at that time,
	 * offsetMod is (11*3)%(BRICK_WIDHT) = 1, which will cause weird 
	 * (also funny) result.
	 * 
	 * Thus, this offsetArray is used. For different BRICK_WIDTH and 
	 * screen moving speed (which is stored in heroSpeedX) combinations,
	 * it will calculate different values for them:
	 * 
	 * offsetArray.length  will be BRICK_WIDTH/ heroSpeedX, all of its 
	 * items (except the last) will be set to heroSpeedX, and the last
	 * item will be heroSpeedX + BRICK_WIDTH % heroSpeedX.
	 * 
	 * For the previous case, the array will be {3,3,3,3,3,3,3,3,3,5},
	 * in the 10th moving, shift value will be looped to 5, and 
	 * offsetMod will be accumulated to 32, and after mod BRICK_WIDTH
	 * will be 0. which results in shifting the brickArray timely.
	 * 
	 */
	private int[] offsetArray;
	
	/**
	 * Pillar color array, every pillar's color will be chosen
	 * randomly from the array
	 */
	private int[] pillarColor = new int[]{
		0xff816550, 0xffa38d76, 0xff8e725a, 
		0xffc0b0a3, 0xffa69079, 0xff8c705a, 
		0xff9c846c};
	
	/**
	 * Back pillar color array, every back pillar's color will be chosen
	 * randomly from the array
	 */
	private int[] backPillarColor = new int[]{ 0xff342d27, 0xff221e1b, 0xff201f1d};
	
	/**
	 * Bush color array, every bush's color will be chosen randomly 
	 * from the array
	 */
	private int[] bushColor = new int[]{
		0xff8d6e23,0xffaa8729,0xff997822,
		0xff7a612b,0xff4c3b1d,0xffccb871,
		0xff7c632c,0xffd4ba5b,0xffc1a446
	};
	
	/**
	 * Saves application's context, used to get resources
	 */
	private Context context;
	
	/**
	 * A random number generator, used to choose color or
	 * decided pillar's height
	 */
	private Random rand = new Random();
	
	
	/** Only used to support pausing / continuing */
	private Object ted = new Object();
	
	/** Moving speed for screen refreshing, got from offsetArray */
	private int speed;
	
	/** offsetArray's index, need more explanation? */
	private int offsetArrayIndex;
	
	/** 
	 * Time stamp for the last updating, if the difference between
	 * current time stamp and it is less than 1000/MAXIMUM_REFRESH_RATE,
	 * then the run() method will just 'continue' and start over.
	 * 
	 * It's used together with MAXIMUM_REFRESH_RATE to prevent 
	 * refreshing too frequently in cellphones with powerful CPU.
	 */
	private long lastRefreshMillis;
	
	Paint backGroundPaint = new Paint();
	Canvas canvas;
 	private class LifeSurfaceViewThread extends Thread{
		public LifeSurfaceViewThread(SurfaceHolder holder, Context context) {
		}
			
		public void run(){
			if(heroSpeedX == 0){
				heroSpeedX = INITIAL_SPEED;
			}
			
			// Set offsetArray's value according to BRICK_WIDTH and heroSpeedX
			offsetArray = new int[(int) Math.floor(BRICK_WIDTH/ heroSpeedX)];
			
			for(int i = 0; i < offsetArray.length -1; i++){
				offsetArray[i] = heroSpeedX;
			}
			
			offsetArray[offsetArray.length-1] = heroSpeedX + BRICK_WIDTH % heroSpeedX;
		
			while(threadState == ThreadState.RUNNING || threadState == ThreadState.PAUSED){
				if((System.currentTimeMillis() - lastRefreshMillis) < (1000/MAXIMUM_REFRESH_RATE))
					continue;
				lastRefreshMillis = System.currentTimeMillis();
				
				speed = offsetArray[offsetArrayIndex];
				
				/* update hero status, 
				 * make him up, make him down,
				 * make him jump, make him drown
				 * 				-- by WuXD
				 */
				updateHero(canvas);
				
				/*
				 * update game resources, such as increase available bricks
				 */
				updateResources();
				
				/* 
				 * Quit the run() method is threadState is not RUNNING or PAUSED.
				 * It means other methods have set the threadState to tell run()
				 * to quit and terminating surfaceViewThread. 
				 */
				if(threadState != ThreadState.RUNNING && threadState != ThreadState.PAUSED)
					break;
				
				/*
				 * Draw stageWithBricks' content in canvas
				 */
				canvas = holder.lockCanvas();
				
				/* 
				 * Copy stage's content to stageWithBricks, and all following
				 * operations will be drawn in stageWithBricks
				 * it's used to support clearing bricks
				 */
//				Canvas canvas = new Canvas(stageWithBricks);
				RectF stageRectF = new RectF( 0, 0, stage.getWidth(), stage.getHeight());			
				canvas.drawBitmap(stage, null, stageRectF, null);
				
				
				/*
				 * Draw brick and hero's shade before drawing them
				 */
				synchronized(brickArray){
					drawBrickShade(canvas, 0, offsetMod);
				}
				
				drawHeroShade(canvas, offsetMod);
				
				/* 
				 * Time to draw brick and hero!
				 */
				synchronized(brickArray){
					drawBrick(canvas, 0, offsetMod);
				}
				
				drawHero(canvas);

				/*
				 * draw distance, available resources, etc in screen
				 */
				drawGameInfo(canvas);
				
				/*
				 * Actually, it's draw pause or continue button at the
				 * upper right corner
				 */
				drawPauseButton(canvas);
				
				/*
				 * Draw counters if gadgets are being used.
				 */
				if(usingDuck)
					drawDuckCounter(canvas);
				
				if(usingUmbrella)
					drawUmbrellaCounter(canvas);
				
				if(usingPassport)
					drawPassportCounter(canvas);

				/*
				 * Source and destination rectangles used in the following action,
				 * they tell the drawing method to draw the first screen-size 
				 * content from the stageWithBricks at the canvas
				 */
				Rect srcRect = new Rect(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
				RectF destRect = new RectF(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
				
				
				//canvas.drawBitmap(stageWithBricks, srcRect, destRect, null);
				holder.unlockCanvasAndPost(canvas);
				
				/*
				 * Check whether threadState is PAUSED, if so, wait
				 * until be awaken by onTouchEvent. During this time,
				 * screen will be freezed.
				 */
				synchronized(ted){
					if(threadState == ThreadState.PAUSED){
						try {
							ted.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				/*
				 * Shift previousTouchX by 'speed' pixels, it will be used
				 * in onTouchEvent to mark previously touched point.
				 */
				previousTouchX -= speed;
				
				/*
				 * Increase offsetMod and offsetArrayIndex
				 */
				offsetMod = (int) ((offsetMod + offsetArray[offsetArrayIndex]) % BRICK_WIDTH);
				offsetArrayIndex = (offsetArrayIndex +1) % offsetArray.length;

				/*
				 * Every time offsetMod equals zero means the screen has been
				 * shifted for BRICK_WIDTH pixels, so it's time to shift the 
				 * brickArray to remove the unnecessary first column and add
				 * a new column in the end.
				 * 
				 * Of course, groundArray also needs shifting, but it's always
				 * shifted with brickArray
				 */
				if(offsetMod == 0){
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
				 * Shift stage for 'speed' pixels 
				 */
				srcRect = new Rect(speed, 0, stage.getWidth(),	stage.getHeight());
				destRect = new RectF(0, 0,	stage.getWidth() - speed, stage.getHeight());
				
				canvas = new Canvas(stage);
				canvas.drawBitmap(stage, srcRect, destRect, null);
				
				/*
				 * After the shifting, the rightmost 'speed' columns of pixels
				 * will be invalid. Thus we should drawn background color on them
				 * and start drawing following background.
				 */
				backGroundPaint.setColor(BACK_GROUND_COLOR);
				canvas.drawRect((previousPillarPosition.getX() + BACK_PILLAR_WIDTH),
					0, stage.getWidth(), stage.getHeight(),	backGroundPaint);
				
				// Draw back pillar
				prevBackPillarEnd = drawBackPillarAndShade(
					canvas, prevBackPillarEnd, stage.getWidth(), speed);
				
				// Draw pillar
				prevPillarEnd = drawPillar(canvas, prevPillarEnd, prevBackPillarEnd, speed);
				
				// Draw cloud
				prevCloudEnd = drawCloudAndShade(canvas, prevCloudEnd, prevBackPillarEnd, speed);
				
				// Generate brick
				prevBrickEnd = generateRandomBrick(prevBrickEnd, brickArray, speed);

				// Generate dark cloud
				prevDarkCloudEnd = generateDarkCloud(prevDarkCloudEnd, brickArray, speed);
				
				// Draw ground
				generateRandomGround(groundArray, prevPillarEnd - lastPillarOffset, offsetMod);
				drawGround(canvas, prevPillarEnd - lastPillarOffset, offsetMod);

				// Draw grass
				prevGrassEnd = drawGrass(canvas, prevGrassEnd, prevPillarEnd, speed);
				
				if(pillarDrawn){
					lastPillarOffsetTmp = 0;
					pillarDrawn = false;
				}
				
				traveledPixels += speed;
			}
		}
 
		/** Record the x position of previous touch event */
		private int previousTouchX = 0;
		
		/** Record the y position of previous touch event */
		private int previousTouchY = 0;
		
		/**
		 * Respond to user's touch events, draw/clear bricks or collect gadgets
		 * @param event
		 * @return
		 */
		public boolean onTouchEvent(MotionEvent event) {
			/*
			 * If the touch event is in gadgets' area and entered
			 * it by tap, not by moving finger to reach it, then
			 * we can make sure that it it really want to use
			 * gadgets. So start counter and decrease available
			 * gadgets for touched gadget, if it it available.
			 */
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(duckyCountRectF.contains(event.getX(), event.getY()) &&
					duckyCount > 0){
					usingDuck = true;
					startUsingDuckyTimeMillis = System.currentTimeMillis();
					duckyCount --;
					return true;
				}
				else if(passportCountRectF.contains(event.getX(), event.getY()) &&
					passportCount > 0){
					usingPassport = true;
					startUsingPassportTime = System.currentTimeMillis();
					passportCount --;
					return true;
				}
				else if(umbrellaCountRectF.contains(event.getX(), event.getY()) &&
					umbrellaCount > 0){
					usingUmbrella = true;
					startUsingUmbrellaTimeMillis = System.currentTimeMillis();
					umbrellaCount --;
					return true;
				}
			}
			
			/* 
			 * Bricks can be cleared or drawn by tap or finger moving.
			 * But if the event is happened at the last row of the
			 * brickArray, a springboard(not yellow or brown brick) will
			 * be drawn.
			 */
			if( event.getAction() == MotionEvent.ACTION_MOVE ||
				event.getAction() == MotionEvent.ACTION_DOWN){
				synchronized(brickArray){
					/*
					 * It may be more than one finger on the screen, so we should
					 * get the number of fingers by getPointerCount() and get 
					 * their positions one by one through getX(pointId)/getY(pointId)
					 */
					for(int pointId = 0; pointId < event.getPointerCount(); pointId ++){
						float x = event.getX(pointId);
						float y = event.getY(pointId);
						
						/* 
						 * Get the position of the brick that has been touched,
						 * horizontal and vertical offsets are needed to complement
						 * screen's offset
						 */
						int brickX = (int) Math.floor((x + offsetMod)/BRICK_WIDTH);
						int brickY = (int) Math.floor((y - BRICK_HEIGHT_OFFSET)/BRICK_HEIGHT);
						
						/*
						 * If finger position is out of the available drawing area,
						 * just continue to next finger
						 */
						if(brickY < 0 || brickY >= BRICK_NUMBER_Y)
							continue;
						
						/* 
						 * Previously touched brick's position is calculated in the same
						 * way as current brick.
						 * 
						 * It is used to prevent more than one action is performed at
						 * a single brick. If it's not considered, a brick may be cleared
						 * immediately after been drawn, because the finger is moving at
						 * the same brick's area, and will toggle the brick's state
						 * in the following calls on onTouchEvent.
						 */
						int previousBrickX = (int) Math.floor((previousTouchX + offsetMod)/BRICK_WIDTH);
						int previousBrickY = (int) Math.floor((previousTouchY - BRICK_HEIGHT_OFFSET)/BRICK_HEIGHT);
						if(brickX != previousBrickX || brickY != previousBrickY){
							previousTouchX = (int) x;
							previousTouchY = (int) y;
							
							if( (brickArray[brickX][brickY] == Brick.YELLOW) ||
								(brickArray[brickX][brickY] == Brick.DUCKY) ||
								(brickArray[brickX][brickY] == Brick.PASSPORT) ||
								(brickArray[brickX][brickY] == Brick.UMBRELLA)){
								
								if(brickArray[brickX][brickY] == Brick.DUCKY)
									duckyCount ++;
								else if (brickArray[brickX][brickY] == Brick.PASSPORT)
									passportCount ++;
								else if (brickArray[brickX][brickY] == Brick.UMBRELLA)
									umbrellaCount ++;
								
								brickArray[brickX][brickY] = Brick.NONE;
							}
							else if(brickArray[brickX][brickY] == Brick.NONE ){
								/*
								 * The available drawing area is actually very limited,
								 * highest position = maximum pillar height
								 * lowest position = two bricks' height above the ground
								 */
								if(	(brickY <= brickArray[0].length - 3) &&	(availableBrickCount > 0) &&
									(y >= (SCREEN_HEIGHT - PILLAR_HEIGHT_MAX * SCREEN_HEIGHT))){
									if(rand.nextDouble() >= BROWN_BRICK_THRESHOLD)
										brickArray[brickX][brickY] = Brick.BROWN;
									else
										brickArray[brickX][brickY] = Brick.YELLOW;
									
									availableBrickCount --;
								}
								/*
								 * If event is at the lowest row of brickArray, a springboard
								 * should be drawn (if available)
								 * 
								 * But it can't be drawn above pool
								 */
								else if((brickY == brickArray[0].length -1) &&
										(availableSpringboardCount > 0) &&
										(groundArray[brickX] != Ground.POOL) &&
										(groundArray[brickX] != Ground.POOL_DRAWN)){
									
									brickArray[brickX][brickY] = Brick.SPRINGBOARD_CLOSED;
									availableSpringboardCount --;
								}
							}
						}
					}
				}
			}
			
			/* 
			 * If finger released, reset previous brick's position.
			 * Because next time a finger touch on the brick, it's
			 * on purpose, not accidental.
			 */
			else if(event.getAction() == MotionEvent.ACTION_UP){
				previousTouchY = -1;
			}
			
			return true;
		}
	}
 	
 	/**
 	 * A pool can't be drawn in one pass, so we need poolRemaining to indicate
 	 * how many blocks of pool are still left
 	 */
 	private int poolRemaining;
 	
 	/* 
 	 * Cooling distance is used to make ground more beautiful, after drawing pool,
 	 * tree, etc, their cooling distance will be set, and in the cooling distance,
 	 * only ground will be shown and thus we won't see a very long pool that is
 	 * several screens wide or many cones together.
 	 */
 	private int poolCoolingDistance;
 	private int treeCoolingDistance;
 	private int bushCoolingDistance;
 	private int coneCoolingDistance;
 	
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
					((POOL_WIDTH_MIN 
					+ rand.nextDouble()* (POOL_WIDTH_MAX - POOL_WIDTH_MIN))/ BRICK_WIDTH);
				
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
			else if( (coneCoolingDistance <= 0) && (rand.nextDouble() > CONE_THRESHOLD)){
				groundArray[i] = Ground.CONE;
				coneCoolingDistance = CONE_SPACE_COUNT;
			}
			else
				groundArray[i] = Ground.NORMAL;
			
			treeCoolingDistance --;
			bushCoolingDistance --;
			coneCoolingDistance --;
			poolCoolingDistance --;
		}
	}

	/*
	 * Time stamp for last springboard increase, in millisecond
	 */
	private long lastTimeIncreaseSpringboardMillis;
	
	/**
	 * update available resources
	 */
	public void updateResources(){
		/* 
		 * If screen has shifted for half or a brick's width, increase
		 * available brick by one
		 */
		if(offsetArrayIndex == 0 || offsetArrayIndex == offsetArray.length /2 )
			if(heroPositionY >= SCREEN_HEIGHT * PILLAR_HEIGHT_MIN)
				availableBrickCount ++;
		
		/*
		 * If more than SPRINGBOARD_INCREASE_INTERVAL_MILLIS milliseconds has passed
		 * since last increase, increase available springboard by one and reset
		 * lastTimeIncreaseSpringboardMillis
		 */
		if(System.currentTimeMillis() - lastTimeIncreaseSpringboardMillis >= SPRINGBOARD_INCREASE_INTERVAL_MILLIS){
			lastTimeIncreaseSpringboardMillis = System.currentTimeMillis();
			availableSpringboardCount ++;
		}
	}

	/** The rectangle that is used to specify cancel/continue button's area */
	private RectF pauseButtonRectF;
	

	private Drawable pauseButtonDrawable;
	private Bitmap pauseButtonBitmap;
		
	private Drawable continueButtonDrawable;
	private Bitmap continueButtonBitmap;
		
	/** 
	 * Draw cancel or continue button on upper right corner, based on current
	 * game status (PAUSED or RUNNING) 
	 */
 	public void drawPauseButton(Canvas canvas) {
 		/*
 		 * Button's drawing area is actually smaller than that as
 		 * pauseButtonRectF declared, which means valid touching area
 		 * it larger than the button
 		 */
 		RectF pauseRectF = new RectF(
			pauseButtonRectF.left + pauseButtonRectF.width() / 4,
			pauseButtonRectF.top ,
			pauseButtonRectF.right - pauseButtonRectF.width() / 4,
			pauseButtonRectF.bottom - pauseButtonRectF.height() / 2
			);
 		
 		if(threadState == ThreadState.PAUSED)
 			canvas.drawBitmap(continueButtonBitmap, null, pauseRectF, null);
 		else
 			canvas.drawBitmap(pauseButtonBitmap, null, pauseRectF, null);
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
 	
	/** 
	 * Used to indicate whether current game is new or is resumed from pausing.
	 * It will decide whether the main screen will be shown or not.
	 */
 	private boolean isAfterPausing;
 	
 	private void retrieveLifeData(){
 		/* 
 		 * Check whether all of the files are present, if not all them are 
 		 * present, just ignore the remnant and quit
 		 */
 		File stageFile = new File("/data/data/edu.crabium.android.life/stage");
 		File arrayFile = new File("/data/data/edu.crabium.android.life/array");
 		File groundArrayFile = new File("/data/data/edu.crabium.android.life/groundArray");
 		if(!(stageFile.exists() && arrayFile.exists() && groundArrayFile.exists()))
 			return;
 		else
 			isAfterPausing = true;
 		
 		/*
 		 * Restore stage's content
 		 */
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
 		
 		/*
 		 * Restore brickArray's content
 		 */
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
 		
 		/*
 		 * Restore groundArray's content
 		 */
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
 		
 		/*
 		 * Restore other variables' contents
 		 */
		SharedPreferences settings = context.getSharedPreferences("Life", 0);
		
		heroPositionX = settings.getInt("heroPositionX", heroPositionX);
		heroPositionY = settings.getInt("heroPositionY", heroPositionY);
		heroSpeedX = settings.getInt("heroSpeedX", heroSpeedX);
		heroSpeedY = settings.getInt("heroSpeedY", heroSpeedY);
		offsetMod = settings.getInt("offsetMod", offsetMod);
		traveledPixels = settings.getInt("traveledPixels", traveledPixels);
		offsetArrayIndex = settings.getInt("offsetArrayIndex", offsetArrayIndex);
		
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
		if(previousPillarPositionX != 0 || previousPillarPositionY != 0){
 			previousPillarPosition = new PillarPosition(
				previousPillarPositionX, previousPillarPositionY);
		}
		
		umbrellaCount = settings.getInt("umbrellaCount", umbrellaCount);
		duckyCount = settings.getInt("duckyCount", duckyCount);
		passportCount = settings.getInt("passportCount", passportCount);
		availableBrickCount = settings.getInt("availableBrickCOunt", availableBrickCount);
		availableSpringboardCount = settings.getInt("availableSpringboardCount", availableSpringboardCount);
 	}
 	
 	private void saveLifeData(){
 		try {
 			/*
 			 * Save stage's content
 			 */
 			FileOutputStream stageOut = new FileOutputStream("/data/data/edu.crabium.android.life/stage");
 			stage.compress(Bitmap.CompressFormat.PNG, 90, stageOut);
 			stageOut.close();
 			
 			/*
 			 * Save brickArray's content
 			 */
 			PrintWriter arrayOut = new PrintWriter("/data/data/edu.crabium.android.life/array");
 			arrayOut.println(brickArray.length);
 			arrayOut.println(brickArray[0].length);
 			for(int i = 0; i < brickArray.length; i ++){
 				for(int j = 0; j < brickArray[0].length; j++){
 					arrayOut.println(brickArray[i][j].toString());
 				}
 			}
 			
 			/*
 			 * Save groundArray's content
 			 */
 			PrintWriter groundArrayOut = new PrintWriter("/data/data/edu.crabium.android.life/groundArray");
 			groundArrayOut.println(groundArray.length);
 			for(int i = 0; i < groundArray.length; i ++){
				groundArrayOut.println(groundArray[i].toString());
			}
 			
 			/*
 			 * Save other variables content
 			 */
 			SharedPreferences settings = context.getSharedPreferences("Life", 0);
 			SharedPreferences.Editor editor = settings.edit();
 			
 			editor.putInt("heroPositionX",heroPositionX);
 			editor.putInt("heroPositionY",heroPositionY);
 			editor.putInt("heroSpeedX",heroSpeedX);
 			editor.putInt("heroSpeedY",heroSpeedY);
 			editor.putInt("offsetMod",offsetMod);
 			editor.putInt("traveledPixels", traveledPixels);
 			editor.putInt("offsetArrayIndex", offsetArrayIndex);
 			
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
 	
 	/**
 	 * Return surfaceViewThread's current state
 	 * @return current thread state
 	 */
 	public ThreadState getThreadState(){
 		return threadState;
 	}
 	
 	/**
 	 * Return the surfaceViewThread of SurfaceView
 	 * @return surfaceViewThread
 	 */
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

	/** 
	 * Set screen height
	 * @param height
	 */
	public void setHeight(int height){
		SCREEN_HEIGHT = height;
	}
	
	/**
	 * Set screen width
	 * @param width
	 */
	public void setWidth(int width){
		SCREEN_WIDTH = width;
	}
	
	/**
	 * Set brick's height
	 * @param height
	 */
	public void setBrickHeight(int height){
		BRICK_HEIGHT = height;
	}
	
	/**
	 * Set brick's width
	 * @param width
	 */
	public void setBrickWidth(int width){
		BRICK_WIDTH = width;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		/*
		 * If game is stopped or is in game over animation, and restart / cancel button is tapped,
		 * then restart game / show home screen.
		 */
		if(event.getPointerCount() == 1 && (threadState == ThreadState.STOPPED || threadState == ThreadState.STOPPING) 
			&& event.getAction() == MotionEvent.ACTION_DOWN){
			if(restartDestRect.contains(event.getX(), event.getY())){
				resetVariables();
				thread = new LifeSurfaceViewThread(holder, context);
				threadState = ThreadState.RUNNING;
				thread.start();
				
				return true;
			}
			else if(cancelDestRect.contains(event.getX(), event.getY())){
				resetVariables();
				displayHome();
				
				threadState = ThreadState.READY;
				return true;
			}
		}
		/* If game is running and pause button is tapped, then set current thread state to PAUSED */
		else if(event.getPointerCount() == 1 && threadState == ThreadState.RUNNING && event.getAction() == MotionEvent.ACTION_DOWN){
			if(pauseButtonRectF.contains(event.getX(), event.getY())){
				threadState = ThreadState.PAUSED;
			}
		}
		/* 
		 * If game is paused and pause button is tapped, then set current thread state to RUNNING,
		 * and notify surfaceViewThread to continue
		 */
		else if(event.getPointerCount() == 1 && threadState == ThreadState.PAUSED && event.getAction() == MotionEvent.ACTION_DOWN){
			if(pauseButtonRectF.contains(event.getX(), event.getY())){
				threadState = ThreadState.RUNNING;
				synchronized(ted){
					ted.notify();
				}
			}
		}
		/*
		 * If game is ready and screen is tappd, start game
		 */
		else if(event.getPointerCount() == 1 && threadState == ThreadState.READY && event.getAction() == MotionEvent.ACTION_DOWN){
			onStart();
		}
		
		if(thread == null)
			return true;
		
		/*
		 * In other circumstances, if game is RUNNING, transfer control to surfaceViewThread's onTouchEvent
		 */
		if(threadState == ThreadState.RUNNING)
			return thread.onTouchEvent(event);
		
		return true;
	}
	
	/**
	 * Set variables to 0, null, etc, and read them from resources again
	 */
	private void resetVariables(){
		groundArray = null;
		brickArray = null;
		
		heroPositionX = 2 * BRICK_WIDTH;
		heroPositionY = 2 * BRICK_HEIGHT;
		heroSpeedX = INITIAL_SPEED;
		traveledPixels = 0;
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

		poolCoolingDistance = SCREEN_WIDTH / BRICK_WIDTH;
		coneCoolingDistance = SCREEN_WIDTH / BRICK_WIDTH;
		poolRemaining = 0;
		
		initializeVariables(context);
		initializeStage();
	}
	
	/** 
	 * Shift from sky to ground, and start thread
	 */
	private void onStart() {
		threadState = ThreadState.SHIFTING;
		
		for(int i = 0; i <= SCREEN_HEIGHT/2; i += GAME_OVER_SCREEN_SHIFT_SPPED){
			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(BACK_GROUND_COLOR);
			Rect srcRect = new Rect(0,0,SCREEN_WIDTH, SCREEN_HEIGHT/2 + i);
			RectF destRect = new RectF(0, SCREEN_HEIGHT/2 - i , SCREEN_WIDTH, SCREEN_HEIGHT);
			canvas.drawBitmap(stageWithBricks, srcRect, destRect, null);
			holder.unlockCanvasAndPost(canvas);
		}
		
		thread = new LifeSurfaceViewThread(holder, context);
		threadState = ThreadState.RUNNING;
		thread.start();
	}

	/**
	 * Draw background on the stage, also create and initiate brickArray, groundArray 
	 * if necessary
	 */
	private void initializeStage() {
		if(brickArray == null){
			brickArray = new Brick[BRICK_NUMBER_X][BRICK_NUMBER_Y];
			for(int i = 0; i < brickArray.length; i++)
				for(int j = 0; j < brickArray[i].length; j++)
					brickArray[i][j] = Brick.NONE;
		}
		
		// Generate random brick
		prevBrickEnd = generateRandomBrick(prevBrickEnd, brickArray, 0);

		Canvas canvas = new Canvas(stage);
		
		// Draw pillar and shade
		prevBackPillarEnd = drawBackPillarAndShade(	canvas, prevBackPillarEnd, stage.getWidth(), speed);
		
		int prevPillarEndTmp = prevPillarEnd;
		
		// Draw pillar
		prevPillarEnd = drawPillar(canvas, (prevPillarEnd == 0) ? PILLAR_WIDTH / 2 : prevPillarEnd , prevBackPillarEnd, speed);
		
		// Generate random ground, if necessary
		if(groundArray == null){
			groundArray = new Ground[(int) (stage.getWidth() / BRICK_WIDTH)];
			for(int i = 0; i < groundArray.length; i++)
				groundArray[i] = Ground.NONE;

			generateRandomGround(groundArray, prevPillarEnd - lastPillarOffset, 0);
		}
		
		// Draw cloud and shade
		prevCloudEnd = drawCloudAndShade(canvas, prevCloudEnd, prevBackPillarEnd, speed);

		// Draw ground
		drawGround(canvas, prevPillarEnd - lastPillarOffset, 0);
		
		// Draw grass
		prevGrassEnd = drawGrass(canvas, prevGrassEnd, prevPillarEndTmp, speed);
		
		prevDarkCloudEnd = prevCloudEnd;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	/**
	 * Called if surface is created.
	 * Initialize variables and stage
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		BRICK_WIDTH = (int) (SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.brick_width_ratio)));
		
		BRICK_HEIGHT = (int) (SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.brick_height_ratio)));
		
		if(BRICK_HEIGHT > BRICK_WIDTH)
			BRICK_HEIGHT = BRICK_WIDTH;
		
		/*
		 * Make sure pool and cone won't shown in the first screen, or the game
		 * will be annoying
		 */
		poolCoolingDistance = SCREEN_WIDTH / BRICK_WIDTH;
		coneCoolingDistance = SCREEN_WIDTH / BRICK_WIDTH;
		poolRemaining = 0;
		
		initializeVariables(context);
		initializeStage();
		
		/*
		 * If the game is resumed from pausing, just start game immediately
		 */
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
	
	//TODO: needs beautifying

	Paint textPaint = new Paint();
	
	/**
	 * Display home screen, draw 'Uniform Motion', 'Life', etc.
	 */
	private void displayHome() {
		Canvas canvas = new Canvas(stageWithBricks);
		RectF stageRectF = new RectF( 0, 0, stage.getWidth(), stage.getHeight());			
		canvas.drawBitmap(stage, null, stageRectF, null);
		
		synchronized(brickArray){
			drawBrickShade(canvas, 0, offsetMod);
		}
		
		drawHeroShade(canvas, speed);
		
		synchronized(brickArray){
			drawBrick(canvas, 0, offsetMod);
		}
		
		drawHero(canvas);
		
		canvas = holder.lockCanvas();
		canvas.drawColor(BACK_GROUND_COLOR);
		Rect srcRect = new Rect(0,0,SCREEN_WIDTH, SCREEN_HEIGHT/2);
		RectF destRect = new RectF(0, SCREEN_HEIGHT/2, SCREEN_WIDTH, SCREEN_HEIGHT);
		
		canvas.drawBitmap(stageWithBricks, srcRect, destRect, null);
		
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(SCREEN_WIDTH / 10);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(Color.WHITE);
		canvas.drawText("Uniform Motion", SCREEN_WIDTH/2, SCREEN_HEIGHT/3, textPaint);
		
		textPaint.setTextSize(SCREEN_WIDTH / 6);
		canvas.drawText("Life", SCREEN_WIDTH/2, SCREEN_HEIGHT*2/3, textPaint);
		holder.unlockCanvasAndPost(canvas);
		
	}

	/**
	 * Generate random bricks in brickArray
	 * @param start
	 * @param array
	 * @param offset
	 * @return ending position
	 */
	private int generateRandomBrick(int start, Brick[][] array, int offset) {
		int end = start;
		lastBrickOffset += offset;
		
		// Used to indicate whether random brick should be drawn
		boolean randomBrickOn = false;
		int randomBricksRemain = 0;
		
		start = (int) (Math.floor(start - lastBrickOffset) / BRICK_WIDTH);
		for(int i = start; i < array.length - RANDOM_BRICK_COUNT_MAX; i++){
			if(randomBrickOn){
				lastBrickOffset = 0;
				
				randomBricksRemain = RANDOM_BRICK_COUNT_MIN + 
					rand.nextInt(RANDOM_BRICK_COUNT_MAX - RANDOM_BRICK_COUNT_MIN);
				
				int randomBrickHeight = SCREEN_HEIGHT - (int) (RANDOM_BRICK_HEIGHT_MIN*SCREEN_HEIGHT +
					rand.nextInt((int)(RANDOM_BRICK_HEIGHT_MAX * SCREEN_HEIGHT - 
						RANDOM_BRICK_HEIGHT_MIN* SCREEN_HEIGHT))) ;
				
				int randomBrickBlockHeight = (int) (randomBrickHeight / BRICK_HEIGHT);

				/*
				 * Generate a pattern, use the following bit operation to make sure
				 * one brown brick will be in these random bricks 
				 * 
				 * 0: yellow brick
				 * 1: brown brick
				 */
				String brickPattern = Integer.toBinaryString((1 << rand.nextInt(randomBricksRemain)) + (1 << (randomBricksRemain))) + "0";
				brickPattern = brickPattern.substring(1);
				
				for(int j = 0; j < randomBricksRemain; j++){
					if( (i+j) < 0 || (i+j) >= array.length)
						continue;
					array[i + j][randomBrickBlockHeight] = 
						Integer.valueOf(brickPattern.substring(j,j+1)) == 1 ? Brick.BROWN : Brick.YELLOW;
					
					/*
					 * Generate random gadgets above brown brick
					 */
					if( array[i + j][randomBrickBlockHeight-1] == Brick.NONE &&
						array[i + j][randomBrickBlockHeight] == Brick.BROWN	&& 
					    rand.nextDouble() > GADGET_THRESHOLD){
						
						double tmp = rand.nextDouble();
						if(tmp > PASSPORT_THRESHOLD)
							array[i + j][randomBrickBlockHeight-1] = Brick.PASSPORT;
						else if(tmp > DUCKY_THRESHOLD)
							array[i + j][randomBrickBlockHeight-1] = Brick.DUCKY;
						else if(tmp > UMBRELLA_THRESHOLD)
							array[i + j][randomBrickBlockHeight-1] = Brick.UMBRELLA;
					}
				}
				
				end = (int) ((i + randomBricksRemain + 1) * BRICK_WIDTH);
				randomBrickOn = false;
				i+= randomBricksRemain / 2;
				
			}else{
				if(rand.nextDouble() >= RANDOM_BRICK_THRESHOLD){
					randomBrickOn = true;
				}
			}
		}
		
		return end;
	}
	
	/**
	 * Record the time start using ducky, in millisecond.
	 * Used to decide when the protection should be invalidated
	 */
	private double startUsingDuckyTimeMillis;
	

	Paint duckPaint = new Paint();
	
	/**
	 * Draw a countdown for ducky 
	 * @param canvas
	 */
	private void drawDuckCounter(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		if(currentTime - startUsingDuckyTimeMillis > DUCKY_EFFECTIVE_DURATION_MILLIS){
			usingDuck = false;
		}
		else{
			double usedTimeRatio = (currentTime - startUsingDuckyTimeMillis) / DUCKY_EFFECTIVE_DURATION_MILLIS * 360;
			duckPaint.setAntiAlias(true);
			
			duckPaint.setStyle(Style.STROKE);
			duckPaint.setColor(Color.BLACK);
			duckPaint.setStrokeWidth(3);
			canvas.drawCircle(9*BRICK_WIDTH, SCREEN_HEIGHT/10  , BRICK_WIDTH + 2, duckPaint);

			duckPaint.setStyle(Style.FILL_AND_STROKE);
			duckPaint.setColor(Color.YELLOW);
			canvas.drawCircle(9*BRICK_WIDTH, SCREEN_HEIGHT/10  , BRICK_WIDTH, duckPaint);
			duckPaint.setColor(Color.GRAY);
			canvas.drawArc(
					new RectF(8*BRICK_WIDTH, SCREEN_HEIGHT/10 - BRICK_WIDTH, 10*BRICK_WIDTH, SCREEN_HEIGHT/10 + BRICK_WIDTH), 
					0, (float)usedTimeRatio, true, duckPaint);
		}
		
	}
	
	/**
	 * Record the time start using umbrella, in millisecond.
	 * Used to decide when the protection should be invalidated
	 */
	private double startUsingUmbrellaTimeMillis;
	
	Paint umbrellaCounterPaint = new Paint();
	
	/**
	 * Draw a countdown for umbrella
	 * @param canvas
	 */
	private void drawUmbrellaCounter(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		if(currentTime - startUsingUmbrellaTimeMillis > UMBRELLA_EFFECTIVE_DURATION_MILLIS){
			usingUmbrella = false;
		}
		else{
			double usedTimeRatio = (currentTime - startUsingUmbrellaTimeMillis) / UMBRELLA_EFFECTIVE_DURATION_MILLIS * 360;
			umbrellaCounterPaint.setAntiAlias(true);
			
			umbrellaCounterPaint.setStyle(Style.STROKE);
			umbrellaCounterPaint.setColor(Color.BLACK);
			umbrellaCounterPaint.setStrokeWidth(3);
			canvas.drawCircle(11*BRICK_WIDTH + BRICK_WIDTH/3, SCREEN_HEIGHT/10, BRICK_WIDTH + 2, umbrellaCounterPaint);

			umbrellaCounterPaint.setStyle(Style.FILL_AND_STROKE);
			umbrellaCounterPaint.setColor(POOL_SECTION_COLOR);
			canvas.drawCircle(11*BRICK_WIDTH + BRICK_WIDTH/3, SCREEN_HEIGHT/10, BRICK_WIDTH, umbrellaCounterPaint);
			umbrellaCounterPaint.setColor(Color.GRAY);
			canvas.drawArc(
					new RectF(10*BRICK_WIDTH + BRICK_WIDTH/3, SCREEN_HEIGHT/10 - BRICK_WIDTH, 12*BRICK_WIDTH + BRICK_WIDTH/3, SCREEN_HEIGHT/10 + BRICK_WIDTH), 
					0, (float)usedTimeRatio, true, umbrellaCounterPaint);
		}
		
	}

	/**
	 * Record the time start using passport, in millisecond.
	 * Used to decide when the protection should be invalidated
	 */
	private double startUsingPassportTime;

	Paint passportPaint = new Paint();

	/**
	 * Draw a countdown for passport
	 * @param canvas
	 */
	private void drawPassportCounter(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		if(currentTime - startUsingPassportTime > PASSPORT_EFFECTIVE_DURATION_MILLIS){
			usingPassport = false;
		}
		else{
			double usedTimeRatio = (currentTime - startUsingPassportTime) / PASSPORT_EFFECTIVE_DURATION_MILLIS * 360;
			passportPaint.setAntiAlias(true);
			
			passportPaint.setStyle(Style.STROKE);
			passportPaint.setColor(Color.BLACK);
			passportPaint.setStrokeWidth(3);
			canvas.drawCircle(13*BRICK_WIDTH + 2 * BRICK_WIDTH/3, SCREEN_HEIGHT/10, BRICK_WIDTH + 2, passportPaint);

			passportPaint.setStyle(Style.FILL_AND_STROKE);
			passportPaint.setColor(0xff85472b);
			canvas.drawCircle(13*BRICK_WIDTH + 2 * BRICK_WIDTH/3, SCREEN_HEIGHT/10, BRICK_WIDTH, passportPaint);
			passportPaint.setColor(Color.GRAY);
			canvas.drawArc(
					new RectF(12*BRICK_WIDTH + 2 * BRICK_WIDTH/3, SCREEN_HEIGHT/10 - BRICK_WIDTH, 14*BRICK_WIDTH + 2 * BRICK_WIDTH/3, SCREEN_HEIGHT/10 + BRICK_WIDTH), 
					0, (float)usedTimeRatio, true, passportPaint);
		}
		
	}
	
	/**
	 * Return the distance have travled so far, calculated by traveldPixels.
	 * @return traveld distance
	 */
	private int getDistance(){
		return traveledPixels / 4;
	}
	
	/*
	 * Rectangles that specify available gadgets' drawing area
	 */
	private RectF duckyCountRectF;
	private RectF umbrellaCountRectF;
	private RectF passportCountRectF;
	private RectF availableBrickCountRectF;
	private RectF availableSpringboardCountRectF;
		
	/*
	 * Text size for gadgets' count
	 */
	private int gameInfoTextSize;

	Paint distancePaint = new Paint();
	Paint duckyPaint = new Paint();
	Paint umbrellaCountPaint = new Paint();
	Paint passportCountPaint = new Paint();
	Paint springboardPaint = new Paint();
	Paint availableBrickPaint = new Paint();
	
	//TODO needs beautifying
	/**
	 * Display available resources
	 */
	private void drawGameInfo(Canvas canvas) {
		// Display distance
		distancePaint.setAntiAlias(true);
		distancePaint.setColor(Color.WHITE);
		distancePaint.setTextSize(SCREEN_WIDTH/20);
		distancePaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(Integer.toString(getDistance()), SCREEN_HEIGHT/10 - SCREEN_WIDTH/20, SCREEN_HEIGHT/10, distancePaint);
		
		// A loop that decide the largest text size that can be put in the drawing area
		if(gameInfoTextSize == 0){
			Paint tmpPaint = new Paint();
			tmpPaint.setTextAlign(Align.LEFT);
			tmpPaint.setTypeface(Typeface.MONOSPACE);
			for(int size = 1;; size++){
				tmpPaint.setTextSize(size);
				Rect tmpRect = new Rect();
				tmpPaint.getTextBounds("00", 0, 2, tmpRect);
				
				if((tmpRect.width() > duckyCountRectF.width()*0.6) ||
				   (tmpRect.height() > duckyCountRectF.height())){
					gameInfoTextSize = size - 1;
					break;
				}
			}
		}
		
		// Display ducky info
		RectF duckyRectF = new RectF(
			duckyCountRectF.left + duckyCountRectF.width()*(float)0.7, 
			duckyCountRectF.top + duckyCountRectF.height()/2, 
			duckyCountRectF.right, duckyCountRectF.bottom);
		canvas.drawBitmap(duckyBitmap, null, duckyRectF, null);
				
		duckyPaint.setAntiAlias(true);
		duckyPaint.setColor(Color.WHITE);
		duckyPaint.setTextSize(gameInfoTextSize);
		duckyPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(Integer.toString(duckyCount),
			duckyCountRectF.left, duckyCountRectF.bottom ,duckyPaint);
		
		// Display umbrella info
		RectF umbrellaRectF = new RectF(
			umbrellaCountRectF.left + umbrellaCountRectF.width()*(float)0.7, 
			umbrellaCountRectF.top + umbrellaCountRectF.height()/2, 
			umbrellaCountRectF.right, umbrellaCountRectF.bottom);
		canvas.drawBitmap(umbrellaBitmap, null, umbrellaRectF, null);
		
		umbrellaCountPaint.setAntiAlias(true);
		umbrellaCountPaint.setColor(Color.WHITE);
		umbrellaCountPaint.setTextSize(gameInfoTextSize);
		umbrellaCountPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(Integer.toString(umbrellaCount),
			umbrellaCountRectF.left, umbrellaCountRectF.bottom,	umbrellaCountPaint);
		
		// Display passport info
		RectF passportRectF = new RectF(
			passportCountRectF.left + passportCountRectF.width() *(float)0.7, 
			passportCountRectF.top + passportCountRectF.height()/2, 
			passportCountRectF.right, passportCountRectF.bottom);
		canvas.drawBitmap(passportBitmap, null, passportRectF, null);
		
		passportCountPaint.setAntiAlias(true);
		passportCountPaint.setColor(Color.WHITE);
		passportCountPaint.setTextSize(gameInfoTextSize);
		passportCountPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(Integer.toString(passportCount),
			passportCountRectF.left, passportCountRectF.bottom,	passportCountPaint);
		
		// Display available brick info
		RectF brickRectF = new RectF(
			availableBrickCountRectF.left, availableBrickCountRectF.top,
			availableBrickCountRectF.left + availableBrickCountRectF.width() / 2, availableBrickCountRectF.bottom);
		canvas.drawBitmap(yellowBrickBitmap, null, brickRectF, null);

		availableBrickPaint.setAntiAlias(true);
		availableBrickPaint.setColor(Color.WHITE);
		availableBrickPaint.setTextSize(gameInfoTextSize);
		availableBrickPaint.setTypeface(Typeface.MONOSPACE);
		
		canvas.drawText(Integer.toString(availableBrickCount),
			availableBrickCountRectF.left + availableBrickCountRectF.width()/2, 
			availableBrickCountRectF.bottom, availableBrickPaint);

		// Display available springboard info
		RectF springboardRectF = new RectF(
			availableSpringboardCountRectF.left, availableSpringboardCountRectF.top,
			availableSpringboardCountRectF.left + availableSpringboardCountRectF.width() / 2, availableSpringboardCountRectF.bottom);
		canvas.drawBitmap(springboardClosedBitmap, null, springboardRectF, null);
		
		springboardPaint.setAntiAlias(true);
		springboardPaint.setColor(Color.WHITE);
		springboardPaint.setTextSize(gameInfoTextSize);
		springboardPaint.setTypeface(Typeface.MONOSPACE);
		canvas.drawText(Long.toString(System.currentTimeMillis() - lastUpdate),
		//canvas.drawText(Integer.toString(availableSpringboardCount),
			availableSpringboardCountRectF.left + availableSpringboardCountRectF.width() / 2 , 
			availableSpringboardCountRectF.bottom, springboardPaint);
		
		lastUpdate = System.currentTimeMillis();
	}
	
	long lastUpdate;
	/* Record hero's x-brick-position in the previous update */
	private int lastHeroPositionBrickX;
	
	/* Record hero's y-brick-position in the previous update */
	private int lastHeroPositionBrickY;
	
	/* 
	 * Used to indicate whether hero is jumping, if it is set, some process
	 * such as judge whether to jump will be ignored.
	 */
	private boolean isJumping;
	
	/* The y accelaeration of the jumping, in pixels */
	private int jumpAccelerationY;
	
	/* Used to indicate how many updates is needed for the jumping */
	private int jumpStages = 8;
	
	/* Used to indicate the ratio of jump height and screen height */
	private double JUMP_Y_DISTANCE_RATIO;
	
	Paint rectPaint = new Paint();
	
	/** 
	 * Update hero's position  according to its current position
	 * and the bricks around him
	 * @param canvas
	 */
	private void updateHero(Canvas canvas){
		/*
		 * Calculate the brick position that hero is currently on
		 */
		int heroPositionBrickX = (int) ((heroPositionX ) / BRICK_WIDTH);
		int heroPositionBrickY = (int) ((SCREEN_HEIGHT - heroPositionY - BRICK_HEIGHT_OFFSET) / BRICK_HEIGHT );
		
		/* Detect gadgets before hero */
		detectGadget(heroPositionBrickX+1, heroPositionBrickY);
		
		if(isJumping){
			if(heroSpeedY > jumpAccelerationY){
				heroPositionY += heroSpeedY;
				heroSpeedY -= jumpAccelerationY;
				return;
			}
			else if(heroPositionY % BRICK_HEIGHT == 0)
				isJumping = false;
			/*
			 * If current y speed is less than jumpAccelerationY, and hero's y position
			 * is not BRICK_HEIGHT's multiple, then just add (heroPositionY % BRICK_HEIGHT)
			 * to make it BRICK_HEIGHT's multiple and quit jumping
			 */
			else{
				int left = (int) (BRICK_HEIGHT -  (heroPositionY % BRICK_HEIGHT));
				
				heroPositionY += left;
				isJumping = false;
				return;
			}
		}
		
		/*
		 * lastHeroPositionBrickX and lastHeroPositionBrickY are used to prevent
		 * the hero jumping up and down from a single brick for more than one time 
		 */
		if(offsetMod == 0){
			lastHeroPositionBrickX = lastHeroPositionBrickY = 0;
		}
		
		/*
		 * If a closed springboard is met, set related variables and start jumping
		 */
		if( heroPositionBrickY < brickArray[0].length -1 && 
			brickArray[heroPositionBrickX][heroPositionBrickY + 1] == Brick.SPRINGBOARD_CLOSED){
			heroPositionY += BRICK_HEIGHT;
			isJumping = true;
			brickArray[heroPositionBrickX][heroPositionBrickY] = Brick.SPRINGBOARD_OPENED_X1;
			brickArray[heroPositionBrickX][heroPositionBrickY + 1] = Brick.SPRINGBOARD_OPENED_X2;
			
			int jumpDistanceY = (int) ( JUMP_Y_DISTANCE_RATIO * SCREEN_HEIGHT);
			heroSpeedY = 2 * jumpDistanceY / jumpStages;
			jumpAccelerationY = (int) Math.ceil(heroSpeedY / jumpStages);
		}
		
		// Down
		if( ( heroPositionBrickY < ( Math.floor((SCREEN_HEIGHT - BRICK_HEIGHT_OFFSET) / BRICK_HEIGHT - 1 )) ) &&
				((brickArray[heroPositionBrickX][heroPositionBrickY+1] != Brick.YELLOW) &&
				( brickArray[heroPositionBrickX][heroPositionBrickY+1] != Brick.BROWN))){
			if( (heroPositionBrickX != lastHeroPositionBrickX) || (heroPositionBrickY + 1 != lastHeroPositionBrickY)){
				heroPositionY -= BRICK_HEIGHT;
				lastHeroPositionBrickX = heroPositionBrickX;
				lastHeroPositionBrickY = heroPositionBrickY;
			}
			
			/* detect gadget below hero */
			detectGadget(heroPositionBrickX, heroPositionBrickY+1);
		}
		// Up
		else if ( (heroPositionBrickY > 1) && (brickArray[heroPositionBrickX+1][heroPositionBrickY] != Brick.NONE) && 
				( ( brickArray[heroPositionBrickX+1][heroPositionBrickY-1] != Brick.YELLOW) &&
				( brickArray[heroPositionBrickX+1][heroPositionBrickY-1] != Brick.BROWN) )){
			
			heroPositionY += BRICK_HEIGHT;
			lastHeroPositionBrickX = heroPositionBrickX;
			lastHeroPositionBrickY = heroPositionBrickY;
			
			/* detect gadget on the lower brick before hero */
			detectGadget(heroPositionBrickX+1, heroPositionBrickY-1);
		}
		/*
		 * If met pool, cone or dark cloud, and not using gadget related to them,
		 * show game over animation
		 */
		else if ( (heroPositionBrickY == brickArray[0].length -1) && !usingDuck && 
				( groundArray[heroPositionBrickX+1] == Ground.POOL_DRAWN ))
			onGameOver();
		else if ( (heroPositionBrickY == brickArray[0].length -1 )&& !usingPassport && 
				( groundArray[heroPositionBrickX+1] == Ground.CONE_DRAWN))
			onGameOver();
		else if( !usingUmbrella && (inCloud(heroPositionBrickX, heroPositionBrickY-3) || 
				 inCloud(heroPositionBrickX, heroPositionBrickY-1) || inCloud(heroPositionBrickX, heroPositionBrickY-2)))
			onGameOver();
		
		/* A white box, used for debugging */
//		rectPaint.setColor(Color.WHITE);
//		canvas.drawRect(
//				(float)(heroPositionBrickX*BRICK_WIDTH - offsetMod), 
//				(float)(heroPositionBrickY*BRICK_HEIGHT+BRICK_HEIGHT_OFFSET), 
//				(float)((heroPositionBrickX+1)*BRICK_WIDTH - offsetMod), 
//				(float)((heroPositionBrickY+1)*BRICK_HEIGHT+BRICK_HEIGHT_OFFSET), 
//				rectPaint);
	}
	
	/** 
	 * Detect gadget in specified position, is there is,
	 * increase its count and clear that place
	 * @param x
	 * @param y
	 */
	private void detectGadget(int x, int y){
		if(brickArray[x][y] == Brick.DUCKY){
			duckyCount ++;
			brickArray[x][y]  = Brick.NONE;
		}else if(brickArray[x][y]  == Brick.PASSPORT){
			passportCount ++;
			brickArray[x][y]  = Brick.NONE;
		}else if(brickArray[x][y]  == Brick.UMBRELLA){
			umbrellaCount ++;
			brickArray[x][y]  = Brick.NONE;
		} 
	}
	
	/**
	 * Show whether hero's is in the dark cloud
	 * @param x
	 * @param y
	 * @return
	 */
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
	
	/** Rectangle that specifies the drawing region of restart button */
	private RectF restartDestRect;
	
	/** Rectangle that specifies the drawing region of cancel button */
	private RectF cancelDestRect;
	
	/** Shows GAME OVER animation, some buttons and clear variables */
	private void onGameOver(){
		/*
		 * Reset variables
		 */
		heroSpeedX  = 0;
		speed = 0;
		threadState = ThreadState.STOPPING;
		
		/*
		 * Draw game over animation
		 */
		int gameOverScreenShift = SCREEN_HEIGHT;
		
		Paint distancePaint = new Paint();
		distancePaint.setAntiAlias(true);
		distancePaint.setColor(Color.WHITE);
		distancePaint.setTextSize(SCREEN_WIDTH / 8);
		
		Paint rankPaint = new Paint();
		rankPaint.setAntiAlias(true);
		rankPaint.setColor(Color.WHITE);
		rankPaint.setTextSize(SCREEN_WIDTH / 16);
		
		Paint toolPaint = new Paint();
		toolPaint.setAntiAlias(true);
		toolPaint.setColor(Color.WHITE);
		toolPaint.setTextSize(SCREEN_WIDTH / 16);
		
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
			(SCREEN_WIDTH / 6),
			(SCREEN_HEIGHT / 2),
			(float)(SCREEN_WIDTH / 6 + 1.5 * BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 2  + 1.5 * BRICK_HEIGHT)
			);
		
		RectF passportDestRect = new RectF(
			(float)(SCREEN_WIDTH / 6),
			(float)(SCREEN_HEIGHT / 2 + 2 * BRICK_HEIGHT),
			(float)(SCREEN_WIDTH / 6 + 1.5*BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 2  + 3.5*BRICK_HEIGHT)
			);
		
		RectF umbrellaDestRect = new RectF(
			(float)(SCREEN_WIDTH / 6),
			(float)(SCREEN_HEIGHT / 2 + 4 * BRICK_HEIGHT),
			(float)(SCREEN_WIDTH / 6 + 1.5*BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 2  + 5.5*BRICK_HEIGHT)
			);
		
		/*
		 * Put this round's game info into database and get its rank
		 */
		lifeDatabase.addScore(getDistance(), duckyCount, passportCount, umbrellaCount);
		int rank =  lifeDatabase.getRank(getDistance()) + 1;
		
		Canvas stageCanvas = new Canvas(stage);
		drawBrickShade(stageCanvas, 0, offsetMod);
		drawHeroShade(stageCanvas, offsetMod);
		drawBrick(stageCanvas, 0, offsetMod);
		drawHero(stageCanvas);

		for(int i = 0; i < gameOverScreenShift && threadState == ThreadState.STOPPING; i+= GAME_OVER_SCREEN_SHIFT_SPPED){
			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(BACK_GROUND_COLOR);
			Rect srcRect = new Rect(0, 0, SCREEN_WIDTH , SCREEN_HEIGHT - i);
			RectF destRect = new RectF(0, i, SCREEN_WIDTH, SCREEN_HEIGHT);
			canvas.drawBitmap(stage, srcRect, destRect, null);
			
			canvas.drawText(
					Integer.toString(getDistance()), 
				SCREEN_WIDTH/6, 
				SCREEN_HEIGHT/6 + SCREEN_WIDTH/12, 
				distancePaint);
			
			canvas.drawText("Rank: " + Integer.toString(rank), SCREEN_WIDTH/6, SCREEN_HEIGHT/6 + SCREEN_WIDTH/6, rankPaint);
			canvas.drawBitmap(duckyBitmap, null, duckyDestRect, null);
			canvas.drawBitmap(passportBitmap, null, passportDestRect, null);
			canvas.drawBitmap(umbrellaBitmap, null, umbrellaDestRect, null);
			
			canvas.drawText(
					Integer.toString(duckyCount), 
				(float)(SCREEN_WIDTH / 6 + 1.5*BRICK_WIDTH + 0.5*BRICK_WIDTH),
				(float)(SCREEN_HEIGHT / 2 + SCREEN_WIDTH / 16), 
				toolPaint
			);
			
			canvas.drawText(
					Integer.toString(passportCount),
				(float)(SCREEN_WIDTH / 6 + 1.5*BRICK_WIDTH + 0.5*BRICK_WIDTH),
				(float)(SCREEN_HEIGHT / 2 + 2 * BRICK_HEIGHT + SCREEN_WIDTH / 16),
				toolPaint
			);
			
			canvas.drawText(
					Integer.toString(umbrellaCount),
				(float)(SCREEN_WIDTH / 6 + 1.5*BRICK_WIDTH + 0.5*BRICK_WIDTH),
				(float)(SCREEN_HEIGHT / 2 + 4 * BRICK_HEIGHT + SCREEN_WIDTH / 16),
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
	
	/** Pixels have shifted since drawing last dark cloud */
	private int lastDarkCloudOffset;
	
	/** Dark cloud cooling distance */
	private int darkCloudCoolingDistance;
	
	//TODO add dark cloud cooling distance
	
	/** Generate dark cloud in brickArray */
	private int generateDarkCloud(int start, Brick[][] array, int offset) {
		int end = start;
		lastDarkCloudOffset += offset;
		
		if(offsetMod != 0)
			return end;

		darkCloudCoolingDistance --;
		if(darkCloudCoolingDistance > 0)
			return end;
		
		if(rand.nextDouble() < DARK_CLOUD_THRESHOLD)
			return end;
		
		start = Math.max(start - lastDarkCloudOffset, SCREEN_WIDTH);
		start = (int) (Math.floor(start) / BRICK_WIDTH);
		if(start < array.length - 3){
			lastDarkCloudOffset = 0;
			
			int darkCloudkHeight = SCREEN_HEIGHT - (int) (DARK_CLOUD_HEIGHT_MIN * SCREEN_HEIGHT +
				rand.nextInt((int)(DARK_CLOUD_HEIGHT_MAX * SCREEN_HEIGHT - 
					DARK_CLOUD_HEIGHT_MIN* SCREEN_HEIGHT))) ;
			
			int darkCloudBrickHeight = (int) (darkCloudkHeight / BRICK_HEIGHT);
			array[start + 0][darkCloudBrickHeight + 0] = Brick.DARK_CLOUD_X0Y0;
			array[start + 1][darkCloudBrickHeight + 0] = Brick.DARK_CLOUD_X1Y0;
			array[start + 2][darkCloudBrickHeight + 0] = Brick.DARK_CLOUD_X2Y0;
			array[start + 0][darkCloudBrickHeight + 1] = Brick.DARK_CLOUD_X0Y1;
			array[start + 1][darkCloudBrickHeight + 1] = Brick.DARK_CLOUD_X1Y1;
			array[start + 2][darkCloudBrickHeight + 1] = Brick.DARK_CLOUD_X2Y1;
			
			end = start*BRICK_WIDTH + (3 + 1)*BRICK_WIDTH;
			darkCloudCoolingDistance = INITIAL_DARK_CLOUD_COOLING_DISTANCE;
			
		}else
			return end;
		
		return end;
	}

	private Drawable cloud1Drawable;
	private Drawable cloud1ShadeDrawable;

	private Bitmap cloud1Bitmap;
	private Bitmap cloud1ShadeBitmap;
	
	Paint shadePaint = new Paint();
	
	/**
	 * Draw clouds and their shade on canvas
	 * @param canvas
	 * @param start
	 * @param end
	 * @param offset
	 * @return
	 */
	private int drawCloudAndShade(Canvas canvas, int start, int end, int offset) {
		lastCloudOffset += offset;
		int result = start;
		
		start = (int) (start - lastCloudOffset 
				+ CLOUD_WIDTH_MAX * CLOUD_SPACE_RATIO
				+ CLOUD_WIDTH_MIN * CLOUD_SPACE_RATIO * rand.nextDouble());
		
		for(int i = start; i< end; i += CLOUD_WIDTH_MAX * CLOUD_SPACE_RATIO){
			if(i + CLOUD_WIDTH_MIN*1.5 <= end){
				lastCloudOffset = 0;
				int cloudWidth = CLOUD_WIDTH_MIN + rand.nextInt(CLOUD_WIDTH_MAX - CLOUD_WIDTH_MIN);
				int cloudHeight = (int) (SCREEN_HEIGHT * 
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
				
				shadePaint.setAlpha(CLOUD_SHADE_ALPHA);
				canvas.drawBitmap(cloud1ShadeBitmap, null, shadeRectF, shadePaint);
				canvas.drawBitmap(cloud1Bitmap, null, cloudRectF, null);
				
				result = (int) (x2 - cloudWidth * CLOUD_SHADE_WIDTH_OFFSET_RATIO);
				i += CLOUD_WIDTH_MIN * CLOUD_SPACE_RATIO * rand.nextDouble();
			}
		}
		
		return result;
	}

	Paint pillarPaint = new Paint();
	
	/**
	 * Draw pillars on the canvas
	 * @param canvas
	 * @param start
	 * @param end
	 * @param offset
	 * @return
	 */
	private int drawPillar(Canvas canvas, int start, int end, int offset) {
		lastPillarOffset += offset;
		lastPillarOffsetTmp += offset;
		int result = start;
		
		pillarPaint.setAntiAlias(true);
		
		start = (int) (start + PILLAR_SPACE - lastPillarOffset);
		end = end - lastBackPillarOffset;
		for(int i = start; i < end; i += PILLAR_WIDTH){
			int pillarHeight = (int) ((SCREEN_HEIGHT* 0.5 * (PILLAR_HEIGHT_MIN + PILLAR_HEIGHT_MAX)) + 
				Math.floor(rand.nextGaussian()*10) / 10.0 * 0.3 *
				(SCREEN_HEIGHT * PILLAR_HEIGHT_MAX - SCREEN_HEIGHT * PILLAR_HEIGHT_MIN));
			pillarPaint.setColor(pillarColor[rand.nextInt(pillarColor.length)]);
			
			/*
			 * A pillar actually is one circle + one rectangle
			 */
			if( i + PILLAR_WIDTH <= end){
				pillarDrawn = true;
				lastPillarOffset = 0;
				canvas.drawCircle(i + PILLAR_WIDTH / 2, SCREEN_HEIGHT - pillarHeight, 
					PILLAR_WIDTH / 2, pillarPaint);
				canvas.drawRect(i, SCREEN_HEIGHT - pillarHeight, 
					i+PILLAR_WIDTH, SCREEN_HEIGHT,	pillarPaint);
				
				result = i+PILLAR_WIDTH;
			}
			
			i += PILLAR_SPACE;
		}
		
		return result;
	}

	Paint backPillarShadePaint = new Paint();
	Paint backPillarPaint = new Paint();
	
	/**
	 * Draw back pillars and their shades
	 * @param canvas
	 * @param start
	 * @param end
	 * @param offset Offset from last drawing position
	 * @return Ending position
	 */
	private int drawBackPillarAndShade(Canvas canvas, int start, int end, int offset) {
		lastBackPillarOffset += offset;
		
		int result = start;
		
		// Used to indicate whether at least pillar has been drawn
		boolean drawn = false;
		
		/*
		 * Draw back pillars' shade
		 */
		backPillarShadePaint.setAntiAlias(true);
		backPillarShadePaint.setColor(Color.BLACK);
		backPillarShadePaint.setAlpha(BACK_PILLAR_SHADE_ALPHA);
		
		if(previousPillarPosition != null)
			start = (int) (previousPillarPosition.getX() + BACK_PILLAR_WIDTH - lastBackPillarOffset 
				+ BACK_PILLAR_SPACE);
		else
			start = (int) (start - lastBackPillarOffset 
			    + BACK_PILLAR_SPACE);
		
		/* PillarPosition instances to store pillar positions */
		List<PillarPosition> backPillarPositionList = new ArrayList<PillarPosition>();
		
		/* 
		 * Generate pillar positions and stores then in backPillarPositionList,
		 * firstly, draw their shades.
		 */
		for(int i = start ; i < end; i += BACK_PILLAR_WIDTH){
			int backPillarHeight = (int)(SCREEN_HEIGHT * BACK_PILLAR_HEIGHT_MIN) + 
				rand.nextInt(
				(int)(SCREEN_HEIGHT * BACK_PILLAR_HEIGHT_MAX 
					- SCREEN_HEIGHT * BACK_PILLAR_HEIGHT_MIN));
			
			if( i + BACK_PILLAR_WIDTH <= end){
				backPillarPositionList.add(new PillarPosition(i, backPillarHeight));
				
				/*
				 * Set drawn to true, tell following prcoess that there's
				 * room for at least one pillar and its shade has been
				 * drawn
				 */
				drawn = true;
				
				double x1 = i - BACK_PILLAR_SHADE_WIDTH_OFFSET;
				double y1 = SCREEN_HEIGHT - (backPillarHeight - BACK_PILLAR_SHADE_HEIGHT_OFFSET + BACK_PILLAR_WIDTH / 2);
				RectF rectF = new RectF(
					(float) (x1),
					(float) (y1), 
					(float)(x1 + BACK_PILLAR_WIDTH),
					(float)SCREEN_HEIGHT
					);
			
				canvas.drawRoundRect(rectF, BACK_PILLAR_WIDTH / 2, BACK_PILLAR_WIDTH / 2, backPillarShadePaint);
			}
			
			i += BACK_PILLAR_SPACE;
		}
		
		// If there is not enough room for at least one pillar, just quit
		if(drawn == false){
			return result;
		}
		
		backPillarPaint.setAlpha(255);
		backPillarPaint.setAntiAlias(true);
		
		/*
		 * If previousPillarPosition is not null, we can make sure that the last back pillar
		 * of the previous process is save in it and has not been drawn, so draw it first
		 */
		if(previousPillarPosition != null){
			backPillarPaint.setColor(backPillarColor[rand.nextInt(backPillarColor.length)]);
			canvas.drawCircle(
				(float)(previousPillarPosition.getX()+BACK_PILLAR_WIDTH/2 - lastBackPillarOffset), 
				(float)(SCREEN_HEIGHT - previousPillarPosition.getY()), 
				BACK_PILLAR_WIDTH / 2, 
				backPillarPaint);
			canvas.drawRect(
				(float)(previousPillarPosition.getX() - lastBackPillarOffset ), 
				(float)(SCREEN_HEIGHT - previousPillarPosition.getY()), 
				(float) (previousPillarPosition.getX()+BACK_PILLAR_WIDTH - lastBackPillarOffset ), 
				SCREEN_HEIGHT, 
				backPillarPaint);
		}
		
		/*
		 * Retrieve back pillar positions from backPillarPositionList and draw them
		 * (except the last one
		 */
		for(int i = 0; i < backPillarPositionList.size() - 1; i ++){
			PillarPosition pillarPosition = backPillarPositionList.get(i);
			
			backPillarPaint.setColor(backPillarColor[rand.nextInt(backPillarColor.length)]);
			
			canvas.drawCircle(
				(float)(pillarPosition.getX()+BACK_PILLAR_WIDTH/2), 
				(float)(SCREEN_HEIGHT - pillarPosition.getY()), 
				BACK_PILLAR_WIDTH/2, 
				backPillarPaint);
			canvas.drawRect(
				(float)(pillarPosition.getX()), 
				(float)(SCREEN_HEIGHT - pillarPosition.getY()), 
				(float) (pillarPosition.getX() + BACK_PILLAR_WIDTH), 
				SCREEN_HEIGHT, 
				backPillarPaint);
			
			result = (int) (pillarPosition.getX() + BACK_PILLAR_WIDTH);
		}
		
		/*
		 * Save the last back pillar's position in previousPillarPosition, draw it
		 * next time
		 */
		previousPillarPosition = backPillarPositionList.get(backPillarPositionList.size() -1);
		
		lastBackPillarOffset = 0;
		return result;
	}
	
	/** 
	 * Initialize variables from values got from resources
	 * or other places
	 * @param context
	 */
	private void initializeVariables(Context context){
		heroPositionX = Integer.valueOf(context.getResources().
			getString(R.string.hero_position_x));
		
		heroPositionY = Integer.valueOf(context.getResources().
			getString(R.string.hero_position_y));
		
		BROWN_BRICK_THRESHOLD = Double.valueOf(
			context.getResources().
				getString(R.string.brown_brick_threshold));
		
		PILLAR_WIDTH = (int) (SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.pillar_width_ratio)));
		
		PILLAR_HEIGHT_MAX = Double.valueOf(
			context.getResources().
				getString(R.string.pillar_height_max_ratio));
		
		PILLAR_HEIGHT_MIN = Double.valueOf(
			context.getResources().
				getString(R.string.pillar_height_min_ratio));
		
		PILLAR_SPACE = SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.pillar_space_ratio));
		
		BACK_PILLAR_WIDTH = (int) (SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.back_pillar_width_ratio)));
		
		BACK_PILLAR_SPACE = SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.back_pillar_space_ratio));
		
		BACK_PILLAR_HEIGHT_MAX = Double.valueOf(
			context.getResources().
				getString(R.string.back_pillar_height_max));
		BACK_PILLAR_HEIGHT_MIN = Double.valueOf(
			context.getResources().
				getString(R.string.back_pillar_height_min));
		
		BACK_PILLAR_SHADE_WIDTH_OFFSET = 
			BACK_PILLAR_WIDTH * Double.valueOf(context.getResources().
				getString(R.string.back_pillar_shade_width_ratio));
		
		/*
		 *  Yes, use BACK_PILLAR_WIDTH, not BACK_PILLAR_HEIGHT,
		 *  although we're calculating height offset
		 */
		
		BACK_PILLAR_SHADE_HEIGHT_OFFSET = 
			BACK_PILLAR_WIDTH * Double.valueOf(context.getResources().
				getString(R.string.back_pillar_shade_height_ratio));
		
		
		HERO_WIDTH = (int) (SCREEN_WIDTH * Double.valueOf(context.getResources().
			getString(R.string.hero_width_ratio)));
		
		HERO_HEIGHT = HERO_WIDTH * 2;
		
		HERO_SHADE_HEIGHT_OFFSET = 
			HERO_HEIGHT * Double.valueOf((context.getResources().
				getString(R.string.hero_shade_height_offset_ratio)));
		
		HERO_SHADE_WIDTH_OFFSET = 
			HERO_WIDTH * Double.valueOf(context.getResources().
				getString(R.string.hero_shade_width_offset_ratio));
		
		CLOUD_HEIGHT_MAX = Double.valueOf(
			context.getResources().
				getString(R.string.cloud_height_max));
		CLOUD_HEIGHT_MIN = Double.valueOf(
			context.getResources().
				getString(R.string.cloud_height_min));
		CLOUD_WIDTH_MAX = (int) (SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.cloud_width_max_ratio)));
		CLOUD_WIDTH_MIN = (int) (SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.cloud_width_min_ratio)));
		CLOUD_SPACE_RATIO = Double.valueOf(
			context.getResources().
				getString(R.string.cloud_space_ratio));
		
		CLOUD_SHADE_HEIGHT_OFFSET_RATIO = Double.valueOf(
			context.getResources().
				getString(R.string.cloud_shade_height_offset_ratio));
		
		CLOUD_SHADE_WIDTH_OFFSET_RATIO = Double.valueOf(
			context.getResources().
				getString(R.string.cloud_shade_width_offset_ratio));
		

		DARK_CLOUD_THRESHOLD =  Double.valueOf(
			context.getResources().
				getString(R.string.dark_cloud_threshold));
		
		DARK_CLOUD_HEIGHT_MIN =  Double.valueOf(
			context.getResources().
				getString(R.string.dark_cloud_height_min));
		
		DARK_CLOUD_HEIGHT_MAX =  Double.valueOf(
			context.getResources().
				getString(R.string.dark_cloud_height_max));
		
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
		
		GROUND_HEIGHT = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.ground_height_ratio));
		
		GROUND_SECTION_HEIGHT = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.ground_section_ratio));
		
		SUBTERRANEAN_HEIGHT = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.subterranean_ratio));
		
		GRASS_HEIGHT = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.grass_ratio));
		
		GRASS_SPACE = Double.valueOf(
			context.getResources().
				getString(R.string.grass_space));
		
		TREE_CROWN_WIDTH = SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.tree_crown_width_ratio));
		
		TREE_CROWN_HEIGHT = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.tree_crown_height_ratio));
		
		TRUNK_HEIGHT = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.trunk_height_ratio));
		
		TRUNK_WIDTH= SCREEN_WIDTH * Double.valueOf(
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
		
		BUSH_WIDTH = SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.bush_width_ratio));
		
		BUSH_HEIGHT_MAX = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.bush_height_max_ratio));
		
		BUSH_HEIGHT_MIN = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.bush_height_min_ratio));
		
		BUSH_HEIGHT_OFFSET_FROM_WALL = SCREEN_HEIGHT * Double.valueOf(
			context.getResources().
				getString(R.string.bush_height_offset_from_wall_ratio));
		
		BUSH_HEIGHT_OFFSET_FROM_BUSH = SCREEN_HEIGHT * Double.valueOf(
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
				
		POOL_WIDTH_MAX = SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.pool_width_max_ratio));
		
		POOL_WIDTH_MIN = SCREEN_WIDTH * Double.valueOf(
			context.getResources().
				getString(R.string.pool_width_min_ratio));
		
		POOL_THRESHOLD = Double.valueOf(
			context.getResources().
				getString(R.string.pool_threshold));
		
		POOL_SPACE_COUNT = Integer.valueOf(
			context.getResources().
				getString(R.string.pool_space_count));
	
		CONE_THRESHOLD = Double.valueOf(
			context.getResources().
				getString(R.string.cone_threshold));
		
		CONE_SPACE_COUNT = Integer.valueOf(
			context.getResources().
				getString(R.string.cone_space_count));
		
		GAME_OVER_SCREEN_SHIFT_SPPED = Integer.valueOf(
			context.getResources().
				getString(R.string.game_over_screen_shift_speed));
		
		SPRINGBOARD_INCREASE_INTERVAL_MILLIS = Integer.valueOf(
			context.getResources().
				getString(R.string.springboard_increase_interval_millis));
		
		GADGET_THRESHOLD = Double.valueOf(
			context.getResources().
				getString(R.string.gadget_threshold));
		DUCKY_THRESHOLD = Double.valueOf(
			context.getResources().
			getString(R.string.ducky_threshold));
		PASSPORT_THRESHOLD = Double.valueOf(
			context.getResources().
				getString(R.string.passport_threshold));
		UMBRELLA_THRESHOLD = Double.valueOf(
			context.getResources().
				getString(R.string.umbrella_threshold));
		
		DUCKY_EFFECTIVE_DURATION_MILLIS = Integer.valueOf(
			context.getResources().
				getString(R.string.ducky_effective_duration_millis));
		PASSPORT_EFFECTIVE_DURATION_MILLIS = Integer.valueOf(
			context.getResources().
				getString(R.string.passport_effective_duration_millis));
		UMBRELLA_EFFECTIVE_DURATION_MILLIS = Integer.valueOf(
			context.getResources().
				getString(R.string.umbrella_effective_duration_millis));
		
		JUMP_Y_DISTANCE_RATIO = Double.valueOf(
			context.getResources().
				getString(R.string.jump_y_distance_ratio));
		
		LIGHTNING_DURATION_MILLIS = Integer.valueOf(
			context.getResources().
				getString(R.string.lightning_duration_millis));
		
		LIGHTNING_INTERVAL_MILLIS = Integer.valueOf(
				context.getResources().
					getString(R.string.lightning_interval_millis));
			
		yellowBrickDrawable = context.getResources().getDrawable(R.drawable.tile_yellow);
		brownBrickDrawable = context.getResources().getDrawable(R.drawable.tile_brown);
		darkCloudDrawable = context.getResources().getDrawable(R.drawable.dark_cloud);
		lightningDrawable = context.getResources().getDrawable(R.drawable.lightning);
		rainDrawable = context.getResources().getDrawable(R.drawable.rain);
		passportDrawable = context.getResources().getDrawable(R.drawable.passport);
		duckyDrawable = context.getResources().getDrawable(R.drawable.ducky);
		umbrellaDrawable = context.getResources().getDrawable(R.drawable.umbrella);
		springboardClosedDrawable = context.getResources().getDrawable(R.drawable.springboard_closed);
		springboardOpened1Drawable = context.getResources().getDrawable(R.drawable.springboard_opened_1);
		springboardOpened2Drawable = context.getResources().getDrawable(R.drawable.springboard_opened_2);
				
		yellowBrickBitmap = ((BitmapDrawable)yellowBrickDrawable).getBitmap();
		brownBrickBitmap = ((BitmapDrawable)brownBrickDrawable).getBitmap();
		darkCloudBitmap = ((BitmapDrawable)darkCloudDrawable).getBitmap();
		lightningBitmap = ((BitmapDrawable)lightningDrawable).getBitmap();
		rainBitmap = ((BitmapDrawable)rainDrawable).getBitmap();
		passportBitmap = ((BitmapDrawable)passportDrawable).getBitmap();
		duckyBitmap = ((BitmapDrawable)duckyDrawable).getBitmap();
		umbrellaBitmap = ((BitmapDrawable)umbrellaDrawable).getBitmap();
		springboardClosedBitmap = ((BitmapDrawable)springboardClosedDrawable).getBitmap();
		springboardOpened1Bitmap = ((BitmapDrawable)springboardOpened1Drawable).getBitmap();
		springboardOpened2Bitmap = ((BitmapDrawable)springboardOpened2Drawable).getBitmap();
		
 		pauseButtonDrawable = context.getResources().getDrawable(R.drawable.pause_button);
 		pauseButtonBitmap = ((BitmapDrawable)pauseButtonDrawable).getBitmap();
 		
 		continueButtonDrawable = context.getResources().getDrawable(R.drawable.continue_button);
 		continueButtonBitmap = ((BitmapDrawable)continueButtonDrawable).getBitmap();
 		
 		cloud1Drawable = context.getResources().getDrawable(R.drawable.cloud1);
		cloud1ShadeDrawable = context.getResources().getDrawable(R.drawable.cloud1_shade);

		cloud1Bitmap = ((BitmapDrawable)cloud1Drawable).getBitmap();
		cloud1ShadeBitmap = ((BitmapDrawable)cloud1ShadeDrawable).getBitmap();

		bushDrawable = context.getResources().getDrawable(R.drawable.bush);
		bushBitmap = ((BitmapDrawable)bushDrawable).getBitmap();
		
		treeCrownShadeDrawable = context.getResources().getDrawable(R.drawable.tree_crown_shade);
		treeCrownShadeBitmap = ((BitmapDrawable)treeCrownShadeDrawable).getBitmap();
		
		treeCrownDrawable = context.getResources().getDrawable(R.drawable.tree_crown);
		treeCrownBitmap = ((BitmapDrawable)treeCrownDrawable).getBitmap();
		
		coneDrawable = context.getResources().getDrawable(R.drawable.cone);
		coneBitmap = ((BitmapDrawable)coneDrawable).getBitmap();
		

		heroShadeDrawable = context.getResources().getDrawable(R.drawable.hero_shade);
		heroShadeBitmap = ((BitmapDrawable)heroShadeDrawable).getBitmap();
		
		heroDrawable = context.getResources().getDrawable(R.drawable.hero);
		heroBitmap = ((BitmapDrawable)heroDrawable).getBitmap();
		
		brickShadeDrawable = context.getResources().getDrawable(R.drawable.shade);
		brickShadeBitmap = ((BitmapDrawable)brickShadeDrawable).getBitmap();
		/*
		 * Stage width should be wider than screen width, or there
		 * is not enough room for drawing background before showing
		 * it
		 */
		double stageWidth =
			SCREEN_WIDTH +
			RANDOM_BRICK_COUNT_MAX * BRICK_WIDTH +
			BACK_PILLAR_WIDTH * 2 + 
			PILLAR_WIDTH + BUSH_WIDTH * 3 + 
			TREE_CROWN_WIDTH + 
			GROUND_HEIGHT / Math.tan(Math.toRadians(TREE_SHADE_ANGLE));
		
		if(stage == null)
			stage = Bitmap.createBitmap(
				(int) stageWidth, 
				SCREEN_HEIGHT, 
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
		
		double availableHeight = SCREEN_HEIGHT - (SUBTERRANEAN_HEIGHT + GROUND_SECTION_HEIGHT);
		double bricksHeight = Math.floor(availableHeight / BRICK_HEIGHT)*BRICK_HEIGHT;
		
		BRICK_HEIGHT_OFFSET = availableHeight - bricksHeight;
		
		BRICK_NUMBER_X = (int) ( stage.getWidth() / BRICK_WIDTH);
		BRICK_NUMBER_Y = (int) ( ( stage.getHeight() - BRICK_HEIGHT_OFFSET ) / BRICK_HEIGHT );
		
		restartDestRect = new RectF(
			(float)(SCREEN_WIDTH / 3 *2 + SCREEN_WIDTH / 3 / 3),
			(float)(SCREEN_HEIGHT / 3 + (SCREEN_HEIGHT/3 - 2*BRICK_HEIGHT) / 2),
			(float)(SCREEN_WIDTH / 3 *2 + SCREEN_WIDTH / 3 / 3 + 2 * BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 3 + (SCREEN_HEIGHT/3 - 2*BRICK_HEIGHT) / 2 + 2 * BRICK_WIDTH)
			);
		
		cancelDestRect = new RectF(
			(float)(SCREEN_WIDTH / 3 *2 + SCREEN_WIDTH / 3 / 3),
			(float)(SCREEN_HEIGHT / 3 * 2 + (SCREEN_HEIGHT/3 - 2*BRICK_HEIGHT) / 2),
			(float)(SCREEN_WIDTH / 3 * 2 + SCREEN_WIDTH / 3 / 3 + 2 * BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 3 * 2+ (SCREEN_HEIGHT/3 - 2*BRICK_HEIGHT) / 2 + 2 * BRICK_WIDTH)
			);
			
		duckyCountRectF = new RectF(
			(float)(SCREEN_HEIGHT/10 - SCREEN_WIDTH/20), 
			(float)(SCREEN_HEIGHT / 10 + SCREEN_HEIGHT / 40),
			(float)(SCREEN_HEIGHT/10 - SCREEN_WIDTH/20 + 1.6 * BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 10  + SCREEN_HEIGHT / 40 + 0.8 * BRICK_HEIGHT));
		
		umbrellaCountRectF = new RectF(
			(float)(SCREEN_HEIGHT/10 - SCREEN_WIDTH/20 + 2 * BRICK_WIDTH), 
			(float)(SCREEN_HEIGHT / 10 + SCREEN_HEIGHT / 40),
			(float)(SCREEN_HEIGHT/10 - SCREEN_WIDTH/20 + 1.6 * BRICK_WIDTH + 2 * BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 10  + SCREEN_HEIGHT / 40 + 0.8 * BRICK_HEIGHT));
				
		passportCountRectF = new RectF(
			(float)(SCREEN_HEIGHT/10 - SCREEN_WIDTH/20 + 4 * BRICK_WIDTH), 
			(float)(SCREEN_HEIGHT / 10 + SCREEN_HEIGHT / 40),
			(float)(SCREEN_HEIGHT/10 - SCREEN_WIDTH/20 + 1.6 * BRICK_WIDTH + 4 * BRICK_WIDTH),
			(float)(SCREEN_HEIGHT / 10  + SCREEN_HEIGHT / 40 + 0.8 * BRICK_HEIGHT));
		
		availableBrickCountRectF = new RectF(
			(float)(SCREEN_WIDTH * 6 / 8 ), 
			(float)(SCREEN_HEIGHT / 30),
			(float)(SCREEN_WIDTH * 6 / 8 + 1.6 * BRICK_WIDTH ),
			(float)(SCREEN_HEIGHT / 30 + 0.8 * BRICK_HEIGHT));
				
		availableSpringboardCountRectF = new RectF(
			(float)(SCREEN_WIDTH * 6 / 8), 
			(float)(SCREEN_HEIGHT / 30 + 0.8 * BRICK_HEIGHT + SCREEN_HEIGHT / 30),
			(float)(SCREEN_WIDTH * 6 / 8 + 1.6 * BRICK_WIDTH ),
			(float)(SCREEN_HEIGHT / 30 + 0.8 * BRICK_HEIGHT + SCREEN_HEIGHT / 30 + 0.8 * BRICK_HEIGHT));
	
		pauseButtonRectF = new RectF(
			(float)(SCREEN_WIDTH * 7 / 8),
			(float)(SCREEN_HEIGHT / 30),
			(float)(SCREEN_WIDTH),
			(float)(SCREEN_HEIGHT / 30 + SCREEN_WIDTH * 1 / 8));
		
		/*
		 * Set initial values
		 */
		availableBrickCount = SCREEN_WIDTH / BRICK_WIDTH;
		availableSpringboardCount = 5;
		INITIAL_DARK_CLOUD_COOLING_DISTANCE = SCREEN_WIDTH / BRICK_WIDTH / 2;
		threadState = ThreadState.STOPPED;
		
		retrieveLifeData();
	}


	private Drawable bushDrawable;
	private Bitmap bushBitmap;

	Paint mBushPaint = new Paint();
	Paint rBushPaint = new Paint();
	Paint lBushPaint = new Paint();
	
	Paint bushShadePaint = new Paint();
	
	/**
	 * Draw bush on canvas
	 * @param canvas
	 * @param start
	 */
	private void drawBush(Canvas canvas, int start){
		start = (int) (start + BRICK_WIDTH - 2 * BUSH_WIDTH);
		
		bushShadePaint.setAlpha(BUSH_SHADE_ALPHA);
		
		/* Draw left bush's shade */
		double lBushHeight = BUSH_HEIGHT_MIN + (BUSH_HEIGHT_MAX - BUSH_HEIGHT_MIN) * rand.nextDouble();
		double lBushX1 = start;
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
		
		/* Draw right bush's shade */
		double rBushHeight = BUSH_HEIGHT_MIN + (BUSH_HEIGHT_MAX - BUSH_HEIGHT_MIN) * rand.nextDouble();
		double rBushX1 = start + BUSH_WIDTH;
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
		
		/* Draw middle bush's shade */
		double mBushHeight = BUSH_HEIGHT_MIN + (BUSH_HEIGHT_MAX - BUSH_HEIGHT_MIN) * rand.nextDouble();
		double mBushX1 = start + BUSH_WIDTH / 2;
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
		
		/* Draw left bush */
		RectF lBushRectF = new RectF(
			(float)(lBushX1),
			(float)(lBushY1),
			(float)(lBushX2),
			(float)(lBushY2)
			);
		
		lBushPaint.setColorFilter(
			new PorterDuffColorFilter(
				bushColor[rand.nextInt(bushColor.length)], 
				PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bushBitmap, null, lBushRectF, lBushPaint);
		
		/* Draw right bush */
		RectF rBushRectF = new RectF(
			(float)(rBushX1),
			(float)(rBushY1),
			(float)(rBushX2),
			(float)(rBushY2)
			);
		rBushPaint.setColorFilter(
			new PorterDuffColorFilter(
				bushColor[rand.nextInt(bushColor.length)], 
				PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bushBitmap, null, rBushRectF, rBushPaint);
		
		/* Draw middle bush */
		RectF mBushRectF = new RectF(
			(float)(mBushX1),
			(float)(mBushY1),
			(float)(mBushX2),
			(float)(mBushY2)
			);
		mBushPaint.setColorFilter(
			new PorterDuffColorFilter(
				bushColor[rand.nextInt(bushColor.length)], 
				PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bushBitmap, null, mBushRectF, mBushPaint);
	}
	
	private Drawable treeCrownShadeDrawable;
	private Bitmap treeCrownShadeBitmap;
	
	private Drawable treeCrownDrawable;
	private Bitmap treeCrownBitmap;

	Paint trunkPaint = new Paint();
	Paint trunkShadePaint = new Paint();

	Paint treeCrownShadePaint = new Paint();
	Paint treeCrownPaint = new Paint();
	
	/**
	 * Draw tree on canvas
	 * @param canvas
	 * @param start
	 */
	private void drawTree(Canvas canvas, int start){
		float rootHeightMax = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT);
		float rootHeightMin = (float) (rootHeightMax + GROUND_HEIGHT - GRASS_HEIGHT);
		float rootHeight = (float) (rootHeightMax + rand.nextDouble() * (rootHeightMin - rootHeightMax));
		
		/* Draw trunk */
		trunkPaint.setColor(TRUNK_COLOR);
		double trunkX1 = start + TREE_CROWN_WIDTH / 2 - TRUNK_WIDTH / 2;
		double trunkY1 = rootHeight - TRUNK_HEIGHT;
		double trunkX2 = start + TREE_CROWN_WIDTH / 2 + TRUNK_WIDTH / 2;
		double trunkY2 = rootHeight;
		RectF trunkRectF = new RectF(
			(float)(trunkX1),
			(float)(trunkY1),
			(float)(trunkX2),
			(float)(trunkY2));
		canvas.drawRect(trunkRectF, trunkPaint);
		
		/* Draw trunk shade */
		trunkShadePaint.setColor(Color.BLACK);
		trunkShadePaint.setStrokeWidth((float) TRUNK_WIDTH);
		trunkShadePaint.setAlpha(60);
		trunkShadePaint.setAntiAlias(true);
		double trunkShadeX1 = trunkX1 - ((rootHeight - rootHeightMax) / Math.tan(Math.toRadians(TREE_SHADE_ANGLE)));
		double trunkShadeY1 = rootHeightMax;
		double trunkShadeX2 = start + TREE_CROWN_WIDTH / 2;
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
		
		/* Draw crown shade */
		double treeCrownX1 = start;
		double treeCrownY1 = trunkY1 - TREE_CROWN_HEIGHT;
		double treeCrownX2 = start + TREE_CROWN_WIDTH;
		double treeCrownY2 = trunkY1;
		
		double treeCrownShadeX1 = start - ((rootHeight - rootHeightMax) / Math.tan(Math.toRadians(TREE_SHADE_ANGLE)));
		double treeCrownShadeY1 = trunkShadeOnWallY1 - TREE_CROWN_HEIGHT;
		double treeCrownShadeX2 = treeCrownShadeX1 + TREE_CROWN_WIDTH;
		double treeCrownShadeY2 = trunkShadeOnWallY1;
		
		RectF treeCrownShadeRectF = new RectF((float)(treeCrownShadeX1),(float)(treeCrownShadeY1),
			(float)(treeCrownShadeX2),(float)(treeCrownShadeY2));
		treeCrownShadePaint.setAlpha(60);
		treeCrownShadePaint.setAntiAlias(true);
		canvas.drawBitmap(treeCrownShadeBitmap, null, treeCrownShadeRectF, treeCrownShadePaint);
		
		/* Draw crown */
		
		RectF treeCrownRectF = new RectF((float)(treeCrownX1),(float)(treeCrownY1),
			(float)(treeCrownX2),(float)(treeCrownY2));
		treeCrownPaint.setAntiAlias(true);
		canvas.drawBitmap(treeCrownBitmap, null, treeCrownRectF, treeCrownPaint);
	}

	Paint grassPaint = new Paint();
	
	/**
	 * Draw grass on canvas
	 * @param canvas
	 * @param start
	 * @param end
	 * @param offset
	 * @return
	 */
	private int drawGrass(Canvas canvas, int start, int end, int offset){
		start = start - lastPillarOffsetTmp;
		for(int i = start; i < end; i ++){
			// Max, from the lower left corner
			float grassHeightMax = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT);
			float grassHeightMin = (float) (grassHeightMax + GROUND_HEIGHT - GRASS_HEIGHT);
			double grassRootOffset = rand.nextGaussian();
			
			float grassHeight = (float) (grassHeightMax + rand.nextDouble()*(grassHeightMin - grassHeightMax));
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

	Paint subterraneanPaint = new Paint();
	Paint groundSectionPaint = new Paint();
	Paint groundPaint = new Paint();
	Paint groundBorderPaint = new Paint();
	
	/**
	 * Draw tree, bush, etc on the ground, and draw the ground
	 * on canvas
	 * @param canvas
	 * @param groundEnd
	 * @param offset
	 */
	private void drawGround(Canvas canvas, int groundEnd, int offset){
		int groundArrayEnd = (int) ((groundEnd + offset) / BRICK_WIDTH);
		
		
		int i = 0;
		while(i < groundArray.length && 
			(  groundArray[i] == Ground.BUSH_DRAWN || groundArray[i] == Ground.NORMAL_DRAWN 
			|| groundArray[i] == Ground.CONE_DRAWN || groundArray[i] == Ground.POOL_DRAWN
			|| groundArray[i] == Ground.TREE_DRAWN))
			i++;
		
		for(; i < groundArrayEnd; i++){
			/*
			 * Set paint's color, based on ground block's status
			 */
			if(groundArray[i] == Ground.NORMAL 
			|| groundArray[i] == Ground.TREE
			|| groundArray[i] == Ground.BUSH
			|| groundArray[i] == Ground.CONE ){
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
			
			/* Draw subterranean layer */
			float subterraneanX1 = start;
			float subterraneanY1 = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT);
			float subterraneanX2 = end;
			float subterraneanY2 = stage.getHeight();
			RectF subterraneanRectF = new RectF(
				subterraneanX1,	subterraneanY1,	
				subterraneanX2, subterraneanY2 );
			canvas.drawRect(subterraneanRectF, subterraneanPaint);
			
			/* Draw ground section */
			float groundSectionX1 = start;
			float groundSectionY1 = (int) (subterraneanY1 - GROUND_SECTION_HEIGHT);
			float groundSectionX2 = end;
			float groundSectionY2 = subterraneanY1;
			RectF groundSectionRectF = new RectF(
				groundSectionX1, groundSectionY1,
				groundSectionX2, groundSectionY2 );
			canvas.drawRect(groundSectionRectF, groundSectionPaint);
			
			/* Draw ground */
			float groundX1 = start;
			float groundY1 = (int) (groundSectionY1 - GROUND_HEIGHT);
			float groundX2 = end;
			float groundY2 = groundSectionY1;
			RectF groundRectF = new RectF(
					groundX1,groundY1,
					groundX2,groundY2 );
			canvas.drawRect(groundRectF, groundPaint);
			
			/* Draw the border of ground section and subterranean layer */
			float groundBorderX1 = start;
			float groundBorderY1 = subterraneanY1;
			float groundBorderX2 = end;
			float groundBorderY2 = subterraneanY1;
			canvas.drawLine(groundBorderX1, groundBorderY1, groundBorderX2, groundBorderY2, groundBorderPaint);
			
			/*
			 * Draw tree, bush, etc if the ground block is more than
			 * an ordinary one
			 */
			if(groundArray[i] == Ground.TREE)
				drawTree(canvas,(int) start);
			else if(groundArray[i] == Ground.BUSH)
				drawBush(canvas, (int) start);
			else if(groundArray[i] == Ground.CONE)
				drawCone(canvas, (int)start);
			
			/*
			 * Mark ground block to drawn, thus it won't be drawn again
			 */
			if (groundArray[i]== Ground.TREE) groundArray[i] = Ground.TREE_DRAWN; 
			if (groundArray[i]== Ground.POOL) groundArray[i] = Ground.POOL_DRAWN; 
			if (groundArray[i]== Ground.BUSH) groundArray[i] = Ground.BUSH_DRAWN; 
			if (groundArray[i]== Ground.CONE) groundArray[i] = Ground.CONE_DRAWN; 
			if (groundArray[i]== Ground.NORMAL) groundArray[i] = Ground.NORMAL_DRAWN; 
		}
	}
	

	private Drawable coneDrawable;
	private Bitmap coneBitmap;
	
	/**
	 * Draw cone on the canvas
	 * @param canvas
	 * @param start
	 */
	private void drawCone(Canvas canvas, int start) {
		
		int CONE_WIDTH = (int) (BRICK_WIDTH );
		int CONE_HEIGHT = (int) (BRICK_HEIGHT);
		int x1 = start - (CONE_WIDTH - BRICK_WIDTH) / 2;
		int y1 = (int) (SCREEN_HEIGHT - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - CONE_HEIGHT);
		int x2 = x1 + CONE_WIDTH;
		int y2 = y1 + CONE_HEIGHT;
		canvas.drawBitmap(coneBitmap, null, new RectF(x1,y1,x2,y2), null);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private Drawable brickShadeDrawable;
	private Bitmap brickShadeBitmap;

	Paint brickShadePaint = new Paint();
	
	/** 
	 * Draw bricks' shades on canvas
	 * @param canvas
	 * @param start
	 * @param offset
	 */
	private void drawBrickShade(Canvas canvas, int start, int offset) {
		brickShadePaint.setColor(BROWN_BRICK_COLOR);
		brickShadePaint.setAlpha(BRICK_SHADE_ALPHA);
		
		for(int i = (int)Math.floor(start / BRICK_WIDTH); 
			i < (int)Math.ceil((start + SCREEN_WIDTH)/ BRICK_WIDTH + 2); i++){
			for(int j = 0; j < brickArray[i].length; j++){
				if( (brickArray[i][j] == Brick.BROWN)||(brickArray[i][j] == Brick.YELLOW) ){
					int x1 = (int) (i * BRICK_WIDTH - BRICK_SHADE_WIDTH_OFFSET - offset);
					int y1 = (int) (j * BRICK_HEIGHT + BRICK_SHADE_HEIGHT_OFFSET + BRICK_HEIGHT_OFFSET);
					int x2 = (int) (x1 + BRICK_WIDTH);
					int y2 = (int) (y1 + BRICK_HEIGHT);
					
					if(y1 > SCREEN_HEIGHT) y1 = SCREEN_HEIGHT;
					if(y2 > SCREEN_HEIGHT) y2 = SCREEN_HEIGHT;
					
					RectF destRect = new RectF(x1,y1,x2,y2);
					canvas.drawRoundRect(destRect, 5, 5, brickShadePaint);
					//canvas.drawBitmap(brickShadeBitmap, null, destRect, brickShadePaint);
				}
			}
		}
	}

	private Drawable heroShadeDrawable;
	private Bitmap heroShadeBitmap;

	Paint heroShadePaint = new Paint();
	/**
	 * Draw hero's shade on canvas
	 * @param canvas
	 * @param startX
	 */
	private void drawHeroShade(Canvas canvas, int startX) {
		heroShadePaint.setAlpha(HERO_SHADE_ALPHA);
		
		double shadeX1 = startX + heroPositionX - (HERO_WIDTH - BRICK_WIDTH)/2 - HERO_SHADE_WIDTH_OFFSET;
		double shadeY1 = 
			(SCREEN_HEIGHT - heroPositionY) + 
			BRICK_HEIGHT - HERO_HEIGHT + 
			HERO_SHADE_HEIGHT_OFFSET + 
			BRICK_HEIGHT_OFFSET;
		double shadeX2 = shadeX1 + HERO_WIDTH;
		double shadeY2 = shadeY1 + HERO_HEIGHT;
		
		RectF heroShadeDestRect = new RectF((float)shadeX1, (float)shadeY1, (float)shadeX2, (float)shadeY2);
		canvas.drawBitmap(heroShadeBitmap, null, heroShadeDestRect, heroShadePaint);
	}

	/** Indicates how many milliseconds are left for the lightning */
	private long lightningTimeRemainingMillis;
	
	/** Time stamp for last lightning's ending, in milliseconds */
	private long lastFlashingTimeMillis;
	
	private Drawable yellowBrickDrawable;
	private Drawable brownBrickDrawable;
	private Drawable darkCloudDrawable;
	private Drawable lightningDrawable;
	private Drawable rainDrawable;
	private Drawable passportDrawable;
	private Drawable duckyDrawable;
	private Drawable umbrellaDrawable;
	private Drawable springboardClosedDrawable;
	private Drawable springboardOpened1Drawable;
	private Drawable springboardOpened2Drawable;
			
	private Bitmap yellowBrickBitmap;
	private Bitmap brownBrickBitmap;
	private Bitmap darkCloudBitmap;
	private Bitmap lightningBitmap;
	private Bitmap rainBitmap;
	private Bitmap passportBitmap;
	private Bitmap duckyBitmap;
	private Bitmap umbrellaBitmap;
	private Bitmap springboardClosedBitmap;
	private Bitmap springboardOpened1Bitmap;
	private Bitmap springboardOpened2Bitmap;

	Paint brickPaint = new Paint();
	Paint darkCloudShadePaint = new Paint();
	Paint rainPaint = new Paint();
	Paint lightningPaint = new Paint();
	
	//TODO move drawables and bitmaps out of this method
	/** 
	 * Draw bricks on canvas
	 * @param start
	 * @param offset
	 */
	private void drawBrick(Canvas canvas, int start, int offset) {		
		//Canvas canvas = new Canvas(stageWithBricks);
		
		for(int i = (int)Math.floor(start / BRICK_WIDTH); 
			i < (int)Math.ceil((start + SCREEN_WIDTH)/ BRICK_WIDTH + 2); i++){
			for(int j = 0; j < brickArray[i].length; j++){
				/* If brick is brown, yellow or springboard */
				if( (brickArray[i][j] == Brick.BROWN) ||
					(brickArray[i][j] == Brick.YELLOW) ||
					(brickArray[i][j] == Brick.SPRINGBOARD_CLOSED) ||
					(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X1) ||
					(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X2)){
					
					RectF destRect = new RectF(
						(i*BRICK_WIDTH - offset), 
						(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
						((i+1)*BRICK_WIDTH - offset), 
						(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET));
					
					if(brickArray[i][j] == Brick.BROWN ){
						brickPaint.setColor(BROWN_BRICK_COLOR);
						canvas.drawRoundRect(destRect, 5, 5, brickPaint);
						//canvas.drawBitmap(brownBrickBitmap, null, destRect,	null);
					}
					else if(brickArray[i][j] == Brick.YELLOW){
						brickPaint.setColor(YELLOW_BRICK_COLOR);
						canvas.drawRoundRect(destRect, 5, 5, brickPaint);
						//canvas.drawBitmap(yellowBrickBitmap, null, destRect, null);
					}
					else if(brickArray[i][j] == Brick.SPRINGBOARD_CLOSED)
						canvas.drawBitmap(springboardClosedBitmap, null, destRect, null);
					else if(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X1)
						canvas.drawBitmap(springboardOpened1Bitmap, null, destRect, null);
					else if(brickArray[i][j] == Brick.SPRINGBOARD_OPENED_X2)
						canvas.drawBitmap(springboardOpened2Bitmap, null, destRect, null);
					
				}
				/* If brick is dark cloud */
				else if( (brickArray[i][j] == Brick.DARK_CLOUD_X0Y0) ||
					(brickArray[i][j] == Brick.DARK_CLOUD_X1Y0) || 
					(brickArray[i][j] == Brick.DARK_CLOUD_X2Y0) || 
					(brickArray[i][j] == Brick.DARK_CLOUD_X0Y1) ||
					(brickArray[i][j] == Brick.DARK_CLOUD_X1Y1) ||
					(brickArray[i][j] == Brick.DARK_CLOUD_X2Y1)){
					
					int srcX1 = 0;
					int srcY1 = 0;
					
					// Indicate whether should draw dark cloud's shade
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
					
					Rect srcRect = new Rect( srcX1, srcY1, srcX2, srcY2);
					RectF destRect = new RectF(
						(float)(i*BRICK_WIDTH - offset), 
						(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
						(float)((i+1)*BRICK_WIDTH - offset), 
						(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  
						);
					canvas.drawBitmap(darkCloudBitmap, srcRect, destRect, null);
					
					if(drawShade){
						darkCloudShadePaint.setAlpha(180);
						RectF shadeDest = new RectF(
								(float)((i-2)*BRICK_WIDTH - offset), 
								(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
								(float)((i+1)*BRICK_WIDTH - offset), 
								(float)((j+4)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET));
						//canvas.drawBitmap(darkCloudShadeBitmap, null, shadeDest, darkCloudShadePaint );
						
						rainPaint.setAlpha(RAIN_ALPHA);
						canvas.drawBitmap(rainBitmap, null, shadeDest, rainPaint );
						
						/* If it still remains some time for lightning, draw it */
						if(lightningTimeRemainingMillis > 0){
							lightningPaint.setAntiAlias(true);
							canvas.drawBitmap(lightningBitmap, null, new RectF(
								(float)((i-1)*BRICK_WIDTH - offset), 
								(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
								(float)((i+0)*BRICK_WIDTH - offset), 
								(float)((j+2)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET)  ), lightningPaint);
							lightningTimeRemainingMillis = 
									LIGHTNING_DURATION_MILLIS - (System.currentTimeMillis() - lastFlashingTimeMillis);
						}
						/* If time have elapsed longer than lightning's interval,
						 * start drawing another lightning 
						 */
						else{
							if( (System.currentTimeMillis() - lastFlashingTimeMillis) > 
								(LIGHTNING_DURATION_MILLIS + LIGHTNING_INTERVAL_MILLIS)){
								lightningTimeRemainingMillis = LIGHTNING_DURATION_MILLIS;
								lastFlashingTimeMillis = System.currentTimeMillis();
							}
						}
					}
				}
				/* If brick is passport */
				else if(brickArray[i][j] == Brick.PASSPORT){
					RectF rDest = new RectF(
						(float)(i*BRICK_WIDTH - offset), 
						(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
						(float)((i+1)*BRICK_WIDTH - offset), 
						(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET));
					canvas.drawBitmap(passportBitmap, null,	rDest, null);
				}
				/* If brick is ducky */
				else if(brickArray[i][j] == Brick.DUCKY){
					RectF rDest = new RectF(
						(float)(i*BRICK_WIDTH - offset), 
						(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
						(float)((i+1)*BRICK_WIDTH - offset), 
						(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET));
					canvas.drawBitmap(duckyBitmap, null,	rDest, null);
				}
				/* If brick is umbrella */
				else if(brickArray[i][j] == Brick.UMBRELLA){
					RectF rDest = new RectF(
						(float)(i*BRICK_WIDTH - offset), 
						(float)(j*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET),
						(float)((i+1)*BRICK_WIDTH - offset), 
						(float)((j+1)*BRICK_HEIGHT + BRICK_HEIGHT_OFFSET));
					canvas.drawBitmap(umbrellaBitmap, null,	rDest, null);
				}
			}
		}
	}

	private Drawable heroDrawable;
	private Bitmap heroBitmap;
	
	/**
	 * Draw hero on canvas
	 * @param canvas
	 */
	private void drawHero(Canvas canvas) {
		int x1 = (int) (heroPositionX - (HERO_WIDTH - BRICK_WIDTH) / 2);
		int y1 = (int) (SCREEN_HEIGHT + BRICK_HEIGHT - HERO_HEIGHT - (heroPositionY + SUBTERRANEAN_HEIGHT + GROUND_SECTION_HEIGHT ));
		
		int x2 = x1 + HERO_WIDTH;
		int y2 = y1 + HERO_HEIGHT;
		RectF heroDest = new RectF(x1, y1, x2, y2);
		canvas.drawBitmap(heroBitmap, null, heroDest, null);
	}

}
