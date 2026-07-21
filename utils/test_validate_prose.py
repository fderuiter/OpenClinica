import unittest
import os
import tempfile
from validate_prose import validate_file

class TestValidateProse(unittest.TestCase):
    def setUp(self):
        self.fd, self.filepath = tempfile.mkstemp()
    
    def tearDown(self):
        os.close(self.fd)
        os.remove(self.filepath)

    def write_content(self, content):
        with open(self.filepath, 'w', encoding='utf-8') as f:
            f.write(content)

    def test_raw_urls_ignored(self):
        self.write_content("Check out this raw url: https://example.com/task/manager\n")
        self.assertFalse(validate_file(self.filepath))

    def test_auto_linked_urls_ignored(self):
        self.write_content("Check out this auto link: <https://example.com/task/manager>\n")
        self.assertFalse(validate_file(self.filepath))

    def test_reference_style_definitions_ignored(self):
        self.write_content("[1]: https://example.com/task \"Task title\"\n")
        self.assertFalse(validate_file(self.filepath))
        
    def test_markdown_links_complex_formatting(self):
        self.write_content("Check out this [link](https://example.com/task (some notes))\n")
        self.assertFalse(validate_file(self.filepath))

        self.write_content("Check out this [link with parens](https://example.com/task(parens))\n")
        self.assertFalse(validate_file(self.filepath))

    def test_inline_bypass_comment(self):
        self.write_content("This prose has a task keyword <!-- ignore -->\n")
        self.assertFalse(validate_file(self.filepath))
        
        self.write_content("This prose has a task keyword <!-- prose-ignore -->\n")
        self.assertFalse(validate_file(self.filepath))

    def test_actual_violation_caught(self):
        self.write_content("This prose definitely has a task keyword in it.\n")
        self.assertTrue(validate_file(self.filepath))
        
    def test_markdown_link_text_validated(self):
        # We strip the URL but keep the text, so "Task" in the link text should trigger a violation
        self.write_content("Check out this [Task list](https://example.com)\n")
        self.assertTrue(validate_file(self.filepath))

if __name__ == '__main__':
    unittest.main()
