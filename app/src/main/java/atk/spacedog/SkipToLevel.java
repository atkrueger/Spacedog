package atk.spacedog;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by Alex on 3/9/2017.
 */

//handles player desire to skip to a particular level
public class SkipToLevel {

    private boolean active;
    private ArrayList<Button> buttons;
    private Paint paint;
    private int maxLevel;
    private Label skipLabel;


    public static final int INACTIVE = -1;
    public static final int ACTIVE = 0;
    //rest are set to level numbers


    public SkipToLevel(int screenwidth, int screenheight, Typeface font, int numLevels){
        //maybe take in the gameView to call newGame on it??? probably best to just send info though
        active = false;
        buttons = new ArrayList<Button>();

        //creating button rectangle dimensions
        float rect_width = screenwidth/ (numLevels +1);
        float total_margin = screenwidth - rect_width * numLevels;
        float num_margins = numLevels+1;
        float margin_size = total_margin / num_margins;

        float rect_height = screenheight * 0.3f;
        float rect_top = screenheight *0.7f - rect_height/2;

        RectF cur_rect;
        Button cur_button;
        float curx;
        for (int i=0; i<numLevels;i++){
            curx = margin_size * (1+i) + i*rect_width;
            cur_rect = new RectF(curx, rect_top,curx+rect_width, rect_top+rect_height);
            cur_button = new Button(cur_rect,String.valueOf(i+1), font,String.valueOf(i+1));
            buttons.add(cur_button);
        }

        float label_width = screenwidth * 0.7f;
        float label_height = screenheight * 0.3f;
        float label_top = screenheight * 0.33f - label_height/2;
        float label_left = screenwidth/2 - label_width/2;
        RectF labelRect = new RectF(label_left, label_top, label_left+label_width, label_top + label_height);

        skipLabel = new Label(labelRect,"Start at Level:", font);

        paint = new Paint();
    }

    public void draw(Canvas canvas){
        if(!active){return;}
        Button b;
        for(int i = 0; i<maxLevel; i++){  //display only the ones that are unlocked
            b = buttons.get(i);
            canvas.drawBitmap(b.bitmap,b.getLeft(), b.getTop(),paint);
        }
        canvas.drawBitmap(skipLabel.bitmap,skipLabel.getLeft(), skipLabel.getTop(),paint);
    }

    public int update(MotionEvent m){
        if(!active){return INACTIVE;}
        int status = ACTIVE; //default status

        int action =  m.getAction() & MotionEvent.ACTION_MASK;
        int cur_pointer_index = m.getActionIndex();
        int x; int y;

        switch (action){

            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                x = (int)m.getX(cur_pointer_index);
                y = (int)m.getY(cur_pointer_index);

                for(int i = 0; i <maxLevel;i++) { //check only the ones that are unlocked
                    Button button = buttons.get(i);
                    if (button.clicked(x, y)) {
                        status = Integer.valueOf(button.button_id); //level number chosen
                    }
                }
                break;
            //end action down events
        }
        return status;
    }

    public void turnOn(int maxLevelReached){
        active=true;
        maxLevel=maxLevelReached;
    }

    public void turnOff(){
        active=false;
    }
}
