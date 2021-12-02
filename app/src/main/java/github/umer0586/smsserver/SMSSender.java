package github.umer0586.smsserver;

import static github.umer0586.smsserver.util.JsonUtil.toJSON;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;


import java.lang.reflect.Field;
import java.util.HashMap;

public class SMSSender {

    private Context context;
    private static SMSSender instance;

    private static final String SENT = "SMS_SENT_ACTION";
    private static final String DELIVERED = "SMS_DELIVERED_ACTION";

    public static final String STATUS_SENT_SUCCESS = "Message successfully sent";
    public static final String STATUS_SENT_FAIL = "Unable to send Message";

    public static final String STATUS_DELIVERY_SUCCESS = "Message successfully delivered";
    public static final String STATUS_DELIVERY_FAIL = "Message not delivered";


    public static final String STATUS_EXCEPTION_OCCURRED = "Exception occurred while sending message";

    private SmsManager smsManager = SmsManager.getDefault();

    public static SMSSender getInstance(@NonNull Context context)
    {

        if( instance == null)
            instance = new SMSSender(context);

        return instance;
    }

    private SMSSender(@NonNull Context context)
    {
        this.context = context;
    }


    /**
     * Sends sms and blocks until sms is successfully or unsuccessfully sent or delivered.
     *
     * @param phone target address to send sms to
     * @param message text to send
     * @return HashMap containing result info
     */
    public HashMap<String,Object> sendSMS(@NonNull final String phone , @NonNull final String message)
    {
        // always declare local
        final HashMap<String, Object> resultHashMap = new HashMap<>();

        // always declare local
        final Object lock = new Object();

        //This PendingIntent is broadcast when the message is successfully sent, or failed.
        // The result code will be Activity.RESULT_OK for success
        PendingIntent sentPI = PendingIntent.getBroadcast(this.context, 0, new Intent(SENT), 0);

        //This PendingIntent is broadcast when the message is delivered to the recipient.
        // The raw pdu of the status report is in the extended data ("pdu").
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this.context, 0, new Intent(DELIVERED), 0);

        this.context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context ctx, Intent intent)
            {
                if(getResultCode() == Activity.RESULT_OK)
                {
                    resultHashMap.clear();
                    resultHashMap.put("status",STATUS_SENT_SUCCESS);
                    resultHashMap.put("result","message successfully sent to " + phone);
                    resultHashMap.put("message sent",message);
                }
                else
                {
                    resultHashMap.clear();
                    resultHashMap.put("status",STATUS_SENT_FAIL);
                    resultHashMap.put("reason",getErrorString(getResultCode()));
                }

                synchronized (lock)
                {
                    lock.notify();
                }

            }
        }, new IntentFilter(SENT));

        this.context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context ctx, Intent intent)
            {
                if(getResultCode() == Activity.RESULT_OK)
                {
                    resultHashMap.clear();
                    resultHashMap.put("status",STATUS_DELIVERY_SUCCESS);
                    resultHashMap.put("result","message successfully delivered to " + phone);
                    resultHashMap.put("message sent",message);
                }
                else
                {
                    resultHashMap.clear();
                    resultHashMap.put("status",STATUS_DELIVERY_FAIL);
                    resultHashMap.put("reason",getErrorString(getResultCode()));
                }


                synchronized (lock)
                {
                    lock.notify();
                }
            }
        }, new IntentFilter(DELIVERED));


        try{

            smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI);

            synchronized (lock)
            {
                lock.wait(); // wait here until some other thread call lock.notify()
            }

        }catch(Exception e){
            e.printStackTrace();
            resultHashMap.clear();
            resultHashMap.put("status",STATUS_EXCEPTION_OCCURRED);
            resultHashMap.put("exception", e.getMessage());

        }

        return resultHashMap;
    }


    /**
     * @see <a href="https://developer.android.com/reference/android/telephony/SmsManager#sendTextMessage(java.lang.String,%20java.lang.String,%20java.lang.String,%20android.app.PendingIntent,%20android.app.PendingIntent)">Read this for more detail</a>
     */
    private static String getErrorString(final int code)
    {
        for(Field field : SmsManager.class.getFields())
        {
            try
            {
                if( field.getName().contains("RESULT_") && (field.getInt(null) == code) )
                    return field.getName();

            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

}
