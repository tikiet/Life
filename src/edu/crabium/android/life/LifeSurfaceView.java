package edu.crabium.android.life;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LifeSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback{

	private SurfaceHolder holder;
	private LifeSurfaceViewThread thread;
	private Bitmap bitmap;
	private Bitmap stage;
	private Bitmap stageWithBricks;
	
	private int stagePillarShift;
	private int stageBackPillarShift;
	private int heroPositionX = 2;
	private int heroPositionY = 10;
	private int speed = 4;
	
	private int BACK_GROUND_COLOR = 0xff9bc3cf;
	private int GROUND_COLOR = 0xffced852;
	private int GROUND_SECTION_COLOR = 0xffb3b827;
	private int GROUND_BORDER_COLOR = 0xff8c8302;
	private int SUBTERRANEAN_COLOR = 0xffea9506;
	private int GRASS_COLOR = 0xff54540e;
	private int TRUNK_COLOR = 0xff4c4238;
	
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
	
	private double BUSH_WIDTH;
	private double BUSH_HEIGHT_MAX;
	private double BUSH_HEIGHT_MIN;
	private double BUSH_HEIGHT_OFFSET_FROM_WALL;
	private double BUSH_HEIGHT_OFFSET_FROM_BUSH;
	private double BUSH_SPACE_COUNT;
	private double BUSH_SHADE_HEIGHT_OFFSET_RATIO;
	private double BUSH_SHADE_WIDTH_OFFSET_RATIO;
	
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

	private int BLOCK_SIDE_LENGTH;
	private double BRICK_HEIGHT;
	private double BRICK_WIDTH;
	private double BRICK_SHADE_HEIGHT_OFFSET;
	private double BRICK_SHADE_WIDTH_OFFSET;
	private double BROWN_BRICK_THRESHOLD;
	
	private int BACK_PILLAR_SHADE_ALPHA = 30;
	private int CLOUD_SHADE_ALPHA = 60;
	private int BRICK_SHADE_ALPHA = 60;
	private int BUSH_SHADE_ALPHA = 60;
	
	private int[][] blockArray;
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
	
	private class PillarPosition{
		private double x;
		private double y;
		
		public PillarPosition(double x, double y){
			this.x = x;
			this.y = y;
		}
		
		public double getX(){
			return x;
		}
		
		public double getY(){
			return y;
		}
	}
	private class LifeSurfaceViewThread extends Thread{
		public LifeSurfaceViewThread(SurfaceHolder holder, Context context) {
			
			BLOCK_SIDE_LENGTH = Integer.valueOf( 
					context.getResources().
							getString(R.string.block_side_length));
			
			BRICK_HEIGHT = Integer.valueOf( 
					context.getResources().
					getString(R.string.brick_height));
			BRICK_WIDTH = Integer.valueOf( 
					context.getResources().
					getString(R.string.brick_width));

			BROWN_BRICK_THRESHOLD = Double.valueOf(
					context.getResources().
							getString(R.string.brown_brick_threshold));
			
			PILLAR_WIDTH = Integer.valueOf(
					context.getResources().
							getString(R.string.pillar_width));
			
			PILLAR_HEIGHT_MAX = Double.valueOf(
					context.getResources().
							getString(R.string.pillar_height_max));
			
			PILLAR_HEIGHT_MIN = Double.valueOf(
					context.getResources().
							getString(R.string.pillar_height_min));
			
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
			
			BACK_PILLAR_SHADE_WIDTH_OFFSET = BACK_PILLAR_WIDTH * Double.valueOf(
					context.getResources().
					getString(R.string.back_pillar_shade_width_ratio));
			
			BACK_PILLAR_SHADE_HEIGHT_OFFSET = BACK_PILLAR_WIDTH * Double.valueOf(
					context.getResources().
					getString(R.string.back_pillar_shade_height_ratio));
			
			HERO_HEIGHT = Integer.valueOf(
					context.getResources().
							getString(R.string.hero_height));
			HERO_WIDTH = Integer.valueOf(
					context.getResources().
							getString(R.string.hero_width));
			
			HERO_SHADE_HEIGHT_OFFSET = HERO_HEIGHT * Double.valueOf(
					context.getResources().
					getString(R.string.hero_shade_height_offset_ratio));
			
			HERO_SHADE_WIDTH_OFFSET = HERO_WIDTH * Double.valueOf(
					context.getResources().
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
			
			heroPositionX *= BLOCK_SIDE_LENGTH;
			heroPositionY *= BLOCK_SIDE_LENGTH;
		}
		
		int startx = 0;
		public void run(){
			while(true){
				Drawable yellowTile = context.getResources().getDrawable(R.drawable.tile_yellow);
			
				Drawable brownTile = context.getResources().getDrawable(R.drawable.tile_brown);
				Drawable shadeTile = context.getResources().getDrawable(R.drawable.shade);
				Drawable heroTile = context.getResources().getDrawable(R.drawable.hero);
				Drawable heroShadeTile = context.getResources().getDrawable(R.drawable.hero_shade);
				
				Bitmap yellowBitmap = ((BitmapDrawable)yellowTile).getBitmap();
				Bitmap brownBitmap = ((BitmapDrawable)brownTile).getBitmap();
				Bitmap shadeBitmap = ((BitmapDrawable)shadeTile).getBitmap();
				Bitmap heroBitmap = ((BitmapDrawable)heroTile).getBitmap();
				Bitmap heroShadeBitmap = ((BitmapDrawable)heroShadeTile).getBitmap();
				
				while(startx <= BITMAP_WIDTH * 2){
					Log.d("Life", "startx = " + startx);
					Canvas canvas = new Canvas(stageWithBricks);
					RectF stageRectF = new RectF(
							0,
							0,
							stage.getWidth(),
							stage.getHeight());
					
					canvas.drawBitmap(stage, null, stageRectF, null);
					
					updateHero();
					
					/*
					 * Draw bricks' shade
					 */
					Paint paint = new Paint();
					paint.setAlpha(BRICK_SHADE_ALPHA);
					for(int i = (int)Math.floor(startx / BLOCK_SIDE_LENGTH); 
							i < (int)Math.ceil((startx + BITMAP_WIDTH)/ BLOCK_SIDE_LENGTH + 2);
							i++){
						for(int j = 0; j < blockArray[i].length; j++){
							if(blockArray[i][j] != 0){
								int x1 = (int) (i * BLOCK_SIDE_LENGTH - BRICK_SHADE_WIDTH_OFFSET);
								int y1 = (int) (j * BLOCK_SIDE_LENGTH + BRICK_SHADE_HEIGHT_OFFSET);
								int x2 = x1 + BLOCK_SIDE_LENGTH;
								int y2 = y1 + BLOCK_SIDE_LENGTH;
								
								if(x1 < 0) x1 = 0;
								if(y1 > BITMAP_HEIGHT) y1 = BITMAP_HEIGHT;
								if(y2 > BITMAP_HEIGHT) y2 = BITMAP_HEIGHT;
								
								RectF rDest = new RectF(x1,y1,x2,y2);
								
								canvas.drawBitmap(shadeBitmap, null, rDest, paint);
							}
						}
					}
						
					/*
					 * Draw hero's shade
					 */
					double sx1 = startx + heroPositionX - (HERO_WIDTH - BLOCK_SIDE_LENGTH)/2 - HERO_SHADE_WIDTH_OFFSET;
					double sy1 = (BITMAP_HEIGHT - heroPositionY) + BLOCK_SIDE_LENGTH - HERO_HEIGHT + HERO_SHADE_HEIGHT_OFFSET;
					double sx2 = sx1 + HERO_WIDTH;
					double sy2 = sy1 + HERO_HEIGHT;
					RectF hsDest = new RectF((float)sx1, (float)sy1, (float)sx2, (float)sy2);
					canvas.drawBitmap(heroShadeBitmap, null, hsDest, paint);
					
					/*
					 * Draw bricks
					 */
					canvas = new Canvas(stageWithBricks);
					for(int i = (int)Math.floor(startx / BLOCK_SIDE_LENGTH); 
							i < (int)Math.ceil((startx + BITMAP_WIDTH)/ BLOCK_SIDE_LENGTH + 2);
							i++){
						for(int j = 0; j < blockArray[i].length; j++){
							if(blockArray[i][j] != 0){
								RectF rDest = new RectF(
										i*BLOCK_SIDE_LENGTH, 
										j*BLOCK_SIDE_LENGTH,
										(i+1)*BLOCK_SIDE_LENGTH, 
										(j+1)*BLOCK_SIDE_LENGTH  
										);
								
								if(blockArray[i][j] == 2 )
									canvas.drawBitmap(
										brownBitmap, 
										null,
										rDest, 
										null);
								else
									canvas.drawBitmap(
										yellowBitmap, 
										null,
										rDest, 
										null);
							}
						}
					}
					Rect rSrc = new Rect(
							startx,
							0,
							startx + BITMAP_WIDTH,
							BITMAP_HEIGHT
							);
					
					RectF rDest = new RectF(
							(float)0.0,
							(float)0.0,
							(float)BITMAP_WIDTH,
							(float)BITMAP_HEIGHT
							);
					
					canvas = new Canvas(bitmap);
					canvas.drawBitmap(stageWithBricks, rSrc, rDest, null);
					
					/*
					 * Draw hero
					 */
					int x1 = heroPositionX - (HERO_WIDTH - BLOCK_SIDE_LENGTH) / 2;
					int y1 = (BITMAP_HEIGHT -heroPositionY) + BLOCK_SIDE_LENGTH - HERO_HEIGHT;
					int x2 = x1 + HERO_WIDTH;
					int y2 = y1 + HERO_HEIGHT;
					RectF hDest = new RectF(x1, y1, x2, y2);
					canvas.drawBitmap(heroBitmap, null, hDest, null);
					
					canvas = holder.lockCanvas();
					canvas.drawBitmap(bitmap, 0, 0, null);
					holder.unlockCanvasAndPost(canvas);
					startx += speed;
				}
				
				Canvas canvas = new Canvas(stage);
				Paint paint = new Paint();
				paint.setColor(BACK_GROUND_COLOR);
				Rect rSrc = new Rect(
						BITMAP_WIDTH*2,
						0,
						BITMAP_WIDTH*2 + BITMAP_WIDTH + PILLAR_WIDTH,
						BITMAP_HEIGHT
						);
				
				RectF rDest = new RectF(
						(float)0.0,
						(float)0.0,
						(float)BITMAP_WIDTH + PILLAR_WIDTH,
						(float)BITMAP_HEIGHT
						);
				canvas.drawBitmap(stage, rSrc, rDest, null);
	
				canvas.drawRect(
						(BITMAP_WIDTH + PILLAR_WIDTH),
						(float)0.0, 
						(float)BITMAP_WIDTH*3 + PILLAR_WIDTH, 
						(float)BITMAP_HEIGHT, 
						paint);
				
				/*
				 * Draw back pillars' shade
				 */
				
				paint.setColor(Color.BLACK);
				paint.setAlpha(BACK_PILLAR_SHADE_ALPHA);
				paint.setAntiAlias(true);
				
				List<PillarPosition> backPillarPositionList = new LinkedList<PillarPosition>();
				for(int i = BITMAP_WIDTH + BACK_PILLAR_WIDTH - stageBackPillarShift ; i < stage.getWidth(); i += BACK_PILLAR_WIDTH){
					Random rand = new Random();
					int backPillarHeight = (int)(BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MIN) + 
							rand.nextInt(
							(int)(BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MAX 
									- BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MIN));
					
					if( i + BACK_PILLAR_WIDTH <= stage.getWidth()){
						backPillarPositionList.add(new PillarPosition(i, backPillarHeight));
						
						double x1 = i - BACK_PILLAR_SHADE_WIDTH_OFFSET;
						double y1 = BITMAP_HEIGHT - (backPillarHeight - BACK_PILLAR_SHADE_HEIGHT_OFFSET + BACK_PILLAR_WIDTH / 2);
						RectF rectF = new RectF(
								(float) (x1),
								(float) (y1), 
								(float)(x1 + BACK_PILLAR_WIDTH),
								(float)BITMAP_HEIGHT
								);
						
						canvas.drawRoundRect(rectF, BACK_PILLAR_WIDTH / 2, BACK_PILLAR_WIDTH / 2, paint);
					}
					else{
						stageBackPillarShift = stage.getWidth() - i;
					}
					
					i += rand.nextInt((int) BACK_PILLAR_SPACE) + BACK_PILLAR_SPACE;
				}
				
				/*
				 * Draw back pillars
				 */
				paint.setAlpha(255);
				paint.setAntiAlias(true);
				for(PillarPosition pillarPosition : backPillarPositionList){
					Random rand = new Random();
					paint.setColor(backPillarColor[rand.nextInt(backPillarColor.length)]);
					
						canvas.drawCircle(
								(float)(pillarPosition.getX() + BACK_PILLAR_WIDTH / 2), 
								(float)(BITMAP_HEIGHT - pillarPosition.getY()), 
								BACK_PILLAR_WIDTH / 2, 
								paint);
						canvas.drawRect(
								(float)(pillarPosition.getX()), 
								(float)(BITMAP_HEIGHT - pillarPosition.getY()), 
								(float) (pillarPosition.getX()+BACK_PILLAR_WIDTH), 
								BITMAP_HEIGHT, 
								paint);
				}
				
				/* 
				 * Draw pillars
				 */
				paint.setAntiAlias(true);
				for(int i = BITMAP_WIDTH + PILLAR_WIDTH - stagePillarShift; i < stage.getWidth()  - stageBackPillarShift; i += PILLAR_WIDTH){
					Random rand = new Random();
					int pillarHeight = (int)((BITMAP_HEIGHT* 0.5 * (PILLAR_HEIGHT_MAX + PILLAR_HEIGHT_MIN)) + 
							rand.nextGaussian() * 0.3 *
							(BITMAP_HEIGHT * PILLAR_HEIGHT_MAX - BITMAP_HEIGHT * PILLAR_HEIGHT_MIN));
					paint.setColor(pillarColor[rand.nextInt(pillarColor.length)]);
					
					if( i + PILLAR_WIDTH <= stage.getWidth() - stageBackPillarShift ){
					canvas.drawCircle(
							i + PILLAR_WIDTH / 2, 
							BITMAP_HEIGHT - pillarHeight, 
							PILLAR_WIDTH / 2, 
							paint);
					canvas.drawRect(
							i, 
							BITMAP_HEIGHT - pillarHeight, 
							i + PILLAR_WIDTH, 
							BITMAP_HEIGHT, 
							paint);
					
					}
					else{
						stagePillarShift = stage.getWidth() - i;
					}
					i += rand.nextInt((int) PILLAR_SPACE) + PILLAR_SPACE;
				}
				
				/*
				 * Draw clouds
				 */
				Drawable cloud1Tile = context.getResources().getDrawable(R.drawable.cloud1);
				Drawable cloud1ShadeTile = context.getResources().getDrawable(R.drawable.cloud1_shade);
				
				Bitmap cloud1Bitmap = ((BitmapDrawable)cloud1Tile).getBitmap();
				Bitmap cloud1ShadeBitmap = ((BitmapDrawable)cloud1ShadeTile).getBitmap();
				
				for(int i = BITMAP_WIDTH + PILLAR_WIDTH; i< stage.getWidth(); i += CLOUD_WIDTH_MAX * CLOUD_SPACE_RATIO ){
					Random rand = new Random();
					if(i + CLOUD_WIDTH_MIN * 1.5 <= stage.getWidth()){
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
						
						i += CLOUD_WIDTH_MIN * CLOUD_SPACE_RATIO * rand.nextDouble();
					}
				}
				
				int newRandomBricksStartX = 0;
				int[][] newBlockArray = new int[blockArray.length][blockArray[0].length];
				for(int i = (int) Math.floor(startx/BLOCK_SIDE_LENGTH); i < blockArray.length; i++){
					newRandomBricksStartX ++;
					for(int j = 0; j < blockArray[i].length; j++){
						newBlockArray[i-(int) Math.floor(startx/BLOCK_SIDE_LENGTH)][j] 
								= blockArray[i][j];
					}
				}
				
				boolean randomBrickOn = false;
				int randomBricksRemain = 0;
				for(int i = newRandomBricksStartX; 
						i < newBlockArray.length - RANDOM_BRICK_COUNT_MAX; 
						i++){
					Random rand = new Random();
					if(randomBrickOn){
						randomBricksRemain = RANDOM_BRICK_COUNT_MIN + 
								rand.nextInt(RANDOM_BRICK_COUNT_MAX - RANDOM_BRICK_COUNT_MIN);
						
						int randomBrickHeight = BITMAP_HEIGHT - (int) (RANDOM_BRICK_HEIGHT_MIN*BITMAP_HEIGHT +
								rand.nextInt((int)(RANDOM_BRICK_HEIGHT_MAX * BITMAP_HEIGHT - 
										RANDOM_BRICK_HEIGHT_MIN* BITMAP_HEIGHT))) ;
						int randomBrickBlockHeight = randomBrickHeight / BLOCK_SIDE_LENGTH;

						String brickPattern = Integer.toBinaryString((1 << rand.nextInt(randomBricksRemain)) + (1 << (randomBricksRemain))) + "0";
						brickPattern = brickPattern.substring(1);
						for(int j = 0; j < randomBricksRemain; j++){
							newBlockArray[i + j][randomBrickBlockHeight] = 
									1 +Integer.valueOf(brickPattern.substring(j,j+1));
						}
						randomBrickOn = false;
						i+= randomBricksRemain / 2;
					}else{
						if(rand.nextDouble() >= RANDOM_BRICK_THRESHOLD){
							randomBrickOn = true;
						}
					}
				}
				drawGroundAndSubterranean(canvas, BITMAP_WIDTH - stagePillarShift);
				drawGrass(canvas, BITMAP_WIDTH - stagePillarShift);
				drawBush(canvas, BITMAP_WIDTH - stagePillarShift);
				drawTree(canvas, BITMAP_WIDTH - stagePillarShift);
				blockArray = newBlockArray;
				startx = 0;
			}
		}

		private void updateHero(){
			if( (startx % BLOCK_SIDE_LENGTH) == 0){
				int x = (heroPositionX + startx) / BLOCK_SIDE_LENGTH;
				int y = (BITMAP_HEIGHT - heroPositionY) / BLOCK_SIDE_LENGTH;
				
				if( ( y < (BITMAP_HEIGHT / BLOCK_SIDE_LENGTH - 1 ) ) && blockArray[x][y+1] == 0)
					heroPositionY -= BLOCK_SIDE_LENGTH;
				else if ( (y > 1) && (blockArray[x+1][y] != 0) && (blockArray[x+1][y-1] == 0))
					heroPositionY += BLOCK_SIDE_LENGTH;
				
			}
		}
		private int prevBlockX = 0;
		private int prevBlockY = 0;
		public boolean onTouchEvent(MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_MOVE ||
					event.getAction() == MotionEvent.ACTION_DOWN){
				synchronized(bitmap){
					
					float x = event.getX();
					float y = event.getY();
					Log.d("Life", "[onTouchEvent] x = " + x + ", y = " + y);
					
					int blockX = (int) Math.floor((x + startx)/BLOCK_SIDE_LENGTH);
					int blockY = (int) Math.floor(y/BLOCK_SIDE_LENGTH);
					Log.d("Life", "[onTouchEvent] blockX = " + blockX + ", blockY = " + blockY);
					
					if(blockX != prevBlockX || blockY != prevBlockY){
						prevBlockX = blockX;
						prevBlockY = blockY;
						
						Random rand = new Random();
						if(blockArray[blockX][blockY] == 1)
								blockArray[blockX][blockY] = 0;
						else if(blockArray[blockX][blockY] != 2){
							if(rand.nextDouble() >= BROWN_BRICK_THRESHOLD)
								blockArray[blockX][blockY] = 2;
							else
								blockArray[blockX][blockY] = 1;
						}
					}
					else{
						Log.d("Life", "[onTouchEvent] prevBlockX = " + prevBlockX +
								", prevBlockY = " + prevBlockY);
					}
				}
			}
			return true;
		}
		
	}
	public LifeSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		holder = getHolder();
		holder.addCallback(this);
		thread = new LifeSurfaceViewThread(holder, context);
		this.context = context;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		return thread.onTouchEvent(event);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Canvas canvas = holder.lockCanvas();
		BITMAP_WIDTH = canvas.getWidth();
		BITMAP_HEIGHT = canvas.getHeight();
		holder.unlockCanvasAndPost(canvas);
		
		GROUND_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
						getString(R.string.ground_ratio));
		
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
		
		TREE_CROWN_WIDTH = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
						getString(R.string.tree_crown_width_ratio));
		
		TREE_CROWN_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
						getString(R.string.tree_crown_height_ratio));
		
		TRUNK_HEIGHT = BITMAP_HEIGHT * Double.valueOf(
				context.getResources().
						getString(R.string.trunk_height_ratio));
		
		TRUNK_WIDTH= BITMAP_HEIGHT * Double.valueOf(
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
		
		BRICK_SHADE_HEIGHT_OFFSET = BRICK_HEIGHT * Double.valueOf(
				context.getResources().
				getString(R.string.brick_shade_height_offset_ratio));
		
		BRICK_SHADE_WIDTH_OFFSET = BRICK_WIDTH * Double.valueOf(
				context.getResources().
				getString(R.string.brick_shade_width_offset_ratio));
				
		bitmap = Bitmap.createBitmap(
				BITMAP_WIDTH, 
				BITMAP_HEIGHT, 
				Bitmap.Config.ARGB_8888);
		
		stage = Bitmap.createBitmap(
				BITMAP_WIDTH*3 + PILLAR_WIDTH, 
				BITMAP_HEIGHT, 
				Bitmap.Config.ARGB_8888);
		
		stageWithBricks = Bitmap.createBitmap(
				stage.getWidth(),
				stage.getHeight(),
				Bitmap.Config.ARGB_8888);
		
		BLOCK_NUMBER_X = (BITMAP_WIDTH * 3 + PILLAR_WIDTH) / BLOCK_SIDE_LENGTH;
		BLOCK_NUMBER_Y = BITMAP_HEIGHT / BLOCK_SIDE_LENGTH;
		blockArray = new int[BLOCK_NUMBER_X][BLOCK_NUMBER_Y];
		
		boolean randomBrickOn = false;
		int randomBricksRemain = 0;
		for(int i = 0; i < blockArray.length - RANDOM_BRICK_COUNT_MAX; i++){
			Random rand = new Random();
			if(randomBrickOn){
				randomBricksRemain = RANDOM_BRICK_COUNT_MIN + 
						rand.nextInt( RANDOM_BRICK_COUNT_MAX - RANDOM_BRICK_COUNT_MIN );
				
				
				int randomBrickHeight = BITMAP_HEIGHT - (int) (RANDOM_BRICK_HEIGHT_MIN*BITMAP_HEIGHT +
						rand.nextInt((int)(RANDOM_BRICK_HEIGHT_MAX * BITMAP_HEIGHT - 
								RANDOM_BRICK_HEIGHT_MIN* BITMAP_HEIGHT))) ;
				int randomBrickBlockHeight = randomBrickHeight / BLOCK_SIDE_LENGTH;
				
				String brickPattern = Integer.toBinaryString((1 << rand.nextInt(randomBricksRemain)) + (1 << (randomBricksRemain))) + "0";
				brickPattern = brickPattern.substring(1);
				Log.d("Life", "brickPattern:"+brickPattern);
				
				for(int j = 0; j < randomBricksRemain; j++){
					blockArray[i + j][randomBrickBlockHeight] = 
							1 + Integer.valueOf(brickPattern.substring(j,j+1));
				}
				
				randomBrickOn = false;
				i+= randomBricksRemain / 2;
			}else{
				if(rand.nextDouble() >= RANDOM_BRICK_THRESHOLD){
					randomBrickOn = true;
				}
			}
		}
			
		canvas = new Canvas(stage);
		Paint paint = new Paint();
		paint.setColor(BACK_GROUND_COLOR);
		canvas.drawRect(
				(float)0.0, 
				(float)0.0, 
				(float)BITMAP_WIDTH*3 + PILLAR_WIDTH, 
				(float)BITMAP_HEIGHT, 
				paint);
		
		/*
		 * Draw back pillars' shade
		 */
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setAlpha(BACK_PILLAR_SHADE_ALPHA);
		
		List<PillarPosition> backPillarPositionList = new LinkedList<PillarPosition>();
		for(int i = BACK_PILLAR_WIDTH /2 ; i < stage.getWidth(); i += BACK_PILLAR_WIDTH){
			Random rand = new Random();
			int backPillarHeight = (int)(BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MIN) + 
					rand.nextInt(
					(int)(BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MAX 
							- BITMAP_HEIGHT * BACK_PILLAR_HEIGHT_MIN));
			
			if( i + BACK_PILLAR_WIDTH <= stage.getWidth()){
				backPillarPositionList.add(new PillarPosition(i, backPillarHeight));
				
				double x1 = i - BACK_PILLAR_SHADE_WIDTH_OFFSET;
				double y1 = BITMAP_HEIGHT - (backPillarHeight - BACK_PILLAR_SHADE_HEIGHT_OFFSET + BACK_PILLAR_WIDTH / 2);
				RectF rectF = new RectF(
						(float) (x1),
						(float) (y1), 
						(float)(x1 + BACK_PILLAR_WIDTH),
						(float)BITMAP_HEIGHT
						);
				
				canvas.drawRoundRect(rectF, BACK_PILLAR_WIDTH / 2, BACK_PILLAR_WIDTH / 2, paint);
			}
			else{
				stageBackPillarShift = stage.getWidth() - i;
			}
			
			i += rand.nextInt((int) BACK_PILLAR_SPACE) + BACK_PILLAR_SPACE;
		}
		
		/*
		 * Draw back pillars
		 */
		paint.setAlpha(255);
		paint.setAntiAlias(true);
		for(PillarPosition pillarPosition : backPillarPositionList){
			Random rand = new Random();
			paint.setColor(backPillarColor[rand.nextInt(backPillarColor.length)]);
			
				canvas.drawCircle(
						(float)(pillarPosition.getX()+BACK_PILLAR_WIDTH/2), 
						(float)(BITMAP_HEIGHT - pillarPosition.getY()), 
						BACK_PILLAR_WIDTH/2, 
						paint);
				canvas.drawRect(
						(float)(pillarPosition.getX()), 
						(float)(BITMAP_HEIGHT - pillarPosition.getY()), 
						(float) (pillarPosition.getX()+BACK_PILLAR_WIDTH), 
						BITMAP_HEIGHT, 
						paint);
		}
		
		
		/*
		 * Draw pillars
		 */
		paint.setAntiAlias(true);
		for(int i = 0; i < stage.getWidth() - stageBackPillarShift; i += PILLAR_WIDTH){
			Random rand = new Random();
			int pillarHeight = (int) ((BITMAP_HEIGHT* 0.5 * (PILLAR_HEIGHT_MIN + PILLAR_HEIGHT_MAX)) + 
					rand.nextGaussian() * 0.3 *
					(BITMAP_HEIGHT * PILLAR_HEIGHT_MAX - BITMAP_HEIGHT * PILLAR_HEIGHT_MIN));
			paint.setColor(pillarColor[rand.nextInt(pillarColor.length)]);
			
			if( i + PILLAR_WIDTH <= stage.getWidth() - stageBackPillarShift ){
				canvas.drawCircle(
						i + PILLAR_WIDTH/2, 
						BITMAP_HEIGHT - pillarHeight, 
						PILLAR_WIDTH/2, 
						paint);
				canvas.drawRect(
						i, 
						BITMAP_HEIGHT - pillarHeight, 
						i+PILLAR_WIDTH, 
						BITMAP_HEIGHT, 
						paint);
			}
			else{
				stagePillarShift = stage.getWidth() - i;
			}
			
			i += rand.nextInt((int) PILLAR_SPACE) + PILLAR_SPACE;
		}
		
		/*
		 * Draw clouds
		 */
		Drawable cloud1Tile = context.getResources().getDrawable(R.drawable.cloud1);
		Drawable cloud1ShadeTile = context.getResources().getDrawable(R.drawable.cloud1_shade);

		Bitmap cloud1Bitmap = ((BitmapDrawable)cloud1Tile).getBitmap();
		Bitmap cloud1ShadeBitmap = ((BitmapDrawable)cloud1ShadeTile).getBitmap();
		
		for(int i = 0; i< stage.getWidth(); i += CLOUD_WIDTH_MAX * CLOUD_SPACE_RATIO){
			Random rand = new Random();
			if(i + CLOUD_WIDTH_MIN*1.5 <= stage.getWidth()){
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
				
				i += CLOUD_WIDTH_MIN * CLOUD_SPACE_RATIO * rand.nextDouble();
			}
		}
		
		drawGroundAndSubterranean(canvas, 0);
		drawGrass(canvas, 0);
		drawBush(canvas, 0);
		drawTree(canvas, 0);
		
		thread.start();
	}

	private void drawBush(Canvas canvas, int start){
		for(int i = (int) (start + BUSH_WIDTH); i < stage.getWidth() - BUSH_WIDTH * BUSH_SPACE_COUNT; i++){
			Random rand = new Random();
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

			i += BUSH_SPACE_COUNT * BUSH_WIDTH + BUSH_SPACE_COUNT * BUSH_WIDTH * rand.nextDouble();
		}
	}
	private void drawTree(Canvas canvas, int start){
		for(int i = (int) (start + TREE_CROWN_WIDTH); i < stage.getWidth() - TREE_SPACE_COUNT * TREE_CROWN_WIDTH; i ++){
			Random rand = new Random();
			float rootHeightMax = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT - GROUND_SECTION_HEIGHT - GROUND_HEIGHT);
			float rootHeightMin = (float) (rootHeightMax + GROUND_HEIGHT - GRASS_HEIGHT);

			float rootHeight = (float) (rootHeightMax + rand.nextDouble()*(rootHeightMin - rootHeightMax));
			
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
			
			i += TREE_SPACE_COUNT * TREE_CROWN_WIDTH + TREE_CROWN_WIDTH * TREE_SPACE_COUNT * rand.nextDouble();
		}
	}
	private void drawGrass(Canvas canvas, int start){
		for(int i = start; i < stage.getWidth(); i ++){
			Random rand = new Random();
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
	}
	
	private void drawGroundAndSubterranean(Canvas canvas, int start){		
		/*
		 * Draw subterranean layer
		 */
		float subterraneanX1 = start;
		float subterraneanY1 = (float) (stage.getHeight() - SUBTERRANEAN_HEIGHT);
		float subterraneanX2 = stage.getWidth();
		float subterraneanY2 = stage.getHeight();
		Paint subterraneanPaint = new Paint();
		subterraneanPaint.setColor(SUBTERRANEAN_COLOR);
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
		float groundSectionX2 = stage.getWidth();
		float groundSectionY2 = subterraneanY1;
		Paint groundSectionPaint = new Paint();
		groundSectionPaint.setColor(GROUND_SECTION_COLOR);
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
		float groundX2 = stage.getWidth();
		float groundY2 = groundSectionY1;
		Paint groundPaint = new Paint();
		groundPaint.setColor(GROUND_COLOR);
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
		float groundBorderX2 = stage.getWidth();
		float groundBorderY2 = subterraneanY1;
		Paint groundBorderPaint = new Paint();
		groundBorderPaint.setColor(GROUND_BORDER_COLOR);
		canvas.drawLine(groundBorderX1, groundBorderY1, groundBorderX2, groundBorderY2, groundBorderPaint);
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

}
