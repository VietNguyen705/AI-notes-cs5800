const API_BASE = 'http://localhost:8000/api';

let currentUser = null;
let currentFilter = 'all';
let editingNoteId = null;
let quillEditor = null; // Single Quill instance for the modal
let currentNoteColor = '#FFFFFF';
let currentNotePinned = false;

// ==================== DARK MODE ====================
function initDarkMode() {
    // Force dark mode as default
    document.documentElement.classList.add('dark');
}

// ==================== TOAST NOTIFICATIONS ====================
function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toastContainer');
    const toast = document.createElement('div');

    const colors = {
        success: 'bg-green-500',
        error: 'bg-red-500',
        warning: 'bg-yellow-500',
        info: 'bg-blue-500'
    };

    const icons = {
        success: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>`,
        error: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>`,
        warning: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>`,
        info: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>`
    };

    toast.className = `${colors[type]} text-white px-6 py-4 rounded-lg shadow-lg flex items-center space-x-3 transform transition-all duration-300 translate-x-0 opacity-100`;
    toast.innerHTML = `
        ${icons[type]}
        <span class="font-medium">${message}</span>
    `;

    toastContainer.appendChild(toast);

    // Auto remove after 3 seconds
    setTimeout(() => {
        toast.classList.add('translate-x-full', 'opacity-0');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ==================== USER MANAGEMENT ====================
async function registerUser() {
    const username = document.getElementById('usernameInput').value.trim();
    const email = document.getElementById('emailInput').value.trim();

    if (!username || !email) {
        showToast('Please enter both username and email', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/users/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email })
        });

        if (response.ok) {
            const user = await response.json();
            setCurrentUser(user);
            showToast('Registration successful!', 'success');
        } else if (response.status === 409) {
            showToast('Username or email already exists', 'error');
        } else {
            showToast('Registration failed', 'error');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showToast('Registration failed', 'error');
    }
}

async function loginUser() {
    const username = document.getElementById('usernameInput').value.trim();

    if (!username) {
        showToast('Please enter username', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/users/username/${username}`);

        if (response.ok) {
            const user = await response.json();
            setCurrentUser(user);
            showToast(`Welcome back, ${user.username}!`, 'success');
        } else {
            showToast('User not found', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showToast('Login failed', 'error');
    }
}

function logoutUser() {
    currentUser = null;
    localStorage.removeItem('currentUser');
    document.getElementById('username').textContent = 'Guest';
    document.getElementById('loginSection').style.display = 'block';
    document.getElementById('notesGrid').innerHTML = '<p class="text-gray-500 dark:text-gray-400 text-center col-span-full py-12">Please login to view notes</p>';
    showToast('Logged out successfully', 'info');
}

function setCurrentUser(user) {
    currentUser = user;
    localStorage.setItem('currentUser', JSON.stringify(user));
    document.getElementById('username').textContent = user.username;
    document.getElementById('loginSection').style.display = 'none';
    document.getElementById('usernameInput').value = '';
    document.getElementById('emailInput').value = '';
    loadCategoryFilter();
    loadNotes();
    loadSidebarTasks();
}

function loadUserFromStorage() {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
        currentUser = JSON.parse(stored);
        document.getElementById('username').textContent = currentUser.username;
        document.getElementById('loginSection').style.display = 'none';
        loadCategoryFilter();
        loadNotes();
        loadSidebarTasks();
    }
}

async function loadCategoryFilter() {
    if (!currentUser) return;

    try {
        const response = await fetch(`${API_BASE}/categories?userId=${currentUser.userId}`);
        const categories = await response.json();

        const filterSelect = document.getElementById('categoryFilter');
        filterSelect.innerHTML = '<option value="">Select Category...</option>';

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.name;
            option.textContent = category.name;
            filterSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading categories for filter:', error);
    }
}

// ==================== NOTES MANAGEMENT ====================
async function loadNotes() {
    if (!currentUser) {
        document.getElementById('notesGrid').innerHTML = '<p class="text-gray-500 dark:text-gray-400 text-center col-span-full py-12">Please login to view notes</p>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/notes?userId=${currentUser.userId}`);
        let notes = await response.json();

        // Apply filter
        if (currentFilter === 'pinned') {
            notes = notes.filter(note => note.isPinned);
        } else if (currentFilter !== 'all' && currentFilter) {
            notes = notes.filter(note => note.category === currentFilter);
        }

        if (notes.length === 0) {
            document.getElementById('notesGrid').innerHTML = '<p class="text-gray-500 dark:text-gray-400 text-center col-span-full py-12">No notes yet. Click "New Note" to create one!</p>';
            return;
        }

        renderNotes(notes);
    } catch (error) {
        console.error('Error loading notes:', error);
        document.getElementById('notesGrid').innerHTML = '<p class="text-red-500 text-center col-span-full py-12">Error loading notes</p>';
    }
}

