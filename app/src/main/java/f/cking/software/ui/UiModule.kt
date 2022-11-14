package f.cking.software.ui

import f.cking.software.common.navigation.NavRouter
import f.cking.software.ui.devicelist.DeviceListViewModel
import f.cking.software.ui.main.MainViewModel
import f.cking.software.ui.profiledetails.ProfileDetailsViewModel
import f.cking.software.ui.radarprofile.RadarProfileViewModel
import f.cking.software.ui.selectfiltertype.SelectFilterTypeViewModel
import f.cking.software.ui.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object UiModule {
    val module = module {
        single { NavRouter() }
        viewModel { MainViewModel(get(), get(), get()) }
        viewModel { DeviceListViewModel(get()) }
        viewModel { SettingsViewModel(get(), get(), get()) }
        viewModel { RadarProfileViewModel(get(), get()) }
        viewModel { ProfileDetailsViewModel(it.get(), get(), get()) }
        viewModel { SelectFilterTypeViewModel(get()) }
    }
}