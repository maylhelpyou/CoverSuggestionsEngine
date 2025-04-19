package it.simonetugnetti.coversuggestionsengine.modelGoodReads

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * @author Simone Tugnetti
 */
@Root(strict = false, name = "results")
data class Results @JvmOverloads constructor(
    @field:ElementList(name = "work", inline = true, required = false)
    var work: List<Work>? = null
)