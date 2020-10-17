package atk.spacedog;


import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;

public class Achievements {
    //TO DO:

    //tracks achievements the player has earned

    //1. play game start to finish without dying
    //2-6. for each level, finish the level without being hit once
    //7. play game from start to finish without being hit once

    Label title;
    Button[] level_labels;
    Button complete_single_run_label;
    Button unhit_run_label;
    boolean active = false;
    Label leveldesc;

    Paint completePaint;
    Paint inPaint; //incomplete

    boolean complete_run;
    boolean[] levels_perfect;
    boolean complete_perfect;

    //variables that track whether player currently has an achievement in progress
    boolean inProgressComplete=false;
    boolean inProgressCompletePerfect= false;
    boolean inProgressLevel[];


    SharedPreferences.Editor achieveEditor;

    Button exit_button;

    public void recordPerfectLevel(int level){
        //returns true if it is a new achievement to be displayed

        levels_perfect[level-1]=true;
        achieveEditor.putBoolean("level" + String.valueOf(level) + "perf", true);
        achieveEditor.commit();
    }

    public void recordCompleteRun(boolean perfect){
        complete_run = true;
        achieveEditor.putBoolean("completerun", true);
        if(perfect){
            complete_perfect = true;
            achieveEditor.putBoolean("completeperfect", true);
        }
        achieveEditor.commit();

    }


    public Achievements(float screenwidth, float screenheight, Typeface font, int numLevels, SharedPreferences sharedPref){

        completePaint = new Paint();

        inPaint = new Paint();
        inPaint.setAlpha(100);
        inProgressLevel = new boolean[numLevels];
        for(int i = 0; i <numLevels; i++){
            inProgressLevel[i] =false;
        }


        //title at the top, says "Achievements"
        float title_width = screenwidth * 0.9f;
        float title_height = screenheight * 0.15f;
        float title_left = screenwidth/2 - title_width/2;
        float title_top = screenheight *0.01f;
        RectF title_rect = new RectF(title_left, title_top, title_left+title_width, title_top + title_height);
        title = new Label(title_rect,"Achievements", font);

        //label for "Complete All Levels in a Single Run"
        float run_width = screenwidth * 0.7f;
        float run_height = screenheight * 0.15f;
        float run_left = screenwidth/2 - run_width/2;
        float run_top = screenheight * 0.2f;
        RectF run_rect = new RectF(run_left, run_top, run_left +run_width, run_top + run_height);
        //complete_single_run_label = new Label(run_rect, "Complete All Levels in a Single Run", font);
        complete_single_run_label = new Button(run_rect,"Complete All Levels in a Single Run",
                font, "completesinglerun");


        //label for complete each level without being hit
        float desc_width = screenwidth * 0.8f;
        float desc_height = screenheight * 0.15f;
        float desc_left = screenwidth/2 - desc_width/2;
        float desc_top = screenheight * 0.37f;
        RectF desc_rect = new RectF(desc_left, desc_top, desc_left+desc_width, desc_top+desc_height);
        leveldesc = new Label(desc_rect, "Complete Each Level Without Being Hit:", font);

        //labels for complete each level w/o being hit once
        //level_labels = new Label[numLevels];
        level_labels = new Button[numLevels];
        float rect_width = screenwidth/ (numLevels +1);
        float total_margin = screenwidth - rect_width * numLevels;
        float num_margins = numLevels+1;
        float margin_size = total_margin / num_margins;

        float rect_height = screenheight * 0.2f;
        float rect_top = screenheight *0.52f;

        RectF cur_rect;
        Label cur_label;
        Button cur_button;
        float curx;
        for (int i=0; i<numLevels;i++){
            curx = margin_size * (1+i) + i*rect_width;
            cur_rect = new RectF(curx, rect_top,curx+rect_width, rect_top+rect_height);
            ///cur_label = new Label(cur_rect,String.valueOf(i+1), font);
            //level_labels[i] = cur_label;

            cur_button = new Button(cur_rect,String.valueOf(i+1), font, String.valueOf(i+1));
            level_labels[i] = cur_button;
        }

        //add a spot to show num_complete out of 7

        //label for "Complete All Levels in a Single Run Without Being Hit"
        RectF unhit_rect = new RectF(run_rect);
        unhit_rect.offsetTo(run_rect.left, screenheight * 0.8f);
        //complete_single_run_label = new Label(run_rect, "Complete All Levels in a Single Run", font);
        unhit_run_label = new Button(unhit_rect,"Complete Run Without Being Hit",
                font, "completesinglerun");

        //load in values from the sharedPreferences
        complete_run = sharedPref.getBoolean("completerun", false);
        levels_perfect = new boolean[numLevels];
        for(int i = 0; i <numLevels; i++){
            levels_perfect[i] = sharedPref.getBoolean("level" + String.valueOf(i+1) + "perf", false);
        }
        complete_perfect = sharedPref.getBoolean("completeperfect", false);
        achieveEditor = sharedPref.edit();

        //exit button
        float exit_width = screenwidth * 0.08f;
        float exit_height = exit_width;
        float exit_left = screenwidth - exit_width;
        float exit_top = screenheight - exit_height;
        RectF exit_rect = new RectF(exit_left, exit_top, exit_left+exit_width, exit_top+exit_height);
        exit_button = new Button(exit_rect, "X", font, "exit");
    }

    private int getComplete(){
        int sum = 0;
        if(complete_run){sum++;}
        if(complete_perfect){sum++;}
        for(int i = 0; i <levels_perfect.length;i++){
            if(levels_perfect[i]){sum++;}
        }
        return sum;
    }

    public void draw(Canvas canvas){
        if(!active){return;}
        title.draw(canvas);
        leveldesc.draw(canvas);
        exit_button.draw(canvas);


        //for actual achievements, paint in grey if incomplete, white if complete
        Paint p;
        p = complete_run? completePaint:inPaint;
        canvas.drawBitmap(complete_single_run_label.bitmap,
                    complete_single_run_label.getLeft(),
                    complete_single_run_label.getTop(), p);


        p = complete_perfect? completePaint:inPaint;
        canvas.drawBitmap(unhit_run_label.bitmap,
                unhit_run_label.getLeft(), unhit_run_label.getTop(), p);

        for(int i =0; i <levels_perfect.length; i++){
            p = levels_perfect[i]? completePaint: inPaint;
            canvas.drawBitmap(level_labels[i].bitmap,
                    level_labels[i].getLeft(), level_labels[i].getTop(), p);
        }

    }

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;
    public static final int  EXIT = 2;
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

                if(exit_button.clicked(x,y)){
                    status = EXIT;
                }
                break;
            //end action down events
        }
        return status;
    }
}
