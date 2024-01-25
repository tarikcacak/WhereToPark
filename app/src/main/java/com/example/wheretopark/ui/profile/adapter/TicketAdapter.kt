package com.example.wheretopark.ui.profile.adapter

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wheretopark.databinding.ValidTicketBinding
import com.example.wheretopark.models.ticket.Ticket
import java.util.Locale

class TicketAdapter() : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    var onItemLongClick: ((Ticket) -> Unit)? = null
    private var tickets = ArrayList<Ticket>()

    fun setTickets(tickets: ArrayList<Ticket>) {
        this.tickets = tickets
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        return TicketViewHolder(ValidTicketBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.binding.tvLocation.text = tickets[position].location
        holder.binding.tvDate.text = formatExpiringDate(tickets[position].expiring)
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(tickets[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    fun onItemLongClickListener(ticket: (Ticket) -> Unit) {
        onItemLongClick = ticket
    }

    private fun formatExpiringDate(expiringDate: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd MMM HH:mm:ss", Locale.ENGLISH)

        try {
            val date = inputFormat.parse(expiringDate)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Invalid Date"
        }
    }

    class TicketViewHolder(val binding: ValidTicketBinding): RecyclerView.ViewHolder(binding.root)
}