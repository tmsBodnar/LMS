package hu.kalandlabor.carmessenger;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import androidx.car.app.model.GridItem;
import androidx.car.app.model.Item;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.GridTemplate;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.Template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessengerScreen extends Screen {

    SharedPreferences sPrefs;
    List<String> buttonTexts;
    List<Button> buttonList;
    boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.

            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };
    protected MessengerScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
            Intent servIntent = new Intent();
            servIntent.setClassName("com.kalandlabor.ledmessengerstrip.services", "MessengerDataService");
            getCarContext().bindService(servIntent,mConnection,Context.BIND_AUTO_CREATE);
            buttonTexts = new ArrayList<>();

            ItemList.Builder itemList = new ItemList.Builder();
            if (buttonTexts.size() > 0) {
                Log.println(Log.INFO, "xxx", buttonTexts.get(0));
                for (String text : buttonTexts) {
                    Row.Builder gridBuilder = new Row.Builder();
                    Item item = gridBuilder.setTitle(text)
                            .build();
                    itemList.addItem(item);
                }
                itemList.setOnSelectedListener(this::onTitleClicked);
            } else {
                Row.Builder gridBuilder = new Row.Builder();
                Item item = gridBuilder.setTitle("nincs üzenet")
                        .build();
                itemList.addItem(item);
            }
            return new ListTemplate.Builder()
                    .setTitle("Messages")
                    .setHeaderAction(Action.APP_ICON)
                    .setSingleList(itemList.build())
                    .build();
    }

    private void onTitleClicked(int index) {
        CarToast.makeText(getCarContext(), buttonTexts.get(index) +" clicked", CarToast.LENGTH_LONG)
                .show();
    }
}