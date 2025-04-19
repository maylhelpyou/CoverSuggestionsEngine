package it.simonetugnetti.coversuggestionsengine.modelGoodReads

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * Goodreads Response
 * Classe utilizzata per gestire la risposta XML di Goodreads al momento della richiesta eseguita
 * con retrofit
 * @author Simone Tugnetti
 */
@Root(strict = false, name = "GoodreadsResponse")  // Nome della field e senza necessità di matching
data class GoodreadsResponse @JvmOverloads constructor(  // Il costruttore viene autogenerato
    @field:Element(name = "search", required = false)
    var search: Search? = null,
    @field:Element(name = "Request", required = false)
    var request: Request? = null
)