package com.example.paymentdemo.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.example.paymentdemo.PayViewModel
import com.example.paymentdemo.model.*
import com.example.paymentdemo.payments.PaymentLogic
import com.example.paymentdemo.scanner.QrScanner
import com.example.paymentdemo.theme.PayDemoTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val Purple = Color(0xFF5F259F)
private val Pale = Color(0xFFF8F4FC)
private val Ink = Color(0xFF212121)

@Composable
fun PayDemoApp(vm: PayViewModel) {
    PayDemoTheme {
        val nav = rememberNavController()
        val tx by vm.transactions.collectAsState()
        val notes by vm.notifications.collectAsState()
        NavHost(navController = nav, startDestination = "main") {
            composable("main") { MainShell(nav, tx, notes) }
            composable("scan") { QrScanner(onBack = { nav.popBackStack() }, onCode = { raw ->
                val recipient = PaymentLogic.parseDemoQr(raw)
                nav.currentBackStackEntry?.savedStateHandle?.set("recipient", recipient)
                nav.currentBackStackEntry?.savedStateHandle?.set("invalid", recipient == null)
                nav.navigate("recipient")
            }) }
            composable("recipient") {
                val recipient = nav.previousBackStackEntry?.savedStateHandle?.get<Recipient>("recipient")
                RecipientScreen(recipient, nav, vm)
            }
            composable("amount") {
                val recipient = nav.previousBackStackEntry?.savedStateHandle?.get<Recipient>("recipient") ?: Recipient("Rahul Sharma", "rahul@demobank")
                AmountScreen(recipient, nav)
            }
            composable("confirm") {
                val recipient = nav.previousBackStackEntry?.savedStateHandle?.get<Recipient>("recipient") ?: Recipient("Rahul Sharma", "rahul@demobank")
                val amount = nav.previousBackStackEntry?.savedStateHandle?.get<Double>("amount") ?: 1.0
                ConfirmScreen(recipient, amount, nav, vm)
            }
            composable("result/{id}") { entry ->
                val id = entry.arguments?.getString("id")
                val item = tx.firstOrNull { it.transactionId == id }
                ResultScreen(item, nav)
            }
            composable("transaction/{id}") { entry ->
                val item = tx.firstOrNull { it.transactionId == entry.arguments?.getString("id") }
                TransactionDetailScreen(item, nav)
            }
            composable("profile") { ProfileScreen(nav) }
            composable("balance") { BalanceScreen(nav) }
            composable("notifications") { NotificationsScreen(notes, nav) }
            composable("service/{name}") { entry -> BasicPage(entry.arguments?.getString("name") ?: "Service", "This service is available as a local demo preview.", { nav.popBackStack() }) }
            composable("quickpay/{kind}") { entry ->
                val kind = entry.arguments?.getString("kind") ?: "Payment"
                var name by remember { mutableStateOf("Rahul Sharma") }
                var upi by remember { mutableStateOf("rahul@demobank") }
                Column(Modifier.fillMaxSize().background(Pale).padding(20.dp)) {
                    TopBack(kind, nav)
                    Text("Demo recipient details", Modifier.padding(top=24.dp), fontWeight=FontWeight.SemiBold)
                    OutlinedTextField(name, { name=it }, Modifier.fillMaxWidth().padding(top=12.dp), label={Text("Recipient name")})
                    OutlinedTextField(upi, { upi=it }, Modifier.fillMaxWidth().padding(top=10.dp), label={Text("Demo UPI ID")})
                    Spacer(Modifier.weight(1f))
                    Button(onClick={ nav.currentBackStackEntry?.savedStateHandle?.set("recipient", Recipient(name, upi)); nav.navigate("amount") }, enabled=name.isNotBlank() && upi.matches(Regex("[a-zA-Z0-9._-]+@demobank")), modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Purple)) { Text("Continue") }
                    Text("Only @demobank test IDs are accepted.", Modifier.padding(top=8.dp), color=Color.Gray, fontSize=11.sp)
                }
            }
        }
    }
}