function renderNotes(notes) {
    const notesGrid = document.getElementById('notesGrid');
    notesGrid.innerHTML = '';

    notes.forEach(note => {
        const noteCard = createNoteCard(note);
        notesGrid.appendChild(noteCard);
    });
}

function createNoteCard(note) {
    const card = document.createElement('div');
    card.className = 'note-card p-5 cursor-pointer group relative';
    card.style.borderLeftColor = note.color || '#8b5cf6';
    card.style.borderLeftWidth = '4px';
    card.dataset.noteId = note.noteId;

    // Create a temporary div to extract text from HTML
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = note.body || '';
    const bodyText = tempDiv.textContent || tempDiv.innerText || '';

    card.innerHTML = `
        <div class="flex justify-between items-start mb-3">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white flex-1">${escapeHtml(note.title)}</h3>
            ${note.isPinned ? '<svg class="w-5 h-5 text-purple-500" fill="currentColor" viewBox="0 0 24 24"><path d="M16 12V4h1c.55 0 1-.45 1-1s-.45-1-1-1H7c-.55 0-1 .45-1 1s.45 1 1 1h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z"/></svg>' : ''}
        </div>
        <div class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-3">${escapeHtml(bodyText)}</div>
        ${note.category ? `<span class="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 mb-2">${escapeHtml(note.category)}</span>` : ''}
        ${note.tags && note.tags.length > 0 ? `
            <div class="flex flex-wrap gap-2 mb-3">
                ${note.tags.map(tag => `<span class="px-2 py-1 text-xs rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">${escapeHtml(tag.name)}</span>`).join('')}
            </div>
        ` : ''}
        <div class="flex gap-2 mt-4 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
            <button data-action="organize" class="btn-secondary text-xs px-3 py-1">AI Organize</button>
            <button data-action="generate-tasks" class="btn-secondary text-xs px-3 py-1">Gen Tasks</button>
        </div>
    `;

    // Add click event to the entire card to open edit modal
    card.addEventListener('click', (e) => {
        // Only open if clicking on the card itself, not buttons
        if (!e.target.closest('button')) {
            openNoteModal(note.noteId);
        }
    });

    // Add event listeners to buttons
    const organizeBtn = card.querySelector('[data-action="organize"]');
    const generateBtn = card.querySelector('[data-action="generate-tasks"]');

    organizeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        autoOrganizeNote(note.noteId);
    });

    generateBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        generateTasksFromNote(note.noteId);
    });

    return card;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Initialize Quill editor once
function initQuillEditor() {
    if (!quillEditor) {
        quillEditor = new Quill('#noteEditor', {
            theme: 'snow',
            placeholder: 'Take a note...',
            modules: {
                toolbar: [
                    ['bold', 'italic', 'underline', 'strike'],
                    ['blockquote', 'code-block'],
                    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                    [{ 'header': [1, 2, 3, false] }],
                    ['clean']
                ]
            }
        });
    }
}

