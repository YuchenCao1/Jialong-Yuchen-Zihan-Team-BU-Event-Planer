import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.explore -> {
                    navController.navigate(R.id.explorePage)
                    true
                }
                R.id.calendar -> {
                    navController.navigate(R.id.calendarPage)
                    true
                }
                R.id.leaderboard -> {
                    navController.navigate(R.id.leaderboardPage)
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.profilePage)
                    true
                }
                else -> false
            }
        }
    }
}

class NavigationPageContent : Fragment() {
    private val user = FirebaseAuth.getInstance().currentUser // Current logged-in user instance.
    private var selectedIndex = 0 // Index of the currently selected page in the bottom navigation.

    private val pages: List<Fragment> = listOf(
        ExplorePage(), // Page for exploring events.
        CalendarPage(), // Calendar page.
        LeaderboardPage(), // Leaderboard page.
        ProfilePage() // User profile page.
    )

    private fun onItemTapped(index: Int) {
        selectedIndex = index // Update the index of the selected page.
        // Logic to switch fragments can be added here.
    }
}

