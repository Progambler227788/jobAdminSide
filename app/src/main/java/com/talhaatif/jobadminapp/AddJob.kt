package com.talhaatif.jobadminapp

import android.R
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.talhaatif.jobadminapp.databinding.ActivityAddJobBinding
import com.talhaatif.notesapplication.firebase.Variables
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddJob : AppCompatActivity() {
    private lateinit var binding: ActivityAddJobBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupIndustryDropdown()
        setupJobTypeDropdown()
        setupJobModeDropdown()

        binding.imageIcon.setOnClickListener {
            openImagePicker()
        }

        binding.addJobButton.setOnClickListener {
            addJob()
        }

        binding.jobTitleAutoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.industryAutoCompleteTextView.text.isNullOrEmpty()) {
                binding.jobTitleAutoCompleteTextView.clearFocus()
                Variables.displayErrorMessage("Please select an industry first.", this)
            }
        }
    }

    private fun setupIndustryDropdown() {
        val industries = arrayOf("Technology", "Healthcare", "Finance", "Education", "Data")
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, industries)
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
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, titles)
        binding.jobTitleAutoCompleteTextView.setAdapter(adapter)
    }


    private fun setupJobTypeDropdown() {
        val jobTypes = arrayOf("Full Time", "Part Time", "Internship") // Populate with your job types
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jobTypes)
        binding.jobTypeAutoCompleteTextView.setAdapter(adapter)
    }

    private fun setupJobModeDropdown() {
        val jobModes = arrayOf("Remote", "On-site", "Hybrid") // Populate with your job modes
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, jobModes)
        binding.jobModeAutoCompleteTextView.setAdapter(adapter)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            val fileName = getFileNameFromUri(imageUri!!)
            binding.imageEditText.setText(fileName)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                return cursor.getString(columnIndex)
            }
        }
        return "Unknown"
    }

    private fun addJob() {
        val title = binding.jobTitleAutoCompleteTextView.text.toString().trim().uppercase(Locale.getDefault())
        val description = binding.descriptionEditText.text.toString().trim().uppercase(Locale.getDefault())
        val industry = binding.industryAutoCompleteTextView.text.toString().trim().uppercase(Locale.getDefault())
        val jobType = binding.jobTypeAutoCompleteTextView.text.toString().trim().uppercase(Locale.getDefault())
        val jobMode = binding.jobModeAutoCompleteTextView.text.toString().trim().uppercase(Locale.getDefault())
        val salaryStartRange = binding.salaryStartRangeEditText.text.toString().trim().uppercase(Locale.getDefault())
        val salaryEndRange = binding.salaryEndRangeEditText.text.toString().trim().uppercase(Locale.getDefault())
        val company = binding.companyEditText.text.toString().trim().uppercase(Locale.getDefault())
        val location = binding.locationEditText.text.toString().trim().uppercase(Locale.getDefault())
        val requirement = binding.requirementEditText.text.toString().trim().uppercase(Locale.getDefault())
        val jid = Variables.db.collection("jobs").document().id

        if (title.isEmpty() || description.isEmpty() || industry.isEmpty() || jobType.isEmpty() || jobMode.isEmpty() ||
            salaryStartRange.isEmpty() || salaryEndRange.isEmpty() || company.isEmpty() || location.isEmpty() ||
            requirement.isEmpty()) {
            Variables.displayErrorMessage("All fields are required.", this)
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Adding job...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val jobData = hashMapOf(
            "jid" to jid,
            "jobTitle" to title,
            "jobDescription" to description,
            "jobIndustry" to industry,
            "jobType" to jobType,
            "jobMode" to jobMode,
            "jobSalaryStartRange" to salaryStartRange,
            "jobSalaryEndRange" to salaryEndRange,
            "jobCompany" to company,
            "jobLocation" to location,
            "jobRequirement" to requirement,
            "jobAppliedUsers" to listOf<String>(),
            "jobPostedTime" to SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
            "jobImage" to "" // Initially empty, will update after upload
        )

        if (imageUri != null) {
            val imageRef = Variables.storageRef.child("Companies/${UUID.randomUUID()}")
            imageRef.putFile(imageUri!!)
                .addOnCompleteListener(this) { uploadTask ->
                    if (uploadTask.isSuccessful) {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            jobData["jobImage"] = uri.toString()
                            saveJobToFirestore(jobData, progressDialog)
                        }
                    } else {
                        Variables.displayErrorMessage("Image upload failed.", this)
                        progressDialog.dismiss()
                    }
                }
        } else {
            saveJobToFirestore(jobData, progressDialog)
        }
    }

    private fun saveJobToFirestore(jobData: Map<String, Any>, progressDialog: ProgressDialog) {
        Variables.db.collection("jobs")
            .document(jobData["jid"] as String)
            .set(jobData, SetOptions.merge())
            .addOnSuccessListener {
                Variables.displayErrorMessage("Job added successfully!", this)
                progressDialog.dismiss()
                val resultIntent = Intent()
                resultIntent.putExtra("jobUpdated", true)
                setResult(RESULT_OK, resultIntent)
                finish() // Close the activity
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage("Error adding job: ${e.message}", this)
                Log.e("AddJob", "Error adding job: ${e.message}", e)
                progressDialog.dismiss()
            }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
