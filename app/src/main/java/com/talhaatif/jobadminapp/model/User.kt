package com.talhaatif.jobadminapp.model

data class User(
    val uid: String = "",
    val username: String = "",
    val useremail: String = "",
    val industry: String = "",
    val role: String = "",
    val number: String = "",
    val location: String = "",
    val cv: String = "",
    val experience: String = "",
    val currentJob: String = "",
    val profilePic: String = "",
    val appliedJobs: List<Map<String, Any>> = emptyList()
)
