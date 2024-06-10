package za.varsitycollege.kronosapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CategoryManager {
    private val categories = mutableStateListOf<Category>()
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun addCategory(categoryName: String) {
        if (categoryName.isNotBlank() && categories.none { it.name == categoryName }) {
            val category = Category(name = categoryName)
            categories.add(category)
            saveCategoryToFirestore(category)
        }
    }

    private fun saveCategoryToFirestore(category: Category) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("categories")
            .add(category)
            .addOnSuccessListener {
                // Successfully saved category
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

    fun getCategories(): List<Category> {
        return categories
    }

    fun fetchCategories(onFetchComplete: (Boolean, Exception?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFetchComplete(false, Exception("User not logged in"))
            return
        }
        firestore.collection("users").document(userId).collection("categories")
            .get()
            .addOnSuccessListener { documents ->
                categories.clear()
                for (document in documents) {
                    val category = document.toObject(Category::class.java)
                    categories.add(category)
                }
                onFetchComplete(true, null)
            }
            .addOnFailureListener { e ->
                onFetchComplete(false, e)
            }
    }
}
