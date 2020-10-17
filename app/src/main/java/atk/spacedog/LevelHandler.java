package atk.spacedog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

import java.util.ArrayList;

//this class handles all aspects of updating the level, such as adding a new obstacle, changing the boss, and stuff like that
public class LevelHandler {
    private double seconds_elapsed; //time since the start of the level
    public int level; //current level
    private BitmapLoader b;
    int screenwidth; int screenheight;



    Achievements achievements;
    boolean hit_this_run = false;
    boolean hit_this_level = false;

    float dt;
    Paint paint;

    //game Objects
    private ArrayList<Obstacle> obstacles;
    private Balls balls;
    Dachshund dachs;
    Farts farts;
    Boss boss;
    Tunnel tunnel;

    //instructions
    Label instructions;
    Typeface font;
    RectF instruct_rect;
    double instruct_time_left;

    int last_sec_updated; //records the last second unit updated

    boolean won_game; //player has won the game
    boolean dachs_hit;
    boolean invincible_hit; //invincible b/c hit
    boolean invincible_ball;  //invincibile b/c caught a ball
    double invincibility_counter;
    final double INVINC_HIT_TIME = 1; //invincibile for 1 second after being hit
    final double INVINC_BALL_TIME =5; //invincible for 10 seconds after catching an invincibility ball

    double hballs_per_sec; //chance of a random health ball
    double iballs_per_sec; //chance of a random invincibilitiy ball

    boolean dachs_visible = true;


    public LevelHandler(BitmapLoader bitmapLoader,  int swidth, int sheight, Typeface gamefont, Context context, Achievements a){
        level = 0; //default
        won_game=false;
        seconds_elapsed = 0; //default
        invincible_hit=false;
        invincible_ball=false;
        b = bitmapLoader;
        screenwidth = swidth; screenheight = sheight;
        obstacles = new ArrayList<Obstacle>();
        balls = new Balls(swidth, sheight);
        paint = new Paint();
        paint.setColor(Color.argb(255, 255, 255, 255));

        hballs_per_sec = 0;
        iballs_per_sec = 0;

        //dachshund
        dachs = new Dachshund(bitmapLoader, screenwidth, screenheight);

        //farts
        farts = new Farts(screenwidth, screenheight, dachs);

        //boss
        boss = null;

        //tunnels
        tunnel = new Tunnel(swidth, sheight);

        //instructions
        instructions = null;

        float instruct_width = screenwidth * (float) 0.8;
        float instruct_height = screenheight / (float)2.5;
        float instruct_left = screenwidth/2 -instruct_width/2;
        float instruct_top = screenheight /4;

        instruct_rect = new RectF(instruct_left, instruct_top, instruct_left+instruct_width, instruct_top + instruct_height);

        font = gamefont;
        last_sec_updated = -1;
        achievements = a;

        //creating duck so sounds are loaded automatically
       // duck = new Duck(swidth, sheight,b,dachs,context);

    }

    public void createInstruction(String text, double time){
        instructions = new Label(instruct_rect, text, font);
        instruct_time_left = time;
    }
    public void updateInstructions(){
        if(instructions !=null){
            instruct_time_left-=dt;
            if(instruct_time_left <=0){
                instructions = null;//clear them
            }
        }
    }

    public void setLevel(int new_level, boolean newGame){
        dachs_visible = true;
        won_game=false;
        iballs_per_sec=0; hballs_per_sec=0;
        level = new_level;
        seconds_elapsed = 0;
        obstacles.clear(); //clean the set of obstacles

        boss = null;
        last_sec_updated = -1;
        dachs_hit=false;
        if(new_level <=1){
            invincible_ball=false;
            invincible_hit=false;
            tunnel.clear();
        }

        //set Achievement inProgress variables
        if(new_level ==1){
            achievements.inProgressComplete=true;
            achievements.inProgressCompletePerfect=true;
            achievements.inProgressLevel[0] = true;
            hit_this_run=false;
            hit_this_level=false;

        } else if (new_level >=2 && new_level <=5){
            achievements.inProgressLevel[new_level-1]= true;
            hit_this_level=false;
            if(newGame){
                achievements.inProgressComplete=false;
                achievements.inProgressCompletePerfect=false;
            }
        }
    }

    public void clearFarts(){
        farts.clear();
    }
    public void clearInstructions(){
        instructions= null;
    }
    public void clearTunnel(){tunnel.clear();}

