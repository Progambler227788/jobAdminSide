package com.talhaatif.jobadminapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.talhaatif.jobadminapp.databinding.ActivityEditJobBinding
import com.talhaatif.jobportalclient.model.Job
import com.talhaatif.notesapplication.firebase.Variables

class EditJob : AppCompatActivity() {
    private lateinit var binding: ActivityEditJobBinding
    private lateinit var jobId: String
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Setup dropdowns
        setupIndustryDropdown()
        setupJobTypeDropdown()
        setupJobModeDropdown()


        jobId = intent.getStringExtra("job_id") ?: ""
        loadJobDetails(jobId)

        binding.editJobButton.setOnClickListener {
            updateJobDetails()
        }

        binding.deleteJobButton.setOnClickListener {
            deleteJob()
        }
    }

    private fun loadJobDetails(jobId: String) {
        Variables.db.collection("jobs").document(jobId).get()
            .addOnSuccessListener { document ->
                job = document.toObject(Job::class.java) ?: return@addOnSuccessListener
                populateFields(job)
            }
            .addOnFailureListener { exception ->
                Log.e("EditJob", "Error loading job details", exception)
            }
    }

    private fun setupIndustryDropdown() {
        val industries = arrayOf("Technology", "Healthcare", "Finance", "Education", "Data")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, industries)
        binding.industryAutoCompleteTextView.setAdapter(adapter)

        // Listener to update job titles when an industry is selected
        binding.industryAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedIndustry = industries[position]
            setupJobTitleDropdown(selectedIndustry)
            binding.jobTitleAutoCompleteTextView.isEnabled = true
        }
    }

    private fun setupJobTitleDropdown(industry: String) {
        val jobTitles = mapOf(
            "Technology" to listOf("Software Engineer", "Data Scientist", "Product Manager"),
            "Healthcare" to listOf("Doctor", "Nurse", "Pharmacist"),
            "Finance" to listOf("Accountant", "Financial Analyst", "Investment Banker"),
            "Education" to listOf("Teacher", "Professor", "Education Consultant"),
            "Data" to listOf("Data Analyst", "Data Engineer", "Data Scientist")
        )

        val titles = jobTitles[industry] ?: emptyList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, titles)
        binding.jobTitleAutoCompleteTextView.setAdapter(adapter)
    }

    private fun setupJobTypeDropdown() {
        val jobTypes = arrayOf("Full Time", "Part Time", "Internship")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jobTypes)
        binding.jobTypeAutoCompleteTextView.setAdapter(adapter)
    }

    private fun setupJobModeDropdown() {
        val jobModes = arrayOf("Remote", "On-site", "Hybrid")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jobModes)
        binding.jobModeAutoCompleteTextView.setAdapter(adapter)
    }


    private fun populateFields(job: Job) {
        // Populate the fields with job details
        binding.industryAutoCompleteTextView.setText(job.jobIndustry)
        binding.jobTitleAutoCompleteTextView.setText(job.jobTitle)  // Correct field for job title
        binding.jobTypeAutoCompleteTextView.setText(job.jobType)
        binding.jobModeAutoCompleteTextView.setText(job.jobMode)
        binding.descriptionEditText.setText(job.jobDescription)
        binding.imageEditText.setText(job.jobImage)  // Assuming this is the image URL or filename
        binding.salaryStartRangeEditText.setText(job.jobSalaryStartRange)
        binding.salaryEndRangeEditText.setText(job.jobSalaryEndRange)
        binding.companyEditText.setText(job.jobCompany)
        binding.locationEditText.setText(job.jobLocation)
        binding.requirementEditText.setText(job.jobRequirement)
    }

    private fun updateJobDetails() {
        // Collect updated job details
        val updatedJob = job.copy(
            jobIndustry = binding.industryAutoCompleteTextView.text.toString(),
            jobTitle = binding.jobTitleAutoCompleteTextView.text.toString(),  // Use correct field
            jobType = binding.jobTypeAutoCompleteTextView.text.toString(),
            jobMode = binding.jobModeAutoCompleteTextView.text.toString(),
            jobDescription = binding.descriptionEditText.text.toString(),
            jobSalaryStartRange = binding.salaryStartRangeEditText.text.toString(),
            jobSalaryEndRange = binding.salaryEndRangeEditText.text.toString(),
            jobLocation = binding.locationEditText.text.toString(),
            jobRequirement = binding.requirementEditText.text.toString(),
            jobCompany = binding.companyEditText.text.toString()
        )

        Variables.db.collection("jobs").document(jobId).set(updatedJob)
            .addOnSuccessListener {
                Toast.makeText(this, "Job updated successfully", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                resultIntent.putExtra("jobUpdated", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e("EditJob", "Error updating job", exception)
                Toast.makeText(this, "Failed to update job", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteJob() {
        Variables.db.collection("jobs").document(jobId).delete()
            .addOnSuccessListener {
                clearAppliedUsers()
                Toast.makeText(this, "Job deleted successfully", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                resultIntent.putExtra("jobUpdated", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e("EditJob", "Error deleting job", exception)
                Toast.makeText(this, "Failed to delete job", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearAppliedUsers() {
        job.jobUsersApplied.forEach { userId ->
            Variables.db.collection("users").document(userId)
                .update("appliedJobs", FieldValue.arrayRemove(jobId))
                .addOnFailureListener { exception ->
                    Log.e("EditJob", "Error clearing applied jobs for user $userId", exception)
                }
        }
    }
}
