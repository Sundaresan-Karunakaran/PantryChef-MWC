package com.example.stepappv3.ui.step;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.steps.Step;

import java.util.Calendar;

public class StepViewModel extends AndroidViewModel {

    // 1. SAYAÇ DURUMU (Sensör çalışıyor mu?)
    private final MutableLiveData<Boolean> _isCounting = new MutableLiveData<>(false);
    public final LiveData<Boolean> isCounting = _isCounting;

    // 2. OTURUM DURUMU (Start'a basıldı mı?) - YENİ EKLENDİ
    // Bu false ise ekranda Büyük Start butonu olur.
    // Bu true ise ekranda Stop/Finish butonları olur.
    private final MutableLiveData<Boolean> _isSessionActive = new MutableLiveData<>(false);
    public final LiveData<Boolean> isSessionActive = _isSessionActive;

    // Adım sayısı
    private final MutableLiveData<Integer> _steps = new MutableLiveData<>(0);
    public final LiveData<Integer> steps;

    private StepRepository repo;
    private final String userId;

    private StepRepository getRepo() {
        if (repo == null) {
            repo = new StepRepository(getApplication());
        }
        return repo;
    }

    public StepViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfTodayTimestamp = calendar.getTimeInMillis();

        String uid = savedStateHandle.get("userId");
        this.userId = (uid != null) ? uid : "";

        LiveData<Integer> rawStepsToday = getRepo().getDailyStepsUser(startOfTodayTimestamp, this.userId);
        steps = Transformations.map(rawStepsToday, total -> total == null ? 0 : total);

        // Uygulama ilk açıldığında eğer zaten adım varsa bile Session kapalı başlasın istiyoruz.
        // Ancak kullanıcı uygulamayı yanlışlıkla kapatıp açarsa kaldığı yeri görmek isteyebilir.
        // Sizin isteğiniz "İlk sadece start olsun" olduğu için varsayılan false bırakıyoruz.
    }

    // BAŞLAT (START)
    public void startCounting() {
        _isSessionActive.setValue(true); // Oturumu aktif et (Butonlar değişsin)
        _isCounting.setValue(true);      // Saymayı başlat
    }

    // DURAKLAT / DEVAM ET (STOP / RESUME)
    public void togglePause() {
        boolean currentState = Boolean.TRUE.equals(_isCounting.getValue());
        _isCounting.setValue(!currentState);
        // Session active kalmaya devam eder, sadece sayma durur.
    }

    // KAYDET VE BİTİR
    public void finishAndSave() {
        _isCounting.setValue(false);
        _isSessionActive.setValue(false); // Oturumu kapat (Başa dön)
        // Veriler veritabanında kalır.
    }

    // SİL VE BİTİR
    public void finishAndDiscard() {
        _isCounting.setValue(false);
        _isSessionActive.setValue(false); // Oturumu kapat (Başa dön)
        getRepo().deleteAllUser(this.userId); // Veriyi sil
        _steps.setValue(0);
    }

    public void onCountClicked() {
        if (Boolean.TRUE.equals(_isCounting.getValue())) {
            Step newStep = new Step(System.currentTimeMillis(), 1, this.userId);
            getRepo().insert(newStep);
        }
    }
}