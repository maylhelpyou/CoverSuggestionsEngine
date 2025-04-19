package it.simonetugnetti.coversuggestionsengine.modelGoodReads

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * @author Simone Tugnetti
 */
@Root(strict = false, name = "Request")
data class Request @JvmOverloads constructor(
    @field:Element(name = "authentication", required = false)
    var authentication: Boolean = true,
    @field:Element(name = "key", required = false)
    var key: String = "",
    @field:Element(name = "method", required = false)
    var method: String = ""
)