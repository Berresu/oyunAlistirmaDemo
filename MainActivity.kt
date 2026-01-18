package com.example.ilkuygulamam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Dao
interface PlayerDao {
    @Query("SELECT * FROM oyuncuOzellikleri WHERE id = 1")
    fun getPlayerStats(): Flow<PlayerAttributes?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(attributes: PlayerAttributes)

    @Update
    suspend fun update(attributes: PlayerAttributes)
}

@Database(entities = [PlayerAttributes::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
}

@Entity(tableName = "oyuncuOzellikleri")
data class PlayerAttributes(
    @PrimaryKey val id: Int = 1,
    val kullaniciAdi: String? = null,
    val kalinanSahneId: String = "baslangic",
    val kanitBulundu: Boolean = false,
    val zeka: Int = 0,
    val sezgi: Int = 0,
    val guc: Int = 0,
    val cesaret: Int = 0,
    val empati: Int = 0,
    val manipulasyon: Int = 0,
    val rutbePuani: Int = 100,
    val iliskiPuaniIsikAslan: Int = 50,
    val iliskiPuaniYektaDemir: Int = 50,
    val iliskiPuaniDuruArisoy: Int = 50
)

data class DiyalogDurumu(val metin: String, val secenekler: List<DiyalogSecenegi>?, val resimId: Int = R.drawable.yerebatansarnici)
data class DiyalogSecenegi(val metin: String, val sonrakiDurumId: String, val gerekenNitelik: String? = null, val gerekenPuan: Int = 0, val kazanilanPuanTuru: String? = null, val kazanilanPuanMiktari: Int = 0)
data class CarouselItem(val id: String, val baslik: String, val aciklama: String)
data class KarakterBilgisi(val id: String, val adSoyad: String, val rol: String, val hikaye: String, val resimId: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "oyun-veritabani"
        ).fallbackToDestructiveMigration().build()

        val dao = db.playerDao()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AnaUygulamaYoneticisi(dao)
                }
            }
        }
    }
}

@Composable
fun AnaUygulamaYoneticisi(dao: PlayerDao) {
    val oyuncuVerisiState = dao.getPlayerStats().collectAsState(initial = null)
    val oyuncuVerisi = oyuncuVerisiState.value

    LaunchedEffect(Unit) { dao.insert(PlayerAttributes(id=1)) }

    if(oyuncuVerisi == null) return

    if(oyuncuVerisi.kullaniciAdi.isNullOrEmpty()){
        KayitEkrani(dao=dao)
    }
    else if(oyuncuVerisi.zeka == 0 && oyuncuVerisi.sezgi == 0 && oyuncuVerisi.guc == 0 && oyuncuVerisi.cesaret == 0 && oyuncuVerisi.empati == 0 && oyuncuVerisi.manipulasyon == 0){
        NitelikSecimEkrani(dao=dao, mevcutVeri = oyuncuVerisi)
    }
    else{
        OyunEkrani(dao, oyuncuVerisi)
    }
}

