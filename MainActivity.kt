package com.example.lkuygulamam

import android.os.Bundle
import android.view.View
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

class MainActivity : ComponentActivity() {
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
        val kanitAnahtar = findViewById<ImageView>(R.id.imageViewResmim)

        kanitAnahtar.setOnClickListener {
            kanitBulundu(kanitAnahtar, "Anahtar")
        }

        diyaloqKutusu = findViewById(R.id.textViewDiyolog)
        diyaloqKutusu.setOnClickListener {
            ilerleDiyalog()
        }
        diyaloqKutusu.text = diyaloglar[0]
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
}
