// API Client Module
const API = {
    baseURL: '',
    
    // Upload CSV file
    async uploadCSV(file) {
        console.log('API: Uploading CSV file:', file.name);
        
        const formData = new FormData();
        formData.append('file', file);
        
        try {
            const response = await fetch('/api/seating/upload-csv', {
                method: 'POST',
                body: formData
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Upload failed');
            }
            
            const students = await response.json();
            console.log('API: CSV uploaded successfully, students:', students.length);
            return students;
            
        } catch (error) {
            console.error('API: CSV upload error:', error);
            throw error;
        }
    },
    
    // Create hall
    async createHall(hallId, rows, cols) {
        console.log('API: Creating hall:', { hallId, rows, cols });
        
        try {
            const response = await fetch(`/api/halls?hallId=${encodeURIComponent(hallId)}&rows=${rows}&cols=${cols}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error('Failed to create hall');
            }
            
            const hall = await response.json();
            console.log('API: Hall created successfully:', hall);
            return hall;
            
        } catch (error) {
            console.error('API: Hall creation error:', error);
            throw error;
        }
    },
    
    // Allocate seats
    async allocateSeats(request) {
        console.log('API: Allocating seats:', request);
        
        try {
            const response = await fetch('/api/seating/allocate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(request)
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Allocation failed');
            }
            
            const result = await response.json();
            console.log('API: Seats allocated successfully:', result);
            return result;
            
        } catch (error) {
            console.error('API: Seat allocation error:', error);
            throw error;
        }
    },
    
    // Export PDF
    async exportPDF(hallId) {
        console.log('API: Exporting PDF for hall:', hallId);
        
        try {
            const response = await fetch(`/api/export/pdf/${encodeURIComponent(hallId)}`);
            
            if (!response.ok) {
                throw new Error('PDF export failed');
            }
            
            const blob = await response.blob();
            
            // Trigger download
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${hallId}_seating_chart.pdf`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            
            console.log('API: PDF exported successfully');
            
        } catch (error) {
            console.error('API: PDF export error:', error);
            throw error;
        }
    },
    
    // Get hall statistics
    async getHallStatistics() {
        console.log('API: Getting hall statistics');
        
        try {
            const response = await fetch('/api/halls/statistics');
            
            if (!response.ok) {
                throw new Error('Failed to get statistics');
            }
            
            const stats = await response.json();
            console.log('API: Statistics retrieved:', stats);
            return stats;
            
        } catch (error) {
            console.error('API: Statistics error:', error);
            throw error;
        }
    }
};