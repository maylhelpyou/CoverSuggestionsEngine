package it.simonetugnetti.coversuggestionsengine.room

import android.app.Application
import androidx.room.*
import it.simonetugnetti.coversuggestionsengine.util.InfoBooksTable

/**
 * Info Books Database
 * Classe astratta utilizzata per creare un database in locale contenente le informazioni dei libri
 * aventi le cover suggerite
 * @author Simone Tugnetti
 */
@Database(version = 1, entities = [InfoBooksTable::class], exportSchema = false)
abstract class InfoBooksDatabase: RoomDatabase() {

    // Dao di Room Database
    abstract val getInfoBooksDao: InfoBooksDao

    // Tutto ciò al suo interno viene istanziato una volta sola
    companion object {

        // L'istanza non verrà salvata in cache
        @Volatile
        private var INSTANCE: InfoBooksDatabase? = null

        fun get(application: Application): InfoBooksDatabase {
            // L'interno non verrà mai eseguito contemporaneamente su due thread diversi
            synchronized(this) {
                var instance = INSTANCE

                // Non permette l'istanza multipla dello stesso database
                if (instance == null) {
                    instance = Room
                        .databaseBuilder(application, InfoBooksDatabase::class.java,
                            "infoBooks")
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }

}