@Composable
private fun MainShell(nav: androidx.navigation.NavHostController, tx: List<TransactionEntity>, notes: List<NotificationEntity>) {
    var tab by remember { mutableStateOf("Home") }
    Scaffold(containerColor = Pale, bottomBar = {
        NavigationBar(containerColor = Color.White) {
            listOf("Home" to Icons.Default.Home, "History" to Icons.Default.ReceiptLong, "Insurance" to Icons.Default.VerifiedUser, "Wealth" to Icons.Default.TrendingUp).forEach { (label, icon) ->
                NavigationBarItem(selected = tab == label, onClick = { tab = label }, icon = { Icon(icon, null) }, label = { Text(label, fontSize = 11.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Purple, selectedTextColor = Purple, indicatorColor = Color(0xFFF0E5FA)))
            }
        }
    }) { padding ->
        when (tab) {
            "Home" -> HomeScreen(Modifier.padding(padding), nav)
            "History" -> HistoryScreen(Modifier.padding(padding), tx, nav)
            "Insurance" -> SimpleServiceScreen(Modifier.padding(padding), "Insurance", "Explore demo insurance products", Icons.Default.VerifiedUser)
            else -> SimpleServiceScreen(Modifier.padding(padding), "Wealth", "Explore demo investments and digital gold", Icons.Default.TrendingUp)
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, nav: androidx.navigation.NavHostController) {
    val context = LocalContext.current
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Column(Modifier.fillMaxWidth().background(Purple).padding(horizontal = 18.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha=.18f)), contentAlignment = Alignment.Center) { Text("P", color=Color.White, fontWeight=FontWeight.Bold, fontSize=22.sp) }
                    Column(Modifier.weight(1f).padding(start=12.dp)) { Text("Good morning", color=Color.White.copy(.8f), fontSize=12.sp); Text("PayDemo", color=Color.White, fontWeight=FontWeight.Bold, fontSize=21.sp) }
                    IconButton(onClick = { nav.navigate("notifications") }) { Icon(Icons.Default.Notifications, null, tint=Color.White) }
                    IconButton(onClick = { nav.navigate("profile") }) { Icon(Icons.Default.AccountCircle, null, tint=Color.White) }
                }
                Spacer(Modifier.height(14.dp))
                Text("DEMO ACCOUNT  ·  No real money", color=Color.White.copy(.85f), fontSize=11.sp)
            }
        }
        item {
            Card(Modifier.fillMaxWidth().padding(16.dp).clickable { nav.navigate("scan") }, shape=RoundedCornerShape(18.dp), colors=CardDefaults.cardColors(containerColor=Color.White), elevation=CardDefaults.cardElevation(2.dp)) {
                Row(Modifier.padding(18.dp), verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(58.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF2E9FA)), contentAlignment=Alignment.Center) { Icon(Icons.Default.QrCodeScanner, null, tint=Purple, modifier=Modifier.size(34.dp)) }
                    Column(Modifier.weight(1f).padding(start=14.dp)) { Text("Scan any QR", fontSize=17.sp, fontWeight=FontWeight.SemiBold, color=Ink); Text("Pay using a demo transaction", color=Color.Gray, fontSize=12.sp) }
                    Icon(Icons.Default.ChevronRight, null, tint=Purple)
                }
            }
        }
        item { SectionTitle("Money Transfers") }
        item { ServiceGrid(listOf("To Mobile" to Icons.Default.Phone, "To Bank" to Icons.Default.AccountBalance, "To UPI ID" to Icons.Default.AlternateEmail, "Self Transfer" to Icons.Default.SwapHoriz, "Check Balance" to Icons.Default.Visibility, "Bank Transfer" to Icons.Default.Send)) { label ->
            when(label) { "Check Balance" -> nav.navigate("balance"); else -> nav.navigate("quickpay/$label") }
        } }
        item { SectionTitle("Recharge & Bill Payments") }
        item { ServiceGrid(listOf("Mobile" to Icons.Default.Smartphone, "Electricity" to Icons.Default.Bolt, "DTH" to Icons.Default.Tv, "Broadband" to Icons.Default.Wifi, "Gas" to Icons.Default.LocalFireDepartment, "Water" to Icons.Default.WaterDrop, "Credit Card" to Icons.Default.CreditCard, "FASTag" to Icons.Default.DirectionsCar, "Education" to Icons.Default.School, "Loan" to Icons.Default.Payments)) { nav.navigate("service/$it") } }
        item { SectionTitle("Offers for you") }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal=16.dp), horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                listOf("Demo cashback", "Recharge rewards", "Bill pay bonus").forEachIndexed { i, title ->
                    Card(Modifier.width(270.dp).height(126.dp), shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=listOf(Color(0xFFEEE2FA), Color(0xFFE4F1F0), Color(0xFFFFEEDC))[i])) {
                        Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.SpaceBetween) { Text("DEMO OFFER", color=Purple, fontSize=10.sp, fontWeight=FontWeight.Bold); Text(title, fontSize=18.sp, fontWeight=FontWeight.Bold, color=Ink); Text("Explore simulated rewards  ›", fontSize=12.sp, color=Color.DarkGray) }
                    }
                }
            }
        }
        item { SectionTitle("More services") }
        item { ServiceGrid(listOf("Insurance" to Icons.Default.VerifiedUser, "Travel" to Icons.Default.Flight, "Investments" to Icons.Default.TrendingUp, "Gold" to Icons.Default.Circle, "Rewards" to Icons.Default.CardGiftcard, "Support" to Icons.Default.SupportAgent)) { nav.navigate("service/$it") } }
        item { Text("PayDemo is an educational simulation. No money is transferred.", Modifier.fillMaxWidth().padding(18.dp), color=Color.Gray, fontSize=11.sp) }
    }
}

