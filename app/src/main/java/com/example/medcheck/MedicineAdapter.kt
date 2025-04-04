package com.example.medcheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicineAdapter(
    private var medicines: List<Medicine>,
    private val onEditClick: (Medicine) -> Unit,
    private val onDeleteClick: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    // Create a data class to hold medicine information
    data class Medicine(
        val id: String = "",
        val name: String = "",
        val dosage: String = "",
        val frequency: String = "",
        val userId: String = ""
    ) {
        constructor() : this("", "", "", "", "") // Required for Firebase
    }

    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineName: TextView = itemView.findViewById(R.id.medicineName)
        val medicineDetails: TextView = itemView.findViewById(R.id.medicineDetails)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.medicineName.text = medicine.name
        holder.medicineDetails.text = "${medicine.dosage} • ${medicine.frequency}"

        holder.editButton.setOnClickListener {
            onEditClick(medicine)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(medicine)
        }
    }

    override fun getItemCount() = medicines.size

    // Add this function to update the list
    fun updateList(newList: List<Medicine>) {
        medicines = newList
        notifyDataSetChanged()
    }
}