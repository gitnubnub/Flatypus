package si.uni_lj.fe.tnuv.flatypus;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import si.uni_lj.fe.tnuv.flatypus.databinding.ActivityMainBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        BottomNavigationView navView = binding.navView;

        userViewModel.isLoggedIn().observe(this, isLoggedIn -> {
            Log.d(TAG, "isLoggedIn observer triggered, value: " + isLoggedIn);
            if (isLoggedIn != null) {
                if (isLoggedIn) {
                    navController.setGraph(R.navigation.mobile_navigation);
                    navView.setVisibility(View.VISIBLE);
                    navView.setSelectedItemId(R.id.navigation_home);
                } else {
                    navController.setGraph(R.navigation.auth_navigation);
                    navView.setVisibility(View.GONE);
                }
            }
        });

        navView.setOnNavigationItemSelectedListener(item -> {
            Boolean isLoggedInValue = userViewModel.isLoggedIn().getValue();
            if (isLoggedInValue == null || !isLoggedInValue) {
                navController.setGraph(R.navigation.auth_navigation);
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        // Add a listener to toggle BottomNavigationView visibility
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_settings) {
                navView.setVisibility(View.GONE); // Hide the bottom navigation on SettingsFragment
            } else {
                navView.setVisibility(View.VISIBLE); // Show it on other fragments
            }
        });
    }

}