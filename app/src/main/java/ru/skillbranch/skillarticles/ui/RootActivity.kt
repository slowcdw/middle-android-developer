package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Checkable
import android.widget.ImageView

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory
import java.lang.Exception

class RootActivity : AppCompatActivity() {
    var searchTextNow : String? = null


    private lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this,vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this){
            renderUi(it)
            setupToolbar()
        }
        viewModel.observeNotifications(this){
            renderNotification(it)
        }

        searchTextNow = savedInstanceState?.getString("SEARCH_TEXT")

//        Log.d("M_MainActivity", "savedInstanceState: $searchTextNow")

/*
        btn_like.setOnClickListener{
            Snackbar.make(coordinator_container,"test", Snackbar.LENGTH_LONG)
                .setAnchorView(bottombar)
                .show()
        }

        switch_mode.setOnClickListener{
            delegate.localNightMode = if (switch_mode.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        }

 */
    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(searchTextNow.isNullOrBlank()) searchTextNow = null
        else {
            outState?.putString("SEARCH_TEXT", searchTextNow)
        }
//        Log.d("M_MainActivity", "SEARCH_TEXT: $searchTextNow")
    }


    private fun setupSubmenu() {
        btn_text_up.setOnClickListener{viewModel.handleUpText()}
        btn_text_down.setOnClickListener{viewModel.handleDownText()}
        switch_mode.setOnClickListener{viewModel.handleNightMode()}
    }

    private fun setupBottombar() {
        btn_like.setOnClickListener{viewModel.handleLike()}
        btn_bookmark.setOnClickListener{viewModel.handleBookmark()}
        btn_share.setOnClickListener{viewModel.handleShare()}
        btn_settings.setOnClickListener{viewModel.handleToggleMenu()}

/*        btn_like.setOnClickListener{
            it as Checkable
            it.toggle()
            Snackbar.make(it, if (it.isChecked) "set like" else "unset like", Snackbar.LENGTH_LONG)
                .setAnchorView(bottombar)
                .show()
        }

        btn_settings.setOnClickListener {
            it as Checkable
            it.toggle()
            if (it.isChecked) submenu.open() else submenu.close()
        }*/
    }

    private fun renderUi(data: ArticleState) {
        btn_settings.isChecked = data.isShowMenu
        //bind submenu state
        if(data.isShowMenu) submenu.open() else submenu.close()

        //bind article person data
        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        //bind submenu views
        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode = if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (data.isBigText) {
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        }else{
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }
        //bind content
        tv_text_content.text = if (data.isLoadingContent) "loading" else data.content.first() as String

        //bind toolbar
        toolbar.title = data.title ?: "Skill Articles"
        toolbar.subtitle = data.category ?: "loading..."
        if (data.categoryIcon != null) toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        try {
            val logo = if (toolbar.childCount>2) toolbar.getChildAt(2) as ImageView else null
            logo?.scaleType = ImageView.ScaleType.CENTER_CROP
            val lp = logo?.layoutParams as? Toolbar.LayoutParams
            lp?.let {
                it.width = this.dpToIntPx(40)
                it.height = this.dpToIntPx(40)
                it.marginEnd = this.dpToIntPx(16)
                logo.layoutParams = it
            }
        }catch (e: Exception){

        }


    }
    private fun renderNotification(notify: Notify){
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)
            .setActionTextColor(getColor(R.color.color_accent_dark))

        when(notify){
            is Notify.TextMessage -> {/* nothing */}
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel){
                    notify.actionHandler?.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar){
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel){
                        notify.errHandler?.invoke()
                    }
                }
            }
        }
        snackbar.show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)
//        Log.d("M_MainActivity", "11111")
        val searchView = menuItem?.actionView as SearchView
        if (searchTextNow != null) {
            menuItem.expandActionView();
            searchView.setQuery(searchTextNow, false)
            searchView.clearFocus();
//            Log.d("M_MainActivity", "setIconified")
        }else{
//            Log.d("M_MainActivity", "222222")
        }

        searchView.queryHint = "Введите строку поиска"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
//                viewModel.handleSearchQuery(query)
                searchTextNow = query
//                Log.d("M_MainActivity", "onQueryTextSubmit: $searchTextNow")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
//                viewModel.handleSearchQuery(newText)
                searchTextNow = newText
//                Log.d("M_MainActivity", "onQueryTextSubmit: $searchTextNow")
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }
}