async function openNoteModal(noteId = null) {
    if (!currentUser) {
        showToast('Please login first', 'warning');
        return;
    }

    editingNoteId = noteId;
    currentNoteColor = '#FFFFFF';
    currentNotePinned = false;

    const modal = document.getElementById('noteModal');
    const modalTitle = document.getElementById('noteModalTitle');
    const noteTitle = document.getElementById('noteTitle');
    const deleteBtn = document.getElementById('deleteNoteBtn');
    const pinBtn = document.getElementById('pinNoteBtn');
    const categoryDisplay = document.getElementById('noteCategoryDisplay');
    const tagsDisplay = document.getElementById('noteTagsDisplay');

    // Initialize Quill if not already done
    initQuillEditor();

    if (noteId) {
        // Edit mode
        try {
            const response = await fetch(`${API_BASE}/notes/${noteId}`);
            const note = await response.json();

            modalTitle.textContent = 'Edit Note';
            noteTitle.value = note.title;
            quillEditor.root.innerHTML = note.body || '';
            currentNoteColor = note.color || '#FFFFFF';
            currentNotePinned = note.isPinned || false;
            deleteBtn.classList.remove('hidden');

            // Update pin button
            updatePinButton();

            // Display category and tags
            if (note.category) {
                categoryDisplay.innerHTML = `<span class="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">${escapeHtml(note.category)}</span>`;
            } else {
                categoryDisplay.innerHTML = '';
            }

            if (note.tags && note.tags.length > 0) {
                tagsDisplay.innerHTML = `<div class="flex flex-wrap gap-2">${note.tags.map(tag => `<span class="px-2 py-1 text-xs rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">${escapeHtml(tag.name)}</span>`).join('')}</div>`;
            } else {
                tagsDisplay.innerHTML = '';
            }
        } catch (error) {
            console.error('Error loading note:', error);
            showToast('Error loading note', 'error');
            return;
        }
    } else {
        // Create mode
        modalTitle.textContent = 'Create Note';
        noteTitle.value = '';
        quillEditor.root.innerHTML = '';
        deleteBtn.classList.add('hidden');
        categoryDisplay.innerHTML = '';
        tagsDisplay.innerHTML = '';
        updatePinButton();
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
    noteTitle.focus();
}

function updatePinButton() {
    const pinBtn = document.getElementById('pinNoteBtn');
    const pinIcon = pinBtn.querySelector('svg');

    if (currentNotePinned) {
        pinIcon.classList.remove('text-gray-400');
        pinIcon.classList.add('text-purple-500');
    } else {
        pinIcon.classList.remove('text-purple-500');
        pinIcon.classList.add('text-gray-400');
    }
}

async function saveNote() {
    const title = document.getElementById('noteTitle').value.trim();
    const body = quillEditor.root.innerHTML;

    if (!title) {
        showToast('Title is required', 'warning');
        return;
    }

    const noteData = {
        userId: currentUser.userId,
        title,
        body,
        color: currentNoteColor,
        isPinned: currentNotePinned
    };

    try {
        if (editingNoteId) {
            // Update existing note
            await fetch(`${API_BASE}/notes/${editingNoteId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(noteData)
            });
            showToast('Note updated successfully!', 'success');
        } else {
            // Create new note
            await fetch(`${API_BASE}/notes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(noteData)
            });
            showToast('Note created successfully!', 'success');
        }

        closeNoteModal();
        loadNotes();
    } catch (error) {
        console.error('Error saving note:', error);
        showToast('Error saving note', 'error');
    }
}

function closeNoteModal() {
    const modal = document.getElementById('noteModal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    editingNoteId = null;
}

async function togglePinInModal() {
    currentNotePinned = !currentNotePinned;
    updatePinButton();
}

async function deleteNote() {
    if (!editingNoteId) return;
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
        await fetch(`${API_BASE}/notes/${editingNoteId}`, {
            method: 'DELETE'
        });

        showToast('Note deleted successfully!', 'success');
        closeNoteModal();
        loadNotes();
    } catch (error) {
        console.error('Error deleting note:', error);
        showToast('Error deleting note', 'error');
    }
}

function selectNoteColor(color) {
    currentNoteColor = color;
    // Optional: provide visual feedback
    document.querySelectorAll('.note-color-btn').forEach(btn => {
        if (btn.dataset.color === color) {
            btn.style.borderColor = '#8b5cf6';
            btn.style.borderWidth = '3px';
        } else {
            btn.style.borderColor = '#d1d5db';
            btn.style.borderWidth = '2px';
        }
    });
}

