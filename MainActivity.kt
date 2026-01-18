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

@Database(entities = [PlayerAttributes::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
}

@Entity(tableName = "oyuncuOzellikleri")
data class PlayerAttributes(
    @PrimaryKey val id: Int = 1,

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

data class DiyalogDurumu(val metin: String, val secenekler: List<DiyalogSecenegi>?)
data class DiyalogSecenegi(val metin: String, val sonrakiDurumId: String, val gerekenNitelik: String? = null, val gerekenPuan: Int = 0, val kazanilanPuanTuru: String? = null, val kazanilanPuanMiktari: Int = 0)
data class CarouselItem(val id: String, val baslik: String, val aciklama: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "oyun-veritabani"
        )
            .fallbackToDestructiveMigration()
            .build()

        val dao = db.playerDao()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    OyunEkrani(dao)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OyunEkrani(dao: PlayerDao){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val oyuncuVerisiState = dao.getPlayerStats().collectAsState(initial = null)
    val oyuncuOzellikleri = oyuncuVerisiState.value ?: PlayerAttributes()

    LaunchedEffect(Unit) {
        dao.insert(PlayerAttributes())
    }

    val mevcutDurumId = oyuncuOzellikleri.kalinanSahneId
    val kanitBulundu = oyuncuOzellikleri.kanitBulundu

    val carouselSecenekleri = listOf(
        CarouselItem("sorgula", "Şüphelileri Sorgula", "Karakoldaki şüphelilerle konuşarak ipucu topla."),
        CarouselItem("incele", "Olay Yerini İncele", "Yerebatan Sarnıcı'ndaki gizli kanıtları ara.")
    )
    val pagerState = rememberPagerState(pageCount = {carouselSecenekleri.size})

    val diyalogHaritasi = mapOf(
        "baslangic" to DiyalogDurumu("İstanbul Emniyet Müdürlüğüne hoş geldin. İlk adımın ne olacak?", null),
        "sorgu_ekrani" to DiyalogDurumu("Sorgu Odası: Karşında Işık Aslan, Yekta Demir ve Duru Arısoy var.", listOf(
            DiyalogSecenegi("Işık Aslan ile konuş.", "isik_konusma"),
            DiyalogSecenegi("Yekta Demir ile konuş.", "yekta_konusma"),
            DiyalogSecenegi("Duru Arısoy ile konuş.", "duru_konusma")
        )),
        "isik_konusma" to DiyalogDurumu("Işık Aslan: Neden buradayım?", listOf(
            DiyalogSecenegi("2 Kasım günü neredeydiniz?","isikAslanSoru1"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "isikAslanSoru2"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        )),
        "isikAslanSoru1" to DiyalogDurumu("Işık Aslan: Pazar günleri resim dersi verdiğimi biliyorsun, kurstaydım.", secenekler = listOf(
            DiyalogSecenegi("Kurstan ne zaman çıktınız?", "isikAslanSoru4"),
            DiyalogSecenegi("Kurstan sonra nereye gittiniz?", "isikAslanSoru5"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        )),
        "isikAslanSoru2" to DiyalogDurumu("Işık Aslan: NE?! Kim yapmış?", secenekler = listOf(
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "isikAslanSoru1"),
            DiyalogSecenegi("Henüz bilmiyoruz.", "isikAslanSoru6"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        )),
        "isikAslanSoru3" to DiyalogDurumu("Işık Aslan: Tabi ki gittim, hemde birden fazla kez.", secenekler = listOf(
            DiyalogSecenegi("En son ne zaman gittiniz?", "isikAslanSoru7"),
            DiyalogSecenegi("En son kiminle gittiniz?", "isikAslanSoru8")
        )),
        "isikAslanSoru4" to DiyalogDurumu("Işık Aslan: 16.00'da dersim bitti herkes çıktıktan sonra bende çıktım.", secenekler = listOf(
            DiyalogSecenegi("Tanığınız var mı?", "isikAslanSoru9"),
            DiyalogSecenegi("Kurstan sonra nereye gittiniz?", "isikAslanSoru5"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        )),
        "isikAslanSoru5" to DiyalogDurumu("Işık Aslan: Dövme stüdyoma gittim.", secenekler = listOf(
            DiyalogSecenegi("Görgü tanığınız var mı?", "isikAslanSoru9"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        )),
        "isikAslanSoru6" to DiyalogDurumu("Işık Aslan: Benden mi şüpheleniyorsunuz?", secenekler = listOf(
            DiyalogSecenegi("Sizi Petridon Koral ile tartışırken görenler var.", "isikAslanSoru10"),
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "isikAslanSoru1"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "isikAslanSoru3")
        )),
        "isikAslanSoru7" to DiyalogDurumu("Işık Aslan: Yazın. Sanırım 19 Ağustosta.", secenekler = listOf(
            DiyalogSecenegi("Tanığınız var mı?", "isikAslanSoru9"),
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        )),
        "isikAslanSoru8" to DiyalogDurumu("Işık Aslan: Kız kardeşim ile gittim.", secenekler = listOf(
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        )),
        "isikAslanSoru9" to DiyalogDurumu("Işık Aslan: Kız kardeşim tanığım.", secenekler = listOf(
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        )),
        "isikAslanSoru10" to DiyalogDurumu("Işık Aslan: Büyük bir tartışma değildi.", secenekler = listOf(
            DiyalogSecenegi("Şahitler tersini söylüyor.", "isikAslanSoru11"),
            DiyalogSecenegi("Tartışma ne ile ilgiliydi?", "isikAslanSoru12"),
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "isikAslanSoru13")
        )),
        "isikAslanSoru11" to DiyalogDurumu("Işık Aslan: Tamam. Evet tartıştık.", secenekler = listOf(
            DiyalogSecenegi("Tartışma ne ile ilgiliydi?", "isikAslanSoru12"),
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "isikAslanSoru13")
        )),
        "isikAslanSoru12" to DiyalogDurumu("Işık Aslan: Aylar önce zor durumda olduğunu söyleyip benden para istedi. Geçen perşembe günü parayı geri istedim ve o parayı geri vermek istemedi bu yüzden tartıştık.", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "isikAslanSoru13")
        )),
        "isikAslanSoru13" to DiyalogDurumu("Işık Aslan: Hayır onu böyle bir sebepten öldürmezdim.", secenekler = listOf(
            DiyalogSecenegi("Neden bu sebepten öldürmezdin?", "isikAslanSoru14")
        )),
        "isikAslanSoru14" to DiyalogDurumu("Işık Aslan: Çünkü onu öldürürsem paramı geri alamazdım.", secenekler = listOf(
            DiyalogSecenegi("Işık Aslan'ı Tutukla!", "isikAslanYanlisSon"),
            DiyalogSecenegi("Diğer Şüphelileri Sorgula", "baslangic")
        )),
        "isikAslanYanlisSon" to DiyalogDurumu("Işık Aslan Suçlu Değildi Yanlış Kişiyi Tutukladın.", secenekler = listOf(
            DiyalogSecenegi("Yeniden Dene", "sorgu_ekrani")
        )),

        "yekta_konusma" to DiyalogDurumu("Yekta Demir: Neden buradayım?", secenekler = listOf(
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "yektaDemirSoru1"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "yektaDemirSoru2"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "yektaDemirSoru3")
        )),
        "yektaDemirSoru1" to DiyalogDurumu("Yekta Demir: Kitap-Kafemdeydim.", secenekler = listOf(
            DiyalogSecenegi("Tanığınız var mı?", "yektaDemirSoru4"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "yektaDemirSoru3"),
            DiyalogSecenegi("Petridon Koral ile aranız nasıldı?", "yektaDemirSoru5")
        )),
        "yektaDemirSoru2" to DiyalogDurumu("Yekta Demir: Ne zaman?! Daha dün dükkanımdaydı.", secenekler = listOf(
            DiyalogSecenegi("İkiniz büyük bir tartışma yaşamışsınız, hatta Petridon darp raporu almış.", "yektaDemirSoru6"),
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "yektaDemirSoru3")
        )),
        "yektaDemirSoru3" to DiyalogDurumu("Yekta Demir: Evet.", secenekler = listOf(
            DiyalogSecenegi("Ne zaman gittiniz?", "yektaDemirSoru7"),
            DiyalogSecenegi("Yanınızda kimse var mıydı?", "yektaDemirSoru8")
        )),
        "yektaDemirSoru4" to DiyalogDurumu("Yekta Demir: Güvenlik kamerası görüntülerim var.", secenekler = listOf(
            DiyalogSecenegi("Şimdilik soracaklarım bu kadar, gidebilirsiniz.", "baslangic")
        )),
        "yektaDemirSoru5" to DiyalogDurumu("Yekta Demir: Çok iyi arkadaşlardık biliyorsun.", secenekler = listOf(
            DiyalogSecenegi("Bu yüzden mi Petridonla kavga ettin?", "yektaDemirSoru6"),
            DiyalogSecenegi("Petridon Koral' sen mi öldürdün?", "yektaDemirSoru9")
        )),
        "yektaDemirSoru6" to DiyalogDurumu("Yekta Demir: Evet, Petridonla kavga ettim. Yine olsa yine yapardım.", secenekler = listOf(
            DiyalogSecenegi("Neden kavga ettiniz?", "yektaDemirSoru10"),
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "yektaDemirSoru9")
        )),
        "yektaDemirSoru7" to DiyalogDurumu("Yekta Demir: Bu sabah. Neden?", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral Yerebatan Sarnıcında öldürüldü.", "yektaDemirSoru8"),
            DiyalogSecenegi("Yanınızda kimse var mıydı?", "yektaDemirSoru8"),
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon"),
        )),
        "yektaDemirSoru8" to DiyalogDurumu("Yekta Demir: Yerebatan Sarnıcına Duru'yu bıraktım. Oraya o yüzden gittim.", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "yektaDemirSoru9"),
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon"),
            DiyalogSecenegi("Duru Arısoy'u Sorgula", "duru_konusma")
        )),
        "yektaDemirSoru9" to DiyalogDurumu("Yekta Demir: Hayır.", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon"),
        )),
        "yektaDemirSoru10" to DiyalogDurumu("Yekta Demir: Kafemdeki kadın müşterileri rahatsız ediyordu.", secenekler = listOf(
            DiyalogSecenegi("Petridon Koral'ı sen mi öldürdün?", "yektaDemirSoru9"),
            DiyalogSecenegi("Yekta Demir'i Tutukla!", "yektaDemirYanlisSon")
        )),
        "yektaDemirYanlisSon" to DiyalogDurumu("Yekta Demir Suçlu Değildi Yanlış Kişiyi Tutukladın", secenekler = listOf(
            DiyalogSecenegi("Yeniden Dene", "sorgu_ekrani")
        )),

        "duru_konusma" to DiyalogDurumu("Duru Arısoy: Beni neden çağırdınız?", secenekler = listOf(
            DiyalogSecenegi("Hiç Yerebatan Sarnıcına gittin mi?", "duruArisoySoru1"),
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "duruArisoySoru2"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "duruArisoySoru3")
        )),
        "duruArisoySoru1" to DiyalogDurumu("Duru Arısoy: Hayır. Hiç Yerebatan Sarnıcına gitmedim.", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir tam tersini söylüyor.", "duruArisoySoru4"),
            DiyalogSecenegi("2 Kasım günü neredeydiniz?", "duruArisoySoru2"),
            DiyalogSecenegi("Petridon Koral öldürüldü.", "duruArisoySoru3")
        )),
        "duruArisoySoru2" to DiyalogDurumu("Duru Arısoy: Evdeydim. Ders çalışıyordum.", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir tam tersini söylüyor.", "duruArisoySoru4"),
            DiyalogSecenegi("Duru Arısoy'u Tutukla!", "vaka_son")
        )),
        "duruArisoySoru3" to DiyalogDurumu("Duru Arısoy: NE???", secenekler = listOf(
            DiyalogSecenegi("Yekta Demir seni olay mahaline götürmüş.", "duruArisoySoru4")
        )),
        "duruArisoySoru4" to DiyalogDurumu("Duru Arısoy: Yekta mı?", secenekler = listOf(
            DiyalogSecenegi("Evet. 2 Kasım sabahı seni Yerebatan Sarnıcına götürdüğünü söyledi.", "duruArisoySoru5"),
            DiyalogSecenegi("Ne oldu Duru?", "duruArisoySoru6")
        )),
        "duruArisoySoru5" to DiyalogDurumu("Duru Arısoy: Doğruyu söylüyor...", secenekler = listOf(
            DiyalogSecenegi("Ne oldu Duru?", "duruArisoySoru6"),
            DiyalogSecenegi("Duru Arısoy'u Tutukla!", "vaka_son")
        )),
        "duruArisoySoru6" to DiyalogDurumu("Duru Arısoy: Petridon beni uzun süredir rahatsız ediyordu. Defalarca durmasını istedim ama dinlemedi.\n" +
                "Petridon'un hareketlerini Denizlerin Tanrısı Poseidon'un Medusa'ya yaptıklarına benzettiğim\n" +
                "için asıl cezalandırılması gereken kişinin Medusa gibi kurbanlar olmasındansa Poseidon gibi\n" +
                "pislikler olduğunu insanlara anlatabilmek için Petridon'u Yerebatan Sarnıcı'nda, Medusa'nın\n" +
                "öldürüldüğü gibi boynunu, Medusa Başı Heykeli'nin önünde kestim.", secenekler = listOf(
            DiyalogSecenegi("Duru Arısoy'u Tutukla!", "vaka_son"),
            DiyalogSecenegi("Duru'yu Kurtar", "baslangic")
        )),
        "vaka_son" to DiyalogDurumu("Dosya Kapandı. Suçlu yakalandı... Ama gerçek adalet bu mu?", null)
    )

    val suankiDurum = diyalogHaritasi[mevcutDurumId] ?: diyalogHaritasi["baslangic"]!!

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("DAVA DOSYASI", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))

                    Text("Şüpheliler", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    val supheliler = listOf("İlhan Dönmez", "Selin Solmaz", "Duru Arısoy", "Petridon Koral", "Celal Baltacı", "Işık Aslan", "Yekta Demir")
                    supheliler.forEach{ isim ->
                        NavigationDrawerItem(
                            label = {Text(isim)},
                            selected = false,
                            icon = {Icon(Icons.Default.Person, contentDescription = null)},
                            onClick = { Toast.makeText(context, isim, Toast.LENGTH_SHORT).show()}
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text("Oyuncu Durumu (Kalıcı Hafıza)", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text("Kayıtlı Sahne: ${oyuncuOzellikleri.kalinanSahneId}")
                    Text("Kanıt Bulundu Mu: ${if(oyuncuOzellikleri.kanitBulundu) "Evet" else "Hayır"}")
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()

                    Text("Cinayet Mahalleri", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    NavigationDrawerItem(
                        label = {Text("Yerebatan Sarnıcı")},
                        selected = false,
                        icon = {Icon(Icons.Default.Place, contentDescription = null)},
                        onClick = { Toast.makeText(context, "Yerebatan Sarnıcı", Toast.LENGTH_SHORT).show()}
                    )

                    Spacer(Modifier.height(40.dp))
                    NavigationDrawerItem(
                        label = {Text("Ayarlar")},
                        selected = false,
                        icon = {Icon(Icons.Default.Settings, contentDescription = null)},
                        onClick = {}
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {Text("DOSYA NO: 34", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)},
                    navigationIcon = {
                        IconButton(onClick = {scope.launch { drawerState.open() }}) {
                            Icon(Icons.Default.Menu, "Menü")
                        }
                    }
                )
            }
        ) {padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF121212)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp).clip(RoundedCornerShape(16.dp))){
                    Image(
                        painter = painterResource(id = com.example.ilkuygulamam.R.drawable.olayyeri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (!kanitBulundu){
                        IconButton(
                            onClick = {
                                scope.launch {
                                    dao.update(oyuncuOzellikleri.copy(kanitBulundu = true))
                                    Toast.makeText(context, "Kanıt Kalıcı Olarak Kaydedildi!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.Center).size(60.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(40.dp))
                        }
                    }
                }

                Text(
                    text = suankiDurum.metin,
                    color = Color.White,
                    modifier = Modifier.padding(24.dp),
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )

                if (mevcutDurumId == "baslangic"){
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentPadding = PaddingValues(horizontal = 48.dp),
                        pageSpacing = 16.dp
                    ) { sayfa ->
                        val item = carouselSecenekleri[sayfa]
                        Card(
                            onClick = {
                                if (item.id == "sorgula") {
                                    scope.launch {
                                        dao.update(oyuncuOzellikleri.copy(kalinanSahneId = "sorgu_ekrani"))
                                    }
                                }
                                else Toast.makeText(context, "İnceleme Modu Aktif!", Toast.LENGTH_SHORT).show()
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
                            border = BorderStroke(1.dp, Color.Cyan)
                        ){
                            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                                Text(item.baslik, fontWeight = FontWeight.Bold, color = Color.Cyan)
                                Text(item.aciklama, fontSize = 12.sp, color = Color.LightGray)
                            }
                        }
                    }
                } else{
                    if (suankiDurum.secenekler != null) {
                        SecenekListesi(
                            secenekler = suankiDurum.secenekler,
                            oyuncuOzellikleri = oyuncuOzellikleri,
                            onSecimYapildi = { secilen ->
                                var yeniHal = oyuncuOzellikleri.copy(kalinanSahneId = secilen.sonrakiDurumId)

                                if (secilen.kazanilanPuanTuru != null) {
                                    yeniHal = when(secilen.kazanilanPuanTuru) {
                                        "iliski_duru" -> yeniHal.copy(iliskiPuaniDuruArisoy = yeniHal.iliskiPuaniDuruArisoy + secilen.kazanilanPuanMiktari)
                                        "rutbe" -> yeniHal.copy(rutbePuani = yeniHal.rutbePuani + secilen.kazanilanPuanMiktari)
                                        else -> yeniHal
                                    }
                                }

                                scope.launch {
                                    dao.update(yeniHal)
                                }
                            }
                        )
                    } else {
                        TextButton(onClick = {
                            scope.launch {
                                val sifirlanmis = PlayerAttributes(id = 1, kalinanSahneId = "baslangic", kanitBulundu = false)
                                dao.insert(sifirlanmis)
                            }
                        }) {
                            Text("Yeniden Başla (Sıfırla)", color = Color.Cyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecenekListesi(
    secenekler: List<DiyalogSecenegi>,
    oyuncuOzellikleri: PlayerAttributes,
    onSecimYapildi: (DiyalogSecenegi) -> Unit
) {
    Column {
        secenekler.forEach { secenek ->
            val yeterliMi = when (secenek.gerekenNitelik) {
                "Zeka" -> oyuncuOzellikleri.zeka >= secenek.gerekenPuan
                "Sezgi" -> oyuncuOzellikleri.sezgi >= secenek.gerekenPuan
                "Cesaret" -> oyuncuOzellikleri.cesaret >= secenek.gerekenPuan
                else -> true
            }

            Button(
                onClick = { onSecimYapildi(secenek) },
                enabled = yeterliMi,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (yeterliMi) Color.DarkGray else Color.Red.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = secenek.metin)

                    if (!yeterliMi) {
                        Text(
                            text = "[Gereken: ${secenek.gerekenNitelik} ${secenek.gerekenPuan}]",
                            fontSize = 10.sp,
                            color = Color(0xFFFFCC00)
                        )
                    }
                }
            }
        }
    }
}
