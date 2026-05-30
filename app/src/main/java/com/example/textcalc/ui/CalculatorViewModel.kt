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
        viewModelScope.launch {
            allDocuments.collect { docs ->
                if (_currentDocumentId.value == null && docs.isNotEmpty()) {
                    selectDocument(docs.first())
                } else if (docs.isEmpty()) {
                    createNewDocument("Untitled")
                }
            }
        }
    }

    fun onTextChanged(newText: String) {
        _inputText.value = newText
        saveCurrentDocument()
    }

    fun selectDocument(document: Document) {
        discardEmptyCurrentDocument()
        _currentDocumentId.value = document.id
        _inputText.value = document.content
    }

    fun createNewDocument(name: String) {
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
        val id = _currentDocumentId.value ?: return
        viewModelScope.launch {
            val currentDoc = documentDao.getDocumentById(id)
            if (currentDoc != null) {
                documentDao.deleteDocument(currentDoc)
                _currentDocumentId.value = null
            }
        }
    }

    private fun discardEmptyCurrentDocument() {
        val id = _currentDocumentId.value ?: return
        val content = _inputText.value
        if (content.isBlank()) {
            viewModelScope.launch {
                val currentDoc = documentDao.getDocumentById(id)
                // Only delete if it's not the only document
                val allDocs = allDocuments.value
                if (currentDoc != null && allDocs.size > 1) {
                    documentDao.deleteDocument(currentDoc)
                }
            }
        }
    }

    private fun saveCurrentDocument() {
        val id = _currentDocumentId.value ?: return
        viewModelScope.launch {
            val currentDoc = documentDao.getDocumentById(id)
            if (currentDoc != null) {
                val updatedDoc = currentDoc.copy(
                    content = _inputText.value,
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
