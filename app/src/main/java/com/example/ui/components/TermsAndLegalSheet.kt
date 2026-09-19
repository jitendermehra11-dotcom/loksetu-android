package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndLegalSheet(
    initialLanguage: AppLanguage = AppLanguage.HINDI,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var language by remember { mutableStateOf(initialLanguage) }
    val isHindi = language == AppLanguage.HINDI

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("terms_legal_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Header with language toggle and close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isHindi) "नियम, शर्तें व कानूनी दिशा-निर्देश" else "Terms of Use & Legal Guidelines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isHindi) "पारदर्शिता • शून्य कमीशन • कानूनी सुरक्षा" else "Transparency • Zero Hidden Fees • Legal Safety",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_terms_sheet_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Language Switcher Chips inside sheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = language == AppLanguage.HINDI,
                    onClick = { language = AppLanguage.HINDI },
                    label = { Text("हिंदी (सरल भाषा)", fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terms_lang_hindi")
                )

                FilterChip(
                    selected = language == AppLanguage.ENGLISH,
                    onClick = { language = AppLanguage.ENGLISH },
                    label = { Text("English (Legal)", fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terms_lang_english")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Zero Hidden Commission Policy
                LegalPolicyCard(
                    icon = Icons.Default.CurrencyRupee,
                    iconTint = Color(0xFF2E7D32),
                    title = if (isHindi) "1. शून्य छिपा कमीशन नीति (Zero Hidden Commission)" else "1. Zero Hidden Commission Policy",
                    points = if (isHindi) {
                        listOf(
                            "LokSetu प्लेटफॉर्म पर किसान, कामगार व वाहन चालक से कोई अप्रत्यक्ष या छुपा हुआ कमीशन नहीं लिया जाता।",
                            "नकद एवं सीधे सौदों पर 0% शुल्क है — पूरी कमाई सीधे आपकी जेब में जाती है।",
                            "डिजिटल एस्क्रो भुगतान में केवल 3% पारदर्शी सर्वर व बैंकिंग शुल्क और ₹1 सूक्ष्म बीमा अंशदान लिया जाता है, जिसका पूरा ब्योरा स्क्रीन पर दिखाई देता है।",
                            "बिचौलियों और निजी एग्रीगेटरों द्वारा की जाने वाली अनियंत्रित कटौती LokSetu पर प्रतिबंधित है।"
                        )
                    } else {
                        listOf(
                            "LokSetu does not charge hidden cuts, unexpected platform deductions, or unfair algorithmic fees.",
                            "Direct cash and farm-gate transactions incur 0% commission — 100% of agreed earnings stay with the provider.",
                            "Optional digital settlements include a clear 3% processing charge and a ₹1 micro-insurance contribution with instant 97% net provider payout.",
                            "No surge penalties or shadow throttling of local trade."
                        )
                    }
                )

                // 2. Motor Vehicles Legal Compliance (Private vs Commercial)
                LegalPolicyCard(
                    icon = Icons.Default.DirectionsCar,
                    iconTint = Color(0xFF1565C0),
                    title = if (isHindi) "2. मोटर वाहन कानूनी अनुपालन (व्यावसायिक व निजी नियम)" else "2. Motor Vehicle Legal Compliance (MV Act Guidelines)",
                    points = if (isHindi) {
                        listOf(
                            "व्यावसायिक माल वाहक (छोटा हाथी, पिकअप, बोलेरो) एवं टैक्सी कैब चालकों के पास वैध कमर्शियल ड्राइविंग लाइसेंस (Commercial DL), पीली नंबर प्लेट, फिटनेस सर्टिफिकेट तथा कमर्शियल बीमा होना अनिवार्य है।",
                            "बाइक पार्सल व डिलीवरी: कूरियर, दस्तावेज व पार्सल ढुलाई के लिए आईएसआई (ISI) मार्का हेलमेट, वैध 2-व्हीलर लाइसेंस व बीमा अनिवार्य है।",
                            "निजी सफेद नंबर प्लेट बाइक पर व्यावसायिक सवारी बैठाना केंद्रीय मोटर वाहन नियमों व राज्य नीतियों के विरुद्ध है। LokSetu केवल कानूनी रूप से मान्य पार्सल व सामान डिलीवरी की अनुमति देता है।",
                            "वाहन की क्षमता (Payload) से अधिक ओवरलोडिंग सख्त प्रतिबंधित है। चालक व ग्राहक दोनों कानूनी वजन सीमा का पालन करने के लिए बाध्य हैं।"
                        )
                    } else {
                        listOf(
                            "Commercial cargo haulers (Tata Ace, Pickup, Tempo) and commercial taxi cabs must possess valid commercial driving licenses, commercial yellow number plates, fitness certificates, and active commercial vehicle insurance.",
                            "Bike Parcel & Delivery: Courier, food, and document delivery operators must wear certified safety helmets, carry valid two-wheeler driving licenses, and hold active insurance.",
                            "Carrying commercial taxi passengers on private white-plate two-wheelers without specific state aggregator permits is prohibited under the Motor Vehicles Act. LokSetu permits legal parcel, document, and grocery delivery.",
                            "Strict zero-overloading policy: Operators and hirers must not exceed vehicle manufacturer payload capacity."
                        )
                    }
                )

                // 3. Fair Escrow & Advance Cancellation Rules
                LegalPolicyCard(
                    icon = Icons.Default.Agriculture,
                    iconTint = Color(0xFFE65100),
                    title = if (isHindi) "3. पारदर्शी एस्क्रो व अग्रिम बयाना नियम (Fair Escrow Rules)" else "3. Fair Escrow & Advance Deposit Rules",
                    points = if (isHindi) {
                        listOf(
                            "किसान के साथ बड़ी फसल खरीद के सौदे में 15% अग्रिम टोकन (Escrow Token) अनिवार्य रूप से सुरक्षित रखा जाता है।",
                            "क्रेता (Buyer) द्वारा निरस्तीकरण: यदि खरीदार फसल कटाई/पैकिंग के बाद सौदा रद्द करता है, तो 15% बयाना राशि सीधे किसान को बतौर हर्जाना दी जाती है।",
                            "विक्रेता (Farmer) द्वारा निरस्तीकरण: यदि किसान निर्धारित गुणवत्ता या समय पर माल देने में असमर्थ रहता है, तो 100% टोकन खरीदार को वापस मिलता है + 5% सहायता शुल्क।",
                            "शेष 85% भुगतान मंडी में डिलीवरी एवं वजन (Dharam Kanta) के तुरंत बाद यूपीआई से जारी किया जाता है।"
                        )
                    } else {
                        listOf(
                            "Direct agricultural grain and bulk produce deals require a 15% token advance held in secure digital escrow.",
                            "Buyer Cancellation Penalty: If a buyer cancels after harvest preparation or loading, 100% of the 15% advance token is forfeited and credited directly to the farmer.",
                            "Farmer Cancellation Penalty: If a seller defaults, the buyer receives a 100% refund of the advance plus a 5% dispute mitigation compensation.",
                            "The remaining 85% balance is settled immediately upon weighbridge verification at the delivery destination."
                        )
                    }
                )

                // 4. Driver Welfare Fund Governance
                LegalPolicyCard(
                    icon = Icons.Default.HealthAndSafety,
                    iconTint = Color(0xFF00838F),
                    title = if (isHindi) "4. चालक कल्याण कोष व सामूहिक बीमा संचालन" else "4. Driver Welfare Fund & Insurance Governance",
                    points = if (isHindi) {
                        listOf(
                            "कल्याण कोष का निर्माण 100% कॉर्पोरेट एवं ऑटोमोटिव प्रायोजकों (महिंद्रा, टाटा, टायर निर्माता) के विज्ञापन राजस्व से होता है।",
                            "चालकों की कमाई से ₹0 काटा जाता है — यह पूर्णतया नि:शुल्क समूह दुर्घटना बीमा सुरक्षा है।",
                            "मानक कवरेज: ₹25,00,000 (25 लाख) तथा हाईवे सुपर कवर: ₹50,00,000 (50 लाख) तक की दुर्घटना मृत्यु एवं पूर्ण अपंगता सहायता।",
                            "पारदर्शिता के लिए सभी प्रायोजक अंशदान ऐप के पब्लिक लेजर में देखे जा सकते हैं।"
                        )
                    } else {
                        listOf(
                            "The driver welfare fund is 100% financed through verified corporate and automotive brand sponsorships.",
                            "₹0 is deducted from driver earnings — providing zero-cost group accidental insurance.",
                            "Provides ₹25 Lakh (Standard) and ₹50 Lakh (Highway Super Cover) for accidental demise, permanent disability, and children's education corpus.",
                            "All sponsor contributions are recorded in real-time on a publicly auditable transparency ledger."
                        )
                    }
                )

                // 5. Emergency SOS & Responsible Use
                LegalPolicyCard(
                    icon = Icons.Default.Emergency,
                    iconTint = Color(0xFFC62828),
                    title = if (isHindi) "5. आपातकालीन एसओएस व सुरक्षा शिष्टाचार" else "5. Emergency SOS & Safety Protocol",
                    points = if (isHindi) {
                        listOf(
                            "3-बार वॉल्यूम बटन दबाने अथवा वॉयस कमांड ('मदद करो', 'इमरजेंसी') से सीधे 112 आपातकालीन हेल्पलाइन और परिजनों को जीपीएस लोकेशन संदेश भेजा जाता है।",
                            "एसओएस का उपयोग केवल वास्तविक हाईवे दुर्घटना, लूट, स्वास्थ्य संकट या सुरक्षा आपातकाल में ही करें।",
                            "झूठे आपातकालीन अलर्ट या शरारतपूर्ण उपयोग कानूनन अपराध है।"
                        )
                    } else {
                        listOf(
                            "Triple-pressing the hardware volume key or speaking emergency keywords initiates high-accuracy GPS SMS dispatch and prepares 1-tap dial to 112 emergency services.",
                            "Use SOS exclusively during genuine highway breakdowns, medical emergencies, theft, or physical distress.",
                            "Hoax alerts or fraudulent abuse of national emergency hotlines is subject to statutory penalties under the law."
                        )
                    }
                )

                // 6. Store-to-Customer Parcel Delivery & Rider Payment Safety
                LegalPolicyCard(
                    icon = Icons.Default.Shield,
                    iconTint = Color(0xFF2E7D32),
                    title = if (isHindi) "6. स्टोर पार्सल डिलीवरी एवं राइडर भुगतान सुरक्षा नीति" else "6. Store Parcel Delivery & Rider Payment Safety",
                    points = if (isHindi) {
                        listOf(
                            "100% प्री-पेड एस्क्रो गारंटी: डिलीवरी असाइन होने से पहले सामान की पूरी कीमत व डिलीवरी शुल्क डिजिटल एस्क्रो में लॉक होता है। राइडर को दुकान पर ₹1 भी अपनी जेब से नहीं देना पड़ता।",
                            "4-अंकीय ग्राहक ओटीपी (OTP): पार्सल ड्रॉप-ऑफ पर ग्राहक द्वारा 4-अंकीय ओटीपी दर्ज करते ही मर्चेंट एवं राइडर के यूपीआई खातों में स्वचालित त्वरित भुगतान (Instant Split Payout) ट्रांसफर हो जाता है।",
                            "कैश-ऑन-डिलीवरी (COD) सुरक्षा: सीओडी ऑर्डर के लिए ग्राहक का पूर्व-सत्यापन अनिवार्य है। डिलीवरी पर प्राप्त नकद राशि से राइडर अपना शुल्क काटकर शेष राशि का डिजिटल मिलान करता है।"
                        )
                    } else {
                        listOf(
                            "100% Pre-paid Escrow Protection: Full item cost plus delivery fee is locked in digital escrow prior to rider assignment, ensuring ZERO out-of-pocket spending by riders at local stores.",
                            "4-Digit Customer OTP Verification: Drop-off verification requires entering the 4-digit customer OTP, which triggers instant automated UPI split payout to merchant and rider.",
                            "Cash-on-Delivery (COD) Protection: COD orders mandate upfront customer mobile confirmation. Collected cash is automatically reconciled with net merchant settlement and zero rider liability."
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Acceptance button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accept_terms_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "मैं समझ गया • नियम स्वीकार हैं" else "I Understand & Accept Guidelines",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LegalPolicyCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    points: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            points.forEach { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        fontWeight = FontWeight.Black,
                        color = iconTint,
                        fontSize = 16.sp
                    )
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
