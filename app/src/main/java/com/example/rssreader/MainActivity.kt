package com.example.rssreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
//
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
//
import com.example.rssreader.ui.theme.RSSReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        val people = listOf(
//            Person("DJ", "Malone", 25),
//            Person("DJ", "Trampu", 30),
//            Person("DJ", "DJampu", 29),
//            Person("DJ", "DJampu", 33),
//            Person("MB", "Trampu", 36),
//            Person("MB", "Trampu", 38),
//            Person("MB", "Trampu", 39),
//        )
//
//        val peopleFiltered = people.filter { it.age >= 30 && it.firstName == "DJ" }

        val rssItems = listOf(
            RSSItem("welcome","aaaaaaaaaa","text"),
            RSSItem("welcome","aaaaaaaaaa","text"),
            RSSItem("welcome","aaaaaaaaaa","text"),
            RSSItem("welcome","aaaaaaaaaa","text"),
            RSSItem("welcome","aaaaaaaaaa","text"),
        )

        setContent {
            RSSReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android", modifier = Modifier.padding(innerPadding)
                    )
                    LazyColumn {
//                        items(people) { person ->
//                            Text(text = person)
//                        }
//                        items(peopleFiltered) {
//                            CardView(it)
////                            person -> ListItem(person) <- 이거 줄인게 "ListItem(it)" 임
//                        }
                        items(rssItems) {
                            RSSItemText(it)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!", modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        RSSReaderTheme {
            Greeting("Android")
        }
    }
}

@Composable
fun CardView(person: Person) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
//        Column {
        Row {
            Image(
                // R <- Resource(res), drawable <- 디렉토리명, baseline_person_24 <- 파일명
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = "Photo of person",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
            Column {
                Text(
                    text = person.firstName + " " + person.lastName,
                    modifier = Modifier.padding(top = 15.dp)
                )
                Text(
                    text = "age : " + person.age, modifier = Modifier.padding(0.dp)
                )
            }
        }
    }
}

@Composable
fun RSSItemText(){
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

    }
}












