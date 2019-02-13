package com.krake.core.login.orchard;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.krake.core.R;

public class OrchardLoginActivity extends AppCompatActivity {
    public static final String ARG_DIALOG_HEIGHT = "argDialogHeight";
    public static final String ARG_DIALOG_WIDTH = "argDialogWidth";
    public static final String ARG_FRAGMENT_TYPE = "argFragmentType";

    public static final int FRAGMENT_REGISTRATION = 1;
    public static final int FRAGMENT_RECOVER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orchard_login);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey(ARG_FRAGMENT_TYPE) && savedInstanceState == null) {
                int type = b.getInt(ARG_FRAGMENT_TYPE);
                if (type == 1) {
                    showRegistrationFragment();
                } else if (type == 2) {
                    showRecoverPasswordFragment();
                }
            }

            Window window = getWindow();

            if (b.containsKey(ARG_DIALOG_HEIGHT) && b.containsKey(ARG_DIALOG_WIDTH)) {
                int height = b.getInt(ARG_DIALOG_HEIGHT);
                int width = b.getInt(ARG_DIALOG_WIDTH);

                WindowManager.LayoutParams lp = window.getAttributes();
                lp.height = height;
                lp.width = width;
                window.setAttributes(lp);
                window.getAttributes().windowAnimations = R.style.OrchardLoginActivityAnimation;

                setFinishOnTouchOutside(false);
            }
        }
    }

    protected void loginCompletedSuccesfully(String message) {
        setResult(Activity.RESULT_OK);

        if (TextUtils.isEmpty(message)) {
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    protected void showRegistrationFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.rootView, new OrchardRegisterFragment())
                .commit();
    }

    protected void showRecoverPasswordFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.rootView, new OrchardRecoverFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.rootView) instanceof OrchardRegisterFragment)
            setResult(Activity.RESULT_CANCELED);
        finish();

        if (!getResources().getBoolean(R.bool.is_tablet))
            overridePendingTransition(R.anim.login_slide_in_first, R.anim.login_slide_out_second);
        else
            overridePendingTransition(0, 0);
    }
}
