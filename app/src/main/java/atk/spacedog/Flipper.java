package atk.spacedog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;


public class Flipper extends Boss {

    //should we make the image a dolphin, like flipper?
    //alternatively it could be an image that has a different meaning upside down

        int stage;
        float xvector;
        float yvector;
        Dachshund dachs;
        Random rand;
        Farts farts;
        Stars stars;
        int flipx;
        int flipy;

        private final float xFlipsPerSec = 0.2f;
        private final float yFlipsPerSec = 0.2f;
        private final float speed = 0.7f;
        private final float oscil_speed = 2f;
        private static final float ANGLE_PER_SEC = (float) Math.toRadians(720);

        private float curAngle;

        Bitmap[][][] bitmaps;
        //first index is whether hit, second whether flippedx, third whether flippedy

        public Flipper(float swidth, float sheight, BitmapLoader b, Dachshund d, Farts f, Stars s){
            super(swidth, sheight, b, d);

            //not animating this one, just flips
            bitmaps = new Bitmap[2][2][2];
            bitmaps[0][0][0] = b.getBitmap("boss5");
            bitmaps[1][0][0] = b.getBitmap("boss5hit");

            bitmaps[0][1][0] = b.getBitmap("boss5flipx");
            bitmaps[1][1][0] = b.getBitmap("boss5hitflipx");

            bitmaps[0][0][1] = b.getBitmap("boss5flipy");
            bitmaps[1][0][1] = b.getBitmap("boss5hitflipy");

            bitmaps[0][1][1] = b.getBitmap("boss5flipxy");
            bitmaps[1][1][1] = b.getBitmap("boss5hitflipxy");

            Bitmap def = bitmaps[0][0][0];
            image_rect = new RectF(0,0, def.getWidth(),
                    def.getHeight());

            //hitbox rectangles
            hit_rects = new RectF[1];
            updateHitRects();

            //lives
            max_life = 50;
            life = max_life;
            updateLifeBar();

            stage = BEGIN;

            name = "flipper";
            dachs = d;
            rand = new Random();
            farts = f;
            stars = s;

            flipx=1;
            flipy=1;

            //maybe change so that, once it gets to half of the screen, it makes a fast dart for your dog?

        }

        @Override
        public void draw(Canvas canvas){
            //Log.d("flipper", "draw called");
            int hiti = hit? 1:0;
            int flipxi = flipx==-1? 1:0;
            int flipyi = flipy==-1? 1:0;
            canvas.drawBitmap(bitmaps[hiti][flipxi][flipyi],image_rect.left, image_rect.top, paint);
        }

        private static final int BEGIN = 0;
        private static final int SWIM = 1;

        private void flipX(){
            flipx*=-1;
            moveTo(screenwidth - image_rect.centerX(), image_rect.centerY());
        }

        private void flipY(){
            flipy*=-1;
            moveTo(image_rect.centerX(), screenheight - image_rect.centerY());

        }

        @Override
        public void update(double t){
            //if it is a dolphin, it should probably be swimming up and down at an angled oscillation - see oscillator obstalce movement, but make the default vector diagnoal
            if(hit){
                hit_timer-=t;
                if(hit_timer<=0){
                    hit=false;
                    frame = 0; //reseting animation when boss no longer hit
                }
            }
            float dt = (float) t;

            /*
            //determine whether to flip the dachshund
            float flipXOddsThisFrame = xFlipsPerSec * dt;
            float flipXRoll = rand.nextFloat();
            if(flipXRoll <= flipXOddsThisFrame){
                dachs.flipX();
                farts.flipX();
                stars.flipX();
                flipX();
            }

            float flipYOddsThisFrame = yFlipsPerSec * dt;
            float flipYRoll = rand.nextFloat();
            if(flipYRoll < flipYOddsThisFrame){
                dachs.flipY();
                farts.flipY();
                stars.flipY();
                flipY();
            }
            */


            float x; float y;
            switch(stage){
                case BEGIN:

                    dachs.flipY();
                    farts.flipY();
                    stars.flipY();
                    flipY();

                    // 1. move to new location
                    if(flipx==1){
                        x = screenwidth + image_rect.width();
                    } else{
                        x = -image_rect.width();
                    }
                    y = Util.randFloat(0,screenheight);
                    moveTo(x,y);

                    // 2. set new diagonal angle
                    double pi = 3.14;
                    double min_angle = pi - Math.atan(image_rect.centerY()/image_rect.left);
                    double max_angle = pi + Math.atan((screenheight - image_rect.centerY())/image_rect.left);
                    double rand = Math.random();
                    double new_angle = rand * (max_angle - min_angle) + min_angle;
                    xvector = (float)(speed * screenwidth * Math.cos(new_angle));
                    yvector = (float)(-1 * speed * screenwidth * Math.sin(new_angle)); // bc up is negative

                    // 3. set angle that will change over time to random value
                    curAngle = (float) (Math.random() * 2 * 3.14);//random starting angle

                    stage = SWIM;
                    break;

                case SWIM:
                    //diagonal movement
                    move(xvector * dt * flipx, yvector * dt * flipy);

                    //oscillation movement
                    curAngle +=ANGLE_PER_SEC * dt;
                    float yswirl = oscil_speed * screenheight * (float) Math.sin(curAngle);
                    move(0, yswirl * dt * flipy);

                    //exited left of screen during normal movement
                    if(flipx==1 && image_rect.right < -1 * image_rect.width()*2){
                        stage = BEGIN;
                        //Log.d("flipper", "reset");
                    }
                    //exited right of screen
                    if(flipx==-1 && image_rect.left > screenwidth + image_rect.width()*2){
                        stage = BEGIN;
                    }

                    break;


            }
        }

        @Override
        public void updateHitRects(){
            hit_rects[0] = image_rect;
        }


        @Override
        protected void hitByFart(){
            super.hitByFart();
            if(!alive){ //just got killed, revert dachshund, farts back to normal orientation
                if(dachs.flippedX){
                    dachs.flipX();
                    farts.flipX();
                    stars.flipX();
                }
                if(dachs.flippedY){
                    dachs.flipY();
                    farts.flipY();
                    stars.flipY();
                }
            }
        }


}
