package com.mobile.dts.dialogs;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mobile.dts.R;

/*Use to show alert dialog box*/
public class AlertDialogMessage extends AlertDialog implements View.OnClickListener {
    private String mMessage;
    private Context mContext;
    private TextView title_tv, msg_tv;
    private Button ok_btn;

    public AlertDialogMessage(@NonNull Context _context, String _msg) {
        super(_context);
        mContext = _context;
        mMessage = _msg;
    }

    public void setDialogMessage(String _msg) {
        mMessage = _msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_msg_dlg);
        setCancelable(false);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        title_tv = (TextView) this.findViewById(R.id.title_tv);
        msg_tv = (TextView) this.findViewById(R.id.msg_tv);
        ok_btn = (Button) this.findViewById(R.id.ok_btn);
        msg_tv.setText(mMessage);
        ok_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
                if (!((Activity) mContext).isFinishing())
                    dismiss();
                break;
        }
    }
}
