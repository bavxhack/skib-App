package com.h2Invent.skibin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.h2Invent.skibin.FragmentChildListCheckin.EXTRA_URL;

public class ChildDetailActivity extends AppCompatActivity {
    private JSONObject data;
    private RequestQueue requestQueue;
    private static String detailUrl;
    private TextView textViewName;
    private LinearLayout linearLayoutInfo;
    private LinearLayout linearLayoutinfoBool;
    private FloatingActionButton callerButton;
    private String phoneNumber;
    private String notfallKontakt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail);

        callerButton = (FloatingActionButton) findViewById(R.id.callParents);
        callerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    call(phoneNumber,notfallKontakt);
            }
        });
        Intent intent = getIntent();
        detailUrl = intent.getStringExtra(EXTRA_URL);
        textViewName = (TextView) findViewById(R.id.childDetailName);
        linearLayoutInfo = (LinearLayout) findViewById(R.id.childDettailInfo);
        linearLayoutinfoBool = (LinearLayout) findViewById(R.id.childDetailBoolean);
        requestQueue = Volley.newRequestQueue(this);
        String uri = String.format(detailUrl + "?communicationToken=%1$s",
                ChildListMainActivity.userToken);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("error")) {
                                //todo errorText in eine Snackbar
                            } else {
                                String name = response.getString("vorname") + " " + response.getString("name");
                                textViewName.setText(name);
                                phoneNumber = response.getString("phone");
                                notfallKontakt = response.getString("parentsName");
                                JSONArray info = response.getJSONArray("info");
                                JSONArray infoBool = response.getJSONArray("boolean");
                                final TextView[] tv = new TextView[info.length() * 2 + 1];
                                for (int i = 0; i < info.length(); i++) {
                                    JSONObject inf = info.getJSONObject(i);
                                    TextView tvTmpInf = new TextView(getApplicationContext());

                                    String infName = inf.getString("name");
                                    String invValue = inf.getString("value") != "null" ? inf.getString("value") : "Keine Angabe";
                                    tvTmpInf.setText(invValue);
                                    tvTmpInf.setTextSize((float) 20);
                                    tvTmpInf.setPadding(0, 16, 20, 0);
                                    tvTmpInf.setTextColor(getApplicationContext().getResources().getColor(R.color.design_default_color_primary_dark));
                                    linearLayoutInfo.addView(tvTmpInf);
                                    TextView tvTmpLabel = new TextView(getApplicationContext());
                                    tvTmpLabel.setText(infName);
                                    tvTmpLabel.setTextSize((float) 12);
                                    tvTmpLabel.setPadding(0, -1, 20, 0);

                                    tvTmpLabel.setTextColor(getApplicationContext().getResources().getColor(R.color.grey));
                                    linearLayoutInfo.addView(tvTmpLabel);
                                }
                                for (int i = 0; i < infoBool.length(); i++) {
                                    JSONObject inf = infoBool.getJSONObject(i);
                                    CheckBox cb = new CheckBox(getApplicationContext());
                                    cb.setText(inf.getString("name"));

                                    cb.setEnabled(false);
                                    cb.setTextColor(getResources().getColor(R.color.black));

                                    if (inf.getBoolean("value")) {
                                       cb.setChecked(true);
                                    } else {
                                     cb.setChecked(false);
                                    }

                                    linearLayoutinfoBool.addView(cb);
                                }


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        requestQueue.add(request);
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }


    private void call(final String phoneNumber, String parents){
        new AlertDialog.Builder(ChildDetailActivity.this)
                .setTitle(parents)
                .setMessage(R.string.callTitle)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + phoneNumber));

                        if (ActivityCompat.checkSelfPermission(ChildDetailActivity.this,
                                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                            Toast.makeText(ChildDetailActivity.this, R.string.noCallPermission, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivity(callIntent);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}