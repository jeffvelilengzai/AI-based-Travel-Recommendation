package my.edu.tarc.travel1



import android.graphics.Color
import android.os.Bundle
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
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import my.edu.tarc.travel1.databinding.FragmentReportBinding
import my.edu.tarc.travel1.history.History

class Report : Fragment() {

    private val database = FirebaseDatabase.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setupSpinners()
        binding.generateReportButton.setOnClickListener { generateReport() }
    }

    private fun setupSpinners() {
        val destinations = listOf("Penang", "Melaka", "Ipoh")
        val types = listOf("Hotel", "Restaurant", "Attraction")

        val destinationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, destinations)
        binding.destinationSpinner.adapter = destinationAdapter

        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        binding.typeSpinner.adapter = typeAdapter
    }

    private fun generateReport() {
        val selectedDestination = binding.destinationSpinner.selectedItem.toString()
        val selectedType = binding.typeSpinner.selectedItem.toString()

        val destinationType = when (selectedType) {
            "Hotel" -> "hotels"
            "Restaurant" -> "restaurants"
            "Attraction" -> "attractions"
            else -> return
        }

        val userRef = database.reference.child("users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typeCountMap = mutableMapOf<String, Int>()

                for (userSnapshot in snapshot.children) {
                    val travelsSnapshot = userSnapshot.child("travels")
                    for (travelSnapshot in travelsSnapshot.children) {
                        val travelSelection = travelSnapshot.child("travel_selection").getValue(TravelSelection::class.java)

                        if (travelSelection?.destination == selectedDestination) {
                            val recommendations = travelSnapshot.child("recommendations").child(destinationType)
                            for (itemSnapshot in recommendations.children) {
                                val recommendationItem = itemSnapshot.getValue(RecommendationItem::class.java)
                                val itemName = recommendationItem?.name ?: continue
                                typeCountMap[itemName] = typeCountMap.getOrDefault(itemName, 0) + 1
                            }
                        }
                    }
                }

                // Find the top 5 most popular items
                val top5 = typeCountMap.entries.sortedByDescending { it.value }.take(5)

                // Generate the bar chart
                val barEntries = top5.mapIndexed { index, entry -> BarEntry(index.toFloat(), entry.value.toFloat()) }
                val barDataSet = BarDataSet(barEntries, "Top 5 $selectedType in $selectedDestination")
                barDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

                val barData = BarData(barDataSet)

                // Set up the x-axis without place names
                val xAxis = binding.barChart.xAxis
                xAxis.valueFormatter = null  // No labels on x-axis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.isGranularityEnabled = true

                // Update the MarkerView with x-axis labels
                CustomMarkerView.xAxisLabels = top5.map { it.key }

                // Set up the y-axis to start from 0 with increments by 1
                val yAxisLeft = binding.barChart.axisLeft
                yAxisLeft.axisMinimum = 0f
                yAxisLeft.granularity = 1f

                // Hide right y-axis
                binding.barChart.axisRight.isEnabled = false

                // Apply the MarkerView to the chart
                val mv = CustomMarkerView(requireContext(), R.layout.marker_view)
                binding.barChart.marker = mv

                binding.barChart.apply {
                    data = barData
                    description.isEnabled = false
                    setFitBars(true)
                    invalidate() // Refresh the chart
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                Toast.makeText(context, "Failed to load data.", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
