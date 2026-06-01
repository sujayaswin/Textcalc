package com.example.textcalc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.textcalc.data.Document
import com.example.textcalc.data.DocumentDao
import com.example.textcalc.logic.NumberExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(private val documentDao: DocumentDao) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _currentDocumentId = MutableStateFlow<Int?>(null)
    val currentDocumentId: StateFlow<Int?> = _currentDocumentId.asStateFlow()

    val allDocuments: StateFlow<List<Document>> = documentDao.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDocument: StateFlow<Document?> = combine(allDocuments, currentDocumentId) { docs, id ->
        docs.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    val totalSum: StateFlow<Double> = _inputText
        .map { text ->
            NumberExtractor.extractAndSum(text)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    init {
        loadLastDocument()
    }

    private fun loadLastDocument() {
        viewModelScope.launch {
            // Fetch directly from DAO to avoid waiting for StateFlow initialization
            val docs = documentDao.getAllDocuments().first()
            
            if (docs.isNotEmpty()) {
                val latest = docs.first()
                _currentDocumentId.value = latest.id
                _inputText.value = latest.content
            } else {
                // Completely empty database, create the very first document
                val newDoc = Document(title = "Untitled", content = "")
                val id = documentDao.insertDocument(newDoc)
                _currentDocumentId.value = id.toInt()
                _inputText.value = ""
            }
            _isInitialized.value = true
        }
    }

    fun onTextChanged(newText: String) {
        if (!_isInitialized.value) return
        _inputText.value = newText
        saveCurrentDocument()
    }

    fun selectDocument(document: Document) {
        if (!_isInitialized.value || _currentDocumentId.value == document.id) return

        discardEmptyCurrentDocument()
        
        _currentDocumentId.value = document.id
        _inputText.value = document.content
        
        // Update lastModified when selecting a document to bring it to top next time
        viewModelScope.launch {
            val updatedDoc = document.copy(lastModified = System.currentTimeMillis())
            documentDao.updateDocument(updatedDoc)
        }
    }

    fun createNewDocument(name: String) {
        if (!_isInitialized.value) return
        discardEmptyCurrentDocument()
        
        viewModelScope.launch {
            val newDoc = Document(title = name, content = "")
            val id = documentDao.insertDocument(newDoc)
            _currentDocumentId.value = id.toInt()
            _inputText.value = ""
        }
    }

    fun renameDocument(id: Int, newName: String) {
        viewModelScope.launch {
            documentDao.updateTitle(id, newName)
        }
    }

    fun deleteCurrentDocument() {
        if (!_isInitialized.value) return
        val id = _currentDocumentId.value ?: return
        viewModelScope.launch {
            val currentDoc = documentDao.getDocumentById(id)
            if (currentDoc != null) {
                documentDao.deleteDocument(currentDoc)
                _currentDocumentId.value = null
                
                // After deletion, select the next available document
                val docs = documentDao.getAllDocuments().first()
                if (docs.isNotEmpty()) {
                    val nextDoc = docs.first()
                    _currentDocumentId.value = nextDoc.id
                    _inputText.value = nextDoc.content
                } else {
                    // Force create a new one if we deleted the last one
                    val newDoc = Document(title = "Untitled", content = "")
                    val newId = documentDao.insertDocument(newDoc)
                    _currentDocumentId.value = newId.toInt()
                    _inputText.value = ""
                }
            }
        }
    }

    private fun discardEmptyCurrentDocument() {
        val id = _currentDocumentId.value ?: return
        val content = _inputText.value
        val currentDoc = currentDocument.value ?: return

        // CONSERVATIVE DISCARD CRITERIA:
        // 1. Must be "Untitled" (unnamed)
        // 2. Must be blank or whitespace-only
        // 3. Must NOT be the only document in the database
        if (currentDoc.title == "Untitled" && content.isBlank()) {
            viewModelScope.launch {
                // Double check database state
                val allDocs = documentDao.getAllDocuments().first()
                if (allDocs.size > 1) {
                    documentDao.deleteDocument(currentDoc)
                }
            }
        }
    }

    private fun saveCurrentDocument() {
        val id = _currentDocumentId.value ?: return
        val content = _inputText.value
        viewModelScope.launch {
            val currentDoc = documentDao.getDocumentById(id)
            if (currentDoc != null) {
                val updatedDoc = currentDoc.copy(
                    content = content,
                    lastModified = System.currentTimeMillis()
                )
                documentDao.updateDocument(updatedDoc)
            }
        }
    }
}

class CalculatorViewModelFactory(private val documentDao: DocumentDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(documentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
