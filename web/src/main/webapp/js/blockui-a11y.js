import jQuery from 'jquery';

(function($) {
    if (!$) return;

    let lastFocusedElement = null;

    // Save the original blockUI and unblockUI methods
    const originalBlockUI = $.blockUI;
    const originalUnblockUI = $.unblockUI;
    const originalFnBlock = $.fn.block;
    const originalFnUnblock = $.fn.unblock;

    function applyA11yOnBlock(opts) {
        lastFocusedElement = document.activeElement;

        const callerOnBlock = opts && opts.onBlock;
        const callerOnUnblock = opts && opts.onUnblock;

        const newOpts = $.extend({}, opts);

        newOpts.onBlock = function() {
            // Apply ARIA roles to active modal
            const $modal = $('.blockUI.blockMsg');
            if ($modal.length) {
                $modal.attr({
                    'role': 'dialog',
                    'aria-modal': 'true'
                });

                // Find focusable elements
                const focusableSelectors = 'a[href], area[href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, embed, [tabindex]:not([tabindex="-1"]), [contenteditable]';
                const $focusable = $modal.find(focusableSelectors).filter(':visible');
                
                if ($focusable.length > 0) {
                    $focusable.first().focus();
                } else {
                    $modal.attr('tabindex', '-1').focus();
                }

                // Keyboard event listener for Focus Trapping and Escape
                $(document).on('keydown.blockUI_a11y', function(e) {
                    if (e.key === 'Escape' || e.keyCode === 27) {
                        e.preventDefault();
                        e.stopPropagation(); // Prevent other escape listeners from firing or breaking
                        $.unblockUI(); // Close active overlay
                    } else if (e.key === 'Tab' || e.keyCode === 9) {
                        const $currentFocusable = $('.blockUI.blockMsg').find(focusableSelectors).filter(':visible');
                        
                        if ($currentFocusable.length === 0) {
                            e.preventDefault();
                            return;
                        }

                        const firstFocusable = $currentFocusable[0];
                        const lastFocusable = $currentFocusable[$currentFocusable.length - 1];

                        if (e.shiftKey) { // Shift + Tab
                            if (document.activeElement === firstFocusable || document.activeElement === $('.blockUI.blockMsg')[0]) {
                                lastFocusable.focus();
                                e.preventDefault();
                            }
                        } else { // Tab
                            if (document.activeElement === lastFocusable) {
                                firstFocusable.focus();
                                e.preventDefault();
                            }
                        }

                        // Safeguard: if focus accidentally escaped the modal, pull it back
                        if (!$('.blockUI.blockMsg')[0].contains(document.activeElement) && document.activeElement !== $('.blockUI.blockMsg')[0]) {
                            firstFocusable.focus();
                            e.preventDefault();
                        }
                    }
                });
            }

            if (callerOnBlock) {
                callerOnBlock.apply(this, arguments);
            }
        };

        newOpts.onUnblock = function() {
            $(document).off('keydown.blockUI_a11y');
            
            if (lastFocusedElement) {
                // Ensure focus returns safely to original element
                try {
                    lastFocusedElement.focus();
                } catch (e) {
                    // Ignore errors if element was removed
                }
            }

            if (callerOnUnblock) {
                callerOnUnblock.apply(this, arguments);
            }
        };

        return newOpts;
    }

    if (originalBlockUI) {
        $.blockUI = function(opts) {
            return originalBlockUI.call(this, applyA11yOnBlock(opts || {}));
        };
        // Re-apply defaults properties that originalBlockUI might have had
        $.extend($.blockUI, originalBlockUI);
    }
    
    if (originalUnblockUI) {
        $.unblockUI = function(opts) {
            // Note: blockUI itself removes the UI and calls onUnblock. 
            // We just call the original.
            return originalUnblockUI.call(this, opts);
        };
    }

    if (originalFnBlock) {
        $.fn.block = function(opts) {
            return originalFnBlock.call(this, applyA11yOnBlock(opts || {}));
        };
    }

})(jQuery);