@Composable private fun SectionTitle(title: String) { Text(title, Modifier.padding(start=18.dp, end=18.dp, top=8.dp, bottom=10.dp), fontSize=17.sp, fontWeight=FontWeight.SemiBold, color=Ink) }
@Composable private fun ServiceGrid(items: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>, onClick: (String)->Unit) {
    Column(Modifier.padding(horizontal=12.dp)) { items.chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)) { row.forEach { (name, icon) -> Column(Modifier.weight(1f).clickable { onClick(name) }.padding(vertical=12.dp), horizontalAlignment=Alignment.CenterHorizontally) { Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Color.White), contentAlignment=Alignment.Center) { Icon(icon, null, tint=Purple, modifier=Modifier.size(24.dp)) }; Spacer(Modifier.height(7.dp)); Text(name, fontSize=11.sp, color=Ink) } }; repeat(3-row.size) { Spacer(Modifier.weight(1f)) } } } }
}

@Composable private fun HistoryScreen(modifier: Modifier, tx: List<TransactionEntity>, nav: androidx.navigation.NavHostController) {
    var filter by remember { mutableStateOf("All") }
    val filtered = tx.filter { filter=="All" || it.transactionStatus==filter.uppercase() || it.transactionType==filter.uppercase() }
    Column(modifier.fillMaxSize().background(Pale)) {
        Text("History", Modifier.padding(18.dp), fontSize=22.sp, fontWeight=FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal=12.dp)) { listOf("All","Sent","Received","Failed").forEach { label -> FilterChip(selected=filter==label,onClick={filter=label},label={Text(label)},modifier=Modifier.padding(horizontal=4.dp)) } }
        if (filtered.isEmpty()) Text("No transactions yet", Modifier.padding(24.dp), color=Color.Gray) else LazyColumn { items(filtered) { txItem -> TransactionRow(txItem) { nav.navigate("transaction/${txItem.transactionId}") } } }
    }
}
@Composable private fun TransactionRow(tx: TransactionEntity, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=6.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).clickable(onClick=onClick).padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFF2E9FA)), contentAlignment=Alignment.Center) { Icon(if(tx.transactionType=="SENT") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,null,tint=Purple) }
        Column(Modifier.weight(1f).padding(start=12.dp)) { Text(tx.recipientName,fontWeight=FontWeight.SemiBold,color=Ink); Text(SimpleDateFormat("dd MMM · hh:mm a",Locale.getDefault()).format(Date(tx.timestamp)),fontSize=11.sp,color=Color.Gray) }
        Column(horizontalAlignment=Alignment.End) { Text((if(tx.transactionType=="SENT") "−" else "+")+"₹"+"%.2f".format(tx.amount),fontWeight=FontWeight.Bold,color=Ink); Text(if(tx.transactionStatus=="SUCCESS") "Successful · DEMO" else "Failed · DEMO",fontSize=10.sp,color=if(tx.transactionStatus=="SUCCESS") Color(0xFF168B52) else Color.Red) }
    }
}

