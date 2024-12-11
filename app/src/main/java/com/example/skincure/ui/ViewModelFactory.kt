package com.example.skincure.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.skincure.data.repository.Repository
import com.example.skincure.di.Injection
import com.example.skincure.ui.camera.CameraViewModel
import com.example.skincure.ui.contactus.ContactUsViewModel
import com.example.skincure.ui.dashboard.DashboardViewModel
import com.example.skincure.ui.favorite.FavoriteViewModel
import com.example.skincure.ui.history.HistoryViewModel
import com.example.skincure.ui.home.HomeViewModel
import com.example.skincure.ui.login.LoginViewModel
import com.example.skincure.ui.mainboard.MainBoardViewModel
import com.example.skincure.ui.news.NewsViewModel
import com.example.skincure.ui.news_detail.NewsDetailViewModel
import com.example.skincure.ui.otp.OtpViewModel
import com.example.skincure.ui.profile.ProfileViewModel
import com.example.skincure.ui.result_detail.ResultDetailViewModel
import com.example.skincure.ui.setting.SettingsViewModel
import com.example.skincure.ui.signup.SignUpViewModel
import kotlin.jvm.java

class ViewModelFactory(
    private val repository: Repository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

        return when {
            modelClass.isAssignableFrom(ResultDetailViewModel::class.java) -> {
                ResultDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel() as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel() as T
            }
            modelClass.isAssignableFrom(OtpViewModel::class.java) -> {
                OtpViewModel() as T
            }
            modelClass.isAssignableFrom(NewsDetailViewModel::class.java) -> {
                NewsDetailViewModel() as T
            }
            modelClass.isAssignableFrom(NewsViewModel::class.java) -> {
                NewsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(MainBoardViewModel::class.java) -> {
                MainBoardViewModel() as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel() as T
            }
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> {
                FavoriteViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ContactUsViewModel::class.java) -> {
                ContactUsViewModel() as T
            }
            modelClass.isAssignableFrom(CameraViewModel::class.java) -> {
                CameraViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel() as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object{
        @Volatile
        private var instance: ViewModelFactory? = null
        fun getInstance(context: Context): ViewModelFactory = instance ?: synchronized(this) {
            instance ?: ViewModelFactory(Injection.provideRepository(context))
        }.also { instance = it }
    }
}