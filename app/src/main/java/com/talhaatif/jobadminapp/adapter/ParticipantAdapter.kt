package com.talhaatif.jobadminapp.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.talhaatif.jobadminapp.ParticipantsJob
import com.talhaatif.jobadminapp.R
import com.talhaatif.jobadminapp.databinding.RvParticipateBinding
import com.talhaatif.jobadminapp.model.User

class ParticipantAdapter(var users: List<User>, private val activity: Activity) :
    RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = RvParticipateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
        holder.binding.acceptButton.setOnClickListener {
            if (activity is ParticipantsJob) {
                activity.updateJobStatus(user.uid, "accepted")
            }
        }
        holder.binding.rejectButton.setOnClickListener {
            if (activity is ParticipantsJob) {
                activity.updateJobStatus(user.uid, "rejected")
            }
        }
    }

    override fun getItemCount(): Int = users.size

    inner class ParticipantViewHolder(val binding: RvParticipateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.userName.text = user.username
            binding.userLocation.text = user.location

            // Load the company logo if it's available
            Glide.with(binding.userProfile.context)
                .load(user.profilePic)
                .placeholder(R.drawable.cartoon_happy_eyes)
                .into(binding.userProfile)
        }
    }
}