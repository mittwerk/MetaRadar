package f.cking.software.ui.filter

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import f.cking.software.R
import f.cking.software.domain.model.RadarProfile
import f.cking.software.utils.graphic.SystemNavbarSpacer
import f.cking.software.utils.navigation.BackCommand
import f.cking.software.utils.navigation.Router

@OptIn(ExperimentalMaterial3Api::class)
object SelectFilterScreen {

    @Composable
    fun Screen(
        initialFilterState: FilterUiState,
        router: Router,
        onConfirm: (filterState: RadarProfile.Filter) -> Unit
    ) {

        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            topBar = {
                AppBar(scrollBehavior) { router.navigate(BackCommand) }
            },
            content = { paddings ->
                Column(
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(paddings)
                ) {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                FilterScreen.Filter(
                                    filterState = initialFilterState,
                                    router = router,
                                    onDeleteClick = { router.navigate(BackCommand) }
                                )
                            }
                        }
                    }

                    Surface(shadowElevation = 12.dp) {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary)
                                .fillMaxWidth(),
                        ) {
                            val context = LocalContext.current
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.primaryContainer),
                                onClick = {
                                    val filter = initialFilterState
                                        .takeIf { it.isCorrect() }
                                        ?.let { FilterUiMapper.mapToDomain(it) }

                                    if (filter != null) {
                                        router.navigate(BackCommand)
                                        onConfirm.invoke(filter)
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.filter_is_not_valid), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(text = stringResource(R.string.confirm), color = MaterialTheme.colorScheme.onPrimary)
                            }

                            SystemNavbarSpacer()
                        }
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppBar(scrollBehavior: TopAppBarScrollBehavior, onBackClick: () -> Unit) {
        TopAppBar(
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
            title = {
                Text(text = stringResource(R.string.create_filter))
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )
    }
}