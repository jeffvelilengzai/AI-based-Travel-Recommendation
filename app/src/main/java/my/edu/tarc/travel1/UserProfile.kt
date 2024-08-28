package my.edu.tarc.travel1

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import my.edu.tarc.travel1.databinding.FragmentUserProfileBinding
import my.edu.tarc.travel1.history.History

class UserProfile : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        setHasOptionsMenu(true)  // Ensure menu methods are called
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            databaseRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val phone = snapshot.child("phone").getValue(String::class.java)

                if (username != null && email != null && phone != null) {
                    binding.usernameTextView.setText(username)
                    binding.emailTextView.text = email
                    binding.phoneTextView.setText(phone)
                } else {
                    Toast.makeText(requireActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editButton.setOnClickListener { enableEditing(true) }
        binding.saveButton.setOnClickListener { saveUserData() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Call a function to set up the menu based on user role asynchronously
        setupMenuBasedOnRole(menu, inflater)
    }

    private fun setupMenuBasedOnRole(menu: Menu, inflater: MenuInflater) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("role").getValue(String::class.java) ?: "user"
            when (role) {
                "admin" -> inflater.inflate(R.menu.drawer_menu, menu)
                "user" -> inflater.inflate(R.menu.menu_user, menu)
                else -> super.onCreateOptionsMenu(menu, inflater)
            }
        }.addOnFailureListener {
            inflater.inflate(R.menu.menu_user, menu)  // Default to user menu on failure
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_profile -> navigateToFragment(UserProfile())
            R.id.nav_logout -> {
                auth.signOut()
                Toast.makeText(requireActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show() // Toast on logout
                navigateToFragment(activity_login())
                true
            }
            R.id.nav_travel -> navigateToFragment(TravelRec())
            R.id.nav_travel_history -> navigateToFragment(History())
            R.id.nav_report -> navigateToFragment(Report())
            R.id.nav_manage_user -> navigateToFragment(AdminProfile())
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToFragment(fragment: Fragment): Boolean {
        parentFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
        return true
    }

    private fun enableEditing(enable: Boolean) {
        binding.usernameTextView.isEnabled = enable
        binding.phoneTextView.isEnabled = enable
        binding.saveButton.visibility = if (enable) View.VISIBLE else View.GONE
        binding.editButton.visibility = if (enable) View.GONE else View.VISIBLE
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return
        val newUsername = binding.usernameTextView.text.toString().trim()
        val newPhone = binding.phoneTextView.text.toString().trim()

        if (newUsername.isNotEmpty() && newPhone.isNotEmpty()) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            val userUpdates = mapOf(
                "username" to newUsername,
                "phone" to newPhone
            )

            databaseRef.updateChildren(userUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    enableEditing(false)
                } else {
                    Toast.makeText(requireActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireActivity(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
