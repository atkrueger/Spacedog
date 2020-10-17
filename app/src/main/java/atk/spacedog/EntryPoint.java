package atk.spacedog;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class EntryPoint extends Activity {

    GameView gameView; //handles all visual displays to the screen
    boolean record_permission = true; //default value
    private static final int AUDIO_RECORD_REQUEST = 1;
    private int numRejections = 0; //number of times the player has denied permission

    private boolean permissionRequested=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameView = new GameView(this, getWindowManager().getDefaultDisplay(),record_permission);

        setContentView(gameView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Log.d("Event", "EntryPoint:OnCreate");

    }


    @Override
    protected void onResume(){
        super.onResume();

        //must check whether user has permitted audio recording first
        //otherwise app will bomb out when audioRecord tries to record audio
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);

        if(hasPermission){
            gameView.resume();
        } else{ //ask permission
            //might want to use shouldShowRquestPermissionRationale() if user has denied previously
            //blah blah blah
            //String reqPermission = Manifest.permission.RECORD_AUDIO;
            //ActivityCompat.requestPermissions(this, new String[]{reqPermission},
             //       AUDIO_RECORD_REQUEST);
            //int duration = Toast.LENGTH_SHORT;
            requestPermission();
        }
        //Log.d("Event", "EntryPoint:onResume");

    }

    void requestPermission()
    {
        if(permissionRequested){return;}
        //Log.d("audio Permission", "no");
        //if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle("Record Audio permission necessary to measure volume");
            alertBuilder.setMessage("Spacedog needs permission to record audio to measure the volume of your voice.\n" +
                    "Spacedog never stores or transmits any audio data (or any other user data)." +
                    "If you deny this permission, the game will simply close.\n" +
                    "See privacy policy at https://lochsiedog.weebly.com/privacy-policy.html");
            alertBuilder.setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(EntryPoint.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            AUDIO_RECORD_REQUEST);

                }
            });
            AlertDialog audioAlert = alertBuilder.create();
            audioAlert.show();
        //}
        Linkify.addLinks((TextView) audioAlert.findViewById(android.R.id.message), Linkify.WEB_URLS);
        permissionRequested=true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        if (requestCode == AUDIO_RECORD_REQUEST){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //nothing to do, the app will automatically move onto onResume on its own
            }
            //else{System.exit(0); }
            else{
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Record Audio permission necessary to measure volume");
                alertBuilder.setMessage("Spacedog needs permission to record audio to measure the volume of your voice.\n" +
                        "Spacedog never stores or transmits any audio data (or any other user data)." +
                        "Because you denied this permission, the game will now close.\n" +
                        "See privacy policy at https://lochsiedog.weebly.com/privacy-policy.html");
                alertBuilder.setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);

                    }
                });
                AlertDialog audioAlert = alertBuilder.create();
                audioAlert.show();
                Linkify.addLinks((TextView) audioAlert.findViewById(android.R.id.message), Linkify.WEB_URLS);
            }
            /*else{

                numRejections++;
                if(numRejections>1){
                    finish();
                    System.exit(0);} //just exit the game if the player keeps rejecting
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this,
                        "Spacedog is controlled with your voice!",
                        duration);
                //toast.setGravity(Gravity.TOP | Gravity.LEFT,0,0);
                toast.show();

            } //what to do if permission not granted?
            */
        }
    }

    @Override
    protected void onPause(){
        //Log.d("Event", "EntryPoint:onPause");
        super.onPause();
        gameView.pause();

    }

}