async function autoOrganizeNote(noteId) {
    // Use the noteId if called from card, or editingNoteId if called from modal
    const targetNoteId = noteId || editingNoteId;

    if (!targetNoteId) {
        showToast('Please save the note first', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/notes/${targetNoteId}/auto-organize`, {
            method: 'POST'
        });

        if (response.ok) {
            showToast('Note organized with AI tags!', 'success');

            // If modal is open, reload the note data
            if (editingNoteId) {
                const noteResponse = await fetch(`${API_BASE}/notes/${targetNoteId}`);
                const note = await noteResponse.json();

                const categoryDisplay = document.getElementById('noteCategoryDisplay');
                const tagsDisplay = document.getElementById('noteTagsDisplay');

                if (note.category) {
                    categoryDisplay.innerHTML = `<span class="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">${escapeHtml(note.category)}</span>`;
                }

                if (note.tags && note.tags.length > 0) {
                    tagsDisplay.innerHTML = `<div class="flex flex-wrap gap-2">${note.tags.map(tag => `<span class="px-2 py-1 text-xs rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">${escapeHtml(tag.name)}</span>`).join('')}</div>`;
                }
            }

            loadNotes();
        } else {
            showToast('Error organizing note', 'error');
        }
    } catch (error) {
        console.error('Error auto-organizing note:', error);
        showToast('Error organizing note', 'error');
    }
}

async function generateTasksFromNote(noteId) {
    if (!currentUser) {
        showToast('Please login first', 'warning');
        return;
    }

    // Use the noteId if called from card, or editingNoteId if called from modal
    const targetNoteId = noteId || editingNoteId;

    if (!targetNoteId) {
        showToast('Please save the note first', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/todos/generate/${targetNoteId}?userId=${currentUser.userId}`, {
            method: 'POST'
        });

        if (response.ok) {
            const tasks = await response.json();
            showToast(`Generated ${tasks.length} tasks!`, 'success');
            loadSidebarTasks(); // Refresh sidebar tasks
        } else {
            showToast('Error generating tasks', 'error');
        }
    } catch (error) {
        console.error('Error generating tasks:', error);
        showToast('Error generating tasks', 'error');
    }
}

// Search with debouncing
let searchTimeout;
function searchNotes() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(async () => {
        if (!currentUser) return;

        const query = document.getElementById('searchInput').value.trim();

        if (!query) {
            loadNotes();
            return;
        }

        try {
            const response = await fetch(`${API_BASE}/notes/search?userId=${currentUser.userId}&query=${encodeURIComponent(query)}`);
            const notes = await response.json();

            if (notes.length === 0) {
                document.getElementById('notesGrid').innerHTML = '<p class="text-gray-500 dark:text-gray-400 text-center col-span-full py-12">No notes found</p>';
                return;
            }

            renderNotes(notes);
        } catch (error) {
            console.error('Error searching notes:', error);
        }
    }, 300);
}

