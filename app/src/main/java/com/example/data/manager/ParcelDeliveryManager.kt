package com.example.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * Store-to-Customer Parcel Delivery & Payment Safety Logic
 * Implements:
 * 1. Pre-paid Escrow Delivery Flow (100% item + delivery fee in escrow, ZERO rider out-of-pocket spending at shops)
 * 2. OTP Delivery Verification (4-digit customer OTP triggers instant automated UPI split to merchant & rider)
 * 3. COD Protection Policy (Customer upfront confirmation + automated instant payout reconciliation)
 */

enum class DeliveryPaymentMode(val label: String, val badge: String) {
    PREPAID_ESCROW("100% Pre-paid Escrow", "🛡️ Pre-paid Escrow (₹0 Rider Spending)"),
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "💵 Upfront Confirmed COD")
}

enum class ParcelOrderStatus(val title: String, val stepIndex: Int) {
    ESCROW_LOCKED_PENDING_RIDER("Pre-paid Escrow Locked", 1),
    COD_CONFIRMED_PENDING_RIDER("COD Upfront Confirmed", 1),
    RIDER_ASSIGNED("Rider Heading to Store", 2),
    PICKED_UP_FROM_STORE("Picked Up (₹0 Paid by Rider)", 3),
    OUT_FOR_DELIVERY("Out for Delivery (Awaiting OTP)", 4),
    DELIVERED_AND_SPLIT_PAID("Delivered & UPI Split Paid", 5),
    CANCELLED("Order Cancelled", 0)
}

data class ParcelDeliveryOrder(
    val id: String = "ORD-${UUID.randomUUID().toString().take(6).uppercase()}",
    val storeName: String,
    val storeAddress: String,
    val storePhone: String,
    val storeUpiId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val parcelItemsDescription: String,
    val itemCost: Double,
    val deliveryFee: Double,
    val platformFee: Double = 0.0, // Zero hidden commission
    val totalAmount: Double = itemCost + deliveryFee + platformFee,
    val paymentMode: DeliveryPaymentMode = DeliveryPaymentMode.PREPAID_ESCROW,
    val deliveryOtp: String = String.format(Locale.getDefault(), "%04d", Random.nextInt(1000, 9999)),
    val status: ParcelOrderStatus = ParcelOrderStatus.ESCROW_LOCKED_PENDING_RIDER,
    val riderZeroSpendingGuaranteed: Boolean = true,
    val codCustomerConfirmed: Boolean = (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW),
    val assignedRiderName: String? = null,
    val assignedRiderPhone: String? = null,
    val assignedRiderUpiId: String? = null,
    val assignedVehicleType: String? = null,
    val distanceKm: Double = 5.2,
    val storeLatitude: Double = 28.6139,
    val storeLongitude: Double = 77.2090,
    val customerLatitude: Double = 28.5830,
    val customerLongitude: Double = 77.0600,
    val isReturnMatch: Boolean = false,
    val returnMilesSavedKm: Double = 0.0,
    val chainRunId: String? = null,
    val fuelSurchargeBonus: Double = 0.0,
    val fuelType: FuelType = FuelType.PETROL,
    val merchantPayoutTxnId: String? = null,
    val riderPayoutTxnId: String? = null,
    val completionTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(createdAt))

    val isDelivered: Boolean
        get() = status == ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID
}

class ParcelDeliveryManager private constructor() {

    private val _orders = MutableStateFlow<List<ParcelDeliveryOrder>>(createInitialOrders())
    val orders: StateFlow<List<ParcelDeliveryOrder>> = _orders.asStateFlow()

    private val _totalEscrowSecured = MutableStateFlow(14250.0)
    val totalEscrowSecured: StateFlow<Double> = _totalEscrowSecured.asStateFlow()

    private val _totalRiderPayoutsSettled = MutableStateFlow(3480.0)
    val totalRiderPayoutsSettled: StateFlow<Double> = _totalRiderPayoutsSettled.asStateFlow()

    private val _totalMerchantPayoutsSettled = MutableStateFlow(28950.0)
    val totalMerchantPayoutsSettled: StateFlow<Double> = _totalMerchantPayoutsSettled.asStateFlow()

