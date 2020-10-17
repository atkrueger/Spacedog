package atk.spacedog;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

import java.util.ArrayList;

public class GameOverButtons {

    ArrayList<Button> buttons;
    boolean active;
    Paint paint;

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;
    public static final int NEWGAME = 2;
    public static final int CALIBRATE = 3;
    public static final int SKIP = 4;
    public static final int ACHIEVEMENTS = 5;
    public static final int TUTORIAL = 6;

    public GameOverButtons(int screenwidth, int screenheight, Typeface font){

        active = false;

        float sens_height = screenheight /6;
        float sens_width = screenwidth * 0.3f;
        float sens_left = screenwidth * 1/8 - sens_width/2;
        float sens_top = screenheight/2 - sens_height/2;
        RectF sens_rect = new RectF(sens_left, sens_top, sens_left+sens_width, sens_top+sens_height);

        buttons = new ArrayList<Button>();
        //add "newgame" button
        RectF newgame_rect = new RectF(sens_rect);
        newgame_rect.offsetTo(screenwidth/2 - sens_width/2, (screenheight * 1/8) - sens_height/2);
        Button newgame_button = new Button(newgame_rect, "New Game", font, "newgame");

        //add skiptolevel button
        /*
        RectF skip_rect = new RectF(sens_rect);
        skip_rect.offsetTo(screenwidth/2 - sens_width/2, (screenheight/2 - sens_height/2));
        Button skip_button = new Button(skip_rect, "Skip To Level", font, "skip");
        */

        RectF achieve_rect = new RectF(sens_rect);
        achieve_rect.offsetTo(screenwidth/2 - sens_width/2, (screenheight * 3/8) - sens_height/2);
        Button achieve_button = new Button(achieve_rect, "Achievements", font, "achievements");


        //add "calibrate" button
        RectF calib_rect = new RectF(sens_rect);
        calib_rect.offsetTo(screenwidth/2 - sens_width/2, (screenheight * 5/8) - sens_height/2);
        Button calib_button = new Button(calib_rect, "Calibrate", font, "calibrate");

        //add "tutorial" button
        RectF tut_rect = new RectF(sens_rect);
        tut_rect.offsetTo(screenwidth/2 - sens_width/2, (screenheight * 7/8) - sens_height/2);
        Button tut_button = new Button(tut_rect, "Tutorial", font, "tutorial");

        buttons.add(newgame_button);
        buttons.add(calib_button);
        buttons.add(achieve_button);
        buttons.add(tut_button);
        //buttons.add(skip_button);

        paint = new Paint();
    }

    public void draw(Canvas canvas){
        if(!active){return;}
        for(Button b: buttons){
            canvas.drawBitmap(b.bitmap,b.getLeft(), b.getTop(),paint);
        }
    }

    public int update(MotionEvent m){
        if(!active){return INACTIVE;}
        int status = ACTIVE; //default status

        int action =  m.getAction() & MotionEvent.ACTION_MASK;
        int cur_pointer_index = m.getActionIndex();
        int cur_pointer_id = m.getPointerId(cur_pointer_index);
        int x; int y;

        switch (action){

            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                x = (int)m.getX(cur_pointer_index);
                y = (int)m.getY(cur_pointer_index);

                for(Button button:buttons) {
                    if(button.clicked(x,y)) {
                        if(button.button_id =="newgame"){status = NEWGAME;}
                        if(button.button_id =="calibrate"){status = CALIBRATE;}
                        if(button.button_id == "achievements"){status = ACHIEVEMENTS;}
                        if(button.button_id == "tutorial"){status = TUTORIAL;}
                    }
                }
                break;
            //end action down events
        }
        return status;
    }
}
