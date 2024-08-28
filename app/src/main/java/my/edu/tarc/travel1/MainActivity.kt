package my.edu.tarc.travel1

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.travel1.databinding.ActivityMainBinding
import my.edu.tarc.travel1.history.History

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navView = binding.navView

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Add the initial fragment to the activity
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, activity_login())
            }
        }

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.drawer_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.nav_profile -> {
//                supportFragmentManager.commit {
//                    replace(R.id.fragment_container, UserProfile())
//                    addToBackStack(null)
//                }
//                true
//            }
//            R.id.nav_logout -> {
//                auth.signOut() // Sign out the user
//                supportFragmentManager.commit {
//                    replace(R.id.fragment_container, activity_login())
//                    addToBackStack(null)
//                }
//                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
//                true
//            }
//            R.id.nav_travel -> {
//                supportFragmentManager.commit {
//                    replace(R.id.fragment_container, TravelRec())
//                    addToBackStack(null)
//                }
//                true
//            }
//            R.id.nav_travel_history -> {
//                supportFragmentManager.commit {
//                    replace(R.id.fragment_container, History())
//                    addToBackStack(null)
//                }
//                true
//            }
//            R.id.nav_report -> {
//                supportFragmentManager.commit {
//                    replace(R.id.fragment_container, Report())
//                    addToBackStack(null)
//                }
//                true
//            }
//            R.id.nav_manage_user -> {
//                supportFragmentManager.commit {
//                    replace(R.id.fragment_container, AdminProfile())
//                    addToBackStack(null)
//                }
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
