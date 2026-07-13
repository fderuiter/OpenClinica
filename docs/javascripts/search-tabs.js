document.addEventListener("DOMContentLoaded", function() {
    const searchOutput = document.querySelector('.md-search__output');
    if (!searchOutput) return;

    // Create tabs container
    const tabsContainer = document.createElement('div');
    tabsContainer.className = 'search-tabs';
    
    // Insert tabs at the top of the search scrollwrap
    const scrollwrap = document.querySelector('.md-search__scrollwrap');
    if (scrollwrap) {
        searchOutput.insertBefore(tabsContainer, scrollwrap);
    }
    
    let activeCategory = 'All';

    // Handle tab clicks
    tabsContainer.addEventListener('click', function(e) {
        if (e.target.classList.contains('search-tab')) {
            document.querySelectorAll('.search-tab').forEach(t => t.classList.remove('active'));
            e.target.classList.add('active');
            activeCategory = e.target.getAttribute('data-category');
            filterResults();
        }
    });

    function updateTabsAndFilter() {
        const results = document.querySelectorAll('.md-search-result__item');
        
        // Extract unique tags
        const uniqueTags = new Set();
        results.forEach(item => {
            const tagElements = item.querySelectorAll('.md-tag');
            tagElements.forEach(tag => {
                uniqueTags.add(tag.textContent.trim());
            });
        });

        // Convert set to array and sort
        const categories = Array.from(uniqueTags).sort();

        // Revert to 'All' if active category is missing
        if (activeCategory !== 'All' && !categories.includes(activeCategory)) {
            activeCategory = 'All';
        }

        // Build tabs HTML
        let html = `<button class="search-tab ${activeCategory === 'All' ? 'active' : ''}" data-category="All">All</button>`;
        categories.forEach(category => {
            if (category) {
                // Escape category name to avoid syntax errors in HTML
                const safeCategory = category.replace(/"/g, '&quot;');
                html += `<button class="search-tab ${activeCategory === category ? 'active' : ''}" data-category="${safeCategory}">${safeCategory}</button>`;
            }
        });
        
        // Update DOM only if changed
        if (tabsContainer.innerHTML !== html) {
            tabsContainer.innerHTML = html;
        }

        filterResults();
    }

    function filterResults() {
        const results = document.querySelectorAll('.md-search-result__item');
        results.forEach(item => {
            if (activeCategory === 'All') {
                item.style.display = '';
                return;
            }
            
            const tagElements = item.querySelectorAll('.md-tag');
            let hasCategory = false;
            tagElements.forEach(tag => {
                const text = tag.textContent.trim();
                if (text === activeCategory) {
                    hasCategory = true;
                }
            });
            
            if (hasCategory) {
                item.style.display = '';
            } else {
                item.style.display = 'none';
            }
        });
    }

    const resultList = document.querySelector('.md-search-result__list');
    if (resultList) {
        const observer = new MutationObserver(function(mutations) {
            updateTabsAndFilter();
        });
        observer.observe(resultList, { childList: true, subtree: true });
    }
    
    updateTabsAndFilter();
});
