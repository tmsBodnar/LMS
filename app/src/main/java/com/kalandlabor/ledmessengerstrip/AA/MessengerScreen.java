package com.kalandlabor.ledmessengerstrip.AA;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

public class MessengerScreen extends Screen {
    protected MessengerScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        Row row = new Row.Builder()
                .setTitle("Hello world!")
                .setOnClickListener(this::onTitleClicked).build();
        Pane pane = new Pane.Builder().addRow(row).build();
        return new PaneTemplate.Builder(pane)
                .setHeaderAction(Action.APP_ICON)
                .build();
    }

    private void onTitleClicked() {
        CarToast.makeText(getCarContext(), "title clicked", CarToast.LENGTH_LONG)
                .show();
    }
}