    public void update(double time_elapsed, int volume, Calibrator calibrator){
        dt = (float)(time_elapsed);
        seconds_elapsed +=dt;

        progressLevel(); //adds new obstacles, etc. along the timeline

        moveObstacles();
        dachs.update(dt, volume, calibrator.sensitivity, calibrator.threshold, calibrator.gravity);
        farts.update(dt, volume, calibrator.threshold, calibrator.sensitivity);
        tunnel.update(dt);
        balls.update(dt, hballs_per_sec, iballs_per_sec);

        if(boss!=null){boss.update(dt);}
        fartBossCollide();

        updateInstructions();
        if(boss != null && !boss.alive && level != PRACTICE_LEVEL){
            //check achievements
            if(hit_this_level==false){achievements.recordPerfectLevel(level);}

            if(level==5 && achievements.inProgressComplete){
                boolean perfect = hit_this_run? false: true;
                achievements.recordCompleteRun(perfect);
            }
            createInstruction("Boss Defeated", 1);
            setLevel(level+1, false); //indicating not a new game
        }

        if(invincBallsCaught()>0){
            invincible_ball = true;
            invincible_hit=false;
            invincibility_counter = INVINC_BALL_TIME;
        }


        //handling of dachshund being hit and health balls being caught
        //is done by GameView so it can handle the hearts simultaneously
    }

    public boolean dachsHit(){
        dachs_hit = checkDachsHit();
        if(dachs_hit){
            hit_this_run=true;
            hit_this_level=true;
        }
        if (invincible_hit || invincible_ball){
            invincibility_counter -= dt;
            if (invincibility_counter <=0) {
                invincible_hit=false;
                invincible_ball=false;
            }
            return false;
        } else if (dachs_hit) { //dachs hit and not invincibile
            invincible_hit = true;
            invincibility_counter = INVINC_HIT_TIME;
            return true;
        }
        return false;
    }

    public void draw(Canvas canvas){

        for(Obstacle o: obstacles){
            canvas.drawBitmap(o.getBitmap(),o.getLeft(), o.getTop(),paint);
        }
        balls.draw(canvas);
        farts.draw(canvas);

        // instructions
        if(instructions!=null) {
            canvas.drawBitmap(instructions.bitmap, instructions.getLeft(), instructions.getTop(), paint);
        }

        //boss
        if(boss!=null){boss.draw(canvas);}

        dachs.draw(canvas, invincible_ball,dachs_hit, dachs_visible);
        tunnel.draw(canvas);


    }

    private void moveObstacles(){
        for (int i = obstacles.size()-1; i>=0; i--){
            obstacles.get(i).update(dt); //move them

            //clean dead ones
            if(obstacles.get(i).lives <=0){
                obstacles.remove(i);
            }
        }

    }

    private void setObstacleDeath(){
        for(Obstacle o: obstacles){
            o.lives=1;
        }
    }

    private void fartBossCollide(){ //handles collision of farts and boss
        if (boss==null){return;}
        if( boss.image_rect.left > dachs.getLeft()){return;} //boss too far to the right to be hit

        for(int i = farts.rects.size()-1;i>=0; i--){
            if(RectF.intersects(farts.rects.get(i),boss.hit_rects[0])){
                boss.hitByFart();
                farts.rects.remove(i);
            }
        }

    }

    private void addObstacle(String bitHash, int mtype, int lives, double speed){
        boolean spaceship = bitHash == "spaceship"? true: false;
        obstacles.add(new Obstacle(b.getBitmap(bitHash), mtype, lives, speed, screenwidth, screenheight, spaceship));
    }

    private void addBall(int balltype, double speed){
        balls.addBall((float)speed,balltype);
    }

    private boolean checkDachsHit(){
        for (Obstacle obstacle : obstacles) {
            if (RectF.intersects(dachs.hit_rects[0], obstacle.hit_rects[0])) {
                return true;
            }
        }
        if(boss!=null){
            if(RectF.intersects(dachs.hit_rects[0], boss.hit_rects[0])){
                return true;
            }
        }

        if(tunnel.hitDachs(dachs)){return true;}

        return false;
    }

    public int healthBallsCaught(){ //checks whether dachs caught any balls, if so, returns number, also deletes caught balls
       return balls.healthBallsCaught(dachs);
    }

    public int invincBallsCaught(){
        return balls.invincBallsCaught(dachs);
    }