@Composable private fun RecipientScreen(recipient: Recipient?, nav: androidx.navigation.NavHostController, vm: PayViewModel) {
    if (recipient == null) { BasicPage("Unsupported QR code", "Only PayDemo sample QR codes are accepted.", onBack={nav.popBackStack()}); return }
    Column(Modifier.fillMaxSize().background(Pale).padding(20.dp), horizontalAlignment=Alignment.CenterHorizontally) {
        TopBack("Recipient", nav)
        Spacer(Modifier.height(44.dp)); Avatar(recipient.name)
        Text(recipient.name, Modifier.padding(top=12.dp), fontSize=22.sp, fontWeight=FontWeight.Bold)
        Text(recipient.upiId, color=Color.Gray)
        Text(recipient.bank, color=Color.Gray, fontSize=12.sp)
        Spacer(Modifier.height(28.dp)); Button(onClick={nav.currentBackStackEntry?.savedStateHandle?.set("recipient",recipient); nav.navigate("amount")}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Purple)) { Text("Continue") }
        Text("Verified demo recipient", Modifier.padding(top=12.dp), color=Color.Gray, fontSize=11.sp)
    }
}

@Composable private fun AmountScreen(recipient: Recipient, nav: androidx.navigation.NavHostController) {
    var amount by remember { mutableStateOf("") }; val parsed=PaymentLogic.validateAmount(amount)
    Column(Modifier.fillMaxSize().background(Pale).padding(20.dp)) {
        TopBack("Enter amount",nav); Spacer(Modifier.height(34.dp)); Text("Paying ${recipient.name}",fontWeight=FontWeight.SemiBold)
        OutlinedTextField(value=amount,onValueChange={ amount=it.filter { c -> c.isDigit() || c=='.' }.take(10) },modifier=Modifier.fillMaxWidth().padding(top=20.dp),label={Text("Amount")},leadingIcon={Text("₹",fontSize=22.sp)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
        Text("Maximum demo amount: ₹1,00,000",Modifier.padding(top=8.dp),fontSize=11.sp,color=Color.Gray)
        Spacer(Modifier.weight(1f)); Button(onClick={nav.currentBackStackEntry?.savedStateHandle?.set("recipient",recipient); nav.currentBackStackEntry?.savedStateHandle?.set("amount",parsed); nav.navigate("confirm")},enabled=parsed!=null,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Purple)) { Text("Continue") }
    }
}

@Composable private fun ConfirmScreen(recipient: Recipient, amount: Double, nav: androidx.navigation.NavHostController, vm: PayViewModel) {
    var busy by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Pale).padding(20.dp)) {
        TopBack("Confirm payment",nav); Spacer(Modifier.height(26.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally) { Avatar(recipient.name); Text(recipient.name,Modifier.padding(top=10.dp),fontWeight=FontWeight.SemiBold); Text(recipient.upiId,color=Color.Gray); Text("₹"+"%.2f".format(amount),Modifier.padding(vertical=20.dp),fontSize=30.sp,fontWeight=FontWeight.Bold); Text("Demo payment method · PayDemo ledger",fontSize=12.sp,color=Color.Gray) } }
        Text("This is a simulation. No bank or UPI network will be contacted.",Modifier.padding(vertical=18.dp),fontSize=12.sp,color=Color.Gray)
        Spacer(Modifier.weight(1f))
        Button(onClick={ if(!busy){ busy=true; vm.pay(recipient,amount,true){ nav.navigate("result/${it.transactionId}"){ popUpTo("main") } } } },enabled=!busy,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Purple)) { if(busy) CircularProgressIndicator(Modifier.size(18.dp),color=Color.White,strokeWidth=2.dp) else Text("Confirm demo payment") }
        TextButton(onClick={if(!busy){busy=true;vm.pay(recipient,amount,false){nav.navigate("result/${it.transactionId}"){popUpTo("main")}}}},modifier=Modifier.align(Alignment.CenterHorizontally)) { Text("Simulate failure",color=Color.Gray) }
    }
}

