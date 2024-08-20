package com.talhaatif.jobadminapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.talhaatif.jobadminapp.adapter.ParticipantAdapter
import com.talhaatif.jobadminapp.databinding.ActivityParticipantsJobBinding
import com.talhaatif.jobadminapp.firebase.Variables
import com.talhaatif.jobadminapp.model.User

class ParticipantsJob : AppCompatActivity() {
    private lateinit var binding: ActivityParticipantsJobBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var participantAdapter: ParticipantAdapter
    private lateinit var jobId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParticipantsJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = Variables.db

        // Retrieve job ID passed from previous activity
        jobId = intent.getStringExtra("job_id") ?: ""
        Log.d("ParticipantsJob", "Job ID: $jobId")


        fetchParticipants()

        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        participantAdapter = ParticipantAdapter(users, this)
        binding.participantRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.participantRecyclerView.adapter = participantAdapter
    }

    private fun fetchParticipants() {
        // Reference to the job document
        val jobRef = firestore.collection("jobs").document(jobId)

        // Get the job document
        jobRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Retrieve the appliedUsers field
                val jobAppliedUsersIds = document.get("jobAppliedUsers") as? List<String> ?: emptyList()

                Log.d("ParticipantsJob", "Size: ${jobAppliedUsersIds.size}")



                if (jobAppliedUsersIds .isNotEmpty()) {
                    // Fetch user details for these IDs
                    fetchUsersDetails(jobAppliedUsersIds )
                } else {
                    // No applied users

                    participantAdapter.users = emptyList()


                }
            } else {
                Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to fetch job details", Toast.LENGTH_SHORT).show()
        }
    }
    private var users = mutableListOf<User>()
    private fun fetchUsersDetails(userIds: List<String>) {
        // Create a query to fetch user documents where the UID is in the provided list
        firestore.collection("users").whereIn("uid", userIds).get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    val job = document.toObject(User::class.java)
                    users.add(job)
                }
                 Log.d("ParticipantsJob", "Fetched users: $users") // Add this line
                participantAdapter.users = users
                participantAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch users ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    fun updateJobStatus(userId: String, status: String) {
        // Update the job status for the given user
        val userRef = firestore.collection("users").document(userId)
        val jobRef = firestore.collection("jobs").document(jobId)

        userRef.get().addOnSuccessListener { document ->
            val appliedJobs = document.get("appliedJobs") as? List<Map<String, Any>> ?: emptyList()
            val updatedJobs = appliedJobs.map {
                if (it["jid"] == jobId) {
                    it.toMutableMap().apply { this["jobStatus"] = status }
                } else {
                    it
                }
            }

            userRef.update("appliedJobs", updatedJobs)
                .addOnSuccessListener {
                    Toast.makeText(this, "Job status updated", Toast.LENGTH_SHORT).show()
                    // Update the job document
                    val appliedUsers = document.get("jobAppliedUsers") as? List<String> ?: emptyList()
                    if (status == "accepted") {
                        jobRef.update("jobAppliedUsers", appliedUsers + userId)
                    }

//                    // Refresh participant list
//                    fetchParticipants()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update job status", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
