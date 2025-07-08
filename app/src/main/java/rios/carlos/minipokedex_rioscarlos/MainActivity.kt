package rios.carlos.minipokedex_rioscarlos

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<Pokemon>()
    private val filteredList = mutableListOf<Pokemon>()
    private lateinit var etSearch: EditText
    private lateinit var spinnerType1: Spinner
    private lateinit var spinnerType2: Spinner

    private var isProgrammaticChange = false

    private lateinit var adapterType1: TypeSpinnerAdapter
    private lateinit var adapterType2: TypeSpinnerAdapter

    private val db = FirebaseFirestore.getInstance()

    private val tipos = listOf(
        "Selecciona tipo", "Normal", "Fuego", "Agua", "Eléctrico", "Planta",
        "Hielo", "Lucha", "Veneno", "Tierra", "Volador",
        "Psíquico", "Bicho", "Roca", "Fantasma", "Dragón",
        "Siniestro", "Acero", "Hada"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        etSearch = findViewById(R.id.etSearch)
        spinnerType1 = findViewById(R.id.spinnerType1)
        spinnerType2 = findViewById(R.id.spinnerType2)

        adapter = PokemonAdapter(filteredList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapterType1 = TypeSpinnerAdapter(this, tipos)
        adapterType2 = TypeSpinnerAdapter(this, tipos)

        spinnerType1.adapter = adapterType1
        spinnerType2.adapter = adapterType2

        spinnerType1.setSelection(0, false)
        spinnerType2.setSelection(0, false)


        val btnRegistrar: MaterialButton = findViewById(R.id.btnContent)
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, AddPokemonActivity::class.java)
            startActivity(intent)
        }
        spinnerType1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isProgrammaticChange) return
                val selectedType = adapterType1.getItem(position)
                isProgrammaticChange = true
                if (selectedType == "Selecciona tipo") {
                    adapterType2.reset()
                } else {
                    adapterType2.setExcludedType(selectedType)
                    if (spinnerType2.selectedItem == selectedType) {
                        spinnerType2.setSelection(0, false)
                    }
                }
                isProgrammaticChange = false
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerType2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isProgrammaticChange) return
                val selectedType = adapterType2.getItem(position)
                isProgrammaticChange = true
                if (selectedType == "Selecciona tipo") {
                    adapterType1.reset()
                } else {
                    adapterType1.setExcludedType(selectedType)
                    if (spinnerType1.selectedItem == selectedType) {
                        spinnerType1.setSelection(0, false)
                    }
                }
                isProgrammaticChange = false
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadPokemonFromFirestore()
    }

    private fun loadPokemonFromFirestore() {
        db.collection("pokemons")
            .orderBy("number")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshots == null) return@addSnapshotListener

                pokemonList.clear()
                for (document in snapshots) {
                    val pokemon = document.toObject(Pokemon::class.java)
                    pokemonList.add(pokemon)
                }
                applyFilters()
            }
    }

    private fun applyFilters() {
        val searchText = etSearch.text.toString().trim().lowercase()
        val selectedType1 = spinnerType1.selectedItem.toString()
        val selectedType2 = spinnerType2.selectedItem.toString()

        filteredList.clear()

        val filtered = pokemonList.filter { pokemon ->
            val matchesSearch = pokemon.name.lowercase().contains(searchText) ||
                    pokemon.number.toString().contains(searchText) ||
                    pokemon.types.any { it.lowercase().contains(searchText) }

            val typeFilter = when {
                selectedType1 == "Selecciona tipo" && selectedType2 == "Selecciona tipo" -> true
                selectedType1 != "Selecciona tipo" && selectedType2 == "Selecciona tipo" -> pokemon.types.contains(selectedType1)
                selectedType1 == "Selecciona tipo" && selectedType2 != "Selecciona tipo" -> pokemon.types.contains(selectedType2)
                else -> pokemon.types.contains(selectedType1) && pokemon.types.contains(selectedType2)
            }

            matchesSearch && typeFilter
        }

        filteredList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    inner class TypeSpinnerAdapter(
        private val context: android.content.Context,
        private val originalItems: List<String>
    ) : BaseAdapter() {

        private var items: List<String> = originalItems
        private var excludedType: String? = null

        fun setExcludedType(type: String?) {
            excludedType = if (type == "Selecciona tipo") null else type
            val currentSelection = getSelectedItemSafe()

            items = originalItems.filter { it != excludedType }.toMutableList().apply {
                if (!contains("Selecciona tipo")) add(0, "Selecciona tipo")
            }

            notifyDataSetChanged()

            // Restaurar selección si posible
            val index = items.indexOf(currentSelection)
            if (index >= 0) {
                if (context is MainActivity) {
                    if (context.spinnerType1.adapter === this) {
                        context.spinnerType1.setSelection(index, false)
                    } else if (context.spinnerType2.adapter === this) {
                        context.spinnerType2.setSelection(index, false)
                    }
                }
            }
        }

        fun reset() {
            excludedType = null
            items = originalItems
            notifyDataSetChanged()
        }

        private fun getSelectedItemSafe(): String? {
            return if (context is MainActivity) {
                when {
                    context.spinnerType1.adapter === this -> context.spinnerType1.selectedItem?.toString()
                    context.spinnerType2.adapter === this -> context.spinnerType2.selectedItem?.toString()
                    else -> null
                }
            } else null
        }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): String = items[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_type, parent, false)
            val tipo = getItem(position)

            val imgIcon = view.findViewById<ImageView>(R.id.imgTypeIcon)
            val tvName = view.findViewById<TextView>(R.id.tvTypeName)

            tvName.text = tipo.replaceFirstChar { it.uppercaseChar() }
            imgIcon.setImageResource(tipoIconMap[tipo] ?: R.drawable.ic_normal)

            imgIcon.visibility = if (tipo == "Selecciona tipo") View.GONE else View.VISIBLE

            val colorString = tipoColorMap[tipo] ?: "#FFFFFF"
            val color = Color.parseColor(colorString)

            val background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f
                setStroke(5, color)
            }

            view.background = background
            tvName.setTextColor(Color.BLACK)

            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_type, parent, false)
            val tipo = getItem(position)


            val tvName = view.findViewById<TextView>(R.id.tvTypeName)

            tvName.text = tipo

            if (tipo == "Selecciona tipo") {
                tvName.setTextColor(Color.BLACK)
                view.background = null
            } else {
                val color = Color.parseColor(tipoColorMap[tipo] ?: "#FFFFFF")
                val background = GradientDrawable().apply {
                    setColor(color)
                    cornerRadius = 16f
                }
                view.background = background
                tvName.setTextColor(Color.WHITE)
            }

            return view
        }
    }

    // Mapas para iconos y colores (copiados del AddPokemonActivity)
    private val tipoIconMap = mapOf(
        "Normal" to R.drawable.ic_normal,
        "Fuego" to R.drawable.ic_fuego,
        "Agua" to R.drawable.ic_agua,
        "Eléctrico" to R.drawable.ic_electrico,
        "Planta" to R.drawable.ic_planta,
        "Hielo" to R.drawable.ic_hielo,
        "Lucha" to R.drawable.ic_lucha,
        "Veneno" to R.drawable.ic_veneno,
        "Tierra" to R.drawable.ic_tierra,
        "Volador" to R.drawable.ic_volador,
        "Psíquico" to R.drawable.ic_psiquico,
        "Bicho" to R.drawable.ic_insecto,
        "Roca" to R.drawable.ic_roca,
        "Fantasma" to R.drawable.ic_fantasma,
        "Dragón" to R.drawable.ic_dragon,
        "Siniestro" to R.drawable.ic_siniestro,
        "Acero" to R.drawable.ic_metal,
        "Hada" to R.drawable.ic_hada
    )

    private val tipoColorMap = mapOf(
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
}
