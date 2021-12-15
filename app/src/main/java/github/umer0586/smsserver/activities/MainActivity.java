package github.umer0586.smsserver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.viewpager2.widget.ViewPager2;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import github.umer0586.smsserver.R;
import github.umer0586.smsserver.fragments.ServerFragment;
import github.umer0586.smsserver.fragments.SettingsFragment;

/**
 *  This activity contains two Fragments
 *
 *  ServerFragment : Contains start/Stop button at center which allows user to start/stop server
 *  SettingsFragment : Contains settings
 */
public class MainActivity extends AppCompatActivity {



    //Fragments
    private ServerFragment serverFragment;
    private SettingsFragment settingsFragment;

    // active fragment
    private Fragment activeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupFragments();

    }

    private void setupFragments()
    {
        serverFragment = new ServerFragment();
        settingsFragment = new SettingsFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, serverFragment, null)
                .hide(serverFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, null)
                .hide(settingsFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .show(serverFragment)
                .commit();

        activeFragment = serverFragment;


        // transaction.commit() is non blocking call therefore we need to make sure no transaction is pending
        getSupportFragmentManager().executePendingTransactions();
    }

    private void showServerFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(serverFragment)
                .commit();

        activeFragment = serverFragment;
        getSupportActionBar().setTitle("SMS Server");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // hide back button from ActionBar
    }

    private void showSettingsFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(settingsFragment)
                .commit();

        activeFragment = settingsFragment;
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button on ActionBar
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.menu_item_settings)
           showSettingsFragment();

        // When back button is pressed on ActionBar
        if(item.getItemId() == android.R.id.home)
            showServerFragment();



        return super.onOptionsItemSelected(item);
    }

    /**
     *  onBackPressed() invokes finish() which in result invoked onDestroy()
     *  so to prevent activity from destroying when user presses back button, we must move activity back task
     */
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }


}