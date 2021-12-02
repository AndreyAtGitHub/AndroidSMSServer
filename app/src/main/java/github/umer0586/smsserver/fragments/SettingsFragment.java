package github.umer0586.smsserver.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions3.RxPermissions;

import github.umer0586.smsserver.R;

public class SettingsFragment extends PreferenceFragmentCompat {

        private static final String TAG = SettingsFragment.class.getSimpleName();
        private SharedPreferences sharedPreferences;
        private RxPermissions rxPermissions;
        private Preference smsPermissionPref;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.settings, rootKey);
            sharedPreferences = getContext().getSharedPreferences(getString(R.string.shared_pref_file), Context.MODE_PRIVATE);
            handlePortPref();
            rxPermissions = new RxPermissions(this);
            smsPermissionPref = findPreference(getString(R.string.pref_key_send_sms_permission));
            handleSMSPermission();
            handleSecureConnectionPref();
            handlePasswordPref();


        }

        private void handlePasswordPref()
        {
            SwitchPreferenceCompat passwordSwitchPref = findPreference(getString(R.string.pref_key_password_switch));
            passwordSwitchPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                sharedPreferences.edit()
                                 .putBoolean(getString(R.string.pref_key_password_switch), (boolean)newValue)
                                 .commit();
                return true;
            }));


            EditTextPreference passwordPref = findPreference(getString(R.string.pref_key_password));

            passwordPref.setSummary(getAsterisks(passwordPref.getText().length()));

            passwordPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            });

            passwordPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String password = (String)newValue;

                if(password.length() >= 4)
                {

                    preference.setSummary(getAsterisks(password.length()));

                    sharedPreferences.edit()
                                     .putString(getString(R.string.pref_key_password),password)
                                     .commit();
                    return true;
                }

                showAlertDialog("Password","Failed to set Password.\nAtleast 4 characters required");
                return false;


            });
        }

        private String getAsterisks(int count)
        {
            String asteric = "";

            for (int i = 0; i < count; i++)
                asteric += "*";

            return asteric;
        }

        private void handleSMSPermission()
        {

            smsPermissionPref.setOnPreferenceClickListener(preference -> {

                rxPermissions
                        .request(Manifest.permission.SEND_SMS)
                        .subscribe(granted -> {
                            if (granted)  // Always true pre-M
                                Snackbar.make(requireView(),"Permission granted",Snackbar.LENGTH_SHORT).show();
                             else
                                Snackbar.make(requireView(),"Permission not granted",Snackbar.LENGTH_SHORT).show();

                        });

                return true;
            });

        }


        private void handlePortPref()
        {
            EditTextPreference portPref = findPreference(getString(R.string.pref_key_port_no));

            portPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

            });

            portPref.setOnPreferenceChangeListener((preference, newValue) -> {

                try {

                    int portNo = Integer.parseInt(newValue.toString());

                    if (portNo >= 1024 && portNo <= 49151)
                    {
                        sharedPreferences.edit()
                                .putInt(getString(R.string.pref_key_port_no),portNo)
                                .commit();
                        return true;
                    }
                    else {
                        showAlertDialog("Invalid Input","Please enter valid port No");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    showAlertDialog("Invalid Input","Please enter valid port No");
                    return false;
                }



            });
        }

        private void handleSecureConnectionPref()
        {
            SwitchPreferenceCompat secureConnectionPref = findPreference(getString(R.string.pref_key_secure_connection));
            secureConnectionPref.setOnPreferenceChangeListener(((preference, newValue) -> {

                boolean newState = (boolean)newValue;

                if(newState == true)
                {

                    final String message = "This app uses self signed certificate\n"+
                                            "You have to ignore certificate check at client side \n" +
                                            "or Add certificate to client trust store\n" +
                                            "Download certificate from github";

                    new AlertDialog.Builder(getContext())
                            .setTitle("Security")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("I Understand", (dialog, id) -> {
                                dialog.cancel();
                            })
                            .setNeutralButton("Open Link",(dialog,id)->{
                                try {
                                    Uri webpage = Uri.parse("https://github.com/umer0586/AndroidSMSServer");
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, webpage);
                                    startActivity(myIntent);
                                } catch (Exception e) {
                                     Snackbar.make(requireView(),"No app found that can open web page",Snackbar.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            })
                            .create()
                            .show();
                }

                sharedPreferences.edit()
                        .putBoolean(getString(R.string.pref_key_secure_connection),newState)
                        .commit();

                return true;

            }));
        }


        private void showAlertDialog(CharSequence title, CharSequence message)
        {

            new AlertDialog.Builder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Okay", (dialog, id) -> {
                        dialog.cancel();
                    })
                    .create()
                    .show();

        }

    }