// ==================== TASKS MANAGEMENT ====================
async function loadSidebarTasks() {
    if (!currentUser) {
        document.getElementById('sidebarTasksList').innerHTML = '<p class="text-xs text-gray-500 dark:text-gray-400 text-center py-4">Login to view tasks</p>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/todos?userId=${currentUser.userId}`);
        const allTasks = await response.json();

        // Show only pending and in-progress tasks, limit to 5
        const activeTasks = allTasks
            .filter(task => task.status === 'PENDING' || task.status === 'IN_PROGRESS')
            .slice(0, 5);

        const sidebarList = document.getElementById('sidebarTasksList');

        if (activeTasks.length === 0) {
            sidebarList.innerHTML = '<p class="text-xs text-gray-500 dark:text-gray-400 text-center py-4">No active tasks</p>';
            return;
        }

        sidebarList.innerHTML = activeTasks.map(task => `
            <div class="flex items-start gap-2 p-2 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-all duration-200 cursor-pointer" onclick="openTaskEditModal('${task.taskId}')">
                <input type="checkbox" ${task.status === 'COMPLETED' ? 'checked' : ''}
                    onchange="event.stopPropagation(); toggleTaskQuick('${task.taskId}')"
                    class="w-4 h-4 mt-0.5 rounded border-gray-300 text-purple-600 focus:ring-purple-500 cursor-pointer">
                <div class="flex-1 min-w-0">
                    <div class="text-xs font-medium text-gray-900 dark:text-white truncate">${escapeHtml(task.title)}</div>
                    ${task.priority !== 'LOW' ? `<span class="inline-block px-1.5 py-0.5 rounded text-xs font-semibold mt-1 ${getPriorityClass(task.priority)}">${task.priority}</span>` : ''}
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading sidebar tasks:', error);
    }
}

async function toggleTaskQuick(taskId) {
    try {
        await fetch(`${API_BASE}/todos/${taskId}/complete`, {
            method: 'PUT'
        });
        loadSidebarTasks();
        // Also reload modal tasks if it's open
        const tasksModal = document.getElementById('tasksModal');
        if (!tasksModal.classList.contains('hidden')) {
            loadTasks();
        }
    } catch (error) {
        console.error('Error toggling task:', error);
        showToast('Error updating task', 'error');
    }
}

window.toggleTaskQuick = toggleTaskQuick;

async function openTasksModal() {
    if (!currentUser) {
        showToast('Please login first', 'warning');
        return;
    }

    document.getElementById('tasksModal').classList.remove('hidden');
    document.getElementById('tasksModal').classList.add('flex');
    loadTasks();
}

async function loadTasks(status = 'all') {
    if (!currentUser) return;

    try {
        let url = `${API_BASE}/todos?userId=${currentUser.userId}`;
        if (status !== 'all') {
            url = `${API_BASE}/todos/status/${status}?userId=${currentUser.userId}`;
        }

        const response = await fetch(url);
        const tasks = await response.json();

        const tasksList = document.getElementById('tasksList');

        if (tasks.length === 0) {
            tasksList.innerHTML = '<p class="text-gray-500 dark:text-gray-400 text-center py-8">No tasks found</p>';
            return;
        }

        tasksList.innerHTML = tasks.map(task => `
            <div class="flex items-center gap-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-all duration-200 ${task.status === 'COMPLETED' ? 'opacity-60' : ''} cursor-pointer" onclick="openTaskEditModal('${task.taskId}')">
                <input type="checkbox" ${task.status === 'COMPLETED' ? 'checked' : ''}
                    onchange="event.stopPropagation(); toggleTask('${task.taskId}')"
                    class="w-5 h-5 rounded border-gray-300 text-purple-600 focus:ring-purple-500">
                <div class="flex-1">
                    <div class="font-medium text-gray-900 dark:text-white ${task.status === 'COMPLETED' ? 'line-through' : ''}">${escapeHtml(task.title)}</div>
                    <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">
                        <span class="inline-block px-2 py-1 rounded text-xs font-semibold ${getPriorityClass(task.priority)}">${task.priority}</span>
                        ${task.dueDate ? `<span class="ml-2">Due: ${new Date(task.dueDate).toLocaleDateString()}</span>` : ''}
                    </div>
                </div>
                <button onclick="event.stopPropagation(); deleteTask('${task.taskId}', event)" class="p-2 hover:bg-red-100 dark:hover:bg-red-900/30 rounded-lg transition-all duration-200">
                    <svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                    </svg>
                </button>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading tasks:', error);
    }
}

function getPriorityClass(priority) {
    const classes = {
        'LOW': 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400',
        'MEDIUM': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400',
        'HIGH': 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-400',
        'URGENT': 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
    };
    return classes[priority] || classes['LOW'];
}

async function toggleTask(taskId) {
    try {
        await fetch(`${API_BASE}/todos/${taskId}/complete`, {
            method: 'PUT'
        });
        loadTasks();
    } catch (error) {
        console.error('Error toggling task:', error);
        showToast('Error updating task', 'error');
    }
}

window.toggleTask = toggleTask;

async function deleteTask(taskId, event) {
    event.stopPropagation();
    if (!confirm('Are you sure you want to delete this task?')) return;

    try {
        await fetch(`${API_BASE}/todos/${taskId}`, {
            method: 'DELETE'
        });
        showToast('Task deleted!', 'success');
        loadTasks();
        loadSidebarTasks(); // Refresh sidebar
    } catch (error) {
        console.error('Error deleting task:', error);
        showToast('Error deleting task', 'error');
    }
}

window.deleteTask = deleteTask;

// ==================== TASK EDITING ====================
let editingTaskId = null;

async function openTaskEditModal(taskId) {
    editingTaskId = taskId;

    try {
        const response = await fetch(`${API_BASE}/todos/${taskId}`);
        const task = await response.json();

        document.getElementById('editTaskTitle').value = task.title;
        document.getElementById('editTaskPriority').value = task.priority;
        document.getElementById('editTaskStatus').value = task.status;

        // Format due date for datetime-local input
        if (task.dueDate) {
            const date = new Date(task.dueDate);
            const localDateTime = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
                .toISOString()
                .slice(0, 16);
            document.getElementById('editTaskDueDate').value = localDateTime;
        } else {
            document.getElementById('editTaskDueDate').value = '';
        }

        const modal = document.getElementById('taskEditModal');
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    } catch (error) {
        console.error('Error loading task:', error);
        showToast('Error loading task', 'error');
    }
}

window.openTaskEditModal = openTaskEditModal;

async function saveTaskEdit() {
    if (!editingTaskId) return;

    const title = document.getElementById('editTaskTitle').value.trim();
    const priority = document.getElementById('editTaskPriority').value;
    const status = document.getElementById('editTaskStatus').value;
    const dueDateValue = document.getElementById('editTaskDueDate').value;

    if (!title) {
        showToast('Title is required', 'warning');
        return;
    }

    const taskData = {
        userId: currentUser.userId,
        title,
        priority,
        status
    };

    // Only add dueDate if it's set
    if (dueDateValue) {
        taskData.dueDate = new Date(dueDateValue).toISOString();
    }

    try {
        await fetch(`${API_BASE}/todos/${editingTaskId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });

        showToast('Task updated successfully!', 'success');
        closeTaskEditModal();
        loadSidebarTasks();

        // Reload tasks modal if it's open
        const tasksModal = document.getElementById('tasksModal');
        if (!tasksModal.classList.contains('hidden')) {
            loadTasks();
        }
    } catch (error) {
        console.error('Error updating task:', error);
        showToast('Error updating task', 'error');
    }
}

async function deleteTaskFromEdit() {
    if (!editingTaskId) return;
    if (!confirm('Are you sure you want to delete this task?')) return;

    try {
        await fetch(`${API_BASE}/todos/${editingTaskId}`, {
            method: 'DELETE'
        });

        showToast('Task deleted!', 'success');
        closeTaskEditModal();
        loadSidebarTasks();

        // Reload tasks modal if it's open
        const tasksModal = document.getElementById('tasksModal');
        if (!tasksModal.classList.contains('hidden')) {
            loadTasks();
        }
    } catch (error) {
        console.error('Error deleting task:', error);
        showToast('Error deleting task', 'error');
    }
}

function closeTaskEditModal() {
    const modal = document.getElementById('taskEditModal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    editingTaskId = null;
}

// ==================== CATEGORIES MANAGEMENT ====================
let currentCategoryColor = '#667eea';

async function openCategoriesModal() {
    if (!currentUser) {
        showToast('Please login first', 'warning');
        return;
    }

    document.getElementById('categoriesModal').classList.remove('hidden');
    document.getElementById('categoriesModal').classList.add('flex');
    currentCategoryColor = '#667eea';
    document.getElementById('categoryName').value = '';
    document.getElementById('categoryDescription').value = '';
    loadCategories();
}

async function loadCategories() {
    if (!currentUser) return;

    try {
        const response = await fetch(`${API_BASE}/categories?userId=${currentUser.userId}`);
        const categories = await response.json();

        const categoriesList = document.getElementById('categoriesList');

        if (categories.length === 0) {
            categoriesList.innerHTML = '<p class="text-gray-500 dark:text-gray-400 text-center py-8">No categories yet. Create one above!</p>';
            return;
        }

        categoriesList.innerHTML = categories.map(category => `
            <div class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg" style="border-left: 4px solid ${category.color}">
                <div>
                    <div class="font-semibold text-gray-900 dark:text-white">${escapeHtml(category.name)}</div>
                    ${category.description ? `<div class="text-sm text-gray-500 dark:text-gray-400 mt-1">${escapeHtml(category.description)}</div>` : ''}
                </div>
                <button onclick="deleteCategory('${category.categoryId}')" class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-lg text-sm">Delete</button>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

async function createCategory(e) {
    e.preventDefault();

    if (!currentUser) {
        showToast('Please login first', 'warning');
        return;
    }

    const name = document.getElementById('categoryName').value.trim();
    const description = document.getElementById('categoryDescription').value.trim();

    if (!name) {
        showToast('Category name is required', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/categories`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.userId,
                name,
                description,
                color: currentCategoryColor
            })
        });

        if (response.status === 409) {
            showToast('Category already exists', 'warning');
        } else if (response.ok) {
            showToast('Category created!', 'success');
            document.getElementById('categoryName').value = '';
            document.getElementById('categoryDescription').value = '';
            currentCategoryColor = '#667eea';
            loadCategories();
            loadCategoryFilter();
        }
    } catch (error) {
        console.error('Error creating category:', error);
        showToast('Error creating category', 'error');
    }
}

async function deleteCategory(categoryId) {
    if (!confirm('Are you sure you want to delete this category?')) return;

    try {
        await fetch(`${API_BASE}/categories/${categoryId}`, {
            method: 'DELETE'
        });
        showToast('Category deleted!', 'success');
        loadCategories();
        loadCategoryFilter();
    } catch (error) {
        console.error('Error deleting category:', error);
        showToast('Error deleting category', 'error');
    }
}

window.deleteCategory = deleteCategory;

// ==================== EVENT LISTENERS ====================
document.addEventListener('DOMContentLoaded', () => {
    initDarkMode();
    loadUserFromStorage();

    // User actions
    document.getElementById('registerBtn').addEventListener('click', registerUser);
    document.getElementById('loginBtn').addEventListener('click', loginUser);
    document.getElementById('logoutBtn').addEventListener('click', logoutUser);

    // Note actions
    document.getElementById('createNoteBtn').addEventListener('click', () => openNoteModal());
    document.getElementById('searchInput').addEventListener('input', searchNotes);
    document.getElementById('saveNoteBtn').addEventListener('click', saveNote);
    document.getElementById('deleteNoteBtn').addEventListener('click', deleteNote);
    document.getElementById('pinNoteBtn').addEventListener('click', togglePinInModal);
    document.getElementById('autoOrganizeModalBtn').addEventListener('click', () => autoOrganizeNote());
    document.getElementById('generateTasksModalBtn').addEventListener('click', () => generateTasksFromNote());

    // Modal actions
    document.getElementById('viewAllTasksBtn').addEventListener('click', openTasksModal);
    document.getElementById('manageCategoriesBtn').addEventListener('click', openCategoriesModal);

    // Task edit modal
    document.getElementById('saveTaskBtn').addEventListener('click', saveTaskEdit);
    document.getElementById('deleteTaskEditBtn').addEventListener('click', deleteTaskFromEdit);

    // Category form
    document.getElementById('categoryForm').addEventListener('submit', createCategory);

    // Filter buttons
    document.querySelectorAll('.sidebar-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('.sidebar-btn').forEach(b => b.classList.remove('active'));
            e.currentTarget.classList.add('active');
            currentFilter = e.currentTarget.dataset.filter;
            document.getElementById('categoryFilter').value = '';
            loadNotes();
        });
    });

    // Category filter
    document.getElementById('categoryFilter').addEventListener('change', (e) => {
        document.querySelectorAll('.sidebar-btn').forEach(b => b.classList.remove('active'));
        currentFilter = e.target.value || 'all';
        loadNotes();
    });

    // Category color picker
    document.querySelectorAll('.category-color-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            currentCategoryColor = e.currentTarget.dataset.color;
            document.querySelectorAll('.category-color-btn').forEach(b => {
                b.style.borderColor = '#d1d5db';
                b.style.borderWidth = '2px';
            });
            e.currentTarget.style.borderColor = '#000';
            e.currentTarget.style.borderWidth = '3px';
        });
    });

    // Task filter buttons
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('task-filter-btn')) {
            document.querySelectorAll('.task-filter-btn').forEach(btn => {
                btn.className = 'task-filter-btn px-4 py-2 rounded-lg font-medium transition-all duration-200 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300';
            });
            e.target.className = 'task-filter-btn px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400';
            loadTasks(e.target.dataset.status);
        }
    });

    // Note color picker
    document.querySelectorAll('.note-color-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            selectNoteColor(e.currentTarget.dataset.color);
        });
    });

    // Close modals
    document.querySelectorAll('.close-modal').forEach(btn => {
        btn.addEventListener('click', () => {
            document.getElementById('tasksModal').classList.add('hidden');
            document.getElementById('tasksModal').classList.remove('flex');
            document.getElementById('categoriesModal').classList.add('hidden');
            document.getElementById('categoriesModal').classList.remove('flex');
            document.getElementById('noteModal').classList.add('hidden');
            document.getElementById('noteModal').classList.remove('flex');
            document.getElementById('taskEditModal').classList.add('hidden');
            document.getElementById('taskEditModal').classList.remove('flex');
        });
    });

    // Close modals on background click
    document.getElementById('tasksModal').addEventListener('click', (e) => {
        if (e.target.id === 'tasksModal') {
            e.target.classList.add('hidden');
            e.target.classList.remove('flex');
        }
    });

    document.getElementById('categoriesModal').addEventListener('click', (e) => {
        if (e.target.id === 'categoriesModal') {
            e.target.classList.add('hidden');
            e.target.classList.remove('flex');
        }
    });

    document.getElementById('noteModal').addEventListener('click', (e) => {
        if (e.target.id === 'noteModal') {
            closeNoteModal();
        }
    });

    document.getElementById('taskEditModal').addEventListener('click', (e) => {
        if (e.target.id === 'taskEditModal') {
            closeTaskEditModal();
        }
    });
});
