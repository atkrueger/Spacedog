package atk.spacedog;

//handles calibrating the volume sensitivity, threshold, and gravity


import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

import java.util.ArrayList;

public class Calibrator {

    ArrayList<Button> sliders;
    Button finish;
    Paint paint;
    SharedPreferences.Editor settingsEditor;
    Label sensLabel;//instructions
    Label gravLabel;
    Button help;

    boolean instructionsVisible;

    //volume defaults and mutable values for sensitivity, gravity, and threshold
    final float DEFAULT_SENSITIVITY = 7f;
    float sensitivity = DEFAULT_SENSITIVITY; //might want to store this eventually
    final float DEFAULT_GRAVITY = 1.4f;
    float gravity = DEFAULT_GRAVITY;
    final int DEFAULT_THRESHOLD = 40;
    int threshold = DEFAULT_THRESHOLD;

    public static final int INACTIVE = 0;
    public static final int CALIBRATING = 1;
    public static final int FINISHED = 2;

    int screenwidth;
    int screenheight;

    boolean active; //whether calibrator is currently active

    public Calibrator(int swidth, int sheight, Typeface font, SharedPreferences sharedPref){
        screenwidth = swidth; screenheight = sheight;

        sliders = new ArrayList<Button>();
        float sens_height = screenheight /6;
        float sens_width = screenwidth * 1/5;
        float sens_left = screenwidth * 1/8 - sens_width/2;
        float sens_top = screenheight/2 - sens_height/2;
        RectF sens_rect = new RectF(sens_left, sens_top, sens_left+sens_width, sens_top+sens_height);
        Button sens_slider = new Button(sens_rect,"Sensitivity", font, "sensitivity");

        //noise_floor button
        RectF floor_rect = new RectF(sens_rect);
        floor_rect.offsetTo(screenwidth * 3/8 - sens_width/2, screenheight/2 - sens_height/2);
        Button floor_slider = new Button(floor_rect, "Noise Floor", font, "noisefloor");

        //gravity slider
        RectF gravity_rect = new RectF(sens_rect);
        gravity_rect.offsetTo(screenwidth * 5/8 - sens_width/2, screenheight/2 - sens_height/2);
        Button gravity_slider = new Button(gravity_rect, "Gravity", font, "gravity");

        //finished button
        RectF finished_rect = new RectF(sens_rect);
        finished_rect.offsetTo(screenwidth * 7/8 - sens_width/2, screenheight/2 - sens_height/2);
        finish = new Button(finished_rect, "Finished",font, "calibfinish");

        sliders.add(floor_slider);sliders.add(sens_slider); sliders.add(gravity_slider);
        paint = new Paint();

        //modify paint for the calibration slider lines
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.argb(255,255,255,255));

        active = false;

        //load in values from the sharedPreferences
        gravity = sharedPref.getFloat("gravity", DEFAULT_GRAVITY);
        sensitivity = sharedPref.getFloat("sensitivity", DEFAULT_SENSITIVITY);
        threshold = sharedPref.getInt("threshold", DEFAULT_THRESHOLD);
        settingsEditor = sharedPref.edit();

        //adjust starting slider positions accordingly using inverse functions
        float gravity_input = gravity / (gravity + DEFAULT_GRAVITY);
        float sensitivity_input = sensitivity / (sensitivity + DEFAULT_SENSITIVITY);
        float threshold_input = (float) threshold / (float) (threshold + DEFAULT_THRESHOLD);

        float gravity_y = screenheight * (1-gravity_input);
        float sensitivity_y = screenheight * (1-sensitivity_input);
        float threshold_y = screenheight * (1-threshold_input);

        gravity_slider.rect.offsetTo(gravity_slider.rect.left, gravity_y - gravity_slider.rect.height()/2);
        sens_slider.rect.offsetTo(sens_slider.rect.left, sensitivity_y - sens_slider.rect.height()/2);
        floor_slider.rect.offsetTo(floor_slider.rect.left, threshold_y - floor_slider.rect.height()/2);

        //instruction labels
        float label_width = screenwidth * 0.95f;
        float label_height = screenheight * 0.25f;

        float slabel_top = screenheight * 0.25f - label_height/2;
        float slabel_left = screenwidth/2 - label_width/2;

        RectF slabel_rect = new RectF(slabel_left, slabel_top, slabel_left+label_width, slabel_top + label_height);

        sensLabel = new Label(slabel_rect,"Slide Sensitivity Up to Make Spacedog Jump Higher", font);

        RectF glabel_rect = new RectF(slabel_rect);
        float glabel_top = screenheight * 0.75f - label_height/2;
        float glabel_left = screenwidth/2 - label_width/2;
        glabel_rect.offsetTo(glabel_left, glabel_top);
        gravLabel = new Label(glabel_rect,"Slide Gravity Up to Make Spacedog Fall Faster", font);
        instructionsVisible=false;

