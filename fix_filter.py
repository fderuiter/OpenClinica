with open('/app/core/src/main/java/org/akaza/openclinica/web/filter/ApiResponseWrapperFilter.java', 'r') as f:
    content = f.read()

content = content.replace('''                    public void write(int b) throws IOException {
                    @Override
                    public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
                    @Override
                    public boolean isReady() { return true; }
                        capture.write(b);
                    }''', '''                    public void write(int b) throws IOException {
                        capture.write(b);
                    }
                    @Override
                    public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
                    @Override
                    public boolean isReady() { return true; }''')

with open('/app/core/src/main/java/org/akaza/openclinica/web/filter/ApiResponseWrapperFilter.java', 'w') as f:
    f.write(content)
