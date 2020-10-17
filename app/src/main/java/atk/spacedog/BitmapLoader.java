package atk.spacedog;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;

import java.util.HashMap;



public class BitmapLoader {
    //loads bitmaps to scale and stores them to be read later

    private HashMap<String,Bitmap> bitmap_hash; //stores the loaded bitmaps
    private BitmapFactory.Options options; //stores the options of the loader
    private Context context;
    private float screenwidth;
    private float screenheight;
    private float screenarea;
    private float screendiag;

    public BitmapLoader(Context c, Display display){
        context = c;

        //get display size
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;
        screenarea = screenwidth * screenheight;
        screendiag = (float)(Math.sqrt(Math.pow(screenwidth,2) + Math.pow(screenheight,2)));

        options = new BitmapFactory.Options();
        options.inScaled = false; //prevents blurring of the pixel art

        bitmap_hash = new HashMap<String,Bitmap>();

        preLoadBitmaps();

        //for game, may want to load all images at runtime here
    }

    private void preLoadBitmaps(){
        //preloads any bitmaps you want to automatically load when the BitmapLoader object is created
        //good for preventing the loading from slowing down the game during the game
        saveScaledBitmap(R.drawable.dachshund1, "dachshund", 0.007);
        saveScaledBitmap(R.drawable.dachshund4hit,"dachshit",0.007);
        saveScaledBitmap(R.drawable.dachsflipped, "dachshundflipx", 0.007);
        saveScaledBitmap(R.drawable.dachsflippedhit, "dachshundflipxhit", 0.007);
        saveScaledBitmap(R.drawable.dachsunhitflipy, "dachshundflipy", 0.007);
        saveScaledBitmap(R.drawable.dachsunhitflipxy, "dachshundflipxy", 0.007);
        saveScaledBitmap(R.drawable.dachshitflipy, "dachshitflipy", 0.007);
        saveScaledBitmap(R.drawable.dachshitflipxy, "dachshitflipxy", 0.007);


        saveScaledBitmap(R.drawable.spaceship1, "spaceship", 0.01);
        saveScaledBitmap(R.drawable.heart1,"heart", 0.002);
        saveScaledBitmap(R.drawable.tennis_ball2,"tennisball", 0.003);
        saveScaledBitmap(R.drawable.pig1,"pigunhit", 0.02);
        saveScaledBitmap(R.drawable.pig_hit1, "pighit", 0.02);
        saveScaledBitmap(R.drawable.pixel_cat1, "catunhit", 0.02);
        saveScaledBitmap(R.drawable.pixel_cat1hit, "cathit", 0.02);
        //saveScaledBitmap(R.drawable.cheese3, "cheese", 0.007);
        saveScaledBitmap(R.drawable.bird2, "bird", 0.008);

        saveScaledBitmap(R.drawable.dachsflipped,"dachsboss",0.01);
        saveScaledBitmap(R.drawable.dachsflippedhit, "dachsbosshit", 0.01);

        saveScaledBitmap(R.drawable.rabbit1, "rabbit", 0.01);
        saveScaledBitmap(R.drawable.rabbit1hit, "rabbithit", 0.01);

        saveScaledBitmap(R.drawable.duck1,"duck", 0.015);
        saveScaledBitmap(R.drawable.duck1hit,"duckhit", 0.015);
        saveScaledBitmap(R.drawable.duck1closed, "duckclosed", 0.015);
        saveScaledBitmap(R.drawable.duck1hitclosed, "duckhitclosed", 0.015);

        saveScaledBitmap(R.drawable.boss5, "boss5", 0.02);
        saveScaledBitmap(R.drawable.boss5hit, "boss5hit", 0.02);

        saveScaledBitmap(R.drawable.boss5flipx, "boss5flipx", 0.02);
        saveScaledBitmap(R.drawable.boss5hitflipx, "boss5hitflipx", 0.02);

        saveScaledBitmap(R.drawable.boss5flipy, "boss5flipy", 0.02);
        saveScaledBitmap(R.drawable.boss5hitflipy, "boss5hitflipy", 0.02);

        saveScaledBitmap(R.drawable.boss5flipxy, "boss5flipxy", 0.02);
        saveScaledBitmap(R.drawable.boss5hitflipxy, "boss5hitflipxy", 0.02);

        saveScaledBitmap(R.drawable.bullet, "bullet", 0.001);
        saveScaledBitmap(R.drawable.shuttlemoving, "shuttlemoving", 0.02);
        saveScaledBitmap(R.drawable.shuttlestationary, "shuttlestationary", 0.02);
        saveScaledBitmap(R.drawable.shuttlestationaryhit, "shuttlestationaryhit", 0.02);
        saveScaledBitmap(R.drawable.shuttlemovinghit, "shuttlemovinghit", 0.02);


    }

    public void saveScaledBitmap(int id, String hashkey, double pOfScreen){
        //id is resource id, like R.drawable.dachshund1.png
        //pOfScreen is the percent of the screen you want the bitmap to occupy

        //load bitmap
        Bitmap input_bitmap = BitmapFactory.decodeResource(context.getResources(), id, options);

        //scale bitmap
        double default_area = input_bitmap.getWidth() * input_bitmap.getHeight();
        double desired_area = pOfScreen * screenarea;
        double side_multiplier = Math.sqrt(desired_area / default_area);
        //square root b/c will multiply both width and height by the side_multiplier
        int desired_width = (int)(input_bitmap.getWidth() * side_multiplier);
        int desired_height = (int)(input_bitmap.getHeight() * side_multiplier);
        Bitmap scaled_bitmap = Bitmap.createScaledBitmap(input_bitmap,desired_width, desired_height, false);

        //save bitmap
        bitmap_hash.put(hashkey, scaled_bitmap);
    }

    public Bitmap getBitmap(String hashkey){
        return bitmap_hash.get(hashkey);
    }


}
