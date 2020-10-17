package atk.spacedog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GameView extends SurfaceView implements Runnable{

    //structure for running game
    Thread gameThread = null;
    SurfaceHolder ourHolder; // helps maintain drawing to surface with double-buffering
    volatile boolean playing;
    Canvas canvas;
    Paint textPaint;
    BitmapLoader bitmapLoader;
    Context app_context; //might be used later to change its contentview
    SharedPreferences sharedPref;
    SharedPreferences.Editor settingsEditor;
    boolean volumeAllowed; //whether player gave permission to use volume


    //fps tracking
    private long timeThisFrame;
    long fps=60; //default value, will actually be tracked dynamically
    long[] hist_fps;
    final int FPS_FRAMES_TRACKED = 100;
    int hist_fps_index = 0;
    float dt = 0;

    //screen size info
    int screenwidth;
    int screenheight;

    //Volume reading
    int avg_volume = 888; //default value if unchanged
    int avg_frequency = 666;
    VolumeReader volumeReader;

    //pressure reading, in case user doesn't want to use volume
    boolean usePressure; //allows player to use touch pressure instead of volume if necessary
    volatile double pressure = 1111; // default value
    double[] hist_pressure;
    final int PRESSURE_FRAMES_TRACKED = 5;
    int hist_pressure_index;
    volatile double avg_pressure;


    //background stars
    Stars stars; final int NUM_STARS = 20; final int STAR_WIDTH = 4;

    //game objects all contained in a single lvlHandler object
    LevelHandler lvlHandler;
    int maxLevelReached; //maximum level player as previously reached
            //will allow them to start at levels they have already reached
            //in options
    public static final int NUM_LEVELS = 5;

    //game logic
    boolean dachs_hit = false; //whether dachshund has been hit by an obstacle
    boolean paused = false;

    //for status of game
    volatile int game_status;
    public static final int INTRO = 100;
    public static final int REQUEST_NEWGAME = 101;
    public static final int PLAYING = 102;
    public static final int LOST_GAME = 103;
    public static final int REQUEST_CALIBRATE = 104;
    public static final int CALIBRATING = 105;
    public static final int GAME_OVER_SCREEN = 106;
    public static final int FINISH_CALIBRATE = 107;
    public static final int WON_GAME = 108;
    public static final int SKIP_SCREEN = 109;
    public static final int ACHIEVEMENTS = 110;
    public static final int EXIT_ACHIEVEMENTS = 111;
    public static final int TUTORIAL = 112;
    public static final int EXIT_TUTORIAL =113;
    //lower statuses reserved for skiptoLevel input


    //game font
    final Typeface GAME_FONT = Typeface.createFromAsset(getContext().getAssets(), "visitor1.ttf");

    //buttons and sliders
    Calibrator calibrator;
    GameOverButtons gameOverButtons;
    Intro intro;
    SkipToLevel levelSkipper;
    Achievements achievements;
    Button exitButton;

    //hearts
    Hearts hearts;

    public GameView(Context context, Display display, boolean recordAllowed){
        super(context);

        //shared preferences for saving level progress
        sharedPref = context.getSharedPreferences(
                "SpaceDogPreferences", Context.MODE_PRIVATE);
        settingsEditor = sharedPref.edit();



        volumeAllowed = recordAllowed;
        usePressure=false;

        app_context = context; //can you use this to set its content display to something else?
                                //e.g. if you have a "IntroView" and "CalibrationView" to switch between?
        ourHolder = getHolder();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x; screenheight = size.y;
        achievements = new Achievements(screenwidth, screenheight, GAME_FONT, NUM_LEVELS, sharedPref);

        bitmapLoader = new BitmapLoader(context, display);
        volumeReader = new VolumeReader();

        //historical fps tracker initialization
        hist_fps = new long[FPS_FRAMES_TRACKED];
        for (int i = 0; i < FPS_FRAMES_TRACKED; i++){hist_fps[i]=0;}

        stars = new Stars(NUM_STARS, STAR_WIDTH, screenwidth, screenheight);

        // add level handler, which handles all the game objects for you
        lvlHandler = new LevelHandler(bitmapLoader, screenwidth, screenheight, GAME_FONT, context, achievements);
        lvlHandler.setLevel(0, true); //blank level

        //Gameover buttons: calibrate and new game
        gameOverButtons = new GameOverButtons(screenwidth, screenheight, GAME_FONT);

        intro = new Intro(screenwidth, screenheight, GAME_FONT);

        //hearts
        hearts = new Hearts(bitmapLoader.getBitmap("heart"));

        paused =false; //game starts active, not paused now
        intro.active = true; //starts active to begin the game
        game_status = INTRO;

        textPaint  = new Paint();
        textPaint.setColor(Color.argb(255,255,255,255));
        textPaint.setTextSize(45);



        //Calibration sliders - can probably create these in a button factory class
        calibrator = new Calibrator(screenwidth, screenheight, GAME_FONT, sharedPref);

        maxLevelReached = sharedPref.getInt("maxlevelreached", 1);
        levelSkipper = new SkipToLevel(screenwidth, screenheight, GAME_FONT,NUM_LEVELS);

        //exit button
        float exit_width = screenwidth * 0.08f;
        float exit_height = exit_width;
        float exit_left = screenwidth - exit_width;
        float exit_top = screenheight - exit_height;
        RectF exit_rect = new RectF(exit_left, exit_top, exit_left+exit_width, exit_top+exit_height);
        exitButton = new Button(exit_rect, "X", GAME_FONT, "exit");
        exitButton.visible=false;

    }

    private void recordPressure(double reading){
        if(hist_pressure_index>=PRESSURE_FRAMES_TRACKED){hist_pressure_index=0;}
        hist_pressure[hist_pressure_index]=reading;
        hist_pressure_index+=1;
    }

    private double getAvgPressure(){
        double sum=0;
        for(int i = 0;i<PRESSURE_FRAMES_TRACKED;i++){
            sum+= hist_pressure[i];
        }
        return sum/PRESSURE_FRAMES_TRACKED;
    }

    private int pressureToVolume(double pressure){
        //converts pressure, mostly on a 0-1 scale, to a volume estimate to pipe into evertyhing else
        return 0;
    }


    private long getAvgFPS(){
        long sum = 0;
        for (int i = 0 ; i < FPS_FRAMES_TRACKED; i++){sum += hist_fps[i];}
        return sum/FPS_FRAMES_TRACKED;
    }
    private void updateFPSIndex(long fps){
        hist_fps[hist_fps_index] = fps; hist_fps_index++;
        if(hist_fps_index ==FPS_FRAMES_TRACKED){hist_fps_index=0;}
    }

    public void toggleStatus(){

        if(game_status>0 && game_status <=NUM_LEVELS){
            //SkipTolevel sent you to this level
            levelSkipper.turnOff();
            newGame(game_status);
        } else {
            switch (game_status) {
                case REQUEST_NEWGAME:
                    lvlHandler.clearFarts();
                    gameOverButtons.active=false;
                    newGame(1);
                    break;
                case WON_GAME:
                    hearts.clear();
                    lvlHandler.clearInstructions();
                    gameOver();
                    break;
                case LOST_GAME:
                    paused=true;
                    Log.d("pause", "set true");
                    lvlHandler.clearInstructions();
                    gameOver();
                    break;
                case REQUEST_CALIBRATE:
                    gameOverButtons.active=false;
                    calibrate();
                    break;
                case FINISH_CALIBRATE:
                    gameOver();
                    break;
                case SKIP_SCREEN:
                    levelSkipper.turnOn(maxLevelReached);
                    intro.active=false;
                    lvlHandler.setLevel(0, true);
                    paused=false;
                    gameOverButtons.active = false;
                    break;
                case ACHIEVEMENTS:
                    paused=false;
                    gameOverButtons.active=false;
                    achievements.active=true;
                    lvlHandler.setLevel(0, true);
                    break;
                case EXIT_ACHIEVEMENTS:
                    achievements.active=false;
                    gameOverButtons.active=true;
                    break;

                case TUTORIAL:
                    gameOverButtons.active=false;
                    newGame(lvlHandler.PRACTICE_LEVEL);
                    exitButton.visible=true;
                    break;

                case EXIT_TUTORIAL:
                    hearts.clear();
                    exitButton.visible=false;
                    gameOverButtons.active=true;
                    lvlHandler.setLevel(0, true);
            }
        }
    }


    @Override
    public void run() { //game loop - updates game logic and draws
        while(playing){
            long startFrameTime = System.currentTimeMillis();

            toggleStatus();
            update();

            boolean drewSuccessfully = draw();
            if(!drewSuccessfully){
                try{gameThread.sleep(1);} //slow thread down if trying to draw too fast
                catch(InterruptedException e){};
            }

            //track time
            timeThisFrame = System.currentTimeMillis()-startFrameTime;
            dt = (float)(timeThisFrame/1000.0); //time elapsed in seconds
            if (timeThisFrame >=1){fps=1000/timeThisFrame;}
            updateFPSIndex(fps);
        }
    }

    private void update(){ //updates game logic

        stars.update(dt); //always update stars regardless

        avg_volume = volumeReader.getAvgVolume(1);//most recent frame only

        if(!paused) { //update dachshund and obstacles only if the game is not stopped

            //update game objects
            lvlHandler.update(dt, avg_volume, calibrator);
            if (lvlHandler.won_game){
                game_status = WON_GAME;
            }
            hearts.addHearts(lvlHandler.healthBallsCaught());
            if (lvlHandler.dachsHit()) {
                hearts.loseHeart();
                if (hearts.remaining <= 0 && lvlHandler.level!=lvlHandler.PRACTICE_LEVEL){game_status = LOST_GAME;}
            }

        }
    }


    private boolean draw(){ //returns true only if successfully drew
        if(ourHolder.getSurface().isValid()){
            canvas = ourHolder.lockCanvas();

            //background
            if(dachs_hit && !paused){ canvas.drawColor(Util.randColor());} //random background
            else {canvas.drawColor(Color.argb(255,0,0,0));} //black background

            //stars, game objects
            stars.draw(canvas);
            lvlHandler.draw(canvas);
            hearts.draw(canvas);

            // buttons and titles
            calibrator.draw(canvas); //will actually draw only if it is active
            gameOverButtons.draw(canvas);
            intro.draw(canvas);
            levelSkipper.draw(canvas);
            achievements.draw(canvas);
            if(exitButton.visible){exitButton.draw(canvas);}

            //status text
            //canvas.drawText("FPS: " + Long.toString(getAvgFPS()), screenwidth - 300, 40,textPaint); //fps
            //canvas.drawText("Volume: " + String.valueOf(avg_volume), 40, 40, textPaint);
            //canvas.drawText("Frequency: " + String.valueOf(avg_frequency), 40, 100, textPaint);
            //canvas.drawText("Pressure: " + String.valueOf(avg_pressure), 40,40,textPaint);

            //post the canvas
            //Log.d("gpu", "canvas hardware accelerated?:" + String.valueOf(canvas.isHardwareAccelerated()));
            ourHolder.unlockCanvasAndPost(canvas);
            return true;
        } else{
            return false; //failed to draw successfully b/c surface was not valid
        }
    }

    private void gameOver(){
        //paused = true;
        gameOverButtons.active = true;
        maxLevelReached = Math.max(maxLevelReached,lvlHandler.level);
        if(maxLevelReached>NUM_LEVELS){maxLevelReached= NUM_LEVELS;}
        settingsEditor.putInt("maxlevelreached", maxLevelReached);
        settingsEditor.commit();
        game_status = GAME_OVER_SCREEN;
    }

    private void newGame(int level){
        dachs_hit=false;
        paused = false;
        lvlHandler.clearTunnel();
        lvlHandler.setLevel(level, true); //true indicates it is a newGame for achievement purposes
        hearts.reset();
        gameOverButtons.active = false;

        intro.active = false;
        game_status = PLAYING;

    }

    private void calibrate(){
        dachs_hit = false;
        paused = false;
        lvlHandler.setLevel(0, true); //calibration level
        gameOverButtons.active =false;

        calibrator.active = true;
        game_status = CALIBRATING;
    }


    //control of pausing and resuming game
    public void pause(){
        playing = false;
        if(volumeAllowed){volumeReader.stopRecording();}
        try{
            if(gameThread!=null){gameThread.join();} //stop the game thread
        } catch(InterruptedException e){Log.e("Error", "joining thread");}
    }

    public void resume(){
        playing = true;
        if(volumeAllowed){volumeReader.startRecording();}
        gameThread = new Thread(this); //uses this class's "run" function
        gameThread.start();
        //Log.d("gpuview", String.valueOf(isHardwareAccelerated()));
    }

    public boolean onTouchEvent(MotionEvent m){

        //read pressure
        //pressure = m.getPressure();

        //calibrators
        if(calibrator.update(m) == Calibrator.FINISHED){
            calibrator.active=false;
            game_status = FINISH_CALIBRATE;
        }

        //gameover buttons
        int gameOverResult = gameOverButtons.update(m);
        if(gameOverResult==GameOverButtons.NEWGAME){
            gameOverButtons.active=false;
            if(maxLevelReached>1){
                game_status = SKIP_SCREEN;
            } else{game_status = REQUEST_NEWGAME;}
        } else if (gameOverResult==GameOverButtons.CALIBRATE){
            game_status = REQUEST_CALIBRATE;
        } else if (gameOverResult == GameOverButtons.SKIP){
            game_status = SKIP_SCREEN;
        } else if (gameOverResult == GameOverButtons.ACHIEVEMENTS){
            game_status = ACHIEVEMENTS;
        } else if(gameOverResult == GameOverButtons.TUTORIAL){
            game_status = TUTORIAL;
        }

        //start button
        if(intro.update(m)==Intro.START){
            intro.active=false;
            if(maxLevelReached>1){
                game_status=SKIP_SCREEN;
            }else{
                game_status = REQUEST_NEWGAME;
            }

        }

        //SkipToLevel
        int level_result = levelSkipper.update(m);
        if(level_result>0){
            game_status = level_result;
        }

        //Achievement
        if(achievements.update(m)==Achievements.EXIT){
            game_status = EXIT_ACHIEVEMENTS;
        }

        //tutorial exit button
        if(exitButton.visible){
            int action =  m.getAction() & MotionEvent.ACTION_MASK;
            if(action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_DOWN){
                int cur_pointer_index = m.getActionIndex();
                int x = (int)m.getX(cur_pointer_index);
                int y = (int)m.getY(cur_pointer_index);
                if(exitButton.clicked(x,y)){
                    game_status = EXIT_TUTORIAL;
                }
            }
        }

        return true;
    }

}