@Composable private fun ResultScreen(item: TransactionEntity?, nav: androidx.navigation.NavHostController) {
    if(item==null){ BasicPage("Transaction saved", "Open History to view your demo transaction.", {nav.navigate("main")}); return }
    val success=item.transactionStatus=="SUCCESS"
    Column(Modifier.fillMaxSize().background(Color.White).padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(60.dp)); Box(Modifier.size(76.dp).clip(CircleShape).background(if(success) Color(0xFFE5F5EC) else Color(0xFFFCE8E6)),contentAlignment=Alignment.Center){Icon(if(success) Icons.Default.Check else Icons.Default.Close,null,tint=if(success) Color(0xFF168B52) else Color.Red,modifier=Modifier.size(42.dp))}
        Text(if(success) "Payment Successful" else "Payment Failed",Modifier.padding(top=18.dp),fontSize=23.sp,fontWeight=FontWeight.Bold)
        Text("DEMO",color=Purple,fontWeight=FontWeight.Bold,fontSize=12.sp)
        Text("₹"+"%.2f".format(item.amount),Modifier.padding(top=14.dp),fontSize=30.sp,fontWeight=FontWeight.Bold)
        Text(if(success) "No actual money was transferred." else "Simulated failure. No money was transferred.",Modifier.padding(top=6.dp),fontSize=12.sp,color=Color.Gray)
        Spacer(Modifier.height(30.dp)); Card(colors=CardDefaults.cardColors(containerColor=Pale),shape=RoundedCornerShape(16.dp)){Column(Modifier.fillMaxWidth().padding(18.dp)){Detail("To",item.recipientName);Detail("UPI ID",item.recipientUpiId);Detail("Transaction ID",item.transactionId);Detail("Reference",item.referenceNumber);Detail("Date",SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(Date(item.timestamp)));Detail("Status",item.transactionStatus+" · DEMO")}}
        Spacer(Modifier.weight(1f))
        val ctx= LocalContext.current
        OutlinedButton(onClick={val share=Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"PayDemo receipt\n${item.transactionStatus} · DEMO\n₹${item.amount}\nTo: ${item.recipientName}\nID: ${item.transactionId}\nNo real money transferred.");ctx.startActivity(Intent.createChooser(share,"Share demo receipt"))},modifier=Modifier.fillMaxWidth()){Text("Share demo receipt")}
        Button(onClick={nav.navigate("main")},modifier=Modifier.fillMaxWidth().padding(top=8.dp),colors=ButtonDefaults.buttonColors(containerColor=Purple)){Text("Done")}
    }
}

@Composable private fun TransactionDetailScreen(item: TransactionEntity?, nav: androidx.navigation.NavHostController) {
    if (item == null) { BasicPage("Transaction unavailable", "Return to History and select a saved transaction.", { nav.popBackStack() }); return }
    Column(Modifier.fillMaxSize().background(Pale).padding(20.dp)) {
        TopBack("Transaction details", nav)
        Spacer(Modifier.height(20.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("₹"+"%.2f".format(item.amount), fontSize=28.sp, fontWeight=FontWeight.Bold)
                Text(item.transactionStatus+" · DEMO", color=if(item.transactionStatus=="SUCCESS") Color(0xFF168B52) else Color.Red, fontSize=12.sp)
                Divider(Modifier.padding(vertical=14.dp))
                Detail("Recipient", item.recipientName); Detail("Demo UPI ID", item.recipientUpiId)
                Detail("Transaction ID", item.transactionId); Detail("Reference", item.referenceNumber)
                Detail("Type", item.transactionType); Detail("Payment method", "PayDemo demo ledger")
                Detail("Date", SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(Date(item.timestamp)))
                Detail("Time", SimpleDateFormat("hh:mm:ss a",Locale.getDefault()).format(Date(item.timestamp)))
            }
        }
        Text("No actual payment was made. This receipt is for demonstration only.", Modifier.padding(top=16.dp), color=Color.Gray, fontSize=11.sp)
        Spacer(Modifier.weight(1f))
        val ctx = LocalContext.current
        OutlinedButton(onClick={ val body="PayDemo receipt · DEMO\n${item.transactionStatus}\n₹${item.amount} to ${item.recipientName}\nTransaction ID: ${item.transactionId}\nNo money transferred."; ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,body),"Share receipt")) }, modifier=Modifier.fillMaxWidth()) { Text("Share receipt") }
    }
}

