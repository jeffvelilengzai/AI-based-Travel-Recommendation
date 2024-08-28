package my.edu.tarc.travel1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import my.edu.tarc.travel1.databinding.FragmentActivityLoginBinding

class activity_login : Fragment() {
    private var _binding: FragmentActivityLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActivityLoginBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Authentication and Database references
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.signup.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, activity_register())
                addToBackStack(null)
            }
        }

        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authenticateUser(email, password)
            } else {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgotPassword.setOnClickListener {
            val email = binding.usernameEditText.text.toString()

            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(requireActivity(), "Please enter your email to reset the password", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun authenticateUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Authentication successful, get the current user
                    val user = auth.currentUser
                    if (user != null) {
                        // Fetch additional user data from the Realtime Database
                        val usersRef = database.getReference("users")
                        usersRef.child(user.uid).get().addOnSuccessListener { snapshot ->
                            val userProfile = snapshot.getValue(User::class.java)
                            if (userProfile != null) {
                                Toast.makeText(requireActivity(), "Login successful", Toast.LENGTH_SHORT).show()
                                navigateToUserProfile(userProfile)
                            } else {
                                Toast.makeText(requireActivity(), "User profile not found", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(requireActivity(), "Failed to fetch user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireActivity(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireActivity(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireActivity(), "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun navigateToUserProfile(user: User) {
        val userProfileFragment = UserProfile()
        parentFragmentManager.commit {
            replace(R.id.fragment_container, userProfileFragment)
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // Hide the menu items when this fragment is visible
    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
    }

    data class User(val username: String = "", val email: String = "", val phone: String = "", val role: String = "")
}
