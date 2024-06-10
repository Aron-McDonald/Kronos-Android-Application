package za.varsitycollege.kronosapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProjectManager {
    private val projects = mutableStateListOf<Project>()
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun addProject(project: Project) {
        if (project.projectName.isNotBlank() && projects.none { it.projectName == project.projectName }) {
            projects.add(project)
            saveProjectToFirestore(project)
        }
    }

    private fun saveProjectToFirestore(project: Project) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("projects")
            .add(project)
            .addOnSuccessListener {
                // Successfully saved project
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

    fun getProjects(): List<Project> {
        return projects
    }

    fun fetchProjects(onFetchComplete: (Boolean, Exception?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFetchComplete(false, Exception("User not logged in"))
            return
        }
        firestore.collection("users").document(userId).collection("projects")
            .get()
            .addOnSuccessListener { documents ->
                projects.clear()
                for (document in documents) {
                    val project = document.toObject(Project::class.java)
                    projects.add(project)
                }
                onFetchComplete(true, null)
            }
            .addOnFailureListener { e ->
                onFetchComplete(false, e)
            }
    }
}
