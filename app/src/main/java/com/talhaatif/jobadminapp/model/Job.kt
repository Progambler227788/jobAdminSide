package com.talhaatif.jobadminapp.model


data class Job(
    val jid: String = "",
    val jobTitle:String = "",
    val jobImage:String = "",
    val jobSalaryEndRange: String = "",
    val jobSalaryStartRange: String = "",
    val jobUsersApplied: List<String> = emptyList(),
    val jobCompany: String = "",
    val jobDescription: String = "",
    val jobIndustry: String = "",
    val jobLocation: String = "",
    val jobMode: String = "Remote",
    val jobPostedTime: String = "",
    val jobAppliedUsers: List<String> = emptyList(),
    val jobRequirement: String = "",
    val jobType: String = "Full Time"
)
