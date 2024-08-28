package my.edu.tarc.travel1

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import my.edu.tarc.travel1.databinding.FragmentAdminProfileBinding
import my.edu.tarc.travel1.history.History

class AdminProfile : Fragment() {
    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(activity_login.User::class.java)
                    if (user != null && user.role == "user") {  // Check if the user's role is "user"
                        addUserRow(userSnapshot) // Pass userSnapshot here
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun addUserRow(userSnapshot: DataSnapshot) {
        val user = userSnapshot.getValue(activity_login.User::class.java) ?: return

        val row = TableRow(requireContext())
        row.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )

        // Create TextView for username
        val username = TextView(requireContext()).apply {
            text = user.username
            gravity = android.view.Gravity.CENTER
        }
        row.addView(username)

        // Create TextView for email
        val email = TextView(requireContext()).apply {
            text = user.email
            gravity = android.view.Gravity.CENTER
        }
        row.addView(email)

        // Create TextView for phone
        val phone = TextView(requireContext()).apply {
            text = user.phone
            gravity = android.view.Gravity.CENTER
        }
        row.addView(phone)

        // Create Delete Button
        val deleteButton = Button(requireContext()).apply {
            text = "Delete"
            setOnClickListener {
                showDeleteConfirmationDialog(userSnapshot.key)
            }
        }
        row.addView(deleteButton)

        // Add the row to the TableLayout
        binding.userTable.addView(row)
    }

    private fun showDeleteConfirmationDialog(userId: String?) {
        userId?.let {
            // Create an AlertDialog to confirm deletion
            AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes") { dialog, which ->
                    deleteUser(it)
                }
                .setNegativeButton("No", null)
                .show()
        }
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

    private fun deleteUser(userId: String) {
        // Remove the user from Firebase Realtime Database using the unique key
        database.child(userId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireActivity(), "User deleted successfully", Toast.LENGTH_SHORT).show()
                // Reload the fragment to refresh the list
                parentFragmentManager.commit {
                    replace(R.id.fragment_container, AdminProfile())
                }
            } else {
                Toast.makeText(requireActivity(), "Failed to delete user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
