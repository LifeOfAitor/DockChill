package com.dev.dockchill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.dockchill.databinding.ItemTodoBinding

// Adapter hau RecyclerView-ari datuak emateko erabiltzen da.
// Eginbehar bakoitza zerrendan bistaratzen da bere izenburuarekin, deskribapenarekin eta "egina" checkbox batekin.
// Horrez gain, botoiak ditu gora edo behera mugitzeko itemak.

class EginbeharraAdapter(
    private val eginbeharrak: MutableList<Eginbeharra>, // Zerrenda mutagarria, elementuak kendu edo mugitzeko
    private val onEginbeharraClick: (Eginbeharra) -> Unit, // Checkbox-a klik egitean deitzen den lambda
    private val onEginbeharraItemClick: (Eginbeharra) -> Unit, // Izenburua eta deskripzioa click egitean
    private val moveListener: OnEginbeharraMoveListener // "Gora" eta "Behera" botoientzako listener
) : RecyclerView.Adapter<EginbeharraAdapter.EginbeharraViewHolder>() {

    // ViewHolder bakoitza item baten datuak mantentzen ditu
    inner class EginbeharraViewHolder(val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // "Gora" botoia klik egitean posizioa txikitu
            binding.buttonGora.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    moveListener.onMoveUp(position)
                }
            }

            // "Behera" botoia klik egitean posizioa handitu
            binding.buttonBehera.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    moveListener.onMoveDown(position)
                }
            }
        }
    }

    // Layout-a inflate eta ViewHolder-a sortu
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EginbeharraViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EginbeharraViewHolder(binding)
    }

    // Datuak bistaratu posizio bakoitzean
    override fun onBindViewHolder(holder: EginbeharraViewHolder, position: Int) {
        val eginbeharra = eginbeharrak[position]

        holder.binding.textViewIzenburua.text = eginbeharra.izenburua
        holder.binding.textViewDeskripzioa.text = eginbeharra.deskripzioa
        holder.binding.checkboxDone.isChecked = eginbeharra.egina

        // Checkbox-a klik egitean, egoera eguneratu eta listener-a deitu
        holder.binding.checkboxDone.setOnClickListener {
            eginbeharra.egina = holder.binding.checkboxDone.isChecked
            onEginbeharraClick(eginbeharra)
        }
        // Izenburua + deskripzioa layoutaren klik listener-a
        holder.binding.layoutTexts.setOnClickListener {
            onEginbeharraItemClick(eginbeharra)
        }
    }

    // Item kopurua bueltatu
    override fun getItemCount() = eginbeharrak.size

    // Elementuak zerrendan mugitzeko metodoa
    fun swapItems(from: Int, to: Int) {
        if (from in eginbeharrak.indices && to in eginbeharrak.indices) {
            val temp = eginbeharrak[from]
            eginbeharrak[from] = eginbeharrak[to]
            eginbeharrak[to] = temp
            notifyItemMoved(from, to)
        }
    }

    // Zerrenda osoa eguneratzeko metodoa (pantaila nagusian erabiliko da onResume-n)
    fun actualizarLista(berria: List<Eginbeharra>) {
        eginbeharrak.clear()
        eginbeharrak.addAll(berria)
        notifyDataSetChanged()
    }
}