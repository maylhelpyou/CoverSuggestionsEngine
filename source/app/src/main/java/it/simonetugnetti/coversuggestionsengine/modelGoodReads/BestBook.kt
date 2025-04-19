package it.simonetugnetti.coversuggestionsengine.modelGoodReads

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * @author Simone Tugnetti
 */
@Root(strict = false, name = "best_book")
data class BestBook @JvmOverloads constructor(
    @field:Element(name = "small_image_url", required = false)
    var smallImageUrl: String? = "",
    @field:Element(name = "author", required = false)
    var author: Author? = null,
    @field:Element(name = "image_url", required = false)
    var imageUrl: String? = "",
    @field:Element(name = "id", required = false)
    var id: Int? = 0,
    @field:Element(name = "title", required = false)
    var title: String? = ""
)
