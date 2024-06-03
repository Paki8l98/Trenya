package space.khay.trenya

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDetailsActivity : AppCompatActivity() {
    private var weightEditText: EditText? = null
    private var heightEditText: EditText? = null
    private var genderTextView: TextView? = null
    private var wristCircumferenceEditText: EditText? = null
    private var levelSpinner: Spinner? = null
    private var muscleGroupSpinner: Spinner? = null
    private lateinit var trainingGoalSpinner: Spinner
    private lateinit var genderSpinner: Spinner
    private var vozrastEditText: EditText? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        mAuth = FirebaseAuth.getInstance()

        genderTextView = findViewById(R.id.genderTextView)
        weightEditText = findViewById(R.id.weightEditText)
        heightEditText = findViewById(R.id.heightEditText)
        wristCircumferenceEditText = findViewById(R.id.wristCircumferenceEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        vozrastEditText = findViewById(R.id.vozrastEditText)
        val trainingGoalTextView: TextView = findViewById(R.id.trainingGoalTextView)

        //   val emailEditText = findViewById<EditText>(R.id.EmailEditText)
        //  val passwordEditText = findViewById<EditText>(R.id.passwordEditText)


        trainingGoalSpinner = findViewById(R.id.trainingGoalSpinner)
        val trainingGoals = listOf(
            "Наращивание мышечной массы",
            "Для придания рельефа мышцам",
            "Для сжигания жира",
            "Для выносливости"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainingGoals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        trainingGoalSpinner.adapter = adapter

        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener { saveUserDataAndNavigate() }

//        val registerButton = findViewById<Button>(R.id.registerButton)
//        registerButton.setOnClickListener {
//            val email = emailEditText.text.toString()
//            val password = passwordEditText.text.toString()
//            registerWithEmail(email, password)
//        }
//
//        val authenticateButton = findViewById<Button>(R.id.authenticateButton)
//        authenticateButton.setOnClickListener {
//            val email = emailEditText.text.toString()
//            val password = passwordEditText.text.toString()
//            authenticateWithEmail(email, password)
//        }
//
//        val loginWithEmailButton = findViewById<Button>(R.id.loginWithEmailButton)
//        loginWithEmailButton.setOnClickListener {
//            val email = emailEditText.text.toString()
//            loginWithEmail()
//        }

        restoreSavedData()
    }

    private fun registerWithEmail(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Регистрация успешна
                    val user = mAuth.currentUser
                    // Сохранение данных пользователя в Firebase Realtime Database
                    saveUserDataToDatabase(user?.email)
                } else {
                    // Регистрация не удалась
                }
            }
    }

    private fun saveUserDataToDatabase(email: String?) {
        if (email != null) {
            val userId = mAuth.currentUser?.uid
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(email.replace(".", ","))
            val weight = weightEditText?.text.toString()
            val height = heightEditText?.text.toString()
            val vozrast = vozrastEditText?.text.toString()
            val wristCircumference = wristCircumferenceEditText?.text.toString()
            val selectedTrainingGoal = trainingGoalSpinner?.selectedItem.toString()
            val selectedGender = genderSpinner?.selectedItem.toString()

            userRef.setValue(
                mapOf(
                    "weight" to weight,
                    "height" to height,
                    "vozrast" to vozrast,
                    "wristCircumference" to wristCircumference,
                    "trainingGoal" to selectedTrainingGoal,
                    "gender" to selectedGender
                )
            )
        }
    }


    private fun authenticateWithEmail(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Аутентификация успешна
                    val user = mAuth.currentUser
                } else {
                    // Аутентификация не удалась
                }
            }
    }

    private fun loginWithEmail() {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emails = mutableListOf<String>()
                for (userSnapshot in snapshot.children) {
                    val email = userSnapshot.key?.replace(",", ".")
                    email?.let { emails.add(it) }
                }

                // Отображение диалога для выбора почты
                val builder = AlertDialog.Builder(this@UserDetailsActivity)
                builder.setTitle("Выберите почту")
                builder.setItems(emails.toTypedArray()) { dialog, which ->
                    val selectedEmail = emails[which]
                    // Переход на активность с данными выбранной почты
                    navigateToUserDetails(selectedEmail)
                }
                val dialog = builder.create()
                dialog.show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserDetailsActivity, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToUserDetails(email: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(email.replace(".", ","))

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.value as? Map<*, *>
                if (userData != null) {
                    val weight = userData["weight"].toString()
                    val height = userData["height"].toString()
                    val vozrast = userData["vozrast"].toString()
                    val wristCircumference = userData["wristCircumference"].toString()
                    val selectedTrainingGoal = userData["trainingGoal"].toString()
                    val selectedGender = userData["gender"].toString()

                    val intent = Intent(this@UserDetailsActivity, PlanDetailsActivity::class.java)
                    intent.putExtra("weight", weight)
                    intent.putExtra("height", height)
                    intent.putExtra("vozrast", vozrast)
                    intent.putExtra("wristCircumference", wristCircumference)
                    intent.putExtra("trainingGoal", selectedTrainingGoal)
                    intent.putExtra("gender", selectedGender)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserDetailsActivity, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
            }
        })
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Успешная аутентификация, получение данных из Firebase Realtime Database
                    val currentUser = mAuth.currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        val database = FirebaseDatabase.getInstance()
                        val userRef = database.getReference("users").child(userId)

                        userRef.get().addOnSuccessListener { dataSnapshot ->
                            val userData = dataSnapshot.value as? Map<*, *>
                            if (userData != null) {
                                // Заполнение элементов интерфейса данными из Firebase Realtime Database
                                weightEditText?.setText(userData["weight"].toString())
                                heightEditText?.setText(userData["height"].toString())
                                vozrastEditText?.setText(userData["vozrast"].toString())
                                wristCircumferenceEditText?.setText(userData["wristCircumference"].toString())
                            }
                        }
                        Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Регистрация не удалась", Toast.LENGTH_LONG).show()
                }
            }
    }






    private fun saveUserDataAndNavigate() {
        // Получение данных из элементов пользовательского интерфейса
        val weight = weightEditText?.text.toString()
        val height = heightEditText?.text.toString()
        val vozrast = vozrastEditText?.text.toString()
        val wristCircumference = wristCircumferenceEditText?.text.toString()
        val selectedLevel = levelSpinner?.selectedItem.toString()
        val selectedMuscleGroup = muscleGroupSpinner?.selectedItem.toString()
        val selectedTrainingGoal = trainingGoalSpinner?.selectedItem.toString()
        val selectedGender = genderSpinner?.selectedItem.toString()

        // Сохранение данных в SharedPreferences
        val preferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("weight", weight)
        editor.putString("height", height)
        editor.putString("vozrast", vozrast)
        editor.putString("wristCircumference", wristCircumference)
        editor.putString("level", selectedLevel)
        editor.putString("muscleGroup", selectedMuscleGroup)
        editor.putString("trainingGoal", selectedTrainingGoal)
        editor.putString("gender", selectedGender)
        editor.apply()

        // Передача данных в PlanDetailsActivity
        val intent1 = Intent(this, PlanDetailsActivity::class.java)
        intent1.putExtra("weight", weight)
        intent1.putExtra("height", height)
        intent1.putExtra("vozrast", vozrast)
        intent1.putExtra("wristCircumference", wristCircumference)
        intent1.putExtra("level", selectedLevel)
        intent1.putExtra("muscleGroup", selectedMuscleGroup)
        intent1.putExtra("trainingGoal", selectedTrainingGoal)
        intent1.putExtra("gender", selectedGender)
        startActivity(intent1)

        // Передача данных в ExerciseActivity
        val intent = Intent(this, ExerciseActivity::class.java)
        intent.putExtra("trainingGoal", selectedTrainingGoal)
        startActivity(intent)
    }


    private fun restoreSavedData() {
        // Восстановление сохраненных данных из SharedPreferences
        val preferences = getSharedPreferences("user_data", MODE_PRIVATE)
        weightEditText?.setText(preferences.getString("weight", ""))
        heightEditText?.setText(preferences.getString("height", ""))
        vozrastEditText?.setText(preferences.getString("vozrast", ""))
        wristCircumferenceEditText?.setText(preferences.getString("wristCircumference", ""))
        levelSpinner?.setSelection(getIndex(levelSpinner, preferences.getString("level", "")))
        muscleGroupSpinner?.setSelection(getIndex(muscleGroupSpinner, preferences.getString("muscleGroup", "")))
        trainingGoalSpinner?.setSelection(getIndex(trainingGoalSpinner, preferences.getString("trainingGoal", "")))
        genderSpinner?.setSelection(getIndex(genderSpinner, preferences.getString("gender", "")))
    }

    private fun getIndex(spinner: Spinner?, value: String?): Int {
        for (i in 0 until spinner!!.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
    override fun onResume() {
        super.onResume()
        restoreSavedData()
    }

    companion object {
        private const val RC_SIGN_IN = 10100
        private const val REQUEST_CODE_EXERCISE = 1002
    }
}