        //help button
        float help_width = sens_width / 2;
        float help_height = sens_height;
        float help_left = finished_rect.centerX()-help_width/2;
        float help_top = screenheight - help_height;
        RectF help_rect = new RectF(help_left, help_top, help_left+help_width, help_top + help_height);
        //RectF help_rect = new RectF(sens_rect);
        //float help_left = finished_rect.left;
        //float help_top = screenheight - help_rect.height();
        //help_rect.offsetTo(help_left, help_top);
        help = new Button(help_rect,"Help",font,"help");

    }

    public void draw(Canvas canvas){
        if(active) {
            canvas.drawBitmap(finish.bitmap, finish.getLeft(), finish.getTop(), paint);
            canvas.drawBitmap(help.bitmap, help.getLeft(), help.getTop(), paint);
            for (Button slider : sliders) {
                canvas.drawBitmap(slider.bitmap, slider.getLeft(), slider.getTop(), paint);

                //vertical lines on the sliders to indicate how to move them
                canvas.drawLine(slider.midX(), 0, slider.midX(),slider.getTop(),paint);
                canvas.drawLine(slider.midX(), slider.getBottom(), slider.midX(),screenheight,paint);

            }
            if(instructionsVisible){
                canvas.drawBitmap(sensLabel.bitmap, sensLabel.getLeft(), sensLabel.getTop(),paint);
                canvas.drawBitmap(gravLabel.bitmap, gravLabel.getLeft(), gravLabel.getTop(),paint);
            }

        }
    }

    public int update(MotionEvent m){
        //returns true only if player clicked "finish"

        if(!active){return INACTIVE;} //just stop function if not currently active
        int status = CALIBRATING;

        int cur_pointer_index = m.getActionIndex();
        int cur_pointer_id = m.getPointerId(cur_pointer_index);
        int x; int y;

        int action =  m.getAction() & MotionEvent.ACTION_MASK;
        switch (action){

            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                x = (int)m.getX(cur_pointer_index);
                y = (int)m.getY(cur_pointer_index);

                for(Button slider: sliders){ //this assumes they are all vertical sliding sliders, can add tha variable if necessary
                    if(slider.clicked(x,y)){
                        slider.is_sliding = true;
                        slider.pointer_id = cur_pointer_id;
                        slider.offset =  y - slider.getTop();
                    }
                }
                if(finish.clicked(x,y)){
                    status = FINISHED;
                    settingsEditor.commit();  //save settings
                }
                if(help.clicked(x,y)){
                    instructionsVisible = !instructionsVisible;
                }
                break;
            //end action down events

            case MotionEvent.ACTION_MOVE: //user might be sliding sliders around

                int button_index;
                float button_y;
                float button_input;
                float button_output;
                ArrayList<String> button_ids_updated = new ArrayList<String>();
                ArrayList<Float> button_outputs = new ArrayList<Float>();
                for(Button button: sliders){
                    if(button.is_sliding){
                        button_index = m.findPointerIndex(button.pointer_id);
                        x = (int) m.getX(button_index); y = (int) m.getY(button_index);
                        button.rect.offsetTo(button.getLeft(), y - button.offset);

                        //keep slider between top and bottom
                        if(button.getTop()<0){button.rect.offsetTo(button.getLeft(),0);}
                        if(button.rect.bottom>screenheight)
                        {button.rect.offsetTo(button.getLeft(),screenheight-button.rect.height());}

                        //now adjust sensitivity function accordingly
                        button_y = button.rect.centerY();
                        button_input = (screenheight - button_y) / screenheight; //% of screen, 0 = bottom top = 1
                        button_input = Math.min((float)0.999,button_input);//caps to prevent division by zero
                        button_output = button_input / (1-button_input);

                        //save id of button updated and output, so you can adjust the appropriate variable
                        button_ids_updated.add(button.button_id);
                        button_outputs.add(button_output);
                    }
                }
                for(int i = 0; i < button_ids_updated.size(); i++){
                    switch (button_ids_updated.get(i)){
                        case "sensitivity":
                            sensitivity = DEFAULT_SENSITIVITY * button_outputs.get(i);
                            settingsEditor.putFloat("sensitivity", sensitivity);
                            break;
                        case "noisefloor":
                            threshold = (int)(DEFAULT_THRESHOLD * button_outputs.get(i));
                            settingsEditor.putInt("threshold", threshold);
                            break;
                        case "gravity":
                            gravity = DEFAULT_GRAVITY * button_outputs.get(i);
                            settingsEditor.putFloat("gravity", gravity);
                            break;
                    }
                }
                break;
            //end movement

            //handle player lifting finger from screen
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                for(Button slider: sliders){
                    if(cur_pointer_id ==slider.pointer_id){
                        slider.is_sliding=false;
                        slider.pointer_id = -1;
                    }
                }
                break;
            //end player lifting finger from screen
        }
        return status;
    }



}
