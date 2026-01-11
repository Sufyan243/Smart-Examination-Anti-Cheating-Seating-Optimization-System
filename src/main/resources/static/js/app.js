// Main Application Logic
class SeatingApp {
    constructor() {
        // State variables
        this.currentStudents = [];
        this.currentHall = null;
        this.beforeData = null;
        this.afterData = null;
        this.currentView = 'after';
        
        this.initializeEventListeners();
    }
    
    // Initialize all event listeners
    initializeEventListeners() {
        // CSV Upload
        document.getElementById('uploadBtn').addEventListener('click', () => this.handleCSVUpload());
        document.getElementById('demoBtn').addEventListener('click', () => this.loadDemoData());
        
        // Hall Creation
        document.getElementById('createHallBtn').addEventListener('click', () => this.handleCreateHall());
        
        // Allocation buttons
        document.getElementById('randomBtn').addEventListener('click', () => this.handleRandomAllocation());
        document.getElementById('optimizeBtn').addEventListener('click', () => this.handleOptimizedAllocation());
        
        // Other actions
        document.getElementById('exportBtn').addEventListener('click', () => this.handleExportPDF());
        document.getElementById('resetBtn').addEventListener('click', () => this.handleReset());
        
        // Modal events
        document.querySelector('.modal-close').addEventListener('click', () => this.hideStudentModal());
        document.querySelector('.modal-overlay').addEventListener('click', () => this.hideStudentModal());
        
        // Comparison toggle
        document.getElementById('beforeBtn').addEventListener('click', () => this.switchView('before'));
        document.getElementById('afterBtn').addEventListener('click', () => this.switchView('after'));
        
        // Keyboard events
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideStudentModal();
            }
        });
    }
    
    // Load demo data
    loadDemoData() {
        this.currentStudents = [
            {rollNo: 'S001', name: 'Alice Johnson', subject: 'Mathematics'},
            {rollNo: 'S002', name: 'Bob Smith', subject: 'Physics'},
            {rollNo: 'S003', name: 'Carol Davis', subject: 'Chemistry'},
            {rollNo: 'S004', name: 'David Wilson', subject: 'Mathematics'},
            {rollNo: 'S005', name: 'Emma Brown', subject: 'Physics'},
            {rollNo: 'S006', name: 'Frank Miller', subject: 'Chemistry'},
            {rollNo: 'S007', name: 'Grace Lee', subject: 'Mathematics'},
            {rollNo: 'S008', name: 'Henry Taylor', subject: 'Physics'},
            {rollNo: 'S009', name: 'Ivy Chen', subject: 'Chemistry'},
            {rollNo: 'S010', name: 'Jack Anderson', subject: 'Mathematics'},
            {rollNo: 'S011', name: 'Kate Thompson', subject: 'Physics'},
            {rollNo: 'S012', name: 'Liam Garcia', subject: 'Chemistry'},
            {rollNo: 'S013', name: 'Maya Patel', subject: 'Mathematics'},
            {rollNo: 'S014', name: 'Noah Rodriguez', subject: 'Physics'},
            {rollNo: 'S015', name: 'Olivia Martinez', subject: 'Chemistry'},
            {rollNo: 'S016', name: 'Paul Jackson', subject: 'Mathematics'},
            {rollNo: 'S017', name: 'Quinn White', subject: 'Physics'},
            {rollNo: 'S018', name: 'Ruby Harris', subject: 'Chemistry'},
            {rollNo: 'S019', name: 'Sam Clark', subject: 'Mathematics'},
            {rollNo: 'S020', name: 'Tina Lewis', subject: 'Physics'}
        ];
        
        // Clear previous allocation state
        this.currentHall = null;
        this.beforeData = null;
        this.afterData = null;
        this.currentView = 'after';
        
        // Clear seating grid and reset metrics
        this.renderEmptyState();
        this.hideComparison();
        
        // Reset risk analytics to zero
        document.getElementById('totalRisk').textContent = '0.0%';
        document.getElementById('conflicts').textContent = '0';
        document.getElementById('occupied').textContent = '0/0';
        document.getElementById('utilization').textContent = '0.0%';
        
        this.showToast(`Loaded ${this.currentStudents.length} demo students`, 'success');
        this.updateButtonStates();
    }
    
    // Handle CSV upload
    async handleCSVUpload() {
        const fileInput = document.getElementById('csvFile');
        const file = fileInput.files[0];
        
        if (!file) {
            this.showToast('Please select a CSV file', 'error');
            return;
        }
        
        if (!file.name.toLowerCase().endsWith('.csv')) {
            this.showToast('Please select a valid CSV file', 'error');
            return;
        }
        
        this.setLoading(true);
        
        try {
            this.currentStudents = await API.uploadCSV(file);
            
            // Clear previous allocation state
            this.currentHall = null;
            this.beforeData = null;
            this.afterData = null;
            this.currentView = 'after';
            
            // Clear seating grid and reset metrics
            this.renderEmptyState();
            this.hideComparison();
            
            // Reset risk analytics to zero
            document.getElementById('totalRisk').textContent = '0.0%';
            document.getElementById('conflicts').textContent = '0';
            document.getElementById('occupied').textContent = '0/0';
            document.getElementById('utilization').textContent = '0.0%';
            
            this.showToast(`Successfully loaded ${this.currentStudents.length} students`, 'success');
            this.updateButtonStates();
        } catch (error) {
            this.showToast(`Upload failed: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
        }
    }
    
    // Handle hall creation
    async handleCreateHall() {
        const hallId = document.getElementById('hallId').value.trim();
        const rows = parseInt(document.getElementById('rows').value);
        const cols = parseInt(document.getElementById('cols').value);
        
        if (!hallId) {
            this.showToast('Please enter a Hall ID', 'error');
            return;
        }
        
        if (!rows || !cols || rows < 1 || cols < 1 || rows > 20 || cols > 20) {
            this.showToast('Please enter valid dimensions (1-20)', 'error');
            return;
        }
        
        this.setLoading(true);
        
        try {
            this.currentHall = await API.createHall(hallId, rows, cols);
            
            // Clear previous allocation state
            this.beforeData = null;
            this.afterData = null;
            this.currentView = 'after';
            
            // Render empty grid for new hall
            this.renderEmptyGrid();
            this.hideComparison();
            
            // Reset risk analytics to zero
            document.getElementById('totalRisk').textContent = '0.0%';
            document.getElementById('conflicts').textContent = '0';
            document.getElementById('occupied').textContent = '0/0';
            document.getElementById('utilization').textContent = '0.0%';
            
            this.showToast(`Hall ${hallId} created successfully`, 'success');
            this.updateButtonStates();
        } catch (error) {
            this.showToast(`Hall creation failed: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
        }
    }
    
    // Handle random allocation
    async handleRandomAllocation() {
        if (!this.validatePrerequisites()) return;
        
        this.setLoading(true);
        
        try {
            // STEP 1: Clear all previous state
            this.beforeData = null;
            this.afterData = null;
            this.hideComparison();
            
            // STEP 2: Clear grid visually
            const container = document.getElementById('seatingGrid');
            container.innerHTML = '<p class="empty-state">Allocating seats...</p>';
            
            // STEP 3: Perform allocation
            const request = {
                students: this.currentStudents,
                hallId: this.currentHall.hallId,
                rows: this.currentHall.rows,
                cols: this.currentHall.cols,
                useOptimization: false
            };
            
            this.beforeData = await API.allocateSeats(request);
            
            // Use backend risk report if available, otherwise calculate locally
            if (!this.beforeData.riskReport) {
                this.beforeData.riskReport = await this.calculateRiskForAllocation(this.beforeData);
            }
            
            // STEP 4: Render fresh grid
            this.renderSeatGrid(this.beforeData);
            this.updateRiskAnalytics(this.beforeData);
            this.currentView = 'before';
            this.updateViewToggle();
            
            this.showToast('Random allocation completed', 'success');
            this.updateButtonStates();
        } catch (error) {
            this.showToast(`Random allocation failed: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
        }
    }
    
    // Handle optimized allocation
    async handleOptimizedAllocation() {
        if (!this.validatePrerequisites()) return;
        
        this.setLoading(true);
        
        try {
            // STEP 1: Clear afterData only (keep beforeData for comparison)
            this.afterData = null;
            
            // STEP 2: Clear grid visually
            const container = document.getElementById('seatingGrid');
            container.innerHTML = '<p class="empty-state">Optimizing seats...</p>';
            
            // STEP 3: Perform allocation
            const request = {
                students: this.currentStudents,
                hallId: this.currentHall.hallId,
                rows: this.currentHall.rows,
                cols: this.currentHall.cols,
                useOptimization: true
            };
            
            this.afterData = await API.allocateSeats(request);
            
            // Use backend risk report if available, otherwise calculate locally
            if (!this.afterData.riskReport) {
                this.afterData.riskReport = await this.calculateRiskForAllocation(this.afterData);
            }
            
            // STEP 4: Render fresh grid
            this.renderSeatGrid(this.afterData);
            this.updateRiskAnalytics(this.afterData);
            this.currentView = 'after';
            this.updateViewToggle();
            
            // STEP 5: Show comparison if beforeData exists
            if (this.beforeData) {
                this.showComparison();
                this.animateSeatTransitions();
            }
            
            this.showToast('Optimized allocation completed', 'success');
            this.updateButtonStates();
        } catch (error) {
            this.showToast(`Optimized allocation failed: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
        }
    }
    
    // Handle PDF export
    async handleExportPDF() {
        if (!this.currentHall) {
            this.showToast('Please create a hall first', 'error');
            return;
        }
        
        this.setLoading(true);
        
        try {
            await API.exportPDF(this.currentHall.hallId);
            this.showToast('PDF exported successfully', 'success');
        } catch (error) {
            this.showToast(`PDF export failed: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
        }
    }
    
    // Handle reset
    handleReset() {
        if (confirm('Are you sure you want to reset all data?')) {
            this.currentStudents = [];
            this.currentHall = null;
            this.beforeData = null;
            this.afterData = null;
            this.currentView = 'after';
            
            // Reset form inputs
            document.getElementById('csvFile').value = '';
            document.getElementById('hallId').value = '';
            document.getElementById('rows').value = '';
            document.getElementById('cols').value = '';
            
            // Clear grid and analytics
            this.renderEmptyState();
            this.hideComparison();
            this.updateButtonStates();
            
            this.showToast('All data reset', 'success');
        }
    }
    
    // Render seat grid
    renderSeatGrid(hallData) {
        const container = document.getElementById('seatingGrid');
        
        // IMPORTANT: Completely clear previous grid
        container.innerHTML = '';
        
        // Create fresh grid
        const grid = document.createElement('div');
        grid.className = 'seat-grid';
        grid.style.gridTemplateColumns = `repeat(${hallData.seats[0].length}, 1fr)`;
        
        // Render seats
        for (let row = 0; row < hallData.seats.length; row++) {
            for (let col = 0; col < hallData.seats[row].length; col++) {
                const seat = hallData.seats[row][col];
                const seatElement = this.createSeatElement(seat, row, col);
                grid.appendChild(seatElement);
            }
        }
        
        container.appendChild(grid);
    }
    
    // Create seat element
    createSeatElement(seat, row, col) {
        const seatDiv = document.createElement('div');
        seatDiv.className = 'seat'; // Start with base class only
        seatDiv.dataset.row = row;
        seatDiv.dataset.col = col;
        
        if (seat.student) {
            seatDiv.innerHTML = `
                <div>
                    <div style="font-weight: bold;">${seat.student.rollNo}</div>
                    <div style="font-size: 8px;">${seat.student.subject}</div>
                </div>
            `;
            
            // Add risk level class (safe, medium, high)
            seatDiv.classList.add(seat.riskLevel.toLowerCase());
            seatDiv.title = `${seat.student.name} (${seat.student.subject}) - Risk: ${seat.riskLevel}`;
            
            seatDiv.addEventListener('click', () => this.showStudentModal(seat));
        } else {
            seatDiv.innerHTML = '<div style="font-size: 8px;">EMPTY</div>';
            seatDiv.classList.add('empty');
        }
        
        return seatDiv;
    }
    
    // Show student modal
    showStudentModal(seat) {
        if (!seat.student) return;
        
        document.getElementById('modalRollNo').textContent = seat.student.rollNo;
        document.getElementById('modalName').textContent = seat.student.name;
        document.getElementById('modalSubject').textContent = seat.student.subject;
        
        const riskBadge = document.getElementById('modalRiskLevel');
        riskBadge.textContent = seat.riskLevel;
        riskBadge.className = `risk-badge ${seat.riskLevel.toLowerCase()}`;
        
        document.getElementById('modalRiskScore').textContent = `${seat.riskScore != null ? seat.riskScore.toFixed(1) : '0.0'}%`;
        
        document.getElementById('studentModal').classList.remove('hidden');
    }
    
    // Hide student modal
    hideStudentModal() {
        document.getElementById('studentModal').classList.add('hidden');
    }
    
    // Update risk analytics
    updateRiskAnalytics(hallData) {
        const report = hallData.riskReport;
        
        document.getElementById('totalRisk').textContent = `${report.totalRiskScore.toFixed(1)}%`;
        document.getElementById('conflicts').textContent = report.totalConflicts;
        document.getElementById('occupied').textContent = `${report.occupiedSeats}/${report.totalSeats}`;
        document.getElementById('utilization').textContent = `${((report.occupiedSeats / report.totalSeats) * 100).toFixed(1)}%`;
    }
    
    // Show comparison
    showComparison() {
        const section = document.getElementById('comparisonSection');
        section.style.display = 'block';
        
        if (this.beforeData && this.afterData) {
            const riskReduction = this.beforeData.riskReport.totalRiskScore - this.afterData.riskReport.totalRiskScore;
            const conflictReduction = this.beforeData.riskReport.totalConflicts - this.afterData.riskReport.totalConflicts;
            
            document.getElementById('riskReduction').textContent = `${riskReduction.toFixed(1)}%`;
            document.getElementById('conflictReduction').textContent = conflictReduction;
            
            document.getElementById('riskReduction').className = riskReduction >= 0 ? 'improvement' : 'degradation';
            document.getElementById('conflictReduction').className = conflictReduction >= 0 ? 'improvement' : 'degradation';
        }
    }
    
    // Hide comparison
    hideComparison() {
        document.getElementById('comparisonSection').style.display = 'none';
    }
    
    // Switch view between before/after
    switchView(view) {
        this.currentView = view;
        this.updateViewToggle();
        
        const data = view === 'before' ? this.beforeData : this.afterData;
        if (data) {
            this.renderSeatGrid(data);
            this.updateRiskAnalytics(data);
        }
    }
    
    // Update view toggle buttons
    updateViewToggle() {
        document.getElementById('beforeBtn').classList.toggle('active', this.currentView === 'before');
        document.getElementById('afterBtn').classList.toggle('active', this.currentView === 'after');
    }
    
    // Animate seat transitions
    animateSeatTransitions() {
        if (!this.beforeData || !this.afterData) return;
        
        const seats = document.querySelectorAll('.seat');
        seats.forEach(seat => {
            seat.classList.add('transitioning');
            setTimeout(() => {
                seat.classList.remove('transitioning');
            }, 300);
        });
    }
    
    // Render empty state
    renderEmptyState() {
        const container = document.getElementById('seatingGrid');
        container.innerHTML = '<p class="empty-state">Upload students and create hall to begin</p>';
    }
    
    // Render empty grid
    renderEmptyGrid() {
        const container = document.getElementById('seatingGrid');
        container.innerHTML = '';
        
        const grid = document.createElement('div');
        grid.className = 'seat-grid';
        grid.style.gridTemplateColumns = `repeat(${this.currentHall.cols}, 1fr)`;
        
        for (let row = 0; row < this.currentHall.rows; row++) {
            for (let col = 0; col < this.currentHall.cols; col++) {
                const seatDiv = document.createElement('div');
                seatDiv.className = 'seat empty';
                seatDiv.innerHTML = '<div style="font-size: 8px;">EMPTY</div>';
                grid.appendChild(seatDiv);
            }
        }
        
        container.appendChild(grid);
    }
    
    // Validate prerequisites
    validatePrerequisites() {
        if (this.currentStudents.length === 0) {
            this.showToast('Please upload students first', 'error');
            return false;
        }
        
        if (!this.currentHall) {
            this.showToast('Please create a hall first', 'error');
            return false;
        }
        
        return true;
    }
    
    // Update button states
    updateButtonStates() {
        const hasStudents = this.currentStudents.length > 0;
        const hasHall = this.currentHall !== null;
        const hasData = this.beforeData || this.afterData;
        
        document.getElementById('createHallBtn').disabled = !hasStudents;
        document.getElementById('randomBtn').disabled = !hasStudents || !hasHall;
        document.getElementById('optimizeBtn').disabled = !hasStudents || !hasHall;
        document.getElementById('exportBtn').disabled = !hasData;
    }
    
    // Set loading state
    setLoading(loading) {
        const buttons = document.querySelectorAll('.btn');
        buttons.forEach(btn => {
            btn.disabled = loading;
        });
        
        document.body.classList.toggle('loading', loading);
        
        // Reapply button states after loading ends
        if (!loading) {
            this.updateButtonStates();
        }
    }
    
    // Show toast notification
    showToast(message, type = 'success') {
        const container = document.getElementById('toastContainer');
        
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        
        container.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 5000);
    }
    
    // Calculate risk for allocation (helper method)
    async calculateRiskForAllocation(hallData) {
        // The backend should already calculate risk, but ensure it's present
        if (hallData.riskReport) {
            return hallData.riskReport;
        }
        
        // Fallback: basic risk calculation on frontend
        let totalConflicts = 0;
        let occupiedSeats = 0;
        
        for (let row = 0; row < hallData.seats.length; row++) {
            for (let col = 0; col < hallData.seats[row].length; col++) {
                const seat = hallData.seats[row][col];
                if (seat.student) {
                    occupiedSeats++;
                    // Simple conflict detection
                    const neighbors = this.getNeighbors(hallData.seats, row, col);
                    let conflicts = 0;
                    neighbors.forEach(neighbor => {
                        if (neighbor.student && neighbor.student.subject === seat.student.subject) {
                            conflicts++;
                        }
                    });
                    
                    if (conflicts === 0) {
                        seat.riskLevel = 'SAFE';
                        seat.colorCode = '#4CAF50';
                        seat.riskScore = 0;
                    } else if (conflicts === 1) {
                        seat.riskLevel = 'MEDIUM';
                        seat.colorCode = '#FFC107';
                        seat.riskScore = 25;
                        totalConflicts += conflicts;
                    } else {
                        seat.riskLevel = 'HIGH';
                        seat.colorCode = '#F44336';
                        seat.riskScore = 50 + (conflicts - 2) * 25;
                        totalConflicts += conflicts;
                    }
                }
            }
        }
        
        totalConflicts = totalConflicts / 2; // Each conflict counted twice
        
        return {
            totalSeats: hallData.seats.length * hallData.seats[0].length,
            occupiedSeats: occupiedSeats,
            totalConflicts: totalConflicts,
            totalRiskScore: occupiedSeats > 0 ? (totalConflicts / occupiedSeats) * 100 : 0,
            occupancyRate: (occupiedSeats / (hallData.seats.length * hallData.seats[0].length)) * 100
        };
    }
    
    // Get neighboring seats
    getNeighbors(seats, row, col) {
        const neighbors = [];
        const directions = [[-1, 0], [1, 0], [0, -1], [0, 1]];
        
        directions.forEach(([dr, dc]) => {
            const newRow = row + dr;
            const newCol = col + dc;
            if (newRow >= 0 && newRow < seats.length && newCol >= 0 && newCol < seats[0].length) {
                neighbors.push(seats[newRow][newCol]);
            }
        });
        
        return neighbors;
    }
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new SeatingApp();
});