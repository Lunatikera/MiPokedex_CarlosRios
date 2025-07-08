package rios.carlos.minipokedex_rioscarlos

data class Pokemon(
    val number: Int = 0,
    val name: String = "",
    val imageUrl: String = "",
    val types: List<String> = emptyList()
)
