package rios.carlos.minipokedex_rioscarlos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class AddPokemonActivity : AppCompatActivity() {

    companion object {
        private const val CLOUD_NAME = "dx8nxf9xf"
        private const val UPLOAD_PRESET = "pokemon-preset"
        private const val REQUEST_IMAGE_GET = 1
    }

    private var imageUri: Uri? = null
    private var imagePublicUrl: String? = null

    private lateinit var etName: TextInputEditText
    private lateinit var etNumber: TextInputEditText
    private lateinit var imageView: ImageView
    private lateinit var tvSelectImage: TextView
    private lateinit var btnSavePokemon: MaterialButton
    private lateinit var spinnerType1: Spinner
    private lateinit var spinnerType2: Spinner

    private val db = FirebaseFirestore.getInstance()

    private val tipos = listOf(
        "Selecciona tipo", "Normal", "Fuego", "Agua", "Eléctrico", "Planta",
        "Hielo", "Lucha", "Veneno", "Tierra", "Volador",
        "Psíquico", "Bicho", "Roca", "Fantasma", "Dragón",
        "Siniestro", "Acero", "Hada"
    )

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

    private lateinit var adapterType1: TypeSpinnerAdapter
    private lateinit var adapterType2: TypeSpinnerAdapter

    private var isProgrammaticChange = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_pokemon)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initCloudinary()

        etName = findViewById(R.id.etName)
        etNumber = findViewById(R.id.etNumber)
        imageView = findViewById(R.id.imageView)
        tvSelectImage = findViewById(R.id.tvSelectImage)
        btnSavePokemon = findViewById(R.id.btnSavePokemon)
        spinnerType1 = findViewById(R.id.spinnerType1)
        spinnerType2 = findViewById(R.id.spinnerType2)

        adapterType1 = TypeSpinnerAdapter(this, tipos)
        adapterType2 = TypeSpinnerAdapter(this, tipos)

        spinnerType1.adapter = adapterType1
        spinnerType2.adapter = adapterType2

        spinnerType1.setSelection(0, false)
        spinnerType2.setSelection(0, false)

        spinnerType1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isProgrammaticChange) return
                val selectedType = adapterType1.getItem(position)
                if (selectedType == "Selecciona tipo") {
                    isProgrammaticChange = true
                    adapterType2.reset()
                    spinnerType2.setSelection(0, false)
                    isProgrammaticChange = false
                } else {
                    isProgrammaticChange = true
                    adapterType2.setExcludedType(selectedType)
                    // Si el segundo spinner tiene el mismo tipo, reseteamos
                    if (spinnerType2.selectedItem.toString() == selectedType) {
                        spinnerType2.setSelection(0, false)
                    }
                    isProgrammaticChange = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerType2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isProgrammaticChange) return
                val selectedType = adapterType2.getItem(position)
                if (selectedType == "Selecciona tipo") {
                    isProgrammaticChange = true
                    adapterType1.reset()
                    spinnerType1.setSelection(0, false)
                    isProgrammaticChange = false
                } else {
                    isProgrammaticChange = true
                    adapterType1.setExcludedType(selectedType)
                    // Si el primer spinner tiene el mismo tipo, reseteamos
                    if (spinnerType1.selectedItem.toString() == selectedType) {
                        spinnerType1.setSelection(0, false)
                    }
                    isProgrammaticChange = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }

        btnSavePokemon.setOnClickListener {
            if (!validateFields()) return@setOnClickListener
            savePokemon()
        }
    }

    private fun validateFields(): Boolean {
        val numberValue = etNumber.text.toString().toIntOrNull()
        val nameValue = etName.text.toString().trim()
        val type1 = spinnerType1.selectedItem.toString()
        val type2 = spinnerType2.selectedItem.toString()

        if (numberValue == null || nameValue.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (type1 == "Selecciona tipo" && type2 == "Selecciona tipo") {
            Toast.makeText(this, "Seleccione al menos un tipo", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            val fullPhotoUrl: Uri? = data?.data
            if (fullPhotoUrl != null) {
                changeImage(fullPhotoUrl)
                imageUri = fullPhotoUrl
            }
        }
    }

    private fun changeImage(uri: Uri) {
        try {
            imageView.setImageURI(uri)
            tvSelectImage.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun savePokemonToFirestore() {
        val numberValue = etNumber.text.toString().toIntOrNull()
        val nameValue = etName.text.toString().trim()
        val type1 = spinnerType1.selectedItem.toString()
        val type2 = spinnerType2.selectedItem.toString()

        // Crear lista de tipos, ignorando "Selecciona tipo" y duplicados
        val tiposSeleccionados = mutableListOf<String>()
        if (type1 != "Selecciona tipo") tiposSeleccionados.add(type1)
        if (type2 != "Selecciona tipo" && type2 != type1) tiposSeleccionados.add(type2)

        val pokemon = hashMapOf(
            "number" to numberValue,
            "name" to nameValue,
            "imageUrl" to (imagePublicUrl ?: ""),
            "types" to tiposSeleccionados
        )

        db.collection("pokemons")
            .add(pokemon)
            .addOnSuccessListener {
                Toast.makeText(this, "Pokémon registrado con éxito", Toast.LENGTH_SHORT).show()
                etNumber.text?.clear()
                etName.text?.clear()
                imageView.setImageDrawable(null)
                tvSelectImage.visibility = View.VISIBLE
                imageUri = null
                imagePublicUrl = null

                // Resetear spinners de forma controlada
                isProgrammaticChange = true
                spinnerType1.setSelection(0, false)
                spinnerType2.setSelection(0, false)
                adapterType1.reset()
                adapterType2.reset()
                isProgrammaticChange = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun initCloudinary() {
        try {
            val config: MutableMap<String, String> = HashMap()
            config["cloud_name"] = CLOUD_NAME
            MediaManager.init(this, config)
        } catch (e: IllegalStateException) {
            Log.w("Cloudinary", "MediaManager ya estaba inicializado")
        }
    }

    private fun savePokemon(): String {
        if (imageUri == null) return ""

        return MediaManager.get().upload(imageUri)
            .unsigned(UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    imagePublicUrl = resultData?.get("secure_url") as String?
                    savePokemonToFirestore()
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("onError", error.toString())
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    inner class TypeSpinnerAdapter(
        private val context: Context,
        private val originalItems: List<String>
    ) : BaseAdapter() {

        private var items: List<String> = originalItems
        private var excludedType: String? = null

        fun setExcludedType(type: String?) {
            excludedType = if (type == "Selecciona tipo") null else type

            // Guardar selección actual
            val currentSelection = getSelectedItemSafe()

            items = originalItems.filter {
                it != excludedType && it != "Selecciona tipo"
            }.toMutableList().apply {
                add(0, "Selecciona tipo")
            }

            notifyDataSetChanged()

            // Restaurar selección si existe
            val index = items.indexOf(currentSelection)
            if (index >= 0) {
                // El spinner correspondiente
                (context as? AddPokemonActivity)?.let { activity ->
                    if (activity.spinnerType1.adapter === this) {
                        activity.spinnerType1.setSelection(index, false)
                    } else if (activity.spinnerType2.adapter === this) {
                        activity.spinnerType2.setSelection(index, false)
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
            return (context as? AddPokemonActivity)?.let { activity ->
                when {
                    activity.spinnerType1.adapter === this -> activity.spinnerType1.selectedItem?.toString()
                    activity.spinnerType2.adapter === this -> activity.spinnerType2.selectedItem?.toString()
                    else -> null
                }
            }
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
}
