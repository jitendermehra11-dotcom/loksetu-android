package com.aistudio.loksetu.vxqtmp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.aistudio.loksetu.vxqtmp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. एड और इंश्योरेंस कार्ड क्लिक विवरण (Dialog Popup)
        val cardInsurance = findViewById<View>(R.id.cardInsurance)
        cardInsurance?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("LokSetu सुरक्षा व विज्ञापन विवरण")
                .setMessage("• एड व्यूज से हुई कमाई: ₹12.50\n• माइक्रो-इंश्योरेंस कवर: ₹50,000\n• एक्टिव प्लान: डेली वर्कर सुरक्षा सेतु")
                .setPositiveButton("ठीक है") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // 2. असली मर्चेंट UPI पेमेंट लिंक (PhonePe / GPay / Paytm)
        val btnMerchantPay = findViewById<View>(R.id.btnMerchantPay)
        btnMerchantPay?.setOnClickListener {
            val upiUri = Uri.parse("upi://pay?pa=yourmerchant@upi&pn=LokSetu&mc=0000&mode=02&purpose=00")
            val intent = Intent(Intent.ACTION_VIEW, upiUri)
            startActivity(Intent.createChooser(intent, "पेमेंट ऐप चुनें"))
        }
    }
}
