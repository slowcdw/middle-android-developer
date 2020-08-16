package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.longText
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class ArticleViewModel(val articleId: String):
    BaseViewModel<ArticleState>(ArticleState()),
    IArticleViewModel  {
    private val repository = ArticleRepository
    private var menuIsShown:Boolean = false

    init {
        //subscriber on mutable data
        subscribeOnDataSource(getArticleData()){ article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
//                author = article.author,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format(),
                author = article.author
            )
        }

        subscribeOnDataSource(getArticleContent()){ content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()){ info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike

            )
        }

        //subscribe on setting
       subscribeOnDataSource(repository.getAppSettings()){ setting, state ->
           state.copy(
               isDarkMode = setting.isDarkMode,
               isBigText = setting.isBigText
           )
       }

    }

    //load text from network
    override fun getArticleContent(): LiveData<List<Any>?>{
        return repository.loadArticleContent(articleId)
    }

    //load data from mdb
    override fun getArticleData(): LiveData<ArticleData?>{
        return repository.getArticle(articleId)
    }

    //load data from db
    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?>{
        return repository.loadArticlePersonalInfo(articleId)
    }

    //app settings
    override fun handleNightMode(){
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    //personal article info
    override fun handleBookmark(){
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))

        val msg = if (currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.TextMessage(msg))
    }

    override fun handleLike(){
        Log.e("Article view model", "handle like: ");
        val isLiked = currentState.isLike;
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()

        val msg = if (!isLiked) Notify.TextMessage("Mark is liked")
        else{
            Notify.ActionMessage(
                "Don`t like it anymore",
                "No, still like it",
                toggleLike
            )
        }
        notify(msg)
    }

    //not implemented
    override fun handleShare(){
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    //session state
    override fun handleToggleMenu(){
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
        /*updateState {state ->
            state.copy(isShowMenu = !state.isShowMenu).also { menuIsShown = !state.isShowMenu }
        }*/
    }


    override fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch=isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearch(query: String?) {
        query ?: return
        val result = (currentState.content.firstOrNull() as? String).indexesOf(query)
            .map { it to it + query.length}
        updateState { it.copy(searchQuery = query, searchResults = result, searchPosition = 0) }
    }

    fun hideMenu(){
        updateState { it.copy(isShowMenu = false) }
    }

    fun showMenu(){
        updateState { it.copy(isShowMenu = menuIsShown) }
    }

    fun handleSearchQuery(query: String?){
        updateState { it.copy(searchQuery = query) }
    }

    fun handleIsSearch(isSearch: Boolean){
        updateState { it.copy(isSearch = isSearch) }
    }
    fun handleUpResult(){
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }
    fun handleDownResult(){
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

}

data class ArticleState(
    val isAuth: Boolean = false,
    val isLoadingContent: Boolean = true,
    val isLoadingReviews: Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false,
    val isBigText: Boolean = false,
    val isDarkMode: Boolean = false,
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val searchResults: List<Pair<Int,Int>> = emptyList(),
    val searchPosition: Int = 0,
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
):IViewModelState{
    override fun save(outState: Bundle) {
        outState.putAll(
            bundleOf(
                "isSearch" to isSearch,
                "searchQuery" to searchQuery,
                "searchResults" to searchResults,
                "searchPosition" to searchPosition

            )
        )
    }

    override fun restore(savedState: Bundle): ArticleState {
        return copy(
            isSearch = savedState["isSearch"] as Boolean,
            searchQuery = savedState["searchQuery"] as? String,
            searchResults = savedState["searchResults"] as List<Pair<Int, Int>>,
            searchPosition = savedState["searchPosition"] as Int
        )
    }
}