    public static final int BLANK_LEVEL = 0;
    public static final int PRACTICE_LEVEL = 999;
    private void progressLevel(){
        int seconds = (int) seconds_elapsed;
        if (seconds <= last_sec_updated){return;} //no further progress if still on the last second
        //prevents it from adding it multiple times
        if(level ==BLANK_LEVEL) {
            //calibration, don't add any obstacles
        }
        if (level==1){

            switch(seconds){
                case 1:
                    createInstruction("Make Noise to Fart Yourself Upwards", 2);
                    last_sec_updated = seconds; break;

                case 4:
                    createInstruction("Catch tennis balls", 2);
                    addBall(Ball.HEALTH,0.7);
                    hballs_per_sec = 0.2;
                    iballs_per_sec = 0.2;
                    last_sec_updated=seconds; break;

                case 7:
                    createInstruction("Dodge everything else to survive", 2);
                    addObstacle("spaceship", Obstacle.HORIZONTAL, 9999, 0.5);
                    addObstacle("spaceship", Obstacle.DIAGONAL, 9999, 0.6);
                    //addObstacle("spaceship", Obstacle.OSCILLATE, 999, 0.6);
                    last_sec_updated = seconds; break;

                /*
                case 11: case 14:
                    addObstacle("spaceship", Obstacle.DIAGONAL, 9999, 0.5);
                    last_sec_updated = seconds; break;
                    */

                case 16:
                    setObstacleDeath();
                    last_sec_updated = seconds; break;

                case 18:
                    boss = new Pig(screenwidth, screenheight,b,dachs);
                    last_sec_updated = seconds; break;

                case 20:
                    hballs_per_sec = 0; //prevents camping out on the balls
                    iballs_per_sec = 0.1;
                    createInstruction("Kill it with your farts",2);
                    last_sec_updated = seconds; break;

            }
        }//end level 1

        if(level==2){
            switch(seconds){
                case 2:
                    createInstruction("Level 2", 2);
                    last_sec_updated = seconds; break;

                case 4:
                    hballs_per_sec = 0.2;
                    iballs_per_sec = 0.2;
                    tunnel.addSection(3,0.95);
                    last_sec_updated = seconds;break;

                case 9:
                    tunnel.addSection(1, 0.8);
                    last_sec_updated = seconds;break;

                case 11:
                    tunnel.addSection(2, 0.9);
                    last_sec_updated = seconds; break;


                case 15:
                    hballs_per_sec=0;
                    boss = new Homing(screenwidth, screenheight, b, dachs);
                    last_sec_updated = seconds; break;

            }
        }//end Level 2

        if(level==3){
            switch(seconds){
                case 2:
                    createInstruction("Level 3", 2);
                    hballs_per_sec = 0.5;
                    iballs_per_sec = 0.3;
                    last_sec_updated = seconds; break;

                case 3:
                    for(int i =0; i <1; i++){
                        addObstacle("spaceship", Obstacle.DIAGONAL,999, 1);
                    }
                    last_sec_updated = seconds; break;

                case 5:
                    tunnel.addSection(0.5,0.7);
                    last_sec_updated = seconds; break;

                case 7:
                    tunnel.addSection(2, 0.85);
                    last_sec_updated = seconds; break;

                case 13:
                    tunnel.addSection(4, 0.9);
                    last_sec_updated = seconds; break;

                case 18:
                    boss = new BossDachs(screenwidth, screenheight, b, dachs);
                    last_sec_updated = seconds; break;

                case 20:
                    setObstacleDeath();
                    hballs_per_sec = 0;
                    iballs_per_sec = 0.1;
                    tunnel.addSection(7,0.95);
                    last_sec_updated = seconds; break;


            }
        }//end level 3

        if(level ==4){
            switch(seconds) {
                case 2:
                    hballs_per_sec = 0.4;
                    iballs_per_sec = 0.2;
                    createInstruction("Level 4", 2);
                    last_sec_updated = seconds; break;

                case 4:case 5:case 6:
                    addObstacle("bird",Obstacle.SWIRL,10,0.9);
                    last_sec_updated = seconds; break;

                case 11:
                    tunnel.addSection(1,0.9);
                    last_sec_updated = seconds; break;

                case 13:
                    setObstacleDeath();
                    last_sec_updated= seconds;break;

                case 15:
                    createInstruction("Abra Cadabra!", 1);
                    dachs_visible = false;
                    boss = new Magician(screenwidth, screenheight, b, dachs);
                    hballs_per_sec=0;
                    iballs_per_sec=0.1;
                    last_sec_updated = seconds; break;

                case 16:
                    last_sec_updated = seconds; break;

            }
        }//end level 4

        if(level==5){
            switch(seconds){
                case 2:
                    hballs_per_sec = 0.2;
                    iballs_per_sec = 0.2;
                    createInstruction("Level 5", 2);
                    last_sec_updated = seconds; break;


                case 4:
                    addObstacle("bird", Obstacle.SWIRL, 3, 0.6);
                    last_sec_updated = seconds; break;

                case 5:
                    tunnel.addSection(2, 0.9);
                    last_sec_updated = seconds; break;


                case 7:case 10:
                    addObstacle("spaceship", Obstacle.OSCILLATE, 5, 0.5);
                    last_sec_updated = seconds; break;

                case 13:
                    tunnel.addSection(1, 0.9);
                    last_sec_updated = seconds; break;

                case 16:
                    tunnel.addSection(0.25, 0.7);
                    last_sec_updated = seconds; break;

                case 18:
                    createInstruction("Final Boss", 1.5);
                    last_sec_updated = seconds; break;
                case 20:
                    boss = new Shooter(screenwidth, screenheight, b, dachs, obstacles);
                    last_sec_updated = seconds; break;

                case 26:case 30:case 34:case 38:
                    tunnel.addSection(0.3, 0.9);
                    last_sec_updated = seconds; break;


            }


        }

        if(level ==6){
            switch(seconds){
                case 3:
                    createInstruction("Victory", 2);
                    last_sec_updated = seconds;
                    break;


                case 5:
                    won_game = true;
                    last_sec_updated = seconds;
                    break;
                //end game
            }
        }

        if(level ==PRACTICE_LEVEL){
           // Log.d("lvlhandler", "practice level entered");
            switch(seconds){

                case 1:
                    Log.d("lvlhandler", "case 1 entered");
                    createInstruction("Noise makes spacedog go higher", 2);
                    last_sec_updated = seconds; break;


                case 3:
                    createInstruction("If you have trouble controlling spacedog...", 3);
                    last_sec_updated = seconds; break;

                case 6:
                    createInstruction("Select Calibrate and adjust the sliders", 3);
                    last_sec_updated = seconds; break;

                case 10:
                    createInstruction("White tennis balls restore hearts", 3);
                    hballs_per_sec = 0.2;
                    last_sec_updated = seconds; break;

                case 11:case12: case13:
                    addBall(Ball.HEALTH,0.7);
                    last_sec_updated = seconds; break;

                case 14:
                    addObstacle("spaceship",Obstacle.HORIZONTAL, 999,0.6);
                    createInstruction("Hitting obstacles loses hearts", 2);
                    last_sec_updated = seconds; break;


                case 16:case 17:
                    addObstacle("spaceship", Obstacle.DIAGONAL, 999,0.6);
                    last_sec_updated = seconds; break;

                case 19:
                    createInstruction("Flashing tennis balls make Spacedog invincible", 3);
                    iballs_per_sec = 0.2;
                    last_sec_updated = seconds; break;

                case 20:case 21: case 22:
                    addBall(Ball.INVINC, 0.6);
                    last_sec_updated = seconds; break;

                case 25:
                    setObstacleDeath();
                    boss = new Pig(screenwidth, screenheight,b,dachs);
                    last_sec_updated = seconds; break;

                case 26:
                    createInstruction("Bosses turn red and lose health they hit farts", 4);
                    last_sec_updated = seconds; break;

                case 31:
                    createInstruction("Bosses become more transparent as they lose health", 4);
                    last_sec_updated = seconds; break;

                case 36:
                    createInstruction("When you defeat a boss, you move onto the next level", 4);
                    last_sec_updated = seconds; break;

                case 40:
                    tunnel.addSection(1, 0.9);
                    addObstacle("spaceship", Obstacle.OSCILLATE, 999, 0.6);
                    last_sec_updated = seconds; break;

                case 43:
                    createInstruction("This practice level will continue until you press X", 2);
                    last_sec_updated = seconds; break;

            }//end switch statement

            //infinite random enemies and tunnels
            if(seconds > 44 && seconds> last_sec_updated){
                if(seconds % 4 ==0){  //every 4 seconds, add a random tunnel
                    tunnel.addSection(Util.randFloat(0.5f,2f),Util.randFloat(0.7f,0.95f));
                }
                if(seconds % 7 ==0){
                    addObstacle("spaceship", Util.randInt(Obstacle.HORIZONTAL, Obstacle.SWIRL),
                            Util.randInt(1,3), //lives
                            Util.randFloat(0.6f, 1.1f)); //speed
                }

                if(seconds % 25 ==0){ //add the pig every 25 seconds
                    boss = new Pig(screenwidth, screenheight,b, dachs);
                }
                last_sec_updated = seconds;
            }


        }

    }
}
