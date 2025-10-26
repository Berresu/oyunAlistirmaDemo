package com.example.lkuygulamam

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val OYUN_SONU_IDLERI = setOf(
        "sonuc_iyi",
        "sonuc_kotu",
        "tehdit_sonucu",
        "karsilastirma"
    )

    private val PREFS_NAME = "KapiciHikayesiPrefs"
    private val KEY_KANIT_BULUNDU = "KanitBulundu_"
    private val KEY_DIYALOG_ID = "DiyalogDurumId"

    private var mevcutDurumId: String = "baslangic"

    private lateinit var diyaloqKutusu: TextView
    private lateinit var secenek1Button: Button
    private lateinit var secenek2Button: Button

    private val diyalogHaritasi = mapOf(
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
                DiyalogSecenegi("Tehdit et.", "tehdit_sonucu"),
                DiyalogSecenegi("Geri çekil.", "karsilastirma")
            )
        ),
        "kanit_goster" to DiyalogDurumu(
            metin = "Işık Aslan şok oldu ve kaçmaya çalıştı. [VAKA SONU]",
            secenekler = null
        ),
        "karsilastirma" to DiyalogDurumu(
            metin = "Dedektif: Kanıtları karşılaştırmalıyım. [ANA EKRAN]",
            secenekler = null
        ),
        "tehdit_sonucu" to DiyalogDurumu(
            metin = "Yekta Demir gülümsedi: Güzel deneme, dedektif. [VAKA SONU]",
            secenekler = null
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val kanitAnahtar = findViewById<ImageView>(R.id.imageViewArkaPlan)
        diyaloqKutusu = findViewById(R.id.textViewDiyalog)
        secenek1Button = findViewById(R.id.buttonSecenek1)
        secenek2Button = findViewById(R.id.buttonSecenek2)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        mevcutDurumId = prefs.getString(KEY_DIYALOG_ID, "baslangic") ?: "baslangic"

        kanitAnahtar.setOnClickListener {
            kanitBulundu(kanitAnahtar, "Anahtar")
        }

        secenek1Button.setOnClickListener {
            butonaTiklandi(secenek1Button.tag as String)
        }

        secenek2Button.setOnClickListener {
            butonaTiklandi(secenek2Button.tag as String)
        }

        yukleKanitDurumu(kanitAnahtar)
        yukleDiyalogDurumu(mevcutDurumId)
    }

    private fun kanitBulundu(kanitView: ImageView, kanitAdi: String) {
        kanitView.visibility = View.INVISIBLE
        Toast.makeText(this, "$kanitAdi bulundu!", Toast.LENGTH_SHORT).show()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            val kanitIdAdı = kanitView.resources.getResourceEntryName(kanitView.id)
            putBoolean(KEY_KANIT_BULUNDU + kanitIdAdı, true)
        }
    }

    private fun butonaTiklandi(sonrakiDurumId: String) {
        mevcutDurumId = sonrakiDurumId

        val kaydedilecekId = if (OYUN_SONU_IDLERI.contains(mevcutDurumId)) {
            "baslangic"
        } else {
            mevcutDurumId
        }

        kaydetDiyalogDurumu(kaydedilecekId)
        yukleDiyalogDurumu(mevcutDurumId)
    }

    private fun yukleDiyalogDurumu(durumId: String) {
        val durum = diyalogHaritasi[durumId] ?: return
        diyaloqKutusu.text = durum.metin

        if (durum.secenekler != null && durum.secenekler.isNotEmpty()) {
            secenek1Button.visibility = View.VISIBLE
            secenek2Button.visibility = View.VISIBLE

            val secenek1 = durum.secenekler[0]
            secenek1Button.text = secenek1.metin
            secenek1Button.tag = secenek1.sonrakiDurumId

            if (durum.secenekler.size > 1) {
                val secenek2 = durum.secenekler[1]
                secenek2Button.text = secenek2.metin
                secenek2Button.tag = secenek2.sonrakiDurumId
            } else {
                secenek2Button.visibility = View.GONE
            }
        } else {
            secenek1Button.visibility = View.GONE
            secenek2Button.visibility = View.GONE
        }
    }

    private fun yukleKanitDurumu(kanitView: ImageView) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val kanitAdi = kanitView.resources.getResourceEntryName(kanitView.id)
        val bulundu = prefs.getBoolean(KEY_KANIT_BULUNDU + kanitAdi, false)

        if (bulundu) {
            kanitView.visibility = View.INVISIBLE
        }
    }

    private fun kaydetDiyalogDurumu(durumId: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DIYALOG_ID, durumId)
            .commit()
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
