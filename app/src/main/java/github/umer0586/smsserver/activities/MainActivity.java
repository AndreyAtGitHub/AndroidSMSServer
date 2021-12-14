package github.umer0586.smsserver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import github.umer0586.smsserver.R;
import github.umer0586.smsserver.fragments.ServerFragment;
import github.umer0586.smsserver.fragments.SettingsFragment;

/**
 *  This activity contains two Fragments (Displayed through viewPager)
 *
 *  MainFragment : Contains start/Stop button at center which allows user to start/stop server
 *  SettingsFragment : Contains settings
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    // Fragments Positions
    private static final int POSITION_MAIN_FRAGMENT = 0;
    private static final int POSITION_SETTING_FRAGMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new MyFragmentStateAdapter(this) );

        viewPager.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position)
            {

                if(position == POSITION_MAIN_FRAGMENT)
                {
                    getSupportActionBar().setTitle("SMS Server");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false); // hide back button from ActionBar
                }

                else if(position == POSITION_SETTING_FRAGMENT)
                {
                    getSupportActionBar().setTitle("Settings");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button on ActionBar
                }

            }
        });



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
           viewPager.setCurrentItem(POSITION_SETTING_FRAGMENT);

        // When back button is pressed on ActionBar
        if(item.getItemId() == android.R.id.home)
            viewPager.setCurrentItem(POSITION_MAIN_FRAGMENT);



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

    private class MyFragmentStateAdapter extends FragmentStateAdapter {


        public MyFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity)
        {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int pos) {

            switch(pos)
            {

                case POSITION_MAIN_FRAGMENT: return new ServerFragment();
                case POSITION_SETTING_FRAGMENT: return new SettingsFragment();

            }

            return new ServerFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


}