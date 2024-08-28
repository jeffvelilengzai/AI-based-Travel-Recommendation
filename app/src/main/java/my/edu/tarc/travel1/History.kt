package my.edu.tarc.travel1.history

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import my.edu.tarc.travel1.AdminProfile
import my.edu.tarc.travel1.HistoryAdapter
import my.edu.tarc.travel1.R
import my.edu.tarc.travel1.Report
import my.edu.tarc.travel1.TravelHistory
import my.edu.tarc.travel1.TravelRec
import my.edu.tarc.travel1.TravelSelection
import my.edu.tarc.travel1.UserProfile
import my.edu.tarc.travel1.activity_login
import my.edu.tarc.travel1.databinding.FragmentHistoryBinding
import my.edu.tarc.travel1.hotel.Hotel
import my.edu.tarc.travel1.restaurant.Restaurant
import my.edu.tarc.travel1.hotel.Attraction

class History : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var historyAdapter: HistoryAdapter

    private val travelHistoryList = mutableListOf<TravelHistory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        loadTravelHistoryFromFirebase()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(travelHistoryList) { travelId ->
            deleteTravelHistory(travelId)
        }
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewHistory.adapter = historyAdapter
    }

    private fun loadTravelHistoryFromFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val travelRef = database.child("users").child(userId).child("travels")

        travelRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                travelHistoryList.clear()
                for (travelSnapshot in snapshot.children) {
                    val id = travelSnapshot.key // Get the unique ID
                    val travelSelection = travelSnapshot.child("travel_selection").getValue(
                        TravelSelection::class.java)
                    val hotels = travelSnapshot.child("recommendations/hotels").children.map { it.getValue(Hotel::class.java)!! }
                    val restaurants = travelSnapshot.child("recommendations/restaurants").children.map { it.getValue(Restaurant::class.java)!! }
                    val attractions = travelSnapshot.child("recommendations/attractions").children.map { it.getValue(Attraction::class.java)!! }

                    val travelHistory = TravelHistory(
                        id = id,
                        travelSelection = travelSelection,
                        hotels = hotels,
                        restaurants = restaurants,
                        attractions = attractions
                    )

                    travelHistoryList.add(travelHistory)
                }
                historyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryFragment", "Error loading travel history", error.toException())
                Toast.makeText(context, "Failed to load travel history", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun deleteTravelHistory(travelId: String) {
        val userId = auth.currentUser?.uid ?: return
        val travelRef = database.child("users").child(userId).child("travels").child(travelId)

        // Show confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Travel History")
            .setMessage("Are you sure you want to delete this travel history?")
            .setPositiveButton("Yes") { dialog, _ ->
                travelRef.removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Travel history deleted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Log.e("HistoryFragment", "Error deleting travel history", it)
                    Toast.makeText(context, "Failed to delete travel history", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()  // Dismiss the dialog without deleting
            }
            .create()
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
