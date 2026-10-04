/**
 * Nova Framework Frontend Logic
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('Nova Framework JS initialized');

    // Toggle Sidebar functionality (if applicable)
    const menuToggle = document.querySelector('.fa-bars');
    if (menuToggle) {
        menuToggle.addEventListener('click', () => {
            const sidebar = document.querySelector('.w-64');
            if (sidebar) {
                sidebar.classList.toggle('hidden');
            }
        });
    }

    // Global form submission animation
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const btn = this.querySelector('button[type="submit"]');
            if (btn) {
                btn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin mr-2"></i> Saving...';
                btn.disabled = true;
                btn.classList.add('opacity-75');
            }
        });
    });
});
