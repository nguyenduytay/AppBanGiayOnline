    package com.midterm22nh12.appbangiayonline.Service

    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.FirebaseUser
    import com.google.firebase.database.FirebaseDatabase

    class AuthService {
        private val auth = FirebaseAuth.getInstance()
        private val database = FirebaseDatabase.getInstance()

        interface AuthCallback {
            fun onSuccess(currentUser: FirebaseUser?)
            fun onFailure(errorMessage: String)
        }
        // đăng ký tài khoản
        fun registerUser(email : String, password : String, callback : AuthCallback){
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback.onSuccess(auth.currentUser)
                    } else {
                        callback.onFailure( "Đăng ký thất bại")
                    }
                }
        }

        // Đăng nhập với email
         fun loginUser(email: String, password: String, callback: AuthCallback) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    callback.onSuccess(authResult.user)
                }
                .addOnFailureListener { _ ->
                    callback.onFailure( "Đăng nhập thất bại")
                }
        }
        //đăng xuất
        fun logoutUser(){
            auth.signOut()
        }

        // kiểm tra người dùng đã đăng nhập chưa
        fun getCurrentUser() : FirebaseUser?
        {
            return auth.currentUser
        }
    }