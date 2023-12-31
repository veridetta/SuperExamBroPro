package com.vr.superexambropro.adapter
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.helper.calculateRemainingTime
import com.vr.superexambropro.helper.convertStringToDate
import com.vr.superexambropro.helper.formatDateToIndonesian
import com.vr.superexambropro.helper.startTimerSiswa
import com.vr.superexambropro.helper.updateCountdownTextSiswa
import com.vr.superexambropro.helper.updateFirebase
import com.vr.superexambropro.model.PaketModel
import com.vr.superexambropro.model.UjianModel
import java.text.SimpleDateFormat
import java.util.Locale


class SiswaUjianAdapter(
    private var dataList: MutableList<UjianModel>,
    val context: Context,
) : RecyclerView.Adapter<SiswaUjianAdapter.DataViewHolder>() {
    public var filteredDataList: MutableList<UjianModel> = mutableListOf()
    init {
        filteredDataList.addAll(dataList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredDataList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }
    fun filter(query: String) {
        filteredDataList.clear()
        if (query !== null || query !=="") {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (data in dataList) {
                val nam = data.namaSiswa?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredDataList.add(data)
                    Log.d("Ada ", data.namaSiswa.toString())
                }
            }
        } else {
            filteredDataList.addAll(dataList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredDataList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_siswa, parent, false)
        return DataViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredDataList.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val currentData = filteredDataList[position]
        holder.tvNamaSiswa.text = currentData.namaSiswa
        holder.tvStatus.text = currentData.status
        val date = convertStringToDate(currentData.waktuMulai!!,"yyyy-MM-dd HH:mm:ss")
        holder.tvTanggal.text = formatDateToIndonesian(date!!)
        //holder.tvTanggal.text = formatDateToIndonesian(convertStringToDate(currentData.waktuMulai.toString(),"dd MMMM yyyy")!!)
        val remaining = calculateRemainingTime(currentData.waktuSelesai!!,currentData.durasi!!.toLong()).toInt()
        Log.d("REMAINING ", remaining.toString())
        if (remaining <= 0){
            holder.tvTimer.text = "Selesai"
            if (currentData.status == "Sedang Mengerjakan"){
                //hash map string any
                val map = hashMapOf<String,Any>(
                    "status" to "Selesai"
                )
                updateFirebase("ujian", FirebaseFirestore.getInstance(),"ujian",currentData.documentId.toString(),map){
                    Log.d("Update","Berhasil")
                }
            }
        }else{
            //startTimerSiswa(remaining,holder.tvTimer)
            var timeLeft : Long = 0
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            try {
                // Parse tanggal pengerjaan ke dalam objek Date
                val waktuSelesai = dateFormat.parse(currentData.waktuSelesai!!)
                // Hitung selisih waktu antara tanggal pengerjaan dan waktu sekarang dalam milidetik
                val waktuSekarangMill = System.currentTimeMillis()
                val waktuSelesaiMill = waktuSelesai?.time ?: 0
                 timeLeft =  waktuSelesaiMill - waktuSekarangMill
                if (timeLeft < 0) {
                    timeLeft = 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var contdown : CountDownTimer? = null
            var timerR = false
            contdown?.cancel()
            contdown = object : CountDownTimer(timeLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    holder.tvTimer.text = ""
                    var timeLeftx = millisUntilFinished
                    //updateCountdownTextSiswa(holder.tvTimer,timeLeft)
                    val hours = (timeLeftx / 3600000).toInt()
                    val minutes = ((timeLeftx % 3600000) / 60000).toInt()
                    val seconds = ((timeLeftx % 60000) / 1000).toInt()
                    val timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    holder.tvTimer.text = timeLeftFormatted
                }
                override fun onFinish() {
                    timerR = false
                    // Timer selesai
                    holder.tvTimer.text = "Selesai"
                }
            }
            contdown?.start()
            timerR = true
        }
        holder.tvKode.text = currentData.kodeKeamanan
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaSiswa: TextView = itemView.findViewById(R.id.tvNamaSiswa)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvTimer: TextView = itemView.findViewById(R.id.tvTimer)
        val tvKode: TextView = itemView.findViewById(R.id.tvKode)

    }
}
