package atk.spacedog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by Alex on 3/5/2017.
 */

public class Hearts {

    //default starting number of heats, max, and remaining
    int starting = 5; //default value
    int remaining = 0;
    final int MAX = 10;

    RectF heart_rects[];
    Bitmap bitmap;
    Paint paint;



    public Hearts( Bitmap b){

        float heartw = b.getWidth();
        float hearth = b.getHeight();
        heart_rects = new RectF[MAX];
        float heart_top = (float)(hearth * 0.2); //buffer on top
        float heart_margin = (float)(heartw * 0.2); //buffer to the sides
        for(int i = 0; i <MAX; i++){
            heart_rects[i] = new RectF(heart_margin * (i+1) + heartw*i, heart_top,0,0); //right and bottom irrelevant for this
        }
        bitmap = b;
        paint = new Paint();
    }

    public void draw(Canvas canvas){
        for (int i = 0; i < remaining; i++) {
            canvas.drawBitmap(bitmap, heart_rects[i].left, heart_rects[i].top, paint);
        }
    }

    //for adding a heart
    public static final int MAX_HEARTS_REACHED = 0;
    public static final int HEART_ADDED = 1;
    public int addHeart(){
        if (remaining >= MAX){return MAX_HEARTS_REACHED;}
        else{
            remaining++;
            return HEART_ADDED;
        }
    }

    public int addHearts(int x){
        remaining+=x;
        if(remaining>MAX){
            remaining = MAX;
            return MAX_HEARTS_REACHED;
        } else{
            return HEART_ADDED;
        }
    }

    public void clear(){
        remaining=0;
    }

    public static final int ZERO_LEFT = 0;
    public static final int SOME_LEFT = 1;
    public int loseHeart(){
        remaining--;
        if(remaining <0){remaining=0;}

        if (remaining==0){return ZERO_LEFT;}
        else{return SOME_LEFT;}
    }

    public void reset(){
        remaining = starting;
    }

}
