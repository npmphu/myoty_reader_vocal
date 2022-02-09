package net.akensys.reader.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import net.akensys.reader.PrefsHelper;
import net.akensys.reader.Reference;

import java.util.Locale;

public class TTSHelper implements TextToSpeech.OnInitListener {
    public final String TAG = "TTSHelper";
    public final TextToSpeech tts;
    public final UtteranceProgressListener upl;
    public final String Uid;
    private Context context = null;
    private Locale locale = Locale.FRANCE;
    private String text = "Attention ! Alerte en cours !";

    public TTSHelper(Context ctx, Locale lc, String txt, String id) {
        tts = new TextToSpeech(ctx, this);
        context = ctx;
        locale = lc;
        text = txt;
        Uid = id;
        upl = new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                if (utteranceId.equals(Uid)) {
                    tts.stop();
                    tts.shutdown();
                }
            }

            @Override
            public void onError(String utteranceId) {

            }
        };
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Error: This Language is not supported");
            } else {
                if (PrefsHelper.getAlertOpt(context) == Reference.ALERT_TTS) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AlertVoiceTTS");
                }
            }
        } else {
            Log.e(TAG, "Error: Failed to Initialize");
        }
    }
}
