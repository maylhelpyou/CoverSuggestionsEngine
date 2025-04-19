package it.simonetugnetti.coversuggestionsengine.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.simonetugnetti.coversuggestionsengine.util.InfoBooksTable

/**
 * Info Books Dao
 * Interfaccia DAO, cioè Data Access Object, in grado di comunicare al databese Room utilizzando
 * il linguaggio SQL
 * @author Simone Tugnetti
 */
@Dao
interface InfoBooksDao {

    // Funzione suspend, usata per comunicare su thread differenti tramite le Coroutines
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: InfoBooksTable)

    @Query("select * from InfoBooksTable")
    suspend fun getBooks(): List<InfoBooksTable>

    @Query("select * from InfoBooksTable where idRequest = :request")
    suspend fun getSpecificBook(request: Int): InfoBooksTable?

    @Query("delete from InfoBooksTable")
    suspend fun deleteAllBooks()

    @Query("delete from InfoBooksTable where idRequest = :request")
    suspend fun deleteBook(request: Int)

}