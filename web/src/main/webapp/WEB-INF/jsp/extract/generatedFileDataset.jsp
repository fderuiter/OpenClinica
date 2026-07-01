<%@ page contentType="text/html; charset=UTF-8" %><%@ page import="java.io.*" %><%
  out.clear();
  out = pageContext.pushBody();
  String path = (String)request.getAttribute("generate");
  if (path != null && !path.trim().isEmpty()) {
      java.io.File local = new java.io.File(path);
      if (local.exists() && local.isFile()) {
          ServletOutputStream sos = null;
          BufferedOutputStream bos = null;
          InputStream is = null;
          BufferedInputStream bis = null;
          try {
              if (!path.endsWith(".html")) {
                  response.setContentType("application/download");
              }
              response.setHeader("Pragma", "public");
              sos = response.getOutputStream();
              bos = new BufferedOutputStream(sos);
              is = new FileInputStream(local);
              bis = new BufferedInputStream(is);
              byte[] buff = new byte[8192];
              int bytesRead;
              while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                  bos.write(buff, 0, bytesRead);
              }
          } catch (Exception ee) {
              ee.printStackTrace();
          } finally {
              if( bis != null ) bis.close();
              if( is != null ) is.close();
              if( bos != null ) bos.close();
              if( sos != null ) {
                  sos.flush();
                  sos.close();
              }
          }
      } else {
          out.print("The requested file is missing or no longer exists on the server.");
      }
  } else {
      out.print("No file reference provided or file reference is invalid.");
  }
%>
