import flatpickr from 'flatpickr';
import 'flatpickr/dist/flatpickr.css';
import './lang/calendar-en.js';

window.Calendar = window.Calendar || {};

const formatMap = {
    '%Y': 'Y', 
    '%y': 'y', 
    '%m': 'm', 
    '%b': 'M', 
    '%B': 'F', 
    '%d': 'd', 
    '%e': 'j', 
    '%H': 'H', 
    '%I': 'h', 
    '%M': 'i', 
    '%S': 'S', 
    '%p': 'K'  
};

function convertFormat(legacyFormat) {
    if (!legacyFormat) return 'Y-m-d';
    let flatFormat = legacyFormat;
    for (const [legacy, flat] of Object.entries(formatMap)) {
        flatFormat = flatFormat.split(legacy).join(flat);
    }
    return flatFormat;
}

window.Calendar.setup = function(params) {
    let inputEl = typeof params.inputField === 'string' ? document.getElementById(params.inputField) : params.inputField;
    let buttonEl = typeof params.button === 'string' ? document.getElementById(params.button) : params.button;
    
    if (!inputEl) {
        console.warn("Calendar.setup: inputField not found", params);
        return;
    }

    if (inputEl._flatpickr) {
        return;
    }

    const format = convertFormat(params.ifFormat);
    
    const locale = {
        weekdays: {},
        months: {}
    };
    let useLocale = false;
    
    if (window.Calendar._DN) { locale.weekdays.longhand = window.Calendar._DN.slice(0, 7); useLocale = true; }
    if (window.Calendar._SDN) { locale.weekdays.shorthand = window.Calendar._SDN.slice(0, 7); useLocale = true; }
    if (window.Calendar._MN) { locale.months.longhand = window.Calendar._MN; useLocale = true; }
    if (window.Calendar._SMN) { locale.months.shorthand = window.Calendar._SMN; useLocale = true; }
    if (window.Calendar._FD !== undefined) { locale.firstDayOfWeek = window.Calendar._FD; useLocale = true; }

    const fpConfig = {
        dateFormat: format,
        allowInput: true,
        clickOpens: !buttonEl,
        locale: useLocale ? locale : 'default'
    };

    const fp = flatpickr(inputEl, fpConfig);

    if (buttonEl) {
        buttonEl.addEventListener(params.eventName || 'click', (e) => {
            e.preventDefault();
            fp.open();
        });
    }
};
