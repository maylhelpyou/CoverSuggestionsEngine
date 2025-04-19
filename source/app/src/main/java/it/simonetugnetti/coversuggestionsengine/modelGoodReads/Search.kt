package it.simonetugnetti.coversuggestionsengine.modelGoodReads

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * @author Simone Tugnetti
 */
@Root(strict = false, name = "search")
data class Search @JvmOverloads constructor(
    @field:Element(name = "results-end", required = false)
    var resultsEnd: Int = 0,
    @field:Element(name = "total-results", required = false)
    var totalResults: Int = 0,
    @field:Element(name = "query", required = false)
    var query: String = "",
    @field:Element(name = "source", required = false)
    var source: String = "",
    @field:Element(name = "query-time-seconds", required = false)
    var queryTimeSeconds: Double? = 0.0,
    @field:Element(name = "results", required = false)
    var results: Results? = null,
    @field:Element(name = "results-start", required = false)
    var resultsStart: Int = 0
)
