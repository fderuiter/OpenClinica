document.addEventListener("DOMContentLoaded", function() {
    const searchOutput = document.querySelector('.md-search__output');
    if (!searchOutput) return;

    // Create tabs container
    const tabsContainer = document.createElement('div');
    tabsContainer.className = 'search-tabs';
    tabsContainer.innerHTML = `
        <button class="search-tab active" data-category="All">All</button>
        <button class="search-tab" data-category="Guides">Guides</button>
        <button class="search-tab" data-category="Reference">Reference</button>
        <button class="search-tab" data-category="Tutorials">Tutorials</button>
    `;
    
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
                // Remove trailing/leading whitespaces and handle possible icon text if any
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
            filterResults();
        });
        observer.observe(resultList, { childList: true, subtree: true });
    }
});
