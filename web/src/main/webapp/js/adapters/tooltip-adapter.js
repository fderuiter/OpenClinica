import tippy from 'tippy.js';
import 'tippy.js/dist/tippy.css';

window._activeTippy = null;

window.Tip = function (content) {
  var event = window.event;
  if (!event) return;

  var el = event.currentTarget || event.srcElement || event.target;
  if (!el) return;

  // Destroy any globally active tippy to emulate single-tooltip legacy behavior
  if (window._activeTippy && window._activeTippy.reference !== el) {
    window._activeTippy.destroy();
    window._activeTippy = null;
  }

  // Legacy wz_tooltip allowed executing scripts, passing config as additional args, etc.
  // We mainly care about the first argument (content) as per the success criteria.
  if (!el._tippy) {
    var instance = tippy(el, {
      content: content,
      showOnCreate: true,
      allowHTML: true,
      onHidden: function (inst) {
        inst.destroy();
        if (window._activeTippy === inst) {
          window._activeTippy = null;
        }
      },
    });
    window._activeTippy = instance;
  } else {
    el._tippy.setContent(content);
    el._tippy.show();
    window._activeTippy = el._tippy;
  }
};

window.UnTip = function () {
  var event = window.event;
  if (!event) return;

  var el = event.currentTarget || event.srcElement || event.target;
  if (el && el._tippy) {
    el._tippy.destroy();
    if (window._activeTippy === el._tippy) {
      window._activeTippy = null;
    }
  } else if (window._activeTippy) {
    // Fallback: if we can't find the element, just destroy the active tippy
    window._activeTippy.destroy();
    window._activeTippy = null;
  }
};

window.TagToTip = function (id) {
  var sourceEl = document.getElementById(id);
  if (!sourceEl) return;
  window.Tip(sourceEl.innerHTML);
};
