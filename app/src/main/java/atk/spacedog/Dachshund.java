package atk.spacedog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by Alex on 3/1/2017.
 */

public class Dachshund {

    RectF image_rect; //rectangle of the image
    RectF[] hit_rects; //hitbox rectangles

    float screenwidth; float screenheight;

    private float x_center;

    Bitmap default_bitmap; //current bitmap to display, can change if there is a frame animation
    Bitmap hit_bitmap;

    Bitmap unhit_flippedx;
    Bitmap hit_flippedx;

    Bitmap unhit_flippedy;
    Bitmap hit_flippedy;

    Bitmap unhit_flippedxy;
    Bitmap hit_flippedxy;


    Paint paint;
    Paint randPaint;
    ColorMatrixColorFilter randfilter;

    boolean flippedX;
    boolean flippedY;

    public Dachshund(BitmapLoader b, float swidth, float sheight)  //bitmap should be pre-scaled by BitmapLoader
    {
        default_bitmap = b.getBitmap("dachshund");
        hit_bitmap = b.getBitmap("dachshit");
        unhit_flippedx = b.getBitmap("dachshundflipx");
        hit_flippedx = b.getBitmap("dachshundflipxhit");

        unhit_flippedy = b.getBitmap("dachshundflipy");
        hit_flippedy = b.getBitmap("dachshitflipy");

        unhit_flippedxy = b.getBitmap("dachshundflipxy");
        hit_flippedxy = b.getBitmap("dachshitflipxy");

        screenwidth = swidth;
        screenheight = sheight;
        image_rect = new RectF(0,0,default_bitmap.getWidth(), default_bitmap.getHeight()); //default value, will be reset

        //for now, just make the hitbox rect the same as the image rect
        hit_rects = new RectF[1];
        hit_rects[0] = image_rect;
        x_center = screenwidth /3;
        paint = new Paint();
        randPaint = new Paint();


        reset();
    }

    public void reset(){ //resets dachshund at default position
        flippedX = false;
        flippedY = false;
        centerAt(image_rect,x_center, screenheight/2);
        updateHitRects();
    }

    private void updateHitRects(){ //will be more complicated with multiple hit_rects
        centerAt(hit_rects[0], image_rect.centerX(), image_rect.centerY());
    }

    public void update(double dt, int volume, float sensitivity, int threshold, float gravity){
        //gravity should be measured in percentage of screen per second they are dropping
        float newY = image_rect.top;

        float yflip = flippedY? -1:1;
        //gravitational pull downwards
        newY+= gravity * screenheight * (float) dt * yflip ;

        //up with higher volume
        float yVolPerSecMax = (float)(screenheight*sensitivity); //maximum allowed movement per second
        float vol_cap =  1500;
        float vol_above_thresh = volume - threshold;
        float capped_vol = Math.min(vol_above_thresh, vol_cap);
        float vol_pct= capped_vol/vol_cap; //fixes range from neg infinity to 1
        float yVolUp = vol_pct * yVolPerSecMax * (float) dt;
        if(yVolUp>0){newY-=yVolUp * yflip;} //move dachshund up if above threshold

        //bound at top and bottom of screen
        if(newY<0){newY=0;}
        if(newY+image_rect.height()>screenheight){newY = screenheight-image_rect.height();}

        //adjust dachshund rectangle coordinates accordingly
        float newCenterY = newY + image_rect.height()/2;
        centerAt(image_rect,image_rect.centerX(), newCenterY);
        updateHitRects();
    }

    public float getLeft(){return image_rect.left;}
    public float getTop(){return image_rect.top;}

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

    private Bitmap getBitmap(boolean hit){
        Bitmap curBitmap = default_bitmap;
        if(hit){
            if(!flippedX && !flippedY){
                curBitmap = hit_bitmap;
            }
            else if(flippedX && !flippedY){
                curBitmap = hit_flippedx;
            }
            else if (flippedX && flippedY){
                curBitmap = hit_flippedxy;
            } else if (!flippedX && flippedY){
                curBitmap = hit_flippedy;
            }
        } else{ //not hit
            if(!flippedX && !flippedY){
                curBitmap = default_bitmap;
            }
            else if (flippedX && !flippedY){
                curBitmap = unhit_flippedx;
            } else if (flippedX && flippedY){
                curBitmap = unhit_flippedxy;
            } else if (!flippedX && flippedY){
                curBitmap = unhit_flippedy;
            }
        }
        return curBitmap;
    }

    public void flipX(){
        flippedX = !flippedX;
        centerAt(image_rect, screenwidth-image_rect.centerX(),image_rect.centerY());
        //move the dachsund accordingly
    }
    public void flipY(){
        flippedY = !flippedY;
        centerAt(image_rect, image_rect.centerX(), screenheight - image_rect.centerY());
        //move the dachshund accordingly
    }

    public void draw(Canvas canvas, boolean invincible, boolean hit, boolean visible){
        if(!visible){return;}

        if(invincible){
            //draw dachshund flashing
            randfilter = new ColorMatrixColorFilter(createMatrix());
            randPaint.setColorFilter(randfilter);
            canvas.drawBitmap(getBitmap(hit), image_rect.left, image_rect.top, randPaint);
        } else{
            canvas.drawBitmap(getBitmap(hit),image_rect.left, image_rect.top,paint);
        }

    }

    private float[] createMatrix(){
        //in model of colormatrix
        float[] src = new float[20];

        //color section of array
        for(int i = 0; i <15;i++){
            src[i]=Util.randFloat(-1,1);
            //should chance colors to random values
        }
        //transparency (alpha) section
        src[15]=0;
        src[16]=0;
        src[17]=0;
        src[18]=1; //maintains current transparency of each pixel
        src[19]=0;
        return src;
    }
}
