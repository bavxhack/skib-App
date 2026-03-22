package com.h2Invent.skibin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CheckinDialog extends AppCompatDialogFragment {
    private String name ;
    private String kurs;
    private String text;
    private boolean background;
    private TextView nameV;
    private TextView kursV;
    private TextView textV;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        nameV = (TextView) view.findViewById(R.id.dialog_name);
        kursV = (TextView)view.findViewById(R.id.dialog_kurs);
        textV = (TextView)view.findViewById(R.id.dialog_text);
        builder.setView(view)
                .setNegativeButton(R.string.OkText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        nameV.setText(name);
        kursV.setText(kurs);
        textV.setText(text);
        if (background){
            view.setBackgroundResource(R.color.backgroundError);
        }else {
            view.setBackgroundResource(R.color.backgroundSuccess);
        }

    return  builder.create();
    }
    public void setName(String nameIn){
        name = nameIn;
    }
    public void setKurs(String kursIn){
       kurs = kursIn;
    }
    public void setText(String textIn){
       text = textIn;
    }
    public void setBackground(boolean backgroundIn){
        background = backgroundIn;
    }
}
