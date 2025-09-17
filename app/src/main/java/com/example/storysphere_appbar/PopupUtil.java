package com.example.storysphere_appbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PopupUtil {

    public static void showConfirm(View rootView, String message, Runnable onConfirm) {
        View popupView = rootView.findViewById(R.id.confirm_popup_container);
        TextView tvMessage = popupView.findViewById(R.id.tv_popup_message);
        Button btnYes = popupView.findViewById(R.id.btn_popup_yes);
        Button btnNo = popupView.findViewById(R.id.btn_popup_no);

        tvMessage.setText(message);
        popupView.setVisibility(View.VISIBLE);

        btnNo.setOnClickListener(v -> popupView.setVisibility(View.GONE));
        btnYes.setOnClickListener(v -> {
            popupView.setVisibility(View.GONE);
            if (onConfirm != null) onConfirm.run();
        });
    }
}
