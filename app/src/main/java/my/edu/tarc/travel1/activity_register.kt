package my.edu.tarc.travel1

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import my.edu.tarc.travel1.databinding.FragmentActivityRegisterBinding

class activity_register : Fragment() {
    private var _binding: FragmentActivityRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActivityRegisterBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // Inform the system that this fragment has its own options menu
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Authentication and Database references
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Show or hide the verification code field based on the admin role switch
        binding.adminRole.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.verificationCodeLayout.visibility = View.VISIBLE
            } else {
                binding.verificationCodeLayout.visibility = View.GONE
            }
        }

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val phone = binding.phoneEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val isAdmin = binding.adminRole.isChecked // Capture the role

            // Validate input
            if (username.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                if (!isPhoneNumberValid(phone)) {
                    Toast.makeText(requireActivity(), "Invalid phone number format. Use 01X-XXXXXXX", Toast.LENGTH_SHORT).show()
                } else {
                    if (isAdmin) {
                        val verificationCode = binding.verificationCodeEditText.text.toString()
                        if (verificationCode == "45500") {
                            checkUsernameExists(username) { exists ->
                                if (!exists) {
                                    registerUser(email, password, username, phone, isAdmin)
                                } else {
                                    Toast.makeText(requireActivity(), "Username already exists. Please choose a different one.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(requireActivity(), "Not a valid verification code", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        checkUsernameExists(username) { exists ->
                            if (!exists) {
                                registerUser(email, password, username, phone, isAdmin)
                            } else {
                                Toast.makeText(requireActivity(), "Username already exists. Please choose a different one.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isPhoneNumberValid(phone: String): Boolean {
        val phonePattern = "01\\d-\\d{7}"
        return phone.matches(Regex(phonePattern))
    }

    private fun checkUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val usersRef = database.getReference("users")

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists()) // Return true if the username exists
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity(), "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false) // Treat database error as non-existing username
            }
        })
    }

    private fun registerUser(email: String, password: String, username: String, phone: String, isAdmin: Boolean) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful, now store additional data in the Realtime Database
                    val user = auth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        val role = if (isAdmin) "admin" else "user"
                        val userProfile = User(username, email, phone, role)

                        val usersRef = database.getReference("users")
                        usersRef.child(uid).setValue(userProfile)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(requireActivity(), "Registered successfully", Toast.LENGTH_SHORT).show()
                                    navigateToLoginFragment()
                                } else {
                                    Toast.makeText(requireActivity(), "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(requireActivity(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToLoginFragment() {
        parentFragmentManager.commit {
            replace(R.id.fragment_container, activity_login())
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

    data class User(val username: String, val email: String, val phone: String, val role: String)
}
