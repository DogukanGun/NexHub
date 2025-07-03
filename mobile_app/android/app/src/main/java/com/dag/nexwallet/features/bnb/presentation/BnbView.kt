import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dag.nexwallet.R
import com.dag.nexwallet.base.components.Accordion
import com.dag.nexwallet.ui.theme.dividerColor


@Composable
fun BnbScreen(){
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            stringResource(R.string.ai_view_title),
            style = MaterialTheme.typography.titleLarge
                .copy(color = Color.White),
            modifier = Modifier
                .padding(8.dp)
        )
        HorizontalDivider(
            modifier = Modifier
                .padding(8.dp),
            color = dividerColor
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Accordion(stringResource(R.string.ai_view_create_image)) {
                    Column {
                        Button(
                            onClick = {}
                        ){
                            Text(stringResource(R.string.ai_view_create_image_button_filter))
                        }
                        Button(
                            onClick = {}
                        ){
                            Text(stringResource(R.string.ai_view_create_image_by_text))
                        }
                    }
                }
                Accordion(stringResource(R.string.ai_view_create_video)) {
                    Column {
                        Button(
                            onClick = {}
                        ){
                            Text(stringResource(R.string.ai_view_create_video_by_text))
                        }
                    }
                }
            }
            Text(
                stringResource(R.string.ai_view_warning),
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = Color.LightGray),
                textAlign = TextAlign.Center
            )

        }

    }
}

@Composable
@Preview
fun BnbScreenPreview(){
    BnbScreen()
}