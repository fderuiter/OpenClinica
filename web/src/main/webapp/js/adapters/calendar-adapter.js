import flatpickr from 'flatpickr';
import 'flatpickr/dist/flatpickr.min.css';

function translateFormat(legacyFormat) {
  if (!legacyFormat) return 'Y-m-d';
  return legacyFormat
    .replace(/%d/g, 'd')
    .replace(/%e/g, 'j')
    .replace(/%b/g, 'M')
    .replace(/%B/g, 'F')
    .replace(/%m/g, 'm')
    .replace(/%Y/g, 'Y')
    .replace(/%y/g, 'y');
}

window.Calendar = {
  setup: function (config) {
    if (!config || !config.inputField) return;

    var inputElement =
      typeof config.inputField === 'string'
        ? document.getElementById(config.inputField) ||
          document.getElementsByName(config.inputField)[0]
        : config.inputField;

    if (!inputElement) return;

    // Ensure we don't re-initialize multiple times
    if (inputElement._flatpickr) return;

    var format = translateFormat(config.ifFormat);

    var fp = flatpickr(inputElement, {
      dateFormat: format,
      allowInput: true,
    });

    if (config.button) {
      var btn =
        typeof config.button === 'string'
          ? document.getElementById(config.button)
          : config.button;

      if (btn) {
        var target =
          btn.tagName &&
          btn.tagName.toLowerCase() === 'img' &&
          btn.parentNode &&
          btn.parentNode.tagName.toLowerCase() === 'a'
            ? btn.parentNode
            : btn;

        if (!target._fp_attached) {
          target._fp_attached = true;
          target.addEventListener('click', function (e) {
            e.preventDefault();
            fp.open();
          });
        }
      }
    }
  },
};

if (window._calQueue && window._calQueue.length > 0) {
  window._calQueue.forEach(function (c) {
    window.Calendar.setup(c);
  });
  window._calQueue = [];
}

window.CalendarPopup = function () {
  this.select = function () {};
};
