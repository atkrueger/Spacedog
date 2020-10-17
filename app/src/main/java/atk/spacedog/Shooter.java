package atk.spacedog;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.ArrayList;

import static android.telephony.PhoneNumberUtils.WAIT;

public class Shooter extends Boss {
    //boss that shoots projectiles at you

    int stage;
    final float HORIZ_SPEED;
    final float TRACKING_SPEED;
    ArrayList<Obstacle> obstacles;

    private final int NUM_BULLETS = 3;
    private final double SECONDS_PER_BULLET = 0.5;
    private final float BULLET_SPEED = 0.9f;
    private double bullet_timer =0;
    private int bullets_fired = 0;

    private final double WAIT_TIME = 1;
    private double wait_counter = 0;

    Bitmap[][] bitmaps;
    //first index is whether hit, second is whether moving

    public Shooter(float swidth, float sheight, BitmapLoader b, Dachshund d, ArrayList<Obstacle> obs){
        super(swidth, sheight, b, d);

        //animation
        unhit_frames.add(bitmapLoader.getBitmap("shuttlemoving"));
        //unhit_frames.add(bitmapLoader.getBitmap("pighit")); //for now, to check if animation works

        hit_frames.add(bitmapLoader.getBitmap("shuttlestationary"));
        seconds_per_frame= 1;
        image_rect = new RectF(0,0, unhit_frames.get(0).getWidth(),
                unhit_frames.get(0).getHeight());

        cur_bitmap = unhit_frames.get(frame); //set default bitmap

        //first index is whether hit, second is whether moving
        bitmaps = new Bitmap[2][2];
        bitmaps[0][0] = bitmapLoader.getBitmap("shuttlestationary");
        bitmaps[0][1] = bitmapLoader.getBitmap("shuttlemoving");
        bitmaps[1][0] = bitmapLoader.getBitmap("shuttlestationaryhit");
        bitmaps[1][1] = bitmapLoader.getBitmap("shuttlemovinghit");

        //hitbox rectangles
        hit_rects = new RectF[1];
        updateHitRects();

        //lives
        max_life = 40;
        life = max_life;
        updateLifeBar();

        stage = BEGIN;

        HORIZ_SPEED = screenwidth * (float) 0.8;
        TRACKING_SPEED = screenheight * (float) 0.5;

        name = "shooter";
        obstacles = obs;

    }

    private static final int BEGIN = 0;
    private static final int ENTERING = 1;
    private static final int SHOOTING = 2;
    private static final int SWOOPING = 3;

    private void fireBullet(){
        float startx = image_rect.left + screenwidth*0.01f;
        float starty = image_rect.centerY();
        Bullet bullet = new Bullet(bitmapLoader.getBitmap("bullet"),
                BULLET_SPEED,screenwidth, screenheight,startx, starty);
        obstacles.add(bullet);
    }

    //override draw and make the bitmap stationary vs moving
    //then add hit aspect of it

    @Override
    public void draw(Canvas canvas){
        if(!alive){return;}
        int hiti = hit? 1:0;
        int movingi = (stage == ENTERING || stage == SWOOPING)? 1:0;

        canvas.drawBitmap(bitmaps[hiti][movingi],image_rect.left, image_rect.top, paint);
    }

    @Override
    public void update(double t){
        super.update(t);
        float dt = (float) t;

        switch(stage){
            case BEGIN: //set random new position
                moveTo(screenwidth + image_rect.width(),Util.randFloat(0,screenheight));
                stage = ENTERING; break;

            case ENTERING: //move to the left into shooting position
                move(-1* HORIZ_SPEED *dt, 0);

                if(image_rect.right<screenwidth * 0.95f){
                    stage = SHOOTING;
                }
                break;


            case SHOOTING:
                //sliding up and down tryign to chase the dog
                boolean dachs_started_above_boss = dachs.image_rect.centerY() < image_rect.centerY();
                int dir = dachs_started_above_boss? -1:1;


                float dy = dir * TRACKING_SPEED * dt;
                move(0, dy);

                //check if overshot, if so, center with the dachshund
                boolean dachs_ended_above_boss = dachs.image_rect.centerY() < image_rect.centerY();
                if(dachs_started_above_boss != dachs_ended_above_boss){
                    moveTo(image_rect.centerX(), dachs.image_rect.centerY());
                }

                //shooting bullets
                bullet_timer +=t;
                if(bullet_timer>= SECONDS_PER_BULLET){
                    bullet_timer=0;
                    bullets_fired++;
                    if(bullets_fired>NUM_BULLETS){
                        stage=SWOOPING;
                        bullets_fired = 0;
                    } else{
                        fireBullet();
                    }

                }
                //shoot three projectiles at the dachshund
                //shoot by adding particular projectiles to the obstacle list
                break;


            case SWOOPING:
                //swoop to the left - this is the dachshund's chance to attack it

                move(-1 * HORIZ_SPEED * dt, 0); //for now, just going left

                //has exited screen
                if(image_rect.right<0){
                    stage = WAIT;
                    hit=false;
                }
                break;

            case WAIT:
                //just wait off the screen
                wait_counter +=dt;
                if(wait_counter >= WAIT_TIME){
                    wait_counter = 0;
                    stage = BEGIN;
                }
                break;

        }
    }

    @Override
    public void updateHitRects(){
        hit_rects[0] = image_rect;
    }
}
