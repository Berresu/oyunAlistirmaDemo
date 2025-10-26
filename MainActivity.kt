package com.example.lkuygulamam

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lkuygulamam.ui.theme.İlkUygulamamTheme

class MainActivity : AppCompatActivity() {
    private lateinit var diyaloqKutusu: TextView

    private val diyaloglar = arrayOf(
        "Karakol: Kapıcı Hikayesi soruşturmasına hoşgeldin.",
        "Dedektif: Etrafta kanıtlar olmalı, dikkatlice incelemeliyim.",
        "Karakol: İyi şanslar. Bir şey bulursan bana bildir.",
        "Oyun: Şimdi etraftaki kanıtları ara."
    )
    private var mevcutDiyalogIndeksi = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val kanitAnahtar = findViewById<ImageView>(R.id.imageViewArkaPlan)

        kanitAnahtar.setOnClickListener {
            kanitBulundu(kanitAnahtar, "Anahtar")
        }

        diyaloqKutusu = findViewById(R.id.textViewDiyalog)
        diyaloqKutusu.setOnClickListener {
            ilerleDiyalog()
        }
        diyaloqKutusu.text = diyaloglar[0]

        secenek1Button=findViewById(R.id.buttonSecenek1)
        secenek2Button=findViewById(R.id.buttonSecenek2)

        secenek1Button.setOnClickListener {
            butonaTiklandi(secenek1Button.tag as String)
        }

        secenek2Button.setOnClickListener {
            butonaTiklandi(secenek2Button.tag as String)
        }

        yukleDiyalogDurumu(mevcutDurumId)
    }

    private fun kanitBulundu(kanitVeiw: ImageView, kanitAdi: String){
        kanitVeiw.visibility = View.INVISIBLE

        Toast.makeText(this, "$kanitAdi bulundu!", Toast.LENGTH_SHORT).show()
    }

    private fun ilerleDiyalog(){
        mevcutDiyalogIndeksi++
        if (mevcutDiyalogIndeksi<diyaloglar.size){
            diyaloqKutusu.text=diyaloglar[mevcutDiyalogIndeksi]
        }
        else{
            diyaloqKutusu.text="Kanıtları Bulmaya Devam Et..."
            diyaloqKutusu.setOnClickListener(null)
        }
    }

    private var mevcutDurumId: String = "baslangic"

    private lateinit var secenek1Button: Button
    private lateinit var secenek2Button: Button

    private val diyalogHaritasi= mapOf(
        "baslangic" to DiyalogDurumu(
            metin="Karakol: Dedektif, bu vakada iki şüpheli var. Hangisiyle konuşmak istersin?",
            secenekler=listOf(
                DiyalogSecenegi("Işık Aslan ile konuş.", "supheli_a_gidis"),
                DiyalogSecenegi("Yekta Demir ile konuş.", "supheli_b_gidis")
            )
        ),
        "supheli_a_gidis" to DiyalogDurumu(
            metin="Işık Aslan: Ben suçsuzum. Kanıtlarınız ne diyor?",
            secenekler=listOf(
                DiyalogSecenegi("Kanıtı göster.", "kanit_goster"),
                DiyalogSecenegi("Geri çekil.", "karsilastirma")
            )
        ),
        "supheli_b_gidis" to DiyalogDurumu(
            metin="Yekta Demir: Neden buradayım? Benimle konuşmanızı gerektirecek bir şey yok.",
            secenekler=listOf(
                DiyalogSecenegi("Tehdit et.", "tehdit_sonucu"),
                DiyalogSecenegi("Geri çekil.", "karsilastirma")
            )
        ),
        "kanit_goster" to DiyalogDurumu(
            metin="Işık Aslan şok oldu ve kaçmaya çalıştı. [VAKA SONU]",
            secenekler = null
        ),
        "karsilastirma" to DiyalogDurumu(
            metin="Dedektif: Kanıtları karşılaştırmalıyım. [ANA EKRAN]",
            secenekler=null
        ),
        "tehdit_sonucu" to DiyalogDurumu(
            metin="Yekta Demir gülümsedi: Güzel deneme, dedektif. [VAKA SONU]",
            secenekler=null
        )
    )

    private fun butonaTiklandi(sonrakiDurumId: String){
        mevcutDurumId=sonrakiDurumId
        yukleDiyalogDurumu(mevcutDurumId)
    }

    private fun yukleDiyalogDurumu(durumId: String){
        val durum=diyalogHaritasi[durumId] ?: return
        diyaloqKutusu.text=durum.metin

        if (durum.secenekler !=null && durum.secenekler.isNotEmpty()){
            secenek1Button.visibility=View.VISIBLE
            secenek2Button.visibility=View.VISIBLE

            val secenek1=durum.secenekler[0]
            secenek1Button.text=secenek1.metin
            secenek1Button.tag=secenek1.sonrakiDurumId

            if (durum.secenekler.size > 1){
                val secenek2=durum.secenekler[1]
                secenek2Button.text=secenek2.metin
                secenek2Button.tag=secenek2.sonrakiDurumId
            }
            else{
                secenek2Button.visibility= View.GONE
            }
        }
        else{
            secenek1Button.visibility= View.GONE
            secenek2Button.visibility= View.GONE
        }
    }
}

data class DiyalogDurumu(
    val metin: String,
    val secenekler: List<DiyalogSecenegi>? = null
)

data class DiyalogSecenegi(
    val metin: String,
    val sonrakiDurumId: String
)
