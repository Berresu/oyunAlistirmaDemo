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

// Veri Modelleri
data class DiyalogDurumu(val metin: String, val secenekler: List<DiyalogSecenegi>?)
data class DiyalogSecenegi(val metin: String, val sonrakiDurumId: String)
data class CarouselItem(val id: String, val baslik: String, val aciklama: String)

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OyunEkrani(){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var mevcutDurumId by remember { mutableStateOf("baslangic") }
    var kanitBulundu by remember { mutableStateOf(false) }

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
        "isik_konusma" to DiyalogDurumu("Işık Aslan: Petridon'un bana yüklü miktarda borcu vardı bu sebeple biraz tartıştık ama onu bu sebepten öldürmezdim.", listOf(
            DiyalogSecenegi("Neden bu sebepten öldürmezdin?","isik_son"),
            DiyalogSecenegi("Geri Çekil", "baslangic")
        )),
        "isik_son" to DiyalogDurumu("Işık Aslan: Çünkü onu öldürürsem paramı geri alamazdım.", secenekler = listOf(
            DiyalogSecenegi("Diğer Şüphelileri Sorgula", "baslangic")
        )),
        "yekta_konusma" to DiyalogDurumu("Yekta Demir: Evet Petridonla kavga ettim ama onu ben öldürmedim.", secenekler = listOf(
            DiyalogSecenegi("Peki neden kavga ettiniz?", "yekta_son"),
            DiyalogSecenegi("Geri Çekil", "baslangic")
        )),
        "yekta_son" to DiyalogDurumu("Yekta Demir: Kitap-Kafemdeki kadın müşterileri rahatsız ettiği için kavga ettik.", secenekler = listOf(
            DiyalogSecenegi("Diğer Şüphelileri Sorgula", "baslangic")
        )),
        "duru_konusma" to DiyalogDurumu("Duru Arısoy: Evet Petridon'u ben öldürdüm.", secenekler = listOf(
            DiyalogSecenegi("Neden?", "duru_son"),
            DiyalogSecenegi("Duru Arısoy'u Tutukla", "vaka_son")
        )),
        "duru_son" to DiyalogDurumu("Duru Arısoy: Petridon beni uzun süredir rahatsız ediyordu. Defalarca durmasını istedim ama dinlemedi.\n" +
                "Petridon'un hareketlerini Denizlerin Tanrısı Poseidon'un Medusa'ya yaptıklarına benzettiğim\n" +
                "için asıl cezalandırılması gereken kişinin Medusa gibi kurbanlar olmasındansa Poseidon gibi\n" +
                "pislikler olduğunu insanlara anlatabilmek için Petridon'u Yerebatan Sarnıcı'nda, Medusa'nın\n" +
                "öldürüldüğü gibi boynunu, Medusa Başı Heykeli'nin önünde kestim.", secenekler = listOf(
            DiyalogSecenegi("Duru Arısoy'u Tutukla", "vaka_son")
                )),
        "vaka_son" to DiyalogDurumu("Dosya Kapandı. Suçlu yakalandı... Ama gerçek adalet bu mu?", null)
    )

    val suankiDurum = diyalogHaritasi[mevcutDurumId]

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
                                kanitBulundu = true
                                Toast.makeText(context, "Kanıt Bulundu!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.Center).size(60.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(40.dp))
                        }
                    }
                }

                Text(
                    text = suankiDurum?.metin ?: "",
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
                                if (item.id == "sorgula") mevcutDurumId = "sorgu_ekrani"
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
                    suankiDurum?.secenekler?.forEach { secenek ->
                        Button(
                            onClick = {mevcutDurumId = secenek.sonrakiDurumId},
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text(secenek.metin)
                        }
                    }
                    if (suankiDurum?.secenekler == null){
                        TextButton(onClick = {mevcutDurumId="baslangic"}) {
                            Text("Yeniden Başla", color = Color.Cyan)
                        }
                    }
                }
            }
        }
    }
}
