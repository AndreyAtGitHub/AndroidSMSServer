package github.umer0586.smsserver;

import static github.umer0586.smsserver.util.JsonUtil.toJSON;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;

import fi.iki.elonen.NanoHTTPD;

public class SMSServer extends NanoHTTPD {


    private static final String TAG = SMSServer.class.getSimpleName();

    private SMSSender smsSender;
    private Context context;

    private boolean isSecure = false;


    private boolean isPasswordEnable = false;
    private String password;


    private onStartedListener onStartedListener;
    private onStoppedListener onStoppedListener;

    public SMSServer(@NonNull Context context,@NonNull String hostname, @NonNull int port)
    {
        super(hostname, port);

        this.context = context;
        smsSender = SMSSender.getInstance(context);

    }

    public boolean isSecure()
    {
        return isSecure;
    }

    public void makeSecure()
    {

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = this.context.getAssets().open("keystore.bks");

            if (keystoreStream == null)
            {
                this.isSecure = false;
                throw new IOException("Unable to load keystore");

            }

            final String PASS = "12345";
            keystore.load(keystoreStream, PASS.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, PASS.toCharArray());
            makeSecure(makeSSLSocketFactory(keystore, keyManagerFactory),null);


        } catch (Exception e) {
            this.isSecure = false;
            e.printStackTrace();
            return;
        }

        this.isSecure = true;

    }

    public void enablePassword()
    {
        isPasswordEnable = true;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        Log.i(TAG, "request URI: "+ session.getUri());
        Log.i(TAG, "request headers " + session.getHeaders());

        final HashMap<String,Object> responseBody = new HashMap<>();

        // check requested method
        if(session.getMethod() != Method.POST)
        {

            responseBody.clear();
            responseBody.put("error","Method " + session.getMethod() + " not allowed, use POST");

            return newFixedLengthResponse(
                    Response.Status.METHOD_NOT_ALLOWED,
                    "application/json",
                    toJSON(responseBody)
            );
        }


        final String contentType = session.getHeaders().get("content-type");

       if(contentType == null || !contentType.equalsIgnoreCase("application/x-www-form-urlencoded"))
       {
           responseBody.clear();
           responseBody.put("error","un supported media type");
           responseBody.put("required content-type","application/x-www-form-urlencoded");

           return newFixedLengthResponse(
                   Response.Status.UNSUPPORTED_MEDIA_TYPE,
                   "application/json",
                   toJSON(responseBody)
           );
       }



        try {
            /*
                This step is important for reading POST parameters from body
                After calling session.parseBody(map) POST parameters will be available via session.getQueryParameterString()

                HashMap<String,String> map = new HashMap<>();
                session.parseBody(map);
                map.get("postData") contains POST parameters from request body if client has set Content-Type to application/json

                To read POST parameters when client Content-Type is application/x-form-www-urlencoded we must use session.getQueryParameterString()

                Note that session.getParams() and session.getParameters() also contains POST parameters for application/x-form-www-urlencoded
                but they also contains parameters from URL
            */
            session.parseBody(new HashMap<>());

        } catch (IOException | ResponseException e) {

            responseBody.clear();
            responseBody.put("error","exception occurred");
            responseBody.put("exception", e.getMessage());

            return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    "application/json",
                    toJSON(responseBody)
            );
        }


        if( session.getUri().equalsIgnoreCase("/sendSMS"))
            return handleSMSRequest(session);


        responseBody.clear();
        responseBody.put("error","unknown request path. Use /sendSMS");

        return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "application/json",
                toJSON(responseBody)
        );
    }

    private Response handleSMSRequest(IHTTPSession session)
    {

        final HashMap<String,Object> responseBody = new HashMap<>();

        Uri uri = Uri.parse(session.getUri()+ "?" +session.getQueryParameterString());


        String phone = uri.getQueryParameter("phone");
        String message = uri.getQueryParameter("message");
        String password = uri.getQueryParameter("password");



        if(phone == null)
        {
            responseBody.clear();
            responseBody.put("error","<phone> parameter missing");

            return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "application/json",
                    toJSON(responseBody)
            );

        }
        else if( message == null)
        {
            responseBody.clear();
            responseBody.put("error","<message> parameter missing");

            return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "application/json",
                    toJSON(responseBody)
            );
        }

        if( isPasswordEnable() )
        {

            if(password == null)
            {
                responseBody.clear();
                responseBody.put("error","<password> parameter required");
                return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "application/json",
                        toJSON(responseBody)
                );

            }

            else if(!this.getPassword().equals(password))
            {
                responseBody.clear();
                responseBody.put("error","invalid Password");

                Response httpsResponse = newFixedLengthResponse(toJSON(responseBody));
                httpsResponse.addHeader("WWW-Authenticate","Invalid Password");
                httpsResponse.setMimeType("application/json");
                httpsResponse.setStatus(Response.Status.UNAUTHORIZED);

                return httpsResponse;
            }

        }

        //blocking call
        final HashMap<String,Object> resultMap = smsSender.sendSMS(phone,message);
        final String status = (String) resultMap.get("status");
        final boolean sentFailed = status.equals(SMSSender.STATUS_SENT_FAIL);


        if(status.equals(SMSSender.STATUS_EXCEPTION_OCCURRED))
            return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST, //because the client has provided invalid address
                    "application/json",
                    toJSON(resultMap)
            );

        if(sentFailed)
            return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    "application/json",
                    toJSON(resultMap)
            );

        // when successful
        return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                toJSON(resultMap)
        );

    }

    private boolean isPasswordEnable()
    {
        return isPasswordEnable;
    }

    public void setOnStartedListener(onStartedListener onStartedListener)
    {
        this.onStartedListener = onStartedListener;
    }

    public void setOnStoppedListener(onStoppedListener onStoppedListener)
    {
        this.onStoppedListener = onStoppedListener;
    }

    @Override
    public void start() throws IOException
    {

         super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
           if(onStartedListener!=null)
               onStartedListener.onStarted(getHostname(),getListeningPort());

    }


    @Override
    public void stop()
    {
        super.stop();
        if(onStoppedListener!=null)
            onStoppedListener.onStopped();
    }


    @FunctionalInterface
    public interface onStartedListener {
        void onStarted(final String host, final int port);
    }
    @FunctionalInterface
    public interface onStoppedListener {
        void onStopped();
    }
}


