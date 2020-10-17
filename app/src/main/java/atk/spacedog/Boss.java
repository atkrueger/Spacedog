package atk.spacedog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Alex on 3/6/2017.
 */

public abstract class Boss {

    //animation
    ArrayList<Bitmap> unhit_frames;
    ArrayList<Bitmap> hit_frames;
    Bitmap cur_bitmap;
    int frame;
    float seconds_per_frame;
    RectF image_rect;
    float frame_timer;
    double hit_timer;
    protected final double HIT_LAG = 0.5; //will remain in "hit" appearance for at least 1/5 seconds after being hit
    boolean hit;


    //hitboxes
    RectF[] hit_rects;

    //life left
    int max_life;
    int life;
    float life_pct;
    RectF life_outline; //outline for the health bar - shows max health
    RectF life_bar; //remaining life
    Paint bar_paint;
    Paint outline_paint;
    float cur_bar_pct;

    boolean alive;

    //screen info
    float screenwidth;
    float screenheight;

    //status
    boolean active;

    Paint paint;

    String name;

    BitmapLoader bitmapLoader;

    Dachshund dachs; //keeping track of dachshund for homing purposes

    public Boss(float swidth, float sheight, BitmapLoader b, Dachshund d){
        //boss object is instatiated but not set to any boss
        screenwidth = swidth;
        screenheight = sheight;
        active = false;
        unhit_frames = new ArrayList<Bitmap>();
        hit_frames = new ArrayList<Bitmap>();
        bitmapLoader = b;
        dachs = d;

        unhit_frames = new ArrayList<Bitmap>();
        hit_frames = new ArrayList<Bitmap>();
        frame = 0;
        frame_timer = 0;

        float outline_width = screenwidth * (float) 0.6;
        float outline_height = screenheight * (float) 0.1;
        float outline_left = screenwidth/2 - outline_width/2;
        float outline_top = screenheight * (float) 0.9 - outline_height/2;
        life_outline = new RectF(outline_left, outline_top,
                outline_left+outline_width, outline_top+outline_height);

        life_bar = new RectF(life_outline);//starts the same
        bar_paint = new Paint();
        bar_paint.setStyle(Paint.Style.FILL);
        bar_paint.setStrokeWidth(10);
        bar_paint.setARGB(100,255,255,255); //partially transparent

        outline_paint = new Paint();
        outline_paint.setStyle(Paint.Style.STROKE);
        outline_paint.setARGB(255,255,255,255);
        cur_bar_pct = 1;

        alive = true;
        hit = false;
        hit_timer = 0;

        paint = new Paint();

    }

    protected void updateLifeBar(){
        life_pct = (float) life  / (float) max_life;
        life_bar.right = life_pct * life_outline.width() + life_outline.left;
    }

    public void draw(Canvas canvas){
        if(alive) {
            drawLifeBar(canvas);
            canvas.drawBitmap(cur_bitmap, image_rect.left, image_rect.top, paint);
        }

    }
    protected abstract void updateHitRects();
    public void update(double dt){
        if(hit){
            hit_timer-=dt;
            if(hit_timer<=0){
                hit=false;
                frame = 0; //reseting animation when boss no longer hit
            }
        }
        if(hit){cur_bitmap = hit_frames.get(frame);}
        else {cur_bitmap = unhit_frames.get(frame);}
    }


    protected void hitByFart(){
        life--;

        if(life<=0) {
            life=0;
            alive = false;
        } //handle death of boss
        hit= true;
        hit_timer = HIT_LAG;
        frame=0; //resetting animation when boss gets hit

        //making boss more transparent as it loses more life
        life_pct = (float) life  / (float) max_life;
        paint.setAlpha((int)(55+ 200 * life_pct));
        updateLifeBar();
    }

    protected void centerAt(RectF rect, float x, float y){
        float width = rect.width();
        float height = rect.height();

        float new_left = x - width/2;
        float new_top = y - height/2;

        rect.left = new_left;
        rect.right = new_left + width;
        rect.top = new_top;
        rect.bottom = new_top + height;

    }

    protected void drawLifeBar(Canvas canvas){
        /*
        canvas.drawRect(life_outline,outline_paint);
        canvas.drawRect(life_bar, bar_paint);
        */
    }

    protected void move(float dx, float dy){
        image_rect.offset(dx, dy);
        updateHitRects();
    }

    protected void moveTo(float x, float y){
        //center of this location
        centerAt(image_rect, x, y);
        updateHitRects();
    }


}
