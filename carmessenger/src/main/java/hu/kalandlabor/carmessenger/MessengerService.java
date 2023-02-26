package hu.kalandlabor.carmessenger;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

public final class MessengerService extends CarAppService {
    SpeechRecognizer speechRecognizer;
    SpeechToTextSession speechToTextSession;
    @NonNull
    @Override
    public HostValidator createHostValidator() {
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
        } else {
            return new HostValidator.Builder(getApplicationContext())
                    .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                    .build();
        }
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        return new SpeechToTextSession(this, speechRecognizer);

    }
}
