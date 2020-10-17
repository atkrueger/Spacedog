package atk.spacedog;

import android.graphics.Color;
import android.graphics.RectF;

/**
 * Created by Alex on 3/8/2017.
 */

public class Util {

    public static float randFloat(float min, float max){
        return min + (float) Math.random() * (max-min);
    }

    public static int randInt(int min, int max){
        return min + (int)(Math.random() * (max-min));
    }

    public static int randColor(){
        return Color.argb(255, (int)(Math.random()*255),
                (int)(Math.random()*255), (int)(Math.random()*255));
    }

    public static float distance(float x1, float y1, float x2, float y2){

        double xdist = Math.pow(x1-x2,2);
        double ydist = Math.pow(y1-y2,2);
        return (float) Math.pow(xdist + ydist,0.5);
    }

    public static void centerAt(RectF rect, float x, float y){
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