@Composable
fun KayitEkrani(dao: PlayerDao) {
    val scope = rememberCoroutineScope()
    var isim by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("POLİS SİSTEMİNE KAYIT", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = isim, onValueChange = { isim = it }, label = { Text("Komiser Adı Soyadı") }, singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Cyan, unfocusedBorderColor = Color.Gray, focusedLabelColor = Color.Cyan, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { if (isim.isNotBlank()) { scope.launch { dao.update(PlayerAttributes(id = 1, kullaniciAdi = isim)) } } },
            modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
        ) {
            Text(text = "DEVAM ET", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NitelikSecimEkrani(dao: PlayerDao, mevcutVeri: PlayerAttributes){
    val scope = rememberCoroutineScope()
    val secilenler = remember { mutableStateListOf<String>() }
    val nitelikListesi = listOf("Zeka", "Sezgi", "Güç", "Cesaret", "Empati", "Manipülasyon")

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))
        Text("UZMANLIK ALANI SEÇ", style = MaterialTheme.typography.headlineSmall, color = Color.Cyan, fontWeight = FontWeight.Bold)
        Text("En fazla 3 adet seçebilirsiniz", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))

        nitelikListesi.forEach { nitelik ->
            val seciliMi = secilenler.contains(nitelik)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable{
                    if(seciliMi) secilenler.remove(nitelik) else if(secilenler.size < 3) secilenler.add(nitelik)
                },
                colors = CardDefaults.cardColors(containerColor = if(seciliMi) Color.Cyan.copy(alpha = 0.2f) else Color.DarkGray),
                border = BorderStroke(1.dp, if(seciliMi) Color.Cyan else Color.DarkGray)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically){
                    Checkbox(checked = seciliMi, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Color.Cyan))
                    Spacer(Modifier.width(10.dp))
                    Text(nitelik, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Kalan Hak: ${3 - secilenler.size}", color = if(secilenler.size == 3) Color.Green else Color.Red)
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val yeniVeri = mevcutVeri.copy(
                    zeka = if (secilenler.contains("Zeka")) 10 else 0,
                    sezgi = if (secilenler.contains("Sezgi")) 10 else 0,
                    guc = if (secilenler.contains("Güç")) 10 else 0,
                    cesaret = if (secilenler.contains("Cesaret")) 10 else 0,
                    empati = if (secilenler.contains("Empati")) 10 else 0,
                    manipulasyon = if (secilenler.contains("Manipülasyon")) 10 else 0
                )
                scope.launch { dao.update(yeniVeri) }
            },
            enabled = secilenler.size == 3,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, disabledContainerColor = Color.DarkGray)
        ) {
            Text("ROZETİ AL VE BAŞLA", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OyunEkrani(dao: PlayerDao, oyuncuOzellikleri: PlayerAttributes){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val mevcutDurumId = oyuncuOzellikleri.kalinanSahneId
    val kanitBulundu = oyuncuOzellikleri.kanitBulundu
    var seciliKarakter by remember { mutableStateOf<KarakterBilgisi?>(null) }

    val karakterListesi = listOf(
        KarakterBilgisi(
            "isikAslan", "Işık Aslan", "Resim Öğretmeni ve Dövme Sanatçısı",
            "${oyuncuOzellikleri.kullaniciAdi} ile resim dersinde tanışıp arkadaş oldular.",
            R.drawable.resimsinifi
        ),
        KarakterBilgisi(
            "yektaDemir", "Yekta Demir", "Kitap-Kafe sahibi.",
            "${oyuncuOzellikleri.kullaniciAdi} ile kafesinde tanışıp arkadaş oldular.",
            R.drawable.kitapkafe
        ),
        KarakterBilgisi(
            "petridonKoral", "Petridon Koral", "Kütüphane Görevlisi",
            "${oyuncuOzellikleri.kullaniciAdi} ile çocukluk arkadaşılar.",
            R.drawable.petridonkutuphane
        ),
        KarakterBilgisi(
            "duruArisoy", "Duru Arısoy", "Psikoloji yüksek lisans öğrencisi.",
            "${oyuncuOzellikleri.kullaniciAdi} ile Yekta Demir'in düzenlediği kitap klübü etkinliğinde tanıştılar.",
            R.drawable.psychology
        )
    )


    val carouselSecenekleri = listOf(
        CarouselItem("sorgula", "Şüphelileri Sorgula", "Karakoldaki şüphelilerle konuşarak ipucu topla."),
        CarouselItem("incele", "Olay Yerini İncele", "Yerebatan Sarnıcı'ndaki bilet ve kanıtları incele.")
    )
    val pagerState = rememberPagerState(pageCount = {carouselSecenekleri.size})

    val diyalogHaritasi = mapOf(
        "baslangic" to DiyalogDurumu("İstanbul Emniyet Müdürlüğüne hoş geldin Komiser ${oyuncuOzellikleri.kullaniciAdi}. İlk adımın ne olacak?", null),
        "sorgu_ekrani" to DiyalogDurumu("Sorgu Odası: Karşında Işık Aslan, Yekta Demir ve Duru Arısoy var.", listOf(
            DiyalogSecenegi("Işık Aslan ile konuş.", "isik_konusma"),
            DiyalogSecenegi("Yekta Demir ile konuş.", "yekta_konusma"),
            DiyalogSecenegi("Duru Arısoy ile konuş.", "duru_konusma")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isik_konusma" to DiyalogDurumu("Işık Aslan: Neden buradayım?", listOf(
            DiyalogSecenegi("2 Kasım günü neredeydiniz?","isikAslanSoru1"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "isikAslanSoru2"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru1" to DiyalogDurumu("Işık Aslan: Pazar günleri resim dersi verdiğimi biliyorsun, kurstaydım.", secenekler = listOf(
            DiyalogSecenegi("Kurstan ne zaman çıktınız?", "isikAslanSoru4"),
            DiyalogSecenegi("Kurstan sonra nereye gittiniz?", "isikAslanSoru5"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru2" to DiyalogDurumu("Işık Aslan: NE?! Kim yapmış?", secenekler = listOf(
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "isikAslanSoru1"),
            DiyalogSecenegi("Henüz bilmiyoruz.", "isikAslanSoru6"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru3" to DiyalogDurumu("Işık Aslan: Tabi ki gittim, hemde birden fazla kez.", secenekler = listOf(
            DiyalogSecenegi("En son ne zaman gittiniz?", "isikAslanSoru7"),
            DiyalogSecenegi("En son kiminle gittiniz?", "isikAslanSoru8")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru4" to DiyalogDurumu("Işık Aslan: 16.00'da dersim bitti herkes çıktıktan sonra bende çıktım.", secenekler = listOf(
            DiyalogSecenegi("Tanığınız var mı?", "isikAslanSoru9"),
            DiyalogSecenegi("Kurstan sonra nereye gittiniz?", "isikAslanSoru5"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru5" to DiyalogDurumu("Işık Aslan: Dövme stüdyoma gittim.", secenekler = listOf(
            DiyalogSecenegi("Görgü tanığınız var mı?", "isikAslanSoru9"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru6" to DiyalogDurumu("Işık Aslan: Benden mi şüpheleniyorsunuz?", secenekler = listOf(
            DiyalogSecenegi("Sizi Petridon Koral ile tartışırken görenler var.", "isikAslanSoru10"),
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "isikAslanSoru1"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru7" to DiyalogDurumu("Işık Aslan: Yazın. Sanırım 19 Ağustosta.", secenekler = listOf(
            DiyalogSecenegi("Tanığınız var mı?", "isikAslanSoru9"),
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru8" to DiyalogDurumu("Işık Aslan: Kız kardeşim ile gittim.", secenekler = listOf(
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru9" to DiyalogDurumu("Işık Aslan: Kız kardeşim tanığım.", secenekler = listOf(
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru10" to DiyalogDurumu("Işık Aslan: Büyük bir tartışma değildi.", secenekler = listOf(
            DiyalogSecenegi("Şahitler tersini söylüyor.", "isikAslanSoru11"),
            DiyalogSecenegi("Tartışma ne ile ilgiliydi?", "isikAslanSoru12"),
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "isikAslanSoru13")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru11" to DiyalogDurumu("Işık Aslan: Tamam. Evet tartıştık.", secenekler = listOf(
            DiyalogSecenegi("Tartışma ne ile ilgiliydi?", "isikAslanSoru12"),
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "isikAslanSoru13")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru12" to DiyalogDurumu("Işık Aslan: Aylar önce zor durumda olduğunu söyleyip benden para istedi. Geçen perşembe günü parayı geri istedim ve o parayı geri vermek istemedi bu yüzden tartıştık.", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "isikAslanSoru13")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru13" to DiyalogDurumu("Işık Aslan: Hayır onu böyle bir sebepten öldürmezdim.", secenekler = listOf(
            DiyalogSecenegi("Neden bu sebepten öldürmezdin?", "isikAslanSoru14")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanSoru14" to DiyalogDurumu("Işık Aslan: Çünkü onu öldürürsem paramı geri alamazdım.", secenekler = listOf(
            DiyalogSecenegi("Işık Aslan'ı Tutukla!", "isikAslanYanlisSon"),
            DiyalogSecenegi("Diğer Şüphelileri Sorgula", "baslangic")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "isikAslanYanlisSon" to DiyalogDurumu("Işık Aslan Suçlu Değildi Yanlış Kişiyi Tutukladın.", secenekler = listOf(
            DiyalogSecenegi("Yeniden Dene", "sorgu_ekrani", kazanilanPuanTuru = "rutbe", kazanilanPuanMiktari = -20)
        ),
            resimId = R.drawable.sorguodasi
        ),

        "yekta_konusma" to DiyalogDurumu("Yekta Demir: Neden buradayım?", secenekler = listOf(
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "yektaDemirSoru1"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "yektaDemirSoru2"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "yektaDemirSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru1" to DiyalogDurumu("Yekta Demir: Kitap-Kafemdeydim.", secenekler = listOf(
            DiyalogSecenegi("Tanığınız var mı?", "yektaDemirSoru4"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "yektaDemirSoru3"),
            DiyalogSecenegi("Petridon Koral ile aranız nasıldı?", "yektaDemirSoru5")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru2" to DiyalogDurumu("Yekta Demir: Ne zaman?! Daha dün dükkanımdaydı.", secenekler = listOf(
            DiyalogSecenegi("İkiniz büyük bir tartışma yaşamışsınız, hatta Petridon darp raporu almış.", "yektaDemirSoru6"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "yektaDemirSoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru3" to DiyalogDurumu("Yekta Demir: Evet.", secenekler = listOf(
            DiyalogSecenegi("Ne zaman gittiniz?", "yektaDemirSoru7"),
            DiyalogSecenegi("Yanınızda kimse var mıydı?", "yektaDemirSoru8")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru4" to DiyalogDurumu("Yekta Demir: Güvenlik kamerası görüntülerim var.", secenekler = listOf(
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru5" to DiyalogDurumu("Yekta Demir: Çok iyi arkadaşlardık biliyorsun.", secenekler = listOf(
            DiyalogSecenegi("Bu yüzden mi Petridonla kavga ettin?", "yektaDemirSoru6"),
            DiyalogSecenegi("Petridon Koral' sen mi öldürdün?", "yektaDemirSoru9")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru6" to DiyalogDurumu("Yekta Demir: Evet, Petridonla kavga ettim. Yine olsa yine yapardım.", secenekler = listOf(
            DiyalogSecenegi("Neden kavga ettiniz?", "yektaDemirSoru10"),
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "yektaDemirSoru9")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru7" to DiyalogDurumu("Yekta Demir: Bu sabah. Neden?", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral Yerebatan Sarnıcında öldürüldü.", "yektaDemirSoru8"),
            DiyalogSecenegi("Yanınızda kimse var mıydı?", "yektaDemirSoru8"),
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon"),
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru8" to DiyalogDurumu("Yekta Demir: Yerebatan Sarnıcına Duru'yu bıraktım. Oraya o yüzden gittim.", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "yektaDemirSoru9"),
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon"),
            DiyalogSecenegi("Duru Arısoy'u Sorgula", "duru_konusma")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru9" to DiyalogDurumu("Yekta Demir: Hayır.", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon"),
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirSoru10" to DiyalogDurumu("Yekta Demir: Kafemdeki kadın müşterileri rahatsız ediyordu.", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "yektaDemirSoru9"),
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "yektaDemirYanlisSon" to DiyalogDurumu("Yekta Demir Suçlu Değildi Yanlış Kişiyi Tutukladın", secenekler = listOf(
            DiyalogSecenegi("Yeniden Dene", "sorgu_ekrani", kazanilanPuanTuru = "rutbe", kazanilanPuanMiktari = -20)
        ),
            resimId = R.drawable.sorguodasi
        ),

        "duru_konusma" to DiyalogDurumu("Duru Arısoy: Beni neden çağırdınız?", secenekler = listOf(
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "duruArisoySoru1"),
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "duruArisoySoru2"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "duruArisoySoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "duruArisoySoru1" to DiyalogDurumu("Duru Arısoy: Hayır. Hiç Yerebatan Sarnıcına gitmedim.", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir tam tersini söylüyor.", "duruArisoySoru4"),
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "duruArisoySoru2"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "duruArisoySoru3")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "duruArisoySoru2" to DiyalogDurumu("Duru Arısoy: Evdeydim. Ders çalışıyordum.", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir tam tersini söylüyor.", "duruArisoySoru4"),
            DiyalogSecenegi("Duru Arısoy'u Tutukla!", "vaka_son")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "duruArisoySoru3" to DiyalogDurumu("Duru Arısoy: NE???", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir seni olay mahaline götürmüş.", "duruArisoySoru4")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "duruArisoySoru4" to DiyalogDurumu("Duru Arısoy: Yekta mı?", secenekler = listOf(
            DiyalogSecenegi("Evet. 2 Kasım sabahı seni Yerebatan Sarnıcına götürdüğünü söyledi.", "duruArisoySoru5"),
            DiyalogSecenegi("Ne oldu Duru?", "duruArisoySoru6")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "duruArisoySoru5" to DiyalogDurumu("Duru Arısoy: Doğruyu söylüyor...", secenekler = listOf(
            DiyalogSecenegi("Ne oldu Duru?", "duruArisoySoru6"),
            DiyalogSecenegi("Duru Arısoy'u Tutukla!", "vaka_son")
        ),
            resimId = R.drawable.sorguodasi
        ),
        "duruArisoySoru6" to DiyalogDurumu("Duru Arısoy: Petridon beni uzun süredir rahatsız ediyordu. Defalarca durmasını istedim ama dinlemedi.\n" +
                "Petridon'un hareketlerini Denizlerin Tanrısı Poseidon'un Medusa'ya yaptıklarına benzettiğim\n" +
                "için asıl cezalandırılması gereken kişinin Medusa gibi kurbanlar olmasındansa Poseidon gibi\n" +
                "pislikler olduğunu insanlara anlatabilmek için Petridon'u Yerebatan Sarnıcı'nda, Medusa'nın\n" +
                "öldürüldüğü gibi boynunu, Medusa Başı Heykeli'nin önünde kestim.", secenekler = listOf(
            DiyalogSecenegi(
                "Duru Arısoy'u Tutukla!",
                "vaka_son",
                kazanilanPuanTuru = "rutbe",
                kazanilanPuanMiktari = 20
            ),
            DiyalogSecenegi(
                "Duru'yu Kurtar",
                "duruArisoyGizliSon",
                kazanilanPuanTuru = "rutbe",
                kazanilanPuanMiktari = -100
            )
        ),
            resimId = R.drawable.sorguodasi
        ),
        "vaka_son" to DiyalogDurumu("Dosya Kapandı. Suçlu yakalandı... Ama gerçek adalet bu mu?", null),
        "duruArisoyGizliSon" to DiyalogDurumu(
            "Duru'yu arka kapıdan çıkardın ve olay yerindeki delilleri kararttın. Ama... vicdanın rahat değil.\n\n"+
                    "Ertesi sabah Emniyet Müdürü seni odasına çağırdı. Delil yetersizliği ve ihmalkarlık sebebiyle hakkında soruşturma açıldı.\n\n"+
                    "RÜTBEN SÖKÜLDÜ. Artık bir polis değilsin...",
            null
        )
    )


    val suankiDurum = diyalogHaritasi[mevcutDurumId] ?: diyalogHaritasi["baslangic"]!!

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    Text("DAVA DOSYASI", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))
                    Text("Komiser", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text(oyuncuOzellikleri.kullaniciAdi ?: "Bilinmiyor", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Text("Şüpheliler", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    karakterListesi.forEach { karakter ->
                        NavigationDrawerItem(
                            label = { Text(karakter.adSoyad) }, selected = false,
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            onClick = { seciliKarakter = karakter; scope.launch { drawerState.close() } }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Oyuncu Durumu", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rütbe: ${oyuncuOzellikleri.rutbePuani}", fontWeight = FontWeight.Bold)
                    }
                    if(oyuncuOzellikleri.zeka>0) Text("• Zeka (Uzman)", color = Color.Cyan)
                    if(oyuncuOzellikleri.sezgi>0) Text("• Sezgi (Uzman)", color = Color.Cyan)
                    if(oyuncuOzellikleri.cesaret>0) Text("• Cesaret (Uzman)", color = Color.Cyan)

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    NavigationDrawerItem(label = {Text("Oturumu Kapat (Sıfırla)")}, selected = false, icon = {Icon(Icons.AutoMirrored.Filled.ExitToApp, null)}, onClick = { scope.launch { dao.update(PlayerAttributes(id=1, kullaniciAdi = null)) } })
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {Text("DOSYA NO: 34", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)},
                    navigationIcon = { IconButton(onClick = {scope.launch { drawerState.open() }}) { Icon(Icons.Default.Menu, "Menü") } },
                    actions = {
                        Surface(shape = RoundedCornerShape(50), color = Color.DarkGray, modifier = Modifier.padding(end = 12.dp)){
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = if (oyuncuOzellikleri.rutbePuani < 0) Color.Red else Color.Yellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${oyuncuOzellikleri.rutbePuani}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }
        ) {padding ->
            if (seciliKarakter != null) {
                Box(modifier = Modifier.padding(padding)) { KarakterDetayEkrani(karakter = seciliKarakter!!, onGeriDon = { seciliKarakter = null }) }
            }
            else if (mevcutDurumId == "bilet_ekrani") {
                BiletEkrani(
                    onKanitBulundu = {
                        scope.launch {
                            dao.update(oyuncuOzellikleri.copy(kanitBulundu = true))
                            Toast.makeText(context, "KANIT: Duru Arısoy olay günü oradaymış!", Toast.LENGTH_LONG).show()
                        }
                    },
                    onGeriDon = { scope.launch { dao.update(oyuncuOzellikleri.copy(kalinanSahneId = "baslangic")) } }
                )
            }
            else {
                Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFF121212)), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp).clip(RoundedCornerShape(16.dp))){
                        Image(painter = painterResource(id = suankiDurum.resimId), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }

                    Text(text = suankiDurum.metin, color = Color.White, modifier = Modifier.padding(24.dp), fontSize = 18.sp)

                    if (mevcutDurumId == "baslangic"){
                        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(150.dp), contentPadding = PaddingValues(horizontal = 48.dp), pageSpacing = 16.dp) { sayfa ->
                            val item = carouselSecenekleri[sayfa]
                            Card(
                                onClick = {
                                    if (item.id == "sorgula") scope.launch { dao.update(oyuncuOzellikleri.copy(kalinanSahneId = "sorgu_ekrani")) }
                                    else if (item.id == "incele") scope.launch { dao.update(oyuncuOzellikleri.copy(kalinanSahneId = "bilet_ekrani")) }
                                },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)), border = BorderStroke(1.dp, Color.Cyan)
                            ){
                                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) { Text(item.baslik, fontWeight = FontWeight.Bold, color = Color.Cyan); Text(item.aciklama, fontSize = 12.sp, color = Color.LightGray) }
                            }
                        }
                    } else if (suankiDurum.secenekler != null) {
                        SecenekListesi(secenekler = suankiDurum.secenekler, oyuncuOzellikleri = oyuncuOzellikleri, onSecimYapildi = { secilen ->
                            scope.launch {
                                var yeniHal = oyuncuOzellikleri.copy(kalinanSahneId = secilen.sonrakiDurumId)
                                if (secilen.kazanilanPuanTuru != null){
                                    yeniHal = when(secilen.kazanilanPuanTuru){
                                        "iliskiDuru" -> yeniHal.copy(iliskiPuaniDuruArisoy = yeniHal.iliskiPuaniDuruArisoy + secilen.kazanilanPuanMiktari)
                                        "rutbe" -> yeniHal.copy(rutbePuani = yeniHal.rutbePuani + secilen.kazanilanPuanMiktari)
                                        else -> yeniHal
                                    }
                                }
                                dao.update(yeniHal)
                            }
                        })
                    } else {
                        TextButton(onClick = { scope.launch { dao.update(PlayerAttributes(id=1, kullaniciAdi = oyuncuOzellikleri.kullaniciAdi)) } }) { Text("Başa Dön (Oyun Sonu)", color = Color.Cyan) }
                    }
                }
            }
        }
    }
}

@Composable
fun BiletEkrani(onKanitBulundu: () -> Unit, onGeriDon: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.yerebatansarnicibilet), contentDescription = "Kanıt Bileti", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().padding(16.dp))

        Box(modifier = Modifier.offset(x = -60.dp, y = 50.dp).size(width = 90.dp, height = 60.dp).clickable { onKanitBulundu() })

        Text("Biletin üzerindeki şüpheli detaylara dokun...", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp))

        Button(onClick = onGeriDon, modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(8.dp)); Text("İncelemeyi Bitir")
        }
    }
}

@Composable
fun SecenekListesi(secenekler: List<DiyalogSecenegi>, oyuncuOzellikleri: PlayerAttributes, onSecimYapildi: (DiyalogSecenegi) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp)) {
        secenekler.forEach { secenek ->
            val yeterliMi = when (secenek.gerekenNitelik) {
                "Zeka" -> oyuncuOzellikleri.zeka >= secenek.gerekenPuan
                "Sezgi" -> oyuncuOzellikleri.sezgi >= secenek.gerekenPuan
                "Cesaret" -> oyuncuOzellikleri.cesaret >= secenek.gerekenPuan
                "Manipülasyon" -> oyuncuOzellikleri.manipulasyon >= secenek.gerekenPuan
                else -> true
            }
            Button(
                onClick = { onSecimYapildi(secenek) }, enabled = yeterliMi,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (yeterliMi) Color.DarkGray else Color.Red.copy(alpha = 0.3f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = secenek.metin)
                    if (!yeterliMi) Text(text = "[Gereken: ${secenek.gerekenNitelik} Uzmanlığı]", fontSize = 10.sp, color = Color.Yellow)
                }
            }
        }
    }
}

@Composable
fun KarakterDetayEkrani(
    karakter: KarakterBilgisi,
    onGeriDon: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = karakter.resimId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = karakter.adSoyad, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = karakter.rol, style = MaterialTheme.typography.titleMedium, color = Color.Cyan)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "HAKKINDA", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = karakter.hikaye, style = MaterialTheme.typography.bodyLarge, color = Color.White)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGeriDon,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Dosyaya Dön")
            }
        }
    }
}
