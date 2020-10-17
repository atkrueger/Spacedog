package atk.spacedog;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


public class VolumeReader{
    //records audio volume in the background
    //key output is getAvgVolume, which takes either an int(frames) or double(seconds)


    AudioRecord audioRecord = null; //will be used to record volume
    Thread recordingThread = null;
    boolean isRecording = false;

    private static final int RECORDER_SAMPLERATE = 44100; //only rate guaranteed to work on all devices
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO; //only format guaranteed to work on all devices
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT; //only format guaranteed..
    int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

    private static final int VOLUME_CAP = 4000; //sounds above this volume are likely claps, things like that

    volatile int volume =777; //default value to indicate audio stream not initialized
    volatile int frequency = 999;

    //for tracking lagged values of volume recorded
    private static final int MAX_TRAILING = 100;
    volatile int list_index = 0;

    volatile int[] volume_list;
    volatile int[] frequency_list;

    public VolumeReader(){

        volume_list = new int[MAX_TRAILING];
        frequency_list = new int[MAX_TRAILING];
        for (int i = 0; i < MAX_TRAILING; i++)
        {
            volume_list[i]=0;
            frequency_list[i] = 0;
        }

    }

    public int getVolume(){return volume;}

    public int getAvgVolume(int num_frames){
        //returns average volume of the psat num_frames frames
        int sum = 0;
        int cur_index = list_index-1; //most recently written frame
        if (cur_index==-1){cur_index = MAX_TRAILING-1;}

        for (int frames_left = num_frames; frames_left >0; frames_left--){
            sum+= volume_list[cur_index]; //add current frame
            cur_index-=1; //move index back 1
            if (cur_index==-1){cur_index = MAX_TRAILING-1;} //loop it around to the back if necessary
        }
        int avg_volume = sum / num_frames;
        return avg_volume;
    }

    public int getAvgFrequency(int num_frames){
        //returns average frequency of the past num_frames frames
        int sum = 0;
        int cur_index = list_index-1; //most recently written frame
        if (cur_index==-1){cur_index = MAX_TRAILING-1;}

        for (int frames_left = num_frames; frames_left >0; frames_left--){
            sum+= frequency_list[cur_index]; //add current frame
            cur_index-=1; //move index back 1
            if (cur_index==-1){cur_index = MAX_TRAILING-1;} //loop it around to the back if necessary
        }
        int avg_frequency = sum / num_frames;
        return avg_frequency;
    }

    public int getAvgVolume(double seconds){
        //returns average volume of the past number of seconds
        int frames_back = (int)(seconds * RECORDER_SAMPLERATE / bufferSize);
        //converts to frames
        if (frames_back ==0){frames_back=1;}
        return getAvgVolume(frames_back);
    }

    public int getAvgFrequency(double seconds){
        int frames_back = (int)(seconds * RECORDER_SAMPLERATE / bufferSize);
        //converts to frames
        if (frames_back ==0){frames_back=1;}
        return getAvgFrequency(frames_back);
    }

    public void startRecording(){
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);
        audioRecord.startRecording();
        Log.d("record", "recording began");
        isRecording = true; //important to run this in a separate thread so not holding up other stuff
        recordingThread = new Thread(new Runnable(){
            @Override
            public void run(){
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                recordVolume();
                //recordVolumeFrequency(); //make sure didn't break this first

            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void recordVolume(){
        //records volume (calculated as root-mean-squared of the input audio data)
        short sData[] = new short[bufferSize]; // for holding the read data
        while(isRecording){
            //gets the voice output from the microphone
            audioRecord.read(sData,0,bufferSize); //stores it in sData
            volume = rms(sData);

            //track trailing volume
            if(volume < VOLUME_CAP) { //sounds above volume cap are likely claps, things like that
                if (list_index == MAX_TRAILING) {
                    list_index = 0;
                }
                volume_list[list_index] = volume;
                list_index += 1;
            }

        }
    }

    private void recordVolumeFrequency(){
        //records volume (calculated as root-mean-squared of the input audio data)
        short sData[] = new short[bufferSize]; // for holding the read data
        while(isRecording){
            //gets the voice output from the microphone
            audioRecord.read(sData,0,bufferSize); //stores it in sData
            volume = rms(sData);
            frequency = frequency(sData);

            //track trailing volume and frequency
            if(volume < VOLUME_CAP) { //sounds above volume cap are likely claps, things like that
                if (list_index == MAX_TRAILING) {
                    list_index = 0;
                }
                volume_list[list_index] = volume;
                frequency_list[list_index] = frequency;
                list_index += 1;
            }

        }
    }


    private int frequency(short[] data){
        //returns an int from 0-100 that is number of switches divided by the number of data points
        int switches = 0; //will track switches between positive and negative
        int last_sign = data[0]>0? 1:-1; //means sets cur_sign equal to 1 if data[0] >0, and -1 otherwise
        int this_sign;
        for(int i = 1; i < data.length; i++){
            this_sign = data[i]>0? 1:-1;
            if(last_sign!=this_sign){
                switches++;
                last_sign = this_sign;
            }
        }
        return (int)((double) switches / (double)data.length * 100);
    }

    private int rms(short[] data){ //returns the root mean squared of some data
        float sum = 0;
        for (int i = 0; i < data.length; i++){
            sum+= (Math.pow(data[i],2)) ;
        }
        float mean_squared = sum/data.length ;
        int result = (int)(Math.sqrt(mean_squared));
        return result;
    }

    public void stopRecording(){
        if (null!=audioRecord){
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            recordingThread = null;
            audioRecord= null;
        }
    }

}