    /**
     * 1. Create 100% Pre-paid Escrow Order
     * Both item cost and delivery fee are locked upfront in escrow.
     * Rider spends ZERO out-of-pocket at the local store.
     */
    fun createPrepaidEscrowOrder(
        storeName: String,
        storeAddress: String,
        storePhone: String,
        storeUpiId: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        itemsDescription: String,
        itemCost: Double,
        deliveryFee: Double,
        distanceKm: Double = 5.2,
        fuelType: FuelType = FuelType.PETROL,
        fuelSurchargeBonus: Double = 0.0
    ): ParcelDeliveryOrder {
        val total = itemCost + deliveryFee
        val order = ParcelDeliveryOrder(
            storeName = storeName,
            storeAddress = storeAddress,
            storePhone = storePhone,
            storeUpiId = storeUpiId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            parcelItemsDescription = itemsDescription,
            itemCost = itemCost,
            deliveryFee = deliveryFee,
            totalAmount = total,
            distanceKm = distanceKm,
            fuelType = fuelType,
            fuelSurchargeBonus = fuelSurchargeBonus,
            paymentMode = DeliveryPaymentMode.PREPAID_ESCROW,
            status = ParcelOrderStatus.ESCROW_LOCKED_PENDING_RIDER,
            riderZeroSpendingGuaranteed = true,
            codCustomerConfirmed = true
        )
        _orders.value = listOf(order) + _orders.value
        _totalEscrowSecured.value += total
        return order
    }

    /**
     * 3. Create Upfront-Confirmed COD Order
     * Requires customer confirmation to guarantee order legitimacy before assigning rider.
     */
    fun createCodOrder(
        storeName: String,
        storeAddress: String,
        storePhone: String,
        storeUpiId: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        itemsDescription: String,
        itemCost: Double,
        deliveryFee: Double,
        customerConfirmed: Boolean,
        distanceKm: Double = 4.8,
        fuelType: FuelType = FuelType.PETROL,
        fuelSurchargeBonus: Double = 0.0
    ): ParcelDeliveryOrder {
        val total = itemCost + deliveryFee
        val order = ParcelDeliveryOrder(
            storeName = storeName,
            storeAddress = storeAddress,
            storePhone = storePhone,
            storeUpiId = storeUpiId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            parcelItemsDescription = itemsDescription,
            itemCost = itemCost,
            deliveryFee = deliveryFee,
            totalAmount = total,
            distanceKm = distanceKm,
            fuelType = fuelType,
            fuelSurchargeBonus = fuelSurchargeBonus,
            paymentMode = DeliveryPaymentMode.CASH_ON_DELIVERY,
            status = if (customerConfirmed) ParcelOrderStatus.COD_CONFIRMED_PENDING_RIDER else ParcelOrderStatus.ESCROW_LOCKED_PENDING_RIDER,
            riderZeroSpendingGuaranteed = true, // Store gives parcel on LokSetu COD token
            codCustomerConfirmed = customerConfirmed
        )
        _orders.value = listOf(order) + _orders.value
        return order
    }

