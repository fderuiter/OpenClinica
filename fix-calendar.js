const fs = require('fs');
const file = '/app/web/local-pkgs/new_cal/calendar.js';
let content = fs.readFileSync(file, 'utf8');

// Replace the with statements
content = content.replace(/with \s*\(\s*Calendar\s*\)\s*\{/g, '{');
content = content.replace(/with \(Calendar\) \{/g, '{');

// Replace variables globally, ensuring we don't double-replace `Calendar.Calendar...`
const props = [
  'addEvent', 'removeEvent', 'dayMouseOver', 'dayMouseDown', 'dayMouseOut',
  'dayMouseDblClick', 'tableMouseUp', 'tableMouseOver', 'calDragIt', 'calDragEnd',
  'stopEvent', 'getElement', 'isRelated', 'removeClass', 'addClass', 'is_ie', 'is_ie5', '_C', '_TT'
];

props.forEach(p => {
  // Negative lookbehind for `.`
  const regex = new RegExp(`(?<!\\.)\\b${p}\\b`, 'g');
  content = content.replace(regex, `Calendar.${p}`);
});

fs.writeFileSync(file, content);
