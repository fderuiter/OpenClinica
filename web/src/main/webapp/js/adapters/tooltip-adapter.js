import tippy from 'tippy.js';
import 'tippy.js/dist/tippy.css';

window.Tip = function(content) {
    var event = window.event;
    if (!event) return;
    
    var el = event.currentTarget || event.srcElement || event.target;
    if (!el) return;

    // Legacy wz_tooltip allowed executing scripts, passing config as additional args, etc. 
    // We mainly care about the first argument (content) as per the success criteria.
    if (!el._tippy) {
        tippy(el, {
            content: content,
            showOnCreate: true,
            allowHTML: true,
            onHidden: function(instance) {
                instance.destroy();
            }
        });
    } else {
        el._tippy.setContent(content);
        el._tippy.show();
    }
};

window.UnTip = function() {
    var event = window.event;
    if (!event) return;

    var el = event.currentTarget || event.srcElement || event.target;
    if (el && el._tippy) {
        el._tippy.destroy();
    }
};

window.TagToTip = function(id) {
    var sourceEl = document.getElementById(id);
    if (!sourceEl) return;
    window.Tip(sourceEl.innerHTML);
};
