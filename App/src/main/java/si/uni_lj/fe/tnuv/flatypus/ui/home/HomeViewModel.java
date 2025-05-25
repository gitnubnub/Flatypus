package si.uni_lj.fe.tnuv.flatypus.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Integer> mHeartCount = new MutableLiveData<>(5);
    private final MutableLiveData<Integer> mNotificationCount = new MutableLiveData<>(8);

    public HomeViewModel() {
        mHeartCount.setValue(3);
        mNotificationCount.setValue(0);
    }

    public LiveData<Integer> getHeartCount() {
        return mHeartCount;
    }

    public LiveData<Integer> getNotificationCount() {
        return mNotificationCount;
    }

    // Methods to update the state
    public void updateHeartCount(int newCount) {
        mHeartCount.setValue(newCount);
    }

    public void incrementNotificationCount() {
        Integer current = mNotificationCount.getValue();
        mNotificationCount.setValue(current != null ? current + 1 : 1);
    }

    public LiveData<Object> getCharacter() {
        return null;
    }
}