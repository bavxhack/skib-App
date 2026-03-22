package com.h2Invent.skibin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

public class FragmentConnection extends Fragment {
    public static final String TITLE = "Verbindung";

    private JSONObject jsonRes;
    private static TextView userName;
    private static TextView userEmail;
    private static TextView userOrg;
    private static Button saveUser;
    private static Button resetUser;
    private static EditText userEmailCode;
    private static Button userEmailBtn;
    private static LinearLayout userinfo;
    public static LinearLayout userEmailLayout;
    public static Button scanConnection;
    private static String sUserName;
    private static String sUserOrg;
    private static String sUSerEmail;
    private static boolean isUser;



    private static String sOrgPartner;
    private static String sOrgName;
    private static boolean isOrg;
    private static LinearLayout orgConnection;
    private static LinearLayout userConnection;
    private static Button orgSave;
    private static Button orgReset;
    private static LinearLayout orgInfo;
    private static TextView orgName;
    private static TextView orgPartner;


    public static FragmentConnection newInstance() {

        return new FragmentConnection();
    }


    private OnItemSelectedListener listener;

    public interface OnItemSelectedListener {
        public void scanClicked();

        public void userSaveClicked();
        public void orgSaveClicked();

        public void resetClicked();

        public void userEmailConfirmClicked(String Token);
        public void init();
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scanConnection = (Button) view.findViewById(R.id.scanConnection);

        userinfo = (LinearLayout) view.findViewById(R.id.user_info);
        userName = (TextView) view.findViewById(R.id.user_name);
        userEmail = (TextView) view.findViewById(R.id.user_email);
        userOrg = (TextView) view.findViewById(R.id.user_organisation);
        userEmailLayout = (LinearLayout) view.findViewById(R.id.userEmailCOnfirmation);
        userEmailCode = (EditText) view.findViewById(R.id.edituseremailCode);
        userEmailBtn = (Button) view.findViewById(R.id.buttonEmailCOnfirmation);
        resetUser = (Button) view.findViewById(R.id.resetUserConnection);
        saveUser = (Button) view.findViewById(R.id.saveUserConnection);

        orgReset = (Button) view.findViewById(R.id.connectionResetOrg);
        orgSave = (Button) view.findViewById(R.id.connectionOrgSave);
        orgName = (TextView) view.findViewById(R.id.org_name);
        orgPartner = (TextView) view.findViewById(R.id.orgPartner);
        orgInfo = (LinearLayout) view.findViewById(R.id.orgInfo);

        if(isUser){
            orgInfo.setVisibility(View.GONE);
            userinfo.setVisibility(View.VISIBLE);
            userName.setText(sUserName);
            userOrg.setText(sUserOrg);
            userEmail.setText(sUSerEmail);
        }
        if (isOrg){
            userinfo.setVisibility(View.GONE);
            orgInfo.setVisibility(View.VISIBLE);
            orgName.setText(sOrgName);
            orgPartner.setText(sOrgPartner);
        }

        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.userSaveClicked();
            }
        });
        orgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.orgSaveClicked();
            }
        });
        scanConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.scanClicked();
            }
        });
        resetUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.resetClicked();
            }
        });
        orgReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.resetClicked();
            }
        });
        userEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.userEmailConfirmClicked(userEmailCode.getText().toString());
            }
        });
        listener.init();
    }

    public void setUserText(String name, String organisation, String email) {
        userName.setText(name);
        userOrg.setText(organisation);
        userEmail.setText(email);
        userinfo.setVisibility(View.VISIBLE);
    }

    public void activateEmail(boolean in) {
        userEmailLayout.setVisibility(in?View.VISIBLE:View.GONE);
        scanConnection.setEnabled(!in);
        scanConnection.setText("Prüfen Sie Ihre E-Mails");

    }

    public void activateScan(){
        scanConnection.setEnabled(true);
    }
    public void deactivateScan(){
        scanConnection.setEnabled(false);
    }
    public void activateSave(){
        saveUser.setEnabled(true);
    }
    public void deactivateSave(){
        saveUser.setEnabled(false);
    }
    public void activateReset(){
        resetUser.setEnabled(true);
    }
    public void deactivateReset(){
        resetUser.setEnabled(false);
    }
    public void setScanText(String text){
        scanConnection.setText(text);
    }

    public String getsUserName() {
        return sUserName;
    }

    public void setsUserName(String sUserName) {
        this.sUserName = sUserName;
    }

    public String getsOrg() {
        return sUserOrg;
    }

    public void setsOrg(String sOrg) {
        this.sUserOrg = sOrg;
    }

    public String getsEmail() {
        return sUSerEmail;
    }

    public void setsEmail(String sEmail) {
        this.sUSerEmail = sEmail;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
    public static String getsOrgPartner() {
        return sOrgPartner;
    }

    public static void setsOrgPartner(String sOrgPartner) {
        FragmentConnection.sOrgPartner = sOrgPartner;
    }

    public static String getsOrgName() {
        return sOrgName;
    }

    public static void setsOrgName(String sOrgName) {
        FragmentConnection.sOrgName = sOrgName;
    }

    public static boolean isIsOrg() {
        return isOrg;
    }

    public static void setIsOrg(boolean isOrg) {
        FragmentConnection.isOrg = isOrg;
    }
    public void setOrgText(String name, String partner){
        orgName.setText(name);
        orgPartner.setText(partner);
        orgInfo.setVisibility(View.VISIBLE);
    }
    public void enableOrgSetting(boolean active){
        orgSave.setEnabled(active);
        orgSave.setVisibility(active?View.VISIBLE:View.GONE);
        orgReset.setEnabled(active);
        orgReset.setVisibility(active?View.VISIBLE:View.GONE);
    }
    public void enableUSerSetting(boolean active){
        saveUser.setEnabled(active);
        saveUser.setVisibility(active?View.VISIBLE:View.GONE);
        resetUser.setEnabled(active);
        resetUser.setVisibility(active?View.VISIBLE:View.GONE);
    }
    public void showUserInfo(boolean in){
        userinfo.setVisibility(in?View.VISIBLE:View.GONE);
    }
    public void showOrgInfo(boolean in){
        orgInfo.setVisibility(in?View.VISIBLE:View.GONE);
    }
    public void enableScanBtn(boolean in){
        scanConnection.setEnabled(in);
    }
    public void enableUSerComplete(boolean in){
        enableUSerSetting(in);
        activateEmail(in);
    }
}
