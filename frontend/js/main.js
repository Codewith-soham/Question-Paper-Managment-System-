// Question Paper Management System Frontend
(function() {
    const API_BASE_URL = 'http://localhost:8080/papers';
    const PDF_REGEX = /\.[Pp][Dd][Ff]$/;

    // Export/Import removed per request

    function toast(message) {
        const el = document.getElementById('toast');
        if (!el) return;
        el.textContent = message;
        el.classList.add('show');
        setTimeout(() => el.classList.remove('show'), 1800);
    }

    function setYear() {
        const y = document.getElementById('yearNow');
        if (y) y.textContent = new Date().getFullYear();
    }

    // Theme toggle
    function initTheme() {
        const themeToggle = document.getElementById('themeToggle');
        const saved = localStorage.getItem('qpms_theme');
        if (saved) document.documentElement.setAttribute('data-theme', saved);
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                const current = document.documentElement.getAttribute('data-theme');
                const next = current === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-theme', next);
                localStorage.setItem('qpms_theme', next);
            });
        }
    }

    // Back to top
    function initBackToTop() {
        const btn = document.getElementById('backToTop');
        if (!btn) return;
        btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
    }

    // Populate all papers on dashboard
    async function renderAllPapers() {
        const table = document.getElementById('allPapersTable');
        if (!table) return;
        const tbody = table.querySelector('tbody');
        const info = document.getElementById('allPapersInfo');
        tbody.innerHTML = '';
        
        try {
            const response = await fetch(API_BASE_URL);
            const items = await response.json();
            
            if (info) info.textContent = items.length ? '' : 'No papers yet. Add one to get started.';
            
            items.forEach(item => {
                const tr = document.createElement('tr');
                // Construct file link - filePath is relative to the PDF folder
                const fileLink = `/pdf/${encodeURIComponent(item.filePath)}`;
                tr.innerHTML = `
                    <td>${item.subject}</td>
                    <td>${item.academicYear || 'N/A'}</td>
                    <td>${item.examMonth || 'N/A'}</td>
                    <td>${item.semester}</td>
                    <td><a href="${fileLink}" target="_blank" rel="noopener">${item.filePath}</a></td>
                    <td>
                        <button class="send-email-btn" data-id="${item.id}">Send to Email</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
            
            // Add listeners
            Array.from(document.querySelectorAll('.send-email-btn')).forEach(btn => {
                btn.addEventListener('click', function() {
                    showEmailModal(this.getAttribute('data-id'));
                });
            });
            // Delete listeners
            Array.from(document.querySelectorAll('.delete-btn')).forEach(btn => {
                btn.addEventListener('click', async function() {
                    const id = this.getAttribute('data-id');
                    if (!confirm('Are you sure you want to delete paper ID ' + id + '?')) return;
                    try {
                        const resp = await fetch(`${API_BASE_URL}/${id}`, { method: 'DELETE' });
                        const txt = await resp.text();
                        if (resp.ok) {
                            toast('Deleted');
                            renderAllPapers();
                        } else {
                            toast('Delete failed');
                            console.error('Delete failed', resp.status, txt);
                        }
                    } catch (err) {
                        console.error('Delete request failed', err);
                        toast('Network error');
                    }
                });
            });
        } catch (err) {
            console.error('Failed to fetch papers:', err);
            toast('Error loading papers');
        }
    }

    function showEmailModal(paperId) {
        const modal = document.getElementById('emailModal');
        const emailInput = document.getElementById('recipientEmail');
        const idInput = document.getElementById('paperId');
        const info = document.getElementById('emailModalInfo');
        if (!modal || !emailInput || !idInput) return;
        modal.style.display = 'block';
        emailInput.value = '';
        idInput.value = paperId;
        info.textContent = '';
        emailInput.focus();
    }

    function initEmailModal() {
        const closeBtn = document.getElementById('closeModal');
        const modal = document.getElementById('emailModal');
        const form = document.getElementById('emailSendForm');
        
        if (closeBtn && modal) {
            closeBtn.onclick = function() {
                modal.style.display = 'none';
            };
        }
        
        // Handle click outside modal to close
        if (modal) {
            window.onclick = function(event) {
                if (event.target === modal) {
                    modal.style.display = 'none';
                }
            };
        }
        
        if (form) {
            form.onsubmit = async function (e) {
                e.preventDefault();
                const recipientEmail = document.getElementById('recipientEmail').value.trim();
                const paperId = document.getElementById('paperId').value;
                const info = document.getElementById('emailModalInfo');
                if (!recipientEmail || !paperId) return;
                
                info.textContent = 'Sending...';
                try {
                    const response = await fetch(`${API_BASE_URL}/${paperId}/email?recipientEmail=${encodeURIComponent(recipientEmail)}`, {
                        method: 'POST'
                    });
                    
                    const text = await response.text();
                    
                    if (!response.ok) {
                        info.textContent = `Error: ${text}`;
                        toast(`Error: ${text}`);
                        console.error('Server error:', response.status, text);
                    } else {
                        info.textContent = text;
                        toast(text);
                        setTimeout(() => {
                            modal.style.display = 'none';
                        }, 1600);
                    }
                } catch (err) {
                    const errorMsg = err.message || 'Failed to send email';
                    info.textContent = `Error: ${errorMsg}`;
                    toast(`Error: ${errorMsg}`);
                    console.error('Network error:', err);
                }
            };
        }
    }

    function initRefreshAll() {
        const btn = document.getElementById('refreshAllBtn');
        if (btn) btn.addEventListener('click', renderAllPapers);
    }

    // Add form
    function initAddForm() {
        const form = document.getElementById('addPaperForm');
        if (!form) return;
        const info = document.getElementById('addInfo');
        const submitBtn = document.getElementById('submitBtn');
        const fileInput = document.getElementById('pdfFile');
        const fileInfo = document.getElementById('fileInfo');
        
        // Show file info when file is selected
        if (fileInput) {
            fileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) {
                    const sizeMB = (file.size / 1024 / 1024).toFixed(2);
                    if (fileInfo) {
                        fileInfo.style.display = 'block';
                        fileInfo.innerHTML = `<strong>✓ Selected:</strong> ${file.name} (${sizeMB} MB)`;
                    }
                    
                    // Validate file size (10MB limit)
                    if (file.size > 10485760) {
                        alert('File size exceeds 10MB limit');
                        fileInput.value = '';
                        fileInfo.style.display = 'none';
                    }
                }
            });
        }
        
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Get form data
            const formData = new FormData(form);
            
            // Validate file is selected
            const file = formData.get('pdfFile');
            if (!file || file.size === 0) {
                toast('Please select a PDF file');
                return;
            }
            
            // Show loading state
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.querySelector('.btn-text').style.display = 'none';
                submitBtn.querySelector('.btn-loading').style.display = 'inline';
            }
            
            try {
                // Upload file with form data
                const response = await fetch(`${API_BASE_URL}/upload`, {
                    method: 'POST',
                    body: formData
                });
                
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.error || 'Upload failed');
                }
                
                const result = await response.json();
                
                if (info) {
                    info.textContent = `✓ Paper uploaded successfully! File: ${result.filename}`;
                    info.style.color = 'var(--accent)';
                }
                toast('✓ Paper uploaded successfully!');
                form.reset();
                if (fileInfo) fileInfo.style.display = 'none';
                
            } catch (err) {
                console.error('Upload failed:', err);
                if (info) {
                    info.textContent = `✗ Error: ${err.message}`;
                    info.style.color = '#e74c3c';
                }
                toast('✗ Upload failed');
            } finally {
                // Reset button state
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.querySelector('.btn-text').style.display = 'inline';
                    submitBtn.querySelector('.btn-loading').style.display = 'none';
                }
            }
        });
    }

    // Search form
    function initSearchForm() {
        const form = document.getElementById('searchPaperForm');
        const table = document.getElementById('resultTable');
        if (!form || !table) return;
        const info = document.getElementById('searchInfo');
        const tbody = table.querySelector('tbody');
        
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = new FormData(form);
            const subject = data.get('subject').trim();
            const academicYear = data.get('academicYear');
            const examMonth = data.get('examMonth');
            const year = Number(data.get('year'));
            const semester = Number(data.get('semester'));
            
            try {
                const response = await fetch(
                    `${API_BASE_URL}/search?subject=${encodeURIComponent(subject)}&academicYear=${encodeURIComponent(academicYear)}&examMonth=${encodeURIComponent(examMonth)}&year=${year}&semester=${semester}`
                );
                const results = await response.json();
                
                tbody.innerHTML = '';
                if (info) info.textContent = results.length ? '' : 'No results found.';
                
                results.forEach(item => {
                    const tr = document.createElement('tr');
                    const fileLink = `/pdf/${encodeURIComponent(item.filePath)}`;
                    tr.innerHTML = `
                        <td>${item.id}</td>
                        <td>${item.subject}</td>
                        <td>${item.academicYear || 'N/A'}</td>
                        <td>${item.examMonth || 'N/A'}</td>
                        <td>${item.year}</td>
                        <td>${item.semester}</td>
                        <td>${item.status}</td>
                        <td><a href="${fileLink}" target="_blank" rel="noopener">${item.filePath}</a></td>
                        <td><button class="send-email-btn" data-id="${item.id}">Send to Email</button></td>
                    `;
                    tbody.appendChild(tr);
                });
                
                // Add email button listeners for search results
                Array.from(table.querySelectorAll('.send-email-btn')).forEach(btn => {
                    btn.addEventListener('click', function() {
                        showEmailModal(this.getAttribute('data-id'));
                    });
                });
            } catch (err) {
                console.error('Search failed:', err);
                toast('Error searching papers');
            }
        });

        const exportBtn = document.getElementById('exportCsvBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => {
                const rows = [['ID','Subject','Academic Year','Exam Month','Year','Semester','Status','File Name']];
                table.querySelectorAll('tbody tr').forEach(tr => {
                    const cols = Array.from(tr.children).map(td => td.textContent || '');
                    rows.push(cols.slice(0, -1)); // Exclude the last column (Send button)
                });
                const csv = rows.map(r => r.map(v => '"' + v.replaceAll('"', '""') + '"').join(',')).join('\n');
                const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'search_results.csv';
                a.click();
                URL.revokeObjectURL(url);
                toast('Exported CSV');
            });
        }
    }

    // Boot
    document.addEventListener('DOMContentLoaded', () => {
        setYear();
        initTheme();
        initBackToTop();
        initRefreshAll();
        renderAllPapers();
        initAddForm();
        initSearchForm();
        initEmailModal();
    });
})();


