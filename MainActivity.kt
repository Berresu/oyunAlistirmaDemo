package com.example.ilkuygulamam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    OyunEkrani()
                }
            }
        }
    }
}

@Composable
fun OyunEkrani() {
    val context = LocalContext.current

    var mevcutDurumId by remember { mutableStateOf("baslangic") }
    var kanitBulundu by remember { mutableStateOf(false) }

    val diyalogHaritasi = mapOf(
        "baslangic" to DiyalogDurumu(
            metin = "Karakol: Dedektif, bu vakada iki şüpheli var. Hangisiyle konuşmak istersin?",
            secenekler = listOf(
                DiyalogSecenegi("Işık Aslan ile konuş.", "supheli_a_gidis"),
                DiyalogSecenegi("Yekta Demir ile konuş.", "supheli_b_gidis")
            )
        ),
        "supheli_a_gidis" to DiyalogDurumu(
            metin = "Işık Aslan: Ben suçsuzum. Kanıtlarınız ne diyor?",
            secenekler = listOf(
                DiyalogSecenegi("Kanıtı göster.", "kanit_goster"),
                DiyalogSecenegi("Geri çekil.", "karsilastirma")
            )
        ),
        "supheli_b_gidis" to DiyalogDurumu(
            metin = "Yekta Demir: Neden buradayım? Benimle konuşmanızı gerektirecek bir şey yok.",
            secenekler = listOf(
                DiyalogSecenegi(metin = "Tehdit et.", sonrakiDurumId = "tehdit_sonucu"),
                DiyalogSecenegi("Geri çekil.", "karsilastirma")
            )
        ),
        "kanit_goster" to DiyalogDurumu(
            "Işık Aslan şok oldu ve kaçmaya çalıştı. [VAKA SONU]",
            null
        ),
        "karsilastirma" to DiyalogDurumu(
            metin = "Dedektif: Kanıtları karşılaştırmalıyım. [ANA EKRAN]",
            secenekler = null
        ),
        "tehdit_sonucu" to DiyalogDurumu(
            metin = "Yekta Demir gülümsedi; Güzel deneme dedektif. [VAKA SONU]",
            secenekler = null
        )
    )

    val suankiDurum = diyalogHaritasi[mevcutDurumId]

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.olayyeri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (!kanitBulundu) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                        .clickable {
                            kanitBulundu = true

                            Toast.makeText(context, "Kanıt Bulundu!", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_search_category_default),
                        contentDescription = null,
                        tint = Color.Red.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = suankiDurum?.metin ?: "Hata: Durum bulunamadı",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        suankiDurum?.secenekler?.forEach { secenek ->
            Button(
                onClick = { mevcutDurumId = secenek.sonrakiDurumId },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(secenek.metin)
            }
        }

        if (suankiDurum?.secenekler == null) {
            Button(onClick = { mevcutDurumId = "baslangic" }) {
                Text("Başa Dön")
            }
        }
    }
}

data class DiyalogDurumu(val metin: String, val secenekler: List<DiyalogSecenegi>?)
data class DiyalogSecenegi(val metin: String, val sonrakiDurumId: String)
