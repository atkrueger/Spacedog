package atk.spacedog;


import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

public class Obstacle {

    //private attributes
    Bitmap default_bitmap;
    RectF image_rect;
    RectF[] hit_rects;
    int move_type;
    int lives;
    double speed; //calculated in percentage of screen per second

    float xvector; //calculated in pixels per second, so just divide by fps
    float yvector;

    private float curAngle; //for the swirl obstacle
    private static final float ANGLE_PER_SEC = (float) Math.toRadians(270);


    //move type constants
    public static final int HORIZONTAL = 1;
    public static final int DIAGONAL = 2;
    public static final int SWIRL = 3;
    public static final int OSCILLATE = 4;

    //screen info for updating itself
    float screenwidth;
    float screenheight;

    private boolean spaceship = false; //true if it is a spaceship, false otherwise



    public Obstacle(Bitmap b, int mtype, int life, double spd, float swidth, float sheight, boolean ship){
        default_bitmap = b;
        screenwidth = swidth;
        screenheight = sheight;
        image_rect = new RectF(0,0,b.getWidth(), b.getHeight()); //default value, will be reset

        //for now, just make the hitbox rect the same as the image rect
        hit_rects = new RectF[1];
        hit_rects[0] = new RectF(image_rect);

        lives = life+1; //1 additional b/c first reset will lower it to zero
        move_type = mtype;
        speed = spd;

        spaceship = ship;

        reset();
    }

    public Obstacle(Bitmap b, int mtype, int life, double spd, float swidth, float sheight)  //bitmap should be pre-scaled by BitmapLoader
    {
        this(b, mtype, life, spd, swidth, sheight, false);
        /*
        default_bitmap = b;
        screenwidth = swidth;
        screenheight = sheight;
        image_rect = new RectF(0,0,b.getWidth(), b.getHeight()); //default value, will be reset

        //for now, just make the hitbox rect the same as the image rect
        hit_rects = new RectF[1];
        hit_rects[0] = image_rect;

        lives = life+1; //1 additional b/c first reset will lower it to zero
        move_type = mtype;
        speed = spd;

        reset();
        */
    }

    public void reset(){

        lives--;
        //updates_since_reset = 0;

        if (move_type ==HORIZONTAL){
            double roll = Math.random();
            if(roll >=0.5){
                moveTo(screenwidth + image_rect.width()*2, screenheight - image_rect.height());
            }else{
                moveTo(screenwidth + image_rect.width()*2, image_rect.height()/2);
            }
            xvector = (float)(-1 * speed * screenwidth);
            //xvector = 0;
            yvector = 0;
        } else if (move_type == DIAGONAL){
            //random diagnoal movement, but trying to make sure it always reached the end of the screen
            //Java's math function is all in radians, so better stick to that
            // in radians, dead right is 0, then straight up is pi/2, left is pi, etc.
            //the line going from the obstacles current location (x,y) to the top left (0,0)
                //is therefore at angle pi - atan(y/x), with atan = inverse tan function
            //and the line going from obstacle current to the bottom left is
                //angle pi + atan((screenheight-y)/x) with

            moveTo(screenwidth + image_rect.width()*2, (float)Math.random()*screenheight);
            double pi = 3.14;
            double min_angle = pi - Math.atan(image_rect.centerY()/image_rect.left);
            double max_angle = pi + Math.atan((screenheight - image_rect.centerY())/image_rect.left);
            double rand = Math.random();
            double new_angle = rand * (max_angle - min_angle) + min_angle;
            xvector = (float)(speed * screenwidth * Math.cos(new_angle));
            yvector = (float)(-1 * speed * screenwidth * Math.sin(new_angle)); // bc up is negative

        } else if (move_type == SWIRL || move_type == OSCILLATE){
            //swirls around, while moving left
            moveTo(screenwidth + image_rect.width()*2, (float)Math.random()*screenheight);
            xvector = (float)(-1 * speed * screenwidth);
            curAngle = (float) (Math.random() * 2 * 3.14);//random starting angle
            yvector = (float) (screenheight * 0.8);
        }

    }

    private void updateHitRects(){ //will be more complicated with multiple hit_rects
        if(spaceship){ //special hit rect to make it more accurate
            hit_rects[0].left = image_rect.left + (3.0f/25.0f)*image_rect.width();
            hit_rects[0].right = image_rect.right - (3.0f/25.0f)*image_rect.width();
            hit_rects[0].top = image_rect.top;
            hit_rects[0].bottom = image_rect.bottom;
        }else {
            centerAt(hit_rects[0], image_rect.centerX(), image_rect.centerY());
        }
    }

    protected void move(float dx, float dy){
        image_rect.offset(dx,dy);
        updateHitRects();
    }

    protected void moveTo(float centerx, float centery){
        centerAt(image_rect,centerx, centery);
        updateHitRects();
    }

    public void update(double dt){
        float t= (float) dt;

        if(move_type == OSCILLATE){
            curAngle+=ANGLE_PER_SEC * t;
            float yswirl = yvector * (float) Math.sin(curAngle);
            move(xvector *t, yswirl * t);
        } else if (move_type == SWIRL){
            curAngle+= ANGLE_PER_SEC * t;
            float yswirl = yvector * (float) Math.sin(curAngle);
            float xswirl = xvector + yvector * (float) Math.cos(curAngle);
            move(xswirl * t, yswirl * t);
        }
        else{
            move(xvector*t, yvector*t);
        }
        //updates_since_reset++;
        if(image_rect.right <0){
            reset();}
    }

    public float getLeft(){return image_rect.left;}
    public float getTop(){return image_rect.top;}

    public Bitmap getBitmap(){ //with an animation, will depend on frame
        return default_bitmap;
    }

    private void centerAt(RectF rect, float x, float y){
        float width = rect.width();
        float height = rect.height();

        float new_left = x - width/2;
        float new_top = y - height/2;

        rect.left = new_left;
        rect.right = new_left + width;
        rect.top = new_top;
        rect.bottom = new_top + height;

    }

}
