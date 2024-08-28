package my.edu.tarc.travel1

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.chaquo.python.Python
import kotlinx.coroutines.*
import my.edu.tarc.travel1.databinding.FragmentTravelRecBinding
import my.edu.tarc.travel1.history.History
import java.text.SimpleDateFormat
import java.util.*

class TravelRec : Fragment() {

    private var _binding: FragmentTravelRecBinding? = null
    private val binding get() = _binding!!

    private val calendar = Calendar.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val cuisineOptions = arrayOf("asian", "malaysian", "chinese", "cafe", "seafood", "fusion", "european", "indian", "italian", "thai", "bar", "grill")
    private val subcategoryOptions = arrayOf("sights & landmarks", "museums", "shopping", "zoo & aquariums", "nature & parks")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTravelRecBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and Database references
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Start Date Picker
        binding.buttonStartDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.textViewStartDate.text = "Selected Start Date: ${format.format(calendar.time)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() // Set current date as minimum date
            }.show()
        }

        // End Date Picker
        binding.buttonEndDate.setOnClickListener {
            val startDate = calendar.timeInMillis
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.textViewEndDate.text = "Selected End Date: ${format.format(calendar.time)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = startDate + (24 * 60 * 60 * 1000) // Set minimum date to 1 day after start date
            }.show()
        }

        // Set up destination dropdown
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.destination_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDestination.adapter = adapter
        }

        // Set up cuisine multi-select dialog
        binding.buttonSelectCuisine.setOnClickListener {
            val selectedCuisines = mutableSetOf<String>()
            AlertDialog.Builder(requireContext())
                .setTitle("Select Cuisines")
                .setMultiChoiceItems(cuisineOptions, null) { _, which, isChecked ->
                    if (isChecked) {
                        selectedCuisines.add(cuisineOptions[which])
                    } else {
                        selectedCuisines.remove(cuisineOptions[which])
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    binding.textViewCuisine.text = "Preferred Cuisines: ${selectedCuisines.joinToString(", ")}"
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Set up subcategory multi-select dialog
        binding.buttonSelectSubcategory.setOnClickListener {
            val selectedSubcategories = mutableSetOf<String>()
            AlertDialog.Builder(requireContext())
                .setTitle("Select Subcategories")
                .setMultiChoiceItems(subcategoryOptions, null) { _, which, isChecked ->
                    if (isChecked) {
                        selectedSubcategories.add(subcategoryOptions[which])
                    } else {
                        selectedSubcategories.remove(subcategoryOptions[which])
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    binding.textViewSubcategory.text = "Preferred Subcategories: ${selectedSubcategories.joinToString(", ")}"
                }
                .setNegativeButton("Cancel", null)
                .show()
        }



        // Set the click listener for the button in onViewCreated
        binding.buttonGenerate.setOnClickListener {
            generateRecommendations()
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

    private fun generateRecommendations() {
        val startDate = binding.textViewStartDate.text.toString().replace("Selected Start Date: ", "")
        val endDate = binding.textViewEndDate.text.toString().replace("Selected End Date: ", "")
        val destination = binding.spinnerDestination.selectedItem.toString()
        val budget = binding.editTextBudget.text.toString().toFloatOrNull()

        val cuisineText = binding.textViewCuisine.text.toString().replace("Preferred Cuisines: ", "")
        val cuisineList = cuisineText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val subcategoryText = binding.textViewSubcategory.text.toString().replace("Preferred Subcategories: ", "")
        val subcategoryList = subcategoryText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Validation for null or empty fields
        if (startDate.isEmpty() || endDate.isEmpty() || destination == "Select a destination" || budget == null) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validation for at least 3 selected cuisines and subcategories
        if (cuisineList.size < 3) {
            Toast.makeText(requireContext(), "Please select at least 3 cuisines", Toast.LENGTH_SHORT).show()
            return
        }

        if (subcategoryList.size < 3) {
            Toast.makeText(requireContext(), "Please select at least 3 subcategories", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear previous results
        binding.textViewRecommendations.text = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val py = Python.getInstance()
                val pyobj = py.getModule("myscript")

                val recommendations = pyobj.callAttr(
                    "generate_recommendations",
                    requireContext(),
                    startDate,
                    endDate,
                    destination,
                    budget,
                    cuisineList.toTypedArray(),
                    subcategoryList.toTypedArray()
                ).toString()

                val travelSelection = TravelSelection(
                    startDate = startDate,
                    endDate = endDate,
                    destination = destination,
                    budget = budget,
                    cuisines = cuisineList.toList(),
                    subcategories = subcategoryList.toList()
                )

                withContext(Dispatchers.Main) {
                    val bundle = Bundle().apply {
                        putString("recommendations", recommendations)
                        putParcelable("travel_selection", travelSelection)
                    }

                    val resultFragment = Result().apply {
                        arguments = bundle
                    }

                    parentFragmentManager.commit {
                        replace(R.id.fragment_container, resultFragment)
                        addToBackStack(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textViewRecommendations.text = "An error occurred: ${e.message}"
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class TravelSelection(
        val startDate: String = "",
        val endDate: String = "",
        val destination: String = "",
        val budget: Float = 0f,
        val cuisines: List<String> = emptyList(),
        val subcategories: List<String> = emptyList()
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readFloat(),
            parcel.createStringArrayList() ?: emptyList(),
            parcel.createStringArrayList() ?: emptyList()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(startDate)
            parcel.writeString(endDate)
            parcel.writeString(destination)
            parcel.writeFloat(budget)
            parcel.writeStringList(cuisines)
            parcel.writeStringList(subcategories)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<TravelSelection> {
            override fun createFromParcel(parcel: Parcel): TravelSelection {
                return TravelSelection(parcel)
            }

            override fun newArray(size: Int): Array<TravelSelection?> {
                return arrayOfNulls(size)
            }
        }
    }
}
