package it.simonetugnetti.coversuggestionsengine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import it.simonetugnetti.coversuggestionsengine.room.InfoBooksDatabase

/**
 * Suggestions Cover
 * Applicazione per il suggerimento di cover verso una piattaforma specifica
 * @author Simone Tugnetti
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Custom Toolbar usando un resource XML
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_toolbar)

        InfoBooksDatabase.get(application)

    }

    // Creazione di un Option Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    // Eseguire un'operazione per il rispettivo item selezionato nell'option menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.historyItem)
            this.findNavController(R.id.navHostFragment).navigate(R.id.historyFragment, null,
            NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(android.R.anim.slide_in_left)
                .setExitAnim(android.R.anim.slide_out_right)
                .setPopEnterAnim(android.R.anim.slide_in_left)
                .setPopExitAnim(android.R.anim.slide_out_right)
                .build()
            )
        return super.onOptionsItemSelected(item)
    }

}
