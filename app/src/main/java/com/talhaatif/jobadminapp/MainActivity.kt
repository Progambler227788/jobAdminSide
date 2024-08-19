package com.talhaatif.jobadminapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.talhaatif.adapter.jobadminapp.JobAdapter
import com.talhaatif.jobadminapp.databinding.ActivityMainBinding
import com.talhaatif.jobportalclient.model.Job
import com.talhaatif.notesapplication.firebase.Variables

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var jobAdapter: JobAdapter
    private val jobList = mutableListOf<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.fab.setOnClickListener {
            startActivityForResult(Intent(this, AddJob::class.java), EDIT_JOB)
        }

        // Start the shimmer animation
        binding.shimmerViewContainer.startShimmer()

        loadJobsFromFirestore()
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(jobList,this)
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = jobAdapter
        }
    }

    private fun loadJobsFromFirestore() {
        jobList.clear()
        Variables.db.collection("jobs")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val job = document.toObject(Job::class.java)
                    jobList.add(job)
                }
                jobAdapter.updateJobs(jobList)
                jobAdapter.notifyDataSetChanged()

                // Stop the shimmer animation and show the RecyclerView
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.rvNotes.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting jobs: ", exception)

                // Stop the shimmer animation and show an error message or empty state
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.noDataText.visibility = View.VISIBLE
            }
    }

    companion object {
        const val EDIT_JOB = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_JOB && resultCode == RESULT_OK) {
            val noteUpdated = data?.getBooleanExtra("jobUpdated", false) ?: false
            if (noteUpdated) {

                loadJobsFromFirestore()

            }
        }
    }
}
