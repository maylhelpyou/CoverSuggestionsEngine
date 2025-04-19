package it.simonetugnetti.coversuggestionsengine.modelGoodReads

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * @author Simone Tugnetti
 */
@Root(strict = false, name = "work")
data class Work @JvmOverloads constructor(
    @field:Element(name = "books_count", required = false)
    var booksCount: Int? = 0,
    @field:Element(name = "text_reviews_count", required = false)
    var textReviewsCount: Int? = 0,
    @field:Element(name = "original_publication_month", required = false)
    var originalPublicationMonth: Int? = 0,
    @field:Element(name = "best_book", required = false)
    var bestBook: BestBook? = null,
    @field:Element(name = "original_publication_day", required = false)
    var originalPublicationDay: Int? = 0,
    @field:Element(name = "average_rating", required = false)
    var averageRating: Double? = 0.0,
    @field:Element(name = "id", required = false)
    var id: Int? = 0,
    @field:Element(name = "original_publication_year", required = false)
    var originalPublicationYear: Int? = 0,
    @field:Element(name = "ratings_count", required = false)
    var ratingsCount: Int? = 0
)