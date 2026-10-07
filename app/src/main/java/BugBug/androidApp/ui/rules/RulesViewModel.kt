package BugBug.androidApp.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import BugBug.androidApp.data.repository.GoldRate
import BugBug.androidApp.data.repository.GoldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RulesViewModel(
    private val repository: GoldRepository = GoldRepository()
) : ViewModel() {

    private val _goldRate = MutableStateFlow<GoldRate?>(null)
    val goldRate: StateFlow<GoldRate?> = _goldRate.asStateFlow()

    init {
        viewModelScope.launch {
            _goldRate.value = repository.getGoldRate()
        }
    }
}