@Composable private fun ProfileScreen(nav: androidx.navigation.NavHostController) {
    Column(Modifier.fillMaxSize().background(Pale).padding(20.dp)) {
        TopBack("Profile", nav); Spacer(Modifier.height(24.dp)); Avatar("Demo User")
        Text("Demo User", Modifier.padding(top=10.dp), fontSize=21.sp, fontWeight=FontWeight.Bold)
        Text("+91 90000 00000 · demo@demobank", color=Color.Gray, fontSize=12.sp)
        Spacer(Modifier.height(22.dp))
        listOf("Bank account · Demo Bank", "Linked payment methods", "Personal details", "Security settings", "Notifications", "Help & Support", "About PayDemo").forEach { row ->
            Row(Modifier.fillMaxWidth().clickable { if(row=="Notifications") nav.navigate("notifications") else nav.navigate("service/${row.substringBefore(" ·")}") }.padding(vertical=15.dp), verticalAlignment=Alignment.CenterVertically) { Text(row, Modifier.weight(1f), color=Ink); Icon(Icons.Default.ChevronRight, null, tint=Color.Gray) }
            Divider(color=Color(0xFFEAE5EE))
        }
        Text("Fictional profile data · Demo application", Modifier.padding(top=20.dp), color=Color.Gray, fontSize=11.sp)
    }
}

@Composable private fun BalanceScreen(nav: androidx.navigation.NavHostController) {
    Column(Modifier.fillMaxSize().background(Pale).padding(20.dp)) {
        TopBack("Check balance", nav); Spacer(Modifier.height(26.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Purple), shape=RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp)) {
                Text("DEMO BANK · SAVINGS", color=Color.White.copy(.8f), fontSize=11.sp)
                Text("••••  ••••  4821", color=Color.White, modifier=Modifier.padding(top=12.dp))
                Text("₹ 24,580.00", color=Color.White, fontSize=30.sp, fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=16.dp))
                Text("Simulated balance", color=Color.White.copy(.8f), fontSize=12.sp)
            }
        }
        Text("Last updated just now", Modifier.padding(top=18.dp), color=Color.Gray, fontSize=12.sp)
        Text("This balance is fictional. PayDemo does not connect to a bank.", Modifier.padding(top=18.dp), color=Color.Gray, fontSize=12.sp)
    }
}

@Composable private fun NotificationsScreen(notes: List<NotificationEntity>, nav: androidx.navigation.NavHostController) {
    Column(Modifier.fillMaxSize().background(Pale)) {
        TopBack("Notifications", nav)
        if(notes.isEmpty()) Text("You're all caught up.", Modifier.padding(22.dp), color=Color.Gray)
        else LazyColumn { items(notes) { item -> Card(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=6.dp), colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(14.dp)) { Column(Modifier.padding(16.dp)) { Text(item.title, fontWeight=FontWeight.SemiBold); Text(item.description, Modifier.padding(top=5.dp), color=Color.Gray, fontSize=12.sp); Text(SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp)), Modifier.padding(top=8.dp), color=Purple, fontSize=10.sp) } } } }
    }
}

@Composable private fun Detail(label:String,value:String){Row(Modifier.fillMaxWidth().padding(vertical=7.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(label,color=Color.Gray,fontSize=12.sp);Text(value,fontSize=12.sp,fontWeight=FontWeight.Medium)}}
@Composable private fun TopBack(title:String,nav:androidx.navigation.NavHostController){Row(verticalAlignment=Alignment.CenterVertically){TextButton(onClick={nav.popBackStack()}){Text("‹ Back",color=Purple)};Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun Avatar(name:String){Box(Modifier.size(66.dp).clip(CircleShape).background(Color(0xFFE9DDF5)),contentAlignment=Alignment.Center){Text(name.split(" ").take(2).mapNotNull{it.firstOrNull()}.joinToString(""),fontSize=23.sp,color=Purple,fontWeight=FontWeight.Bold)}}
@Composable private fun BasicPage(title:String,description:String,onBack:()->Unit){Column(Modifier.fillMaxSize().padding(20.dp)){TextButton(onClick=onBack){Text("‹ Back")};Text(title,fontSize=22.sp,fontWeight=FontWeight.Bold);Text(description,Modifier.padding(top=10.dp),color=Color.Gray)}}
@Composable private fun SimpleServiceScreen(modifier:Modifier,title:String,description:String,icon:androidx.compose.ui.graphics.vector.ImageVector){Column(modifier.fillMaxSize().background(Pale).padding(22.dp)){Icon(icon,null,tint=Purple,modifier=Modifier.size(40.dp));Text(title,Modifier.padding(top=16.dp),fontSize=23.sp,fontWeight=FontWeight.Bold);Text(description,Modifier.padding(top=8.dp),color=Color.Gray);Text("Demo content only",Modifier.padding(top=22.dp),color=Purple,fontSize=12.sp)}}
