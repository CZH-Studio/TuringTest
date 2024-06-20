package com.czhstudio.turingtest.activity.dialog;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.czhstudio.turingtest.R;
import com.czhstudio.turingtest.activity.MainActivity;
import com.czhstudio.turingtest.utils.Error;
import org.jetbrains.annotations.NotNull;

public class ErrorDialog extends AlertDialog.Builder{
    public ErrorDialog(@NonNull @NotNull Context context, int errno, boolean isNotMainActivity) {
        super(context);
        this.setCancelable(false);
        this.setTitle(R.string.err_title);
        this.setMessage(Error.getErrMsg(errno));
        this.setPositiveButton(R.string.action_back, (dialog, which) -> {
            dialog.dismiss();
            if (isNotMainActivity) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        });
    }
}
