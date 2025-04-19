package it.simonetugnetti.coversuggestionsengine.modelGoodReads

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * @author Simone Tugnetti
 */
@Root(strict = false, name = "author")
data class Author @JvmOverloads constructor(
    @field:Element(name = "name", required = false)
    var name: String? = "",
    @field:Element(name = "id", required = false)
    var id: Int? = 0
)