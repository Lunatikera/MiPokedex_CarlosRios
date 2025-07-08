package rios.carlos.minipokedex_rioscarlos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PokemonAdapter(
    private val pokemonList: List<Pokemon>
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    private val typeColorMap = mapOf(
        "Normal" to R.drawable.bg_type_normal,
        "Fuego" to R.drawable.bg_type_fuego,
        "Agua" to R.drawable.bg_type_agua,
        "Planta" to R.drawable.bg_type_planta,
        "Eléctrico" to R.drawable.bg_type_electrico,
        "Hielo" to R.drawable.bg_type_hielo,
        "Lucha" to R.drawable.bg_type_luchador,
        "Veneno" to R.drawable.bg_type_veneno,
        "Tierra" to R.drawable.bg_type_tierra,
        "Volador" to R.drawable.bg_type_volador,
        "Psíquico" to R.drawable.bg_type_psiquico,
        "Bicho" to R.drawable.bg_type_insecto,
        "Insecto" to R.drawable.bg_type_insecto,
        "Roca" to R.drawable.bg_type_roca,
        "Fantasma" to R.drawable.bg_type_fantasma,
        "Dragón" to R.drawable.bg_type_dragon,
        "Siniestro" to R.drawable.bg_type_siniestro,
        "Acero" to R.drawable.bg_type_metal,
        "Hada" to R.drawable.bg_type_hada
    )

    private val typeIconMap = mapOf(
        "Normal" to R.drawable.ic_normal,
        "Fuego" to R.drawable.ic_fuego,
        "Agua" to R.drawable.ic_agua,
        "Planta" to R.drawable.ic_planta,
        "Eléctrico" to R.drawable.ic_electrico,
        "Hielo" to R.drawable.ic_hielo,
        "Lucha" to R.drawable.ic_lucha,
        "Veneno" to R.drawable.ic_veneno,
        "Tierra" to R.drawable.ic_tierra,
        "Volador" to R.drawable.ic_volador,
        "Psíquico" to R.drawable.ic_psiquico,
        "Bicho" to R.drawable.ic_insecto, // alias
        "Insecto" to R.drawable.ic_insecto,
        "Roca" to R.drawable.ic_roca,
        "Fantasma" to R.drawable.ic_fantasma,
        "Dragón" to R.drawable.ic_dragon,
        "Siniestro" to R.drawable.ic_siniestro,
        "Acero" to R.drawable.ic_metal,
        "Hada" to R.drawable.ic_hada
    )

    private val itemBackgroundMap = mapOf(
        "Normal" to "#919AA2",
        "Fuego" to "#FF9D55",
        "Agua" to "#5090D6",
        "Eléctrico" to "#F4D23C",
        "Planta" to "#63BC5A",
        "Hielo" to "#73CEC0",
        "Lucha" to "#CE416B",
        "Veneno" to "#A33EA1",
        "Tierra" to "#D97845",
        "Volador" to "#89AAE3",
        "Psíquico" to "#FA7179",
        "Bicho" to "#A6B91A",
        "Roca" to "#C5B78C",
        "Fantasma" to "#5269AD",
        "Dragón" to "#0B6DC3",
        "Siniestro" to "#5A5465",
        "Acero" to "#5A8EA2",
        "Hada" to "#EC8FE6"
    )

    private val itemBackground = mapOf(
        "Normal" to R.color.bg_normal,
        "Fuego" to R.color.bg_fuego,
        "Agua" to R.color.bg_agua,
        "Planta" to R.color.bg_planta,
        "Eléctrico" to R.color.bg_electrico,
        "Hielo" to R.color.bg_hielo,
        "Lucha" to R.color.bg_luchador,
        "Veneno" to R.color.bg_veneno,
        "Tierra" to R.color.bg_tierra,
        "Volador" to R.color.bg_volador,
        "Psíquico" to R.color.bg_psiquico,
        "Bicho" to R.color.bg_insecto,
        "Insecto" to R.color.bg_insecto,
        "Roca" to R.color.bg_roca,
        "Fantasma" to R.color.bg_fantasma,
        "Dragón" to R.color.bg_dragon,
        "Siniestro" to R.color.bg_siniestro,
        "Acero" to R.color.bg_metal,
        "Hada" to R.color.bg_hada
    )

    class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: View = itemView.findViewById(R.id.rootLayout)
        val ivPokemon: ImageView = itemView.findViewById(R.id.ivPokemon)
        val tvPokemonName: TextView = itemView.findViewById(R.id.tvName)
        val tvPokemonNumber: TextView = itemView.findViewById(R.id.tvNumber)
        val llTypesContainer: LinearLayout = itemView.findViewById(R.id.typeContainer)
        val imageBackgroundContainer: CardView = itemView.findViewById(R.id.imageBackgroundContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pokemon_layout, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        holder.tvPokemonName.text = pokemon.name
        holder.tvPokemonNumber.text = "#${pokemon.number}"

        Glide.with(holder.itemView.context)
            .load(pokemon.imageUrl)
            .into(holder.ivPokemon)

        val firstType = pokemon.types.firstOrNull() ?: "Normal"

        // Fondo del contenedor de la imagen con color hex
        val bgHexColor = itemBackgroundMap[firstType] ?: "#FFFFFF"
        holder.imageBackgroundContainer.setCardBackgroundColor(android.graphics.Color.parseColor(bgHexColor))

        // Fondo general del item con recurso de color
        val bgColorRes = itemBackground[firstType] ?: R.color.bg_default
        holder.rootLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, bgColorRes))

        holder.llTypesContainer.removeAllViews()

        for (type in pokemon.types) {
            val chip = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.chip_type, holder.llTypesContainer, false)

            val icon = chip.findViewById<ImageView>(R.id.iconType)
            val text = chip.findViewById<TextView>(R.id.tvTypeName)

            text.text = type
            icon.setImageResource(typeIconMap[type] ?: R.drawable.ic_normal)
            chip.background = ContextCompat.getDrawable(holder.itemView.context, typeColorMap[type] ?: R.drawable.bg_chip_type_default)

            holder.llTypesContainer.addView(chip)
        }
    }

    override fun getItemCount(): Int = pokemonList.size
}
