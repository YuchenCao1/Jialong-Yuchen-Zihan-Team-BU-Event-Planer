import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.example.bu_passport.ui.theme.BuPassportTheme
import com.example.bu_passport.auth.AuthGate
import com.example.bu_passport.pages.ExplorePage
import com.example.bu_passport.pages.LoginPage
import com.example.bu_passport.pages.ProfilePage
import com.example.bu_passport.pages.SignUpPage
import com.example.bu_passport.pages.OnboardingPage
import com.jakewharton.timber.Timber
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Timezone initialization for check in
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))

        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    BuPassportTheme {
        Surface {
            // Your navigation setup here
            // For example, using Jetpack Navigation Component
            // NavHost(navController, startDestination = "auth_gate") {
            //     composable("auth_gate") { AuthGate() }
            //     composable("onboarding") { OnboardingPage() }
            //     composable("login") { LoginPage() }
            //     composable("signup") { SignUpPage() }
            //     composable("home") { AuthGate() }
            //     composable("explore_page") { ExplorePage() }
            //     composable("profile_page") { ProfilePage() }
            // }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp()
}

