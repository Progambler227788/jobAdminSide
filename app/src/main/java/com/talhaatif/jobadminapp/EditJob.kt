package com.talhaatif.jobadminapp
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.talhaatif.jobadminapp.databinding.ActivityEditJobBinding
import com.talhaatif.jobadminapp.model.Job
import com.talhaatif.jobadminapp.firebase.Variables
import java.util.UUID

class EditJob : AppCompatActivity() {
    private lateinit var binding: ActivityEditJobBinding
    private lateinit var jobId: String
    private lateinit var job: Job
    private var imageUri: Uri? = null
    private lateinit var storage: FirebaseStorage
    private var newImageUri: Uri? = null // Store new image URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()

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

        binding.imageIcon.setOnClickListener {
            pickImage()
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

    private fun pickImage() {
        // Launch the image picker
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            newImageUri = data?.data
            binding.imageEditText.setText("New image selected")
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
        binding.industryAutoCompleteTextView.setText(job.jobIndustry)
        binding.jobTitleAutoCompleteTextView.setText(job.jobTitle)
        binding.jobTypeAutoCompleteTextView.setText(job.jobType)
        binding.jobModeAutoCompleteTextView.setText(job.jobMode)
        binding.descriptionEditText.setText(job.jobDescription)
        binding.imageEditText.setText("Attached")  // Assuming this is the image URL or filename
        binding.salaryStartRangeEditText.setText(job.jobSalaryStartRange)
        binding.salaryEndRangeEditText.setText(job.jobSalaryEndRange)
        binding.companyEditText.setText(job.jobCompany)
        binding.locationEditText.setText(job.jobLocation)
        binding.requirementEditText.setText(job.jobRequirement)
    }

    private fun updateJobDetails() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating job...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        // Delete previous image if a new one is selected
        job.jobImage?.let { imageUrl ->
            val imageRef: StorageReference = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete()
                .addOnSuccessListener {
                    uploadNewImageAndSaveJob()
                    progressDialog.dismiss()
                }
                .addOnFailureListener { exception ->
                    Log.e("EditJob", "Error deleting previous image", exception)
                    uploadNewImageAndSaveJob() // Proceed even if deletion fails
                    progressDialog.dismiss()
                }
        } // If no previous image, just upload the new one
    }

    private fun uploadNewImageAndSaveJob() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating job...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        if (newImageUri != null) { // Check if a new image is selected
            val newImageRef = Variables.storageRef.child("Companies/${UUID.randomUUID()}")
            newImageRef.putFile(newImageUri!!)
                .addOnCompleteListener(this) { uploadTask ->
                    if (uploadTask.isSuccessful) {
                        newImageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Update job details with the new image URL
                            val updatedJob = job.copy(
                                jobIndustry = binding.industryAutoCompleteTextView.text.toString(),
                                jobTitle = binding.jobTitleAutoCompleteTextView.text.toString(),
                                jobType = binding.jobTypeAutoCompleteTextView.text.toString(),
                                jobMode = binding.jobModeAutoCompleteTextView.text.toString(),
                                jobDescription = binding.descriptionEditText.text.toString(),
                                jobSalaryStartRange = binding.salaryStartRangeEditText.text.toString(),
                                jobSalaryEndRange = binding.salaryEndRangeEditText.text.toString(),
                                jobLocation = binding.locationEditText.text.toString(),
                                jobRequirement = binding.requirementEditText.text.toString(),
                                jobCompany = binding.companyEditText.text.toString(),
                                jobImage = uri.toString() // Set new image URL
                            )

                            Variables.db.collection("jobs").document(jobId).set(updatedJob)
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Job updated successfully", Toast.LENGTH_SHORT).show()
                                    val resultIntent = Intent()
                                    resultIntent.putExtra("jobUpdated", true)
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    progressDialog.dismiss()
                                    Log.e("EditJob", "Error updating job", exception)
                                    Toast.makeText(this, "Failed to update job", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        progressDialog.dismiss()
                        Variables.displayErrorMessage("Image upload failed.", this)
                    }
                }
        } else {
            // If no new image is selected, just update the job details without changing the image
            val updatedJob = job.copy(
                jobIndustry = binding.industryAutoCompleteTextView.text.toString(),
                jobTitle = binding.jobTitleAutoCompleteTextView.text.toString(),
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
                    progressDialog.dismiss()
                    Toast.makeText(this, "Job updated successfully", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("jobUpdated", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Log.e("EditJob", "Error updating job", exception)
                    Toast.makeText(this, "Failed to update job", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun deleteJob() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Adding job...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        Variables.db.collection("jobs").document(jobId).delete()
            .addOnSuccessListener {
                clearAppliedUsers()
                Toast.makeText(this, "Job deleted successfully", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                resultIntent.putExtra("jobUpdated", true)
                progressDialog.dismiss()
                setResult(RESULT_OK, resultIntent)
                finish()

            }
            .addOnFailureListener { exception ->
                Log.e("EditJob", "Error deleting job", exception)
                Toast.makeText(this, "Failed to delete job", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }

    private fun clearAppliedUsers() {
        job.jobUsersApplied.forEach { userId ->
            Variables.db.collection("users").document(userId)
                .update("appliedJobs", FieldValue.arrayRemove(jobId))
                .addOnFailureListener { exception ->
                    Log.e("EditJob", "Error removing job from user's applied jobs", exception)
                }
        }
    }
}
