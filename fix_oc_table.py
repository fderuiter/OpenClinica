import os
import re

file = 'web/src/main/java/org/akaza/openclinica/control/OCTableFacadeImpl.java'
with open(file, 'r') as f:
    c = f.read()

# Replace constructor super call
c = c.replace('super(id, request);', 'super(id, org.akaza.openclinica.web.filter.HttpServletRequestAdapter.adapt(request));')
c = c.replace('new XmlViewExporter(view, cc, request, response)', 'new XmlViewExporter(view, cc, org.akaza.openclinica.web.filter.HttpServletRequestAdapter.adapt(request), org.akaza.openclinica.web.filter.HttpServletResponseAdapter.adapt(response))')
c = c.replace('new CsvViewExporter(view, cc, response, fileName + ".txt")', 'new CsvViewExporter(view, cc, org.akaza.openclinica.web.filter.HttpServletResponseAdapter.adapt(response), fileName + ".txt")')
c = c.replace('new ExcelViewExporter(view, cc, response, fileName + ".xls")', 'new ExcelViewExporter(view, cc, org.akaza.openclinica.web.filter.HttpServletResponseAdapter.adapt(response), fileName + ".xls")')

with open(file, 'w') as f:
    f.write(c)

