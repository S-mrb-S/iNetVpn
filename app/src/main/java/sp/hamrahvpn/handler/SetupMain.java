package sp.hamrahvpn.handler;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;

import sp.hamrahvpn.R;
import sp.hamrahvpn.databinding.ActivityMainBinding;
import sp.hamrahvpn.ui.main.MainActivity;

/**
 * Setup for MainActivity
 * by MehrabSp
 */
public class SetupMain {

    /**
     * Main Drawer
     */
    public static void setupDrawer(MainActivity context, ActivityMainBinding binding) {
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(context, binding.drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        // set listener
        NavigationView navigationView = context.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(context);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                context,
                binding.drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.drawerLayout.useCustomBehavior(GravityCompat.START); //assign custom behavior for "Left" drawer
        binding.drawerLayout.useCustomBehavior(GravityCompat.END); //assign custom behavior for "Right" drawer
        binding.drawerLayout.setRadius(
                GravityCompat.START,
                25f
        ); //set end container's corner radius (dimension)
    }

}
