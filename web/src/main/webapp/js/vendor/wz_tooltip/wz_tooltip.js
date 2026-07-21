import tippy from 'tippy.js';
import 'tippy.js/dist/tippy.css';

// Global variables heavily used by legacy tooltips
window.BGCOLOR = 'BGCOLOR';
window.BORDERCOLOR = 'BORDERCOLOR';
window.STICKY = 'STICKY';
// other dummy constants
window.FONTCOLOR = 'FONTCOLOR';
window.WIDTH = 'WIDTH';
window.TITLE = 'TITLE';
window.LEFT = 'LEFT';
window.RIGHT = 'RIGHT';

let currentTippy = null;
let currentTarget = null;

// Legacy tip call
window.Tip = function(html, ...args) {
    // The target element is usually the element that triggered the hover (event.target)
    // Legacy wz_tooltip gets target from the event implicitly by inspecting window.event
    // But how can we find the event target if not passed?
    const event = window.event;
    const target = event ? (event.target || event.srcElement) : document.body;

    if (!target) return;

    if (currentTippy) {
        currentTippy.destroy();
    }
    
    // We parse some args if needed, but for now we just show the HTML
    currentTarget = target;
    
    // Check if target is a simple DOM node, if not fallback to document.body
    if (target.nodeType !== 1) return;

    // Use Tippy to show tooltip
    currentTippy = tippy(target, {
        content: html,
        allowHTML: true,
        showOnCreate: true,
        trigger: 'manual',
        interactive: args.includes(window.STICKY) || args.includes(true),
        theme: 'light', 
        placement: 'auto'
    });
};

window.UnTip = function() {
    if (currentTippy) {
        currentTippy.destroy();
        currentTippy = null;
    }
};

window.callTip = function(html) {
    window.Tip(html, window.BGCOLOR, '#FFFFE5', window.BORDERCOLOR, '', window.STICKY, true);
};
