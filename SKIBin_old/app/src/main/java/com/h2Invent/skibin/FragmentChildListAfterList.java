package com.h2Invent.skibin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.h2Invent.skibin.FragmentChildListCheckin.EXTRA_URL;

public class FragmentChildListAfterList extends Fragment implements ChildListAdapter.OnItemClickListener {

    public static final String TITLE = "Anwesend nach Liste";
    private TextView url;
    private static String urlString;
    private static String userToken;
    private static RecyclerView recyclerView;
    private static ChildListAdapter childListAdapter;
    private static ArrayList<ChildListItem> childList;
    private static ArrayList<ChildListItem> childListFull;
    private RequestQueue requestQueue;
    private static MaterialSpinner spinner;
    private static SwipeRefreshLayout pullRefresh;
    private String checkinUrl;
    public static FragmentChildListAfterList newInstance() {
        return new FragmentChildListAfterList();
    }

    private OnItemSelectedListener listener;

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(getContext(), ChildDetailActivity.class);
        ChildListItem clickedChid = childList.get(position);
        detailIntent.putExtra(EXTRA_URL, clickedChid.getmDetailUrl());
        startActivity(detailIntent);
    }

    @Override
    public void onCheckinClick(int position) {
        ChildListItem clickedChid = childList.get(position);
        checkinUrl = String.format(clickedChid.getmCheckinUrl() + "?communicationToken=%1$s",
                userToken);
        new AlertDialog.Builder(getContext())
                .setTitle(clickedChid.getmName())
                .setMessage(R.string.checkinTitle)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, checkinUrl, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            Toast.makeText(getContext(),response.getString("errorText"),Toast.LENGTH_LONG).show();
                                            ((ChildListMainActivity)getActivity()).initApp();
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
                                }
                        );
                        requestQueue.add(request);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .show();

    }
    public interface OnItemSelectedListener {
    }
    // Store the listener (activity) that will have events fired once the fragment is attached

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kinderliste, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
        url = (TextView) view.findViewById(R.id.url);


        spinner = (MaterialSpinner) view.findViewById(R.id.schulenChooseListe);
        url.setText(urlString);
        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        url.setText(currentDate);
        recyclerView = view.findViewById(R.id.recyclerKinderListe);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        childList = new ArrayList<>();
        childListFull = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this.getContext());
        parseJson();
    }

    public void setUrl(String lUrl) {
        urlString = lUrl;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userTokenIn) {
        userToken = userTokenIn;
    }

    private void parseJson() {
        String url = urlString;

        String uri = String.format(urlString + "?communicationToken=%1$s",
                userToken);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("error")) {
                                Toast.makeText(getContext(),response.getString("errorText"),Toast.LENGTH_LONG).show();
                            } else {
                                JSONArray result = response.getJSONArray("result");
                                childListFull.clear();
                                childList.clear();
                                setChildsnumber(response.getInt("number"));
                                if (response.getInt("number") == 0) {
                                    childListFull.add(new ChildListItem(getContext().getResources().getString(R.string.noCheckin), "", 0, false, "", -1,false,""));
                                } else {
                                    for (int i = 0; i < result.length(); i++) {
                                        JSONObject child = null;
                                        try {
                                            child = result.getJSONObject(i);
                                            String nameChild = child.getString("vorname") + " " + child.getString("name");
                                            int klasse = child.getInt("klasse");
                                            String schule = child.getString("schule");
                                            boolean checkin = child.getBoolean("checkin");
                                            String detailurl = child.getString("detail");
                                            int schulId = child.getInt("schuleId");
                                            boolean hasBirthday = child.getBoolean("hasBirthday");
                                            String checkinUrl = child.getString("checkinUrl");
                                            childListFull.add(new ChildListItem(nameChild,schule,klasse,checkin,detailurl,schulId,hasBirthday,checkinUrl));


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    ArrayList<Schule> schulList = new ArrayList<>();
                                    JSONArray schulen = response.getJSONArray("schulen");
                                    schulList.add(new Schule(-1, "Alle Schulen"));
                                    for (int i = 0; i < schulen.length(); i++) {
                                        JSONObject schule = schulen.getJSONObject(i);
                                        schulList.add(new Schule(schule.getInt("id"), schule.getString("name")));
                                    }
                                    spinner.setItems(schulList);
                                    spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Schule>() {
                                        @Override
                                        public void onItemSelected(MaterialSpinner view, int position, long id, Schule item) {
                                            childList.clear();
                                            if (item.getId() > 0) {
                                                for (int i = 0; i < childListFull.size(); i++) {
                                                    if (childListFull.get(i).getmSchulId() == item.getId()) {
                                                        childList.add(childListFull.get(i));
                                                    }
                                                }
                                            } else {
                                                childList.addAll(childListFull);
                                            }
                                            if(childList.size()==0){
                                                childList.add(new ChildListItem(getContext().getResources().getString(R.string.noCheckin), "", 0, false, "", -1,false,""));
                                            }
                                            childListAdapter = new ChildListAdapter(getContext(), childList);
                                            recyclerView.setAdapter(childListAdapter);
                                            childListAdapter.setmListener(FragmentChildListAfterList.this);
                                        }
                                    });
                                }
                                childList.addAll(childListFull);
                                childListAdapter = new ChildListAdapter(getContext(), childList);
                                recyclerView.setAdapter(childListAdapter);
                                childListAdapter.setmListener(FragmentChildListAfterList.this);
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
    private void setChildsnumber(int number){
        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date())+" ("+String.valueOf(number)+")";
        url.setText(currentDate);
    }
}