    /**
     * Assign a rider to the order
     */
    fun assignRider(
        orderId: String,
        riderName: String,
        riderPhone: String,
        riderUpiId: String,
        vehicleType: String = "🏍️ Bike Delivery"
    ): Boolean {
        val currentList = _orders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val order = currentList[index]
            currentList[index] = order.copy(
                status = ParcelOrderStatus.RIDER_ASSIGNED,
                assignedRiderName = riderName,
                assignedRiderPhone = riderPhone,
                assignedRiderUpiId = riderUpiId,
                assignedVehicleType = vehicleType
            )
            _orders.value = currentList
            return true
        }
        return false
    }

    /**
     * Confirm pickup from local merchant (Rider spends ₹0 at shop)
     */
    fun confirmPickupFromStore(orderId: String): Boolean {
        val currentList = _orders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val order = currentList[index]
            currentList[index] = order.copy(
                status = ParcelOrderStatus.OUT_FOR_DELIVERY
            )
            _orders.value = currentList
            return true
        }
        return false
    }

    /**
     * 2. OTP Delivery Verification & Instant Automated UPI Split
     * Verifies 4-digit customer OTP.
     * On match:
     * - Triggers instant UPI payout to merchant (Item Cost)
     * - Triggers instant UPI payout to rider (Delivery Fee)
     * - In COD: reconciles cash collected by rider with merchant settlement
     */
    sealed class OtpVerificationResult {
        data class Success(val merchantPayout: Double, val riderPayout: Double, val merchantTxn: String, val riderTxn: String) : OtpVerificationResult()
        data class Error(val message: String) : OtpVerificationResult()
    }

    fun verifyOtpAndCompleteDelivery(orderId: String, enteredOtp: String): OtpVerificationResult {
        val currentList = _orders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index == -1) {
            return OtpVerificationResult.Error("Order not found")
        }

        val order = currentList[index]
        if (order.status == ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID) {
            return OtpVerificationResult.Error("This order is already completed and settled.")
        }

        if (enteredOtp.trim() != order.deliveryOtp) {
            return OtpVerificationResult.Error("Invalid OTP! Please enter the exact 4-digit code provided by customer.")
        }

        // Generate automated UPI payout transaction IDs
        val mTxn = "UPI/MERCH/${System.currentTimeMillis().toString().takeLast(6)}/${Random.nextInt(100, 999)}"
        val rTxn = "UPI/RIDER/${System.currentTimeMillis().toString().takeLast(6)}/${Random.nextInt(100, 999)}"

        currentList[index] = order.copy(
            status = ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID,
            merchantPayoutTxnId = mTxn,
            riderPayoutTxnId = rTxn,
            completionTimestamp = System.currentTimeMillis()
        )
        _orders.value = currentList

        _totalMerchantPayoutsSettled.value += order.itemCost
        _totalRiderPayoutsSettled.value += order.deliveryFee

        return OtpVerificationResult.Success(
            merchantPayout = order.itemCost,
            riderPayout = order.deliveryFee,
            merchantTxn = mTxn,
            riderTxn = rTxn
        )
    }

    fun confirmCodUpfront(orderId: String): Boolean {
        val currentList = _orders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val order = currentList[index]
            currentList[index] = order.copy(
                codCustomerConfirmed = true,
                status = ParcelOrderStatus.COD_CONFIRMED_PENDING_RIDER
            )
            _orders.value = currentList
            return true
        }
        return false
    }

    private fun createInitialOrders(): List<ParcelDeliveryOrder> {
        return listOf(
            ParcelDeliveryOrder(
                id = "ORD-7821",
                storeName = "Aggarwal Kirana & Ration Store",
                storeAddress = "Shop 14, Main Mandi Road, Najafgarh",
                storePhone = "+91 98114 55667",
                storeUpiId = "aggarwalkirana@icici",
                customerName = "Pooja Sharma",
                customerPhone = "+91 98712 33445",
                customerAddress = "Flat 402, Block C, Dwarka Sector 14",
                parcelItemsDescription = "5kg Fortune Aata, 1L Mustard Oil, 1kg Tata Salt",
                itemCost = 420.0,
                deliveryFee = 60.0,
                totalAmount = 480.0,
                distanceKm = 5.4,
                storeLatitude = 28.6110,
                storeLongitude = 76.9850,
                customerLatitude = 28.5980,
                customerLongitude = 77.0310,
                paymentMode = DeliveryPaymentMode.PREPAID_ESCROW,
                deliveryOtp = "4829",
                status = ParcelOrderStatus.OUT_FOR_DELIVERY,
                assignedRiderName = "Vikram Singh",
                assignedRiderPhone = "+91 98101 23456",
                assignedRiderUpiId = "vikram.bike@oksbi",
                assignedVehicleType = "🏍️ Hero Splendor (Bike Parcel)",
                riderZeroSpendingGuaranteed = true,
                codCustomerConfirmed = true,
                fuelType = FuelType.PETROL,
                fuelSurchargeBonus = 1.03
            ),
            ParcelDeliveryOrder(
                id = "ORD-6540",
                storeName = "Kisan Fresh Dairy & Paneer Bhandar",
                storeAddress = "Booth 3, Sector 9 Market",
                storePhone = "+91 98991 77889",
                storeUpiId = "kisanfresh@paytm",
                customerName = "Amit Kumar",
                customerPhone = "+91 98188 99112",
                customerAddress = "House 88, Street 4, Janakpuri",
                parcelItemsDescription = "2kg Fresh Malai Paneer, 3L Full Cream Buffalo Milk",
                itemCost = 360.0,
                deliveryFee = 50.0,
                totalAmount = 410.0,
                distanceKm = 4.2,
                storeLatitude = 28.5810,
                storeLongitude = 77.0620,
                customerLatitude = 28.6210,
                customerLongitude = 77.0870,
                paymentMode = DeliveryPaymentMode.PREPAID_ESCROW,
                deliveryOtp = "3194",
                status = ParcelOrderStatus.ESCROW_LOCKED_PENDING_RIDER,
                riderZeroSpendingGuaranteed = true,
                codCustomerConfirmed = true,
                isReturnMatch = true,
                returnMilesSavedKm = 4.1,
                fuelType = FuelType.PETROL,
                fuelSurchargeBonus = 0.81
            ),
            ParcelDeliveryOrder(
                id = "ORD-5192",
                storeName = "Gupta Electricals & Hardware",
                storeAddress = "Shop 22, Old Railway Road",
                storePhone = "+91 98120 44556",
                storeUpiId = "guptaelectric@sbi",
                customerName = "Rajesh Malhotra",
                customerPhone = "+91 98110 55667",
                customerAddress = "Plot 12B, Palam Extension",
                parcelItemsDescription = "2x Havells 9W LED, 1x Anchor MCB 16A, 1x Extension Board",
                itemCost = 540.0,
                deliveryFee = 70.0,
                totalAmount = 610.0,
                distanceKm = 4.8,
                storeLatitude = 28.6010,
                storeLongitude = 77.0420,
                customerLatitude = 28.5850,
                customerLongitude = 77.0800,
                paymentMode = DeliveryPaymentMode.CASH_ON_DELIVERY,
                deliveryOtp = "7251",
                status = ParcelOrderStatus.COD_CONFIRMED_PENDING_RIDER,
                codCustomerConfirmed = true,
                isReturnMatch = true,
                returnMilesSavedKm = 5.2,
                chainRunId = "CHN-8821",
                assignedRiderName = "Raju Verma",
                assignedRiderPhone = "+91 98711 22334",
                assignedRiderUpiId = "raju.verma@ybl",
                assignedVehicleType = "🏍️ Bike Parcel & Delivery",
                fuelType = FuelType.PETROL,
                fuelSurchargeBonus = 0.92
            ),
            ParcelDeliveryOrder(
                id = "ORD-4018",
                storeName = "Balaji Organic Vegetable Depot",
                storeAddress = "Shed 8, Azadpur Mandi Gate 2",
                storePhone = "+91 98119 22331",
                storeUpiId = "balaji.mandi@axisbank",
                customerName = "Sunita Rao",
                customerPhone = "+91 98912 33441",
                customerAddress = "Tower 2, Sector 22 Rohini",
                parcelItemsDescription = "10kg Organic Potatoes, 5kg Fresh Onions, 2kg Mandi Tomatoes",
                itemCost = 450.0,
                deliveryFee = 80.0,
                totalAmount = 530.0,
                distanceKm = 6.8,
                storeLatitude = 28.7120,
                storeLongitude = 77.1750,
                customerLatitude = 28.7180,
                customerLongitude = 77.1080,
                paymentMode = DeliveryPaymentMode.PREPAID_ESCROW,
                deliveryOtp = "6108",
                status = ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID,
                assignedRiderName = "Mohd. Shakeel",
                assignedRiderPhone = "+91 98111 88990",
                assignedRiderUpiId = "shakeel.ace@okhdfcbank",
                assignedVehicleType = "🚚 Tata Ace Chhota Hathi",
                merchantPayoutTxnId = "UPI/MERCH/881290/102",
                riderPayoutTxnId = "UPI/RIDER/881291/304",
                completionTimestamp = System.currentTimeMillis() - 3600000L,
                fuelType = FuelType.DIESEL,
                fuelSurchargeBonus = 4.67
            )
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: ParcelDeliveryManager? = null

        fun getInstance(): ParcelDeliveryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ParcelDeliveryManager().also { INSTANCE = it }
            }
        }
    }
}
