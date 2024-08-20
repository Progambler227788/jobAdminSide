package com.talhaatif.jobadminapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.talhaatif.jobadminapp.EditJob
import com.talhaatif.jobadminapp.ParticipantsJob
import com.talhaatif.jobadminapp.R
import com.talhaatif.jobadminapp.databinding.RvJobBinding
import com.talhaatif.jobadminapp.model.Job

class JobAdapter(private var jobs: List<Job>, private val activity: Activity) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    companion object {
        const val EDIT_JOB_REQUEST_CODE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = RvJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]
        holder.bind(job)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditJob::class.java).apply {
                putExtra("job_id", job.jid)
            }
            activity.startActivityForResult(intent, EDIT_JOB_REQUEST_CODE)
        }
        holder.binding.applyButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, ParticipantsJob::class.java).apply {
                putExtra("job_id", job.jid)
            }
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = jobs.size

    fun updateJobs(newJobs: List<Job>) {
        this.jobs = newJobs
        notifyDataSetChanged()
    }

    inner class JobViewHolder(val binding: RvJobBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.jobTitle.text = job.jobTitle
            binding.companyAndLocation.text = "${job.jobCompany}, ${job.jobLocation}"
            binding.jobType.text = job.jobType
            binding.jobMode.text = job.jobMode
            binding.salaryRange.text = "$${job.jobSalaryStartRange} - $${job.jobSalaryEndRange}"

            // Load the company logo if it's available
            Glide.with(binding.companyLogo.context)
                .load(job.jobImage)
                .placeholder(R.drawable.cartoon_happy_eyes)
                .into(binding.companyLogo)
        }
    }
}
