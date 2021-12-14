package github.umer0586.smsserver.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.io.IOException;


import github.umer0586.smsserver.R;
import github.umer0586.smsserver.SMSServer;
import github.umer0586.smsserver.util.IpUtil;


public class ServerFragment extends Fragment implements SMSServer.onStartedListener , SMSServer.onStoppedListener{

    // Button at center to start/stop server
    private MaterialButton startButton;

    // Address of server (http://192.168.2.1:8081)
    private TextView serverAddress;

    // Lock icon placed at left of serverAddress
    private AppCompatImageView lockIcon;

    // card view which holds serverAddress and lockIcon
    private CardView cardView;

    //Ripple animation behind startButton
    private SpinKitView pulseAnimation;


    private SMSServer smsServer;
    private SharedPreferences sharedPreferences;
    private RxPermissions rxPermissions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        startButton = view.findViewById(R.id.start_button);
        serverAddress = view.findViewById(R.id.server_address);
        pulseAnimation = view.findViewById(R.id.loading_animation);
        lockIcon = view.findViewById(R.id.lock_icon);
        cardView = view.findViewById(R.id.card_view);

        rxPermissions = new RxPermissions(this);

        hidePulseAnimation();
        hideServerAddress();

        // we will use tag to determine last state of button
        startButton.setOnClickListener(v -> {
            if(v.getTag().equals("stopped"))
                startServer();
            else if(v.getTag().equals("started"))
                stopServer();
        });

        sharedPreferences = getContext().getSharedPreferences(getString(R.string.shared_pref_file),getContext().MODE_PRIVATE);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopServer();
    }

    private void startServer()
    {

        if(!rxPermissions.isGranted(Manifest.permission.SEND_SMS))
        {
            Snackbar snackbar = Snackbar.make(getView().findViewById(R.id.relative_layout),
                    "Allow SMS permission from settings",10_000);
            snackbar.show();
            return;
        }


        String hostIp = IpUtil.getWifiIpAddress(getContext());
        int portNo = sharedPreferences.getInt(getString(R.string.pref_key_port_no),8080);

        if( hostIp == null )
        {
            Snackbar.make(getView(),"No Network",Snackbar.LENGTH_SHORT).show();
            return;
        }

        smsServer = new SMSServer(getContext(),hostIp,portNo);
        smsServer.setOnStartedListener(this);
        smsServer.setOnStoppedListener(this);

        boolean isSecureConnectionEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_secure_connection),false);
        // If user has enabled "Use secure connection" option
        if(isSecureConnectionEnable)
            smsServer.makeSecure();


        boolean isPasswordEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_password_switch),false);
        //If user has enabled the password option
        if(isPasswordEnable)
        {
            String password = sharedPreferences.getString(getString(R.string.pref_key_password),null);
            smsServer.enablePassword();
            smsServer.setPassword(password);
        }


        try
        {
           smsServer.start();

        } catch (IOException e)
        {
            hideServerAddress();
            Snackbar.make(getView(),"Failed to start",Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void showServerAddress(final String address)
    {
        if(smsServer != null)
        {
            cardView.setVisibility(View.VISIBLE);

            serverAddress.setVisibility(View.VISIBLE);


            if(smsServer.isSecure())
            {
                lockIcon.setVisibility(View.VISIBLE);
                serverAddress.setText(Html.fromHtml("<font color=\"#5c6bc0\">https://</font>" + address));
            }
            else
            {
                lockIcon.setVisibility(View.GONE);
                serverAddress.setText("http://" + address);
            }



        }


    }

    private void showPulseAnimation()
    {
        pulseAnimation.setVisibility(View.VISIBLE);
    }

    private void hidePulseAnimation()
    {
        pulseAnimation.setVisibility(View.INVISIBLE);
    }

    private void hideServerAddress()
    {
        cardView.setVisibility(View.GONE);
        serverAddress.setVisibility(View.GONE);
        lockIcon.setVisibility(View.GONE);
    }

    private void stopServer()
    {
        if(smsServer != null)
            smsServer.stop();
    }

    // A callback from server thread when server is started
    @Override
    public void onStarted(final String host, final int port)
    {
        getActivity().runOnUiThread(()->{
            showServerAddress(host + ":" + port);
            showPulseAnimation();
            startButton.setTag("started");
            startButton.setText("STOP");
            Snackbar.make(getView(),"SMS Server started",Snackbar.LENGTH_SHORT).show();

        });



    }

    // A callback from server thread when server is stopped
    @Override
    public void onStopped()
    {
        getActivity().runOnUiThread(()->{
            hideServerAddress();
            hidePulseAnimation();
            startButton.setTag("stopped");
            startButton.setText("START");
            Snackbar.make(getView(),"SMS Server stopped",Snackbar.LENGTH_SHORT).show();

        });